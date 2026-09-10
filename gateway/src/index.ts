import { createHmac, randomUUID, timingSafeEqual } from 'node:crypto';
import { createServer } from 'node:http';
import { McpServer } from '@modelcontextprotocol/sdk/server/mcp.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';
import { z } from 'zod';

const required = (name: string) => { const value = process.env[name]; if (!value) throw new Error(`Missing ${name}`); return value; };
const secret = required('BRIDGE_SECRET');
const bridgeUrl = required('BRIDGE_URL').replace(/\/$/, '');
const baseUrl = (process.env.OPENAI_BASE_URL ?? 'https://api.openai.com/v1').replace(/\/$/, '');
const model = required('OPENAI_MODEL');
const mode = process.env.GATEWAY_MODE ?? 'both';
const cooldown = Number(process.env.QUESTION_COOLDOWN_SECONDS ?? 60) * 1000;
const dailyLimit = Number(process.env.MAX_DAILY_QUESTIONS ?? 100);
const usage = new Map<string, number>(); let daily = { day: '', count: 0 };
let answerInFlight = false;

function signature(timestamp: string, nonce: string, method: string, path: string, body: string) { return createHmac('sha256', secret).update([timestamp, nonce, method, path, body].join('\n')).digest('hex'); }
async function bridge(path: string, method = 'GET', body = '') {
  const timestamp = Math.floor(Date.now() / 1000).toString(), nonce = randomUUID();
  const response = await fetch(`${bridgeUrl}${path}`, { method, body: body || undefined, headers: { 'content-type': 'application/json', 'x-zcn-timestamp': timestamp, 'x-zcn-nonce': nonce, 'x-zcn-signature': signature(timestamp, nonce, method, path, body) } });
  const data = await response.json(); if (!response.ok) throw new Error(data.error ?? `Bridge ${response.status}`); return data;
}
function text(data: unknown) { return { content: [{ type: 'text' as const, text: JSON.stringify(data, null, 2) }], structuredContent: data as Record<string, unknown> }; }
async function answer(question: string, player: string) {
  const [status, progress] = await Promise.all([bridge('/v1/status'), bridge(`/v1/player?name=${encodeURIComponent(player)}`)]);
  const response = await fetch(`${baseUrl}/responses`, { method: 'POST', headers: { authorization: `Bearer ${required('OPENAI_API_KEY')}`, 'content-type': 'application/json' }, body: JSON.stringify({ model, max_output_tokens: 450, input: `你是 Zyu 的 Minecraft Cobblemon 助手。简洁中文回答，不编造库存。当前服务器：${JSON.stringify(status)}。提问玩家状态：${JSON.stringify(progress)}。问题：${question}` }) });
  if (!response.ok) throw new Error(`OpenAI ${response.status}`); const data = await response.json() as { output_text?: string; output?: Array<{ content?: Array<{ text?: string }> }> };
  return data.output_text ?? data.output?.flatMap(item => item.content ?? []).map(item => item.text ?? '').join('') ?? '没有生成可显示的回答。';
}
function allowed(player: string) { const now = Date.now(), day = new Date().toISOString().slice(0, 10); if (daily.day !== day) daily = { day, count: 0 }; if (answerInFlight || daily.count >= dailyLimit || now - (usage.get(player) ?? 0) < cooldown) return false; usage.set(player, now); daily.count++; return true; }
const mcp = new McpServer({ name: 'zyu-cobblemon-note', version: '0.1.0' });
const tools: Array<[string, string, Record<string, z.ZodType>, (args: Record<string, string>) => Promise<unknown>]> = [
  ['get_server_status', '读取当前 TPS、MSPT 与在线人数。', {}, () => bridge('/v1/status')],
  ['list_online_players', '读取在线玩家的位置与基础状态。', {}, () => bridge('/v1/players')],
  ['get_player_progress', '读取指定在线玩家的背包、状态和队伍。', { name: z.string() }, args => bridge(`/v1/player?name=${encodeURIComponent(args.name)}`)],
  ['get_player_party', '读取指定在线玩家的 Cobblemon 队伍。', { name: z.string() }, args => bridge(`/v1/party?name=${encodeURIComponent(args.name)}`)],
  ['get_player_pc_summary', '读取指定在线玩家的电脑宝可梦摘要。', { name: z.string() }, args => bridge(`/v1/pc?name=${encodeURIComponent(args.name)}`)],
  ['list_bases', '列出已登记基地。', {}, () => bridge('/v1/bases')],
  ['get_base_inventory', '读取已登记基地的容器物资；未加载区块会明确标注。', { name: z.string() }, args => bridge(`/v1/base?name=${encodeURIComponent(args.name)}`)]
];
for (const [name, description, inputSchema, handler] of tools) mcp.registerTool(name, { description, inputSchema }, async args => text(await handler(args as Record<string, string>)));
const host = process.env.GATEWAY_HOST ?? '127.0.0.1', port = Number(process.env.GATEWAY_PORT ?? 25932);
if (!['http', 'mcp', 'both'].includes(mode)) throw new Error('GATEWAY_MODE must be http, mcp, or both');

if (mode !== 'mcp') {
  createServer(async (request, response) => {
    if (request.method !== 'POST' || request.url !== '/v1/questions') { response.writeHead(404).end(); return; }
    const body = await new Promise<string>(resolve => { let value = ''; request.on('data', part => value += part); request.on('end', () => resolve(value)); });
    const timestamp = request.headers['x-zcn-timestamp'] as string, nonce = request.headers['x-zcn-nonce'] as string, actual = request.headers['x-zcn-signature'] as string;
    const expected = signature(timestamp ?? '', nonce ?? '', 'POST', '/v1/questions', body);
    const expectedBytes = Buffer.from(expected), actualBytes = Buffer.from(actual ?? '');
    if (!timestamp || Math.abs(Date.now() / 1000 - Number(timestamp)) > 60 || !actual || actualBytes.length !== expectedBytes.length || !timingSafeEqual(expectedBytes, actualBytes)) { response.writeHead(401).end(); return; }
    let question: { requestId: string; player: string; playerUuid: string; question: string };
    try { question = JSON.parse(body); } catch { response.writeHead(400).end(); return; }
    if (!question.requestId || !question.player || !question.playerUuid || !question.question || !allowed(question.playerUuid) || question.question.length > 500) { response.writeHead(429).end(); return; }
    response.writeHead(202).end();
    answerInFlight = true;
    void answer(question.question, question.player)
      .then(value => bridge('/v1/answers', 'POST', JSON.stringify({ requestId: question.requestId, answer: value })))
      .catch(error => bridge('/v1/answers', 'POST', JSON.stringify({ requestId: question.requestId, answer: `暂时无法回答：${error.message}` })).catch(() => undefined))
      .finally(() => { answerInFlight = false; });
  }).listen(port, host);
}

if (mode !== 'http') await mcp.connect(new StdioServerTransport());
