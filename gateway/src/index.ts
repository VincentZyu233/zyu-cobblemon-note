import { createHmac, randomUUID, timingSafeEqual } from 'node:crypto';
import { readdir, readFile } from 'node:fs/promises';
import { createServer, IncomingMessage, ServerResponse } from 'node:http';
import { basename, extname, join, relative, resolve } from 'node:path';

import { McpServer } from '@modelcontextprotocol/sdk/server/mcp.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';
import { z } from 'zod';

const required = (name: string): string => {
  const value = process.env[name];
  if (!value) throw new Error(`Missing ${name}`);
  return value;
};

const secret = required('BRIDGE_SECRET');
const bridgeUrl = required('BRIDGE_URL').replace(/\/$/, '');
const baseUrl = (process.env.OPENAI_BASE_URL ?? 'https://api.openai.com/v1').replace(/\/$/, '');
const model = required('OPENAI_MODEL');
const mode = process.env.GATEWAY_MODE ?? 'both';
const sourceRoot = resolve(process.env.COBBLEMON_SOURCE_ROOT ?? '/data/data1/aaa_from_git_aaa/cobblemon');
const webAiToken = required('WEB_AI_TOKEN');
const cooldown = Number(process.env.QUESTION_COOLDOWN_SECONDS ?? 60) * 1_000;
const dailyLimit = Number(process.env.MAX_DAILY_QUESTIONS ?? 100);
const host = process.env.GATEWAY_HOST ?? '127.0.0.1';
const port = Number(process.env.GATEWAY_PORT ?? 25932);

const usage = new Map<string, number>();
let daily = { day: '', count: 0 };
let answerInFlight = false;

type BridgeData = Record<string, unknown>;
type Question = { player: string; question: string };
type GameQuestion = Question & { requestId: string; playerUuid: string };

function signature(timestamp: string, nonce: string, method: string, path: string, body: string): string {
  return createHmac('sha256', secret).update([timestamp, nonce, method, path, body].join('\n')).digest('hex');
}

function tokenMatches(actual: string | undefined, expected: string): boolean {
  if (!actual) return false;
  const actualBytes = Buffer.from(actual);
  const expectedBytes = Buffer.from(expected);
  return actualBytes.length === expectedBytes.length && timingSafeEqual(actualBytes, expectedBytes);
}

function isLoopback(address: string | undefined): boolean {
  if (!address) return false;
  const normalized = address.replace(/^::ffff:/, '');
  return normalized === '127.0.0.1' || normalized === '::1';
}

async function bridge(path: string, method = 'GET', body = ''): Promise<BridgeData> {
  const timestamp = Math.floor(Date.now() / 1_000).toString();
  const nonce = randomUUID();
  const response = await fetch(`${bridgeUrl}${path}`, {
    method,
    body: body || undefined,
    headers: {
      'content-type': 'application/json',
      'x-zcn-nonce': nonce,
      'x-zcn-signature': signature(timestamp, nonce, method, path, body),
      'x-zcn-timestamp': timestamp,
    },
  });
  const data = await response.json() as BridgeData;
  if (!response.ok) throw new Error(String(data.error ?? `Bridge ${response.status}`));
  return data;
}

function mcpText(data: unknown) {
  return { content: [{ type: 'text' as const, text: JSON.stringify(data, null, 2) }], structuredContent: data as Record<string, unknown> };
}

async function sourceFiles(directory: string, files: string[], depth = 0): Promise<void> {
  if (files.length >= 120 || depth > 6) return;
  const entries = await readdir(directory, { withFileTypes: true });
  for (const entry of entries) {
    if (files.length >= 120 || ['.git', '.gradle', 'build', 'run'].includes(entry.name)) continue;
    const path = join(directory, entry.name);
    if (entry.isDirectory()) await sourceFiles(path, files, depth + 1);
    else if (entry.isFile() && ['.json', '.kt', '.md'].includes(extname(entry.name))) files.push(path);
  }
}

function keywords(question: string): string[] {
  return question.toLowerCase().split(/[^a-z0-9_\u4e00-\u9fff]+/u).filter(word => word.length >= 2).slice(0, 12);
}

async function sourceContext(question: string): Promise<string> {
  try {
    const files: string[] = [];
    await sourceFiles(sourceRoot, files);
    const terms = keywords(question);
    const snippets: string[] = [];
    let total = 0;
    for (const path of files) {
      if (snippets.length >= 6 || total >= 12_000) break;
      const content = await readFile(path, 'utf8');
      const lower = content.toLowerCase();
      const name = basename(path).toLowerCase();
      if (!terms.some(term => lower.includes(term) || name.includes(term))) continue;
      const matchedAt = terms.map(term => lower.indexOf(term)).find(index => index >= 0) ?? 0;
      const excerpt = content.slice(Math.max(0, matchedAt - 500), matchedAt + 1_500);
      const label = relative(sourceRoot, path).replace(/\\/g, '/');
      snippets.push(`--- ${label} ---\n${excerpt}`);
      total += label.length + excerpt.length;
    }
    return snippets.join('\n\n');
  } catch {
    return '';
  }
}

async function answer(question: string, player: string): Promise<string> {
  const [status, progress, sources] = await Promise.all([
    bridge('/v1/status'),
    bridge(`/v1/player?name=${encodeURIComponent(player)}`),
    sourceContext(question),
  ]);
  const response = await fetch(`${baseUrl}/responses`, {
    method: 'POST',
    headers: { authorization: `Bearer ${required('OPENAI_API_KEY')}`, 'content-type': 'application/json' },
    body: JSON.stringify({
      model,
      max_output_tokens: 450,
      input: [
        '你是 Zyu 的 Minecraft Cobblemon 助手。用简洁中文回答。',
        '只能根据下方实时数据和源码片段断言事实；不存在的数据明确说未知。不得建议执行作弊、给物品或修改世界的操作。',
        `当前服务器：${JSON.stringify(status)}`,
        `提问玩家状态：${JSON.stringify(progress)}`,
        sources ? `有限 Cobblemon 源码检索结果：\n${sources}` : '本次没有匹配的源码片段。',
        `问题：${question}`,
      ].join('\n\n'),
    }),
  });
  if (!response.ok) throw new Error(`OpenAI ${response.status}`);
  const data = await response.json() as { output_text?: string; output?: Array<{ content?: Array<{ text?: string }> }> };
  return data.output_text ?? data.output?.flatMap(item => item.content ?? []).map(item => item.text ?? '').join('') ?? '没有生成可显示的回答。';
}

function allowed(identity: string): boolean {
  const now = Date.now();
  const day = new Date().toISOString().slice(0, 10);
  if (daily.day !== day) daily = { day, count: 0 };
  if (answerInFlight || daily.count >= dailyLimit || now - (usage.get(identity) ?? 0) < cooldown) return false;
  usage.set(identity, now);
  daily.count++;
  return true;
}

function readBody(request: IncomingMessage): Promise<string> {
  return new Promise((resolveBody, reject) => {
    let value = '';
    request.setEncoding('utf8');
    request.on('data', (part: string) => {
      value += part;
      if (value.length > 8_192) reject(new Error('body_too_large'));
    });
    request.on('end', () => resolveBody(value));
    request.on('error', reject);
  });
}

function reply(response: ServerResponse, status: number, value?: BridgeData): void {
  if (value) response.writeHead(status, { 'content-type': 'application/json' }).end(JSON.stringify(value));
  else response.writeHead(status).end();
}

function validQuestion(value: unknown): value is Question {
  if (!value || typeof value !== 'object') return false;
  const question = value as Partial<Question>;
  return typeof question.player === 'string' && question.player.length > 0 && typeof question.question === 'string' && question.question.length > 0 && question.question.length <= 500;
}

function hasValidBridgeSignature(request: IncomingMessage, body: string): boolean {
  const timestamp = request.headers['x-zcn-timestamp'];
  const nonce = request.headers['x-zcn-nonce'];
  const actual = request.headers['x-zcn-signature'];
  if (typeof timestamp !== 'string' || typeof nonce !== 'string' || typeof actual !== 'string') return false;
  if (!Number.isFinite(Number(timestamp)) || Math.abs(Date.now() / 1_000 - Number(timestamp)) > 60) return false;
  return tokenMatches(actual, signature(timestamp, nonce, 'POST', '/v1/questions', body));
}

async function handleHttp(request: IncomingMessage, response: ServerResponse): Promise<void> {
  if (request.method !== 'POST' || !request.url) return reply(response, 404);
  let body: string;
  try {
    body = await readBody(request);
  } catch {
    return reply(response, 413);
  }
  let value: unknown;
  try {
    value = JSON.parse(body);
  } catch {
    return reply(response, 400);
  }

  if (request.url === '/v1/questions') {
    if (!hasValidBridgeSignature(request, body)) return reply(response, 401, { error: 'unauthorized' });
    if (!validQuestion(value)) return reply(response, 400, { error: 'invalid_question' });
    const gameQuestion = value as Partial<GameQuestion>;
    if (typeof gameQuestion.requestId !== 'string' || typeof gameQuestion.playerUuid !== 'string') return reply(response, 400, { error: 'invalid_game_question' });
    if (!allowed(gameQuestion.playerUuid)) return reply(response, 429, { error: 'question_rate_limited' });
    const verifiedQuestion = gameQuestion as GameQuestion;
    reply(response, 202);
    answerInFlight = true;
    void answer(verifiedQuestion.question, verifiedQuestion.player)
      .then(answerText => bridge('/v1/answers', 'POST', JSON.stringify({ requestId: verifiedQuestion.requestId, answer: answerText })))
      .catch(error => bridge('/v1/answers', 'POST', JSON.stringify({ requestId: verifiedQuestion.requestId, answer: `暂时无法回答：${error.message}` })).catch(() => undefined))
      .finally(() => { answerInFlight = false; });
    return;
  }

  if (request.url !== '/v1/web-questions') return reply(response, 404);
  if (!isLoopback(request.socket.remoteAddress) || !tokenMatches(request.headers.authorization?.replace(/^Bearer\s+/i, ''), webAiToken) || !validQuestion(value) || !allowed(`web:${value.player}`)) return reply(response, 401);
  answerInFlight = true;
  try {
    reply(response, 200, { answer: await answer(value.question, value.player) });
  } catch (error) {
    reply(response, 502, { error: error instanceof Error ? error.message : 'answer_failed' });
  } finally {
    answerInFlight = false;
  }
}

const mcp = new McpServer({ name: 'zyu-cobblemon-note', version: '0.1.1' });
const tools: Array<[string, string, Record<string, z.ZodType>, (args: Record<string, string>) => Promise<unknown>]> = [
  ['get_server_status', '读取当前 TPS、MSPT 与在线人数。', {}, () => bridge('/v1/status')],
  ['list_online_players', '读取在线玩家的位置与基础状态。', {}, () => bridge('/v1/players')],
  ['get_player_progress', '读取指定在线玩家的背包、状态和队伍。', { name: z.string() }, args => bridge(`/v1/player?name=${encodeURIComponent(args.name)}`)],
  ['get_player_party', '读取指定在线玩家的 Cobblemon 队伍。', { name: z.string() }, args => bridge(`/v1/party?name=${encodeURIComponent(args.name)}`)],
  ['get_player_pc_summary', '读取指定在线玩家的电脑宝可梦摘要。', { name: z.string() }, args => bridge(`/v1/pc?name=${encodeURIComponent(args.name)}`)],
  ['list_bases', '列出已登记基地。', {}, () => bridge('/v1/bases')],
  ['get_base_inventory', '读取已登记基地的容器物资；未加载区块会明确标注。', { name: z.string() }, args => bridge(`/v1/base?name=${encodeURIComponent(args.name)}`)],
];

for (const [name, description, inputSchema, handler] of tools) {
  mcp.registerTool(name, { description, inputSchema }, async args => mcpText(await handler(args as Record<string, string>)));
}

if (!['http', 'mcp', 'both'].includes(mode)) throw new Error('GATEWAY_MODE must be http, mcp, or both');
if (mode !== 'mcp') createServer((request, response) => { void handleHttp(request, response); }).listen(port, host);
if (mode !== 'http') await mcp.connect(new StdioServerTransport());
