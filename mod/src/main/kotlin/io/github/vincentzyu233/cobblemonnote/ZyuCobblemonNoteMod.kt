package io.github.vincentzyu233.cobblemonnote

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mojang.brigadier.arguments.StringArgumentType
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.coordinates.Vec3Argument
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult

import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URI
import java.net.URLDecoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class ZyuCobblemonNoteMod : ModInitializer {
    private lateinit var config: BridgeConfig
    private lateinit var baseSelections: BaseSelectionService
    private lateinit var server: MinecraftServer
    private lateinit var snapshots: SnapshotService
    private var http: HttpServer? = null
    private val pending = ConcurrentHashMap<String, UUID>()
    private val gson = Gson()

    override fun onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register { started -> start(started) }
        ServerLifecycleEvents.SERVER_STOPPING.register { http?.stop(0) }
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            if (this::baseSelections.isInitialized) baseSelections.remove(handler.player)
        }
        AttackBlockCallback.EVENT.register { player, world, hand, position, _ ->
            val serverPlayer = player as? ServerPlayer ?: return@register InteractionResult.PASS
            if (!this::baseSelections.isInitialized || !serverPlayer.hasPermissions(2) || !baseSelections.isWand(serverPlayer.getItemInHand(hand))) return@register InteractionResult.PASS
            baseSelections.setFirst(serverPlayer, world.dimension().location().toString(), position)
            InteractionResult.FAIL
        }
        UseBlockCallback.EVENT.register { player, world, hand, hitResult ->
            val serverPlayer = player as? ServerPlayer ?: return@register InteractionResult.PASS
            if (!this::baseSelections.isInitialized || !serverPlayer.hasPermissions(2) || !baseSelections.isWand(serverPlayer.getItemInHand(hand))) return@register InteractionResult.PASS
            baseSelections.setSecond(serverPlayer, world.dimension().location().toString(), hitResult.blockPos)
            InteractionResult.FAIL
        }
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ -> registerCommands(dispatcher) }
    }

    private fun start(started: MinecraftServer) {
        server = started; config = BridgeConfig.load(); baseSelections = BaseSelectionService(config); snapshots = SnapshotService(server, config)
        if (!config.hasSecret()) { LOGGER.warn("AI bridge disabled: set sharedSecret in config/zyu-cobblemon-note.json"); return }
        http = HttpServer.create(InetSocketAddress(InetAddress.getByName(config.bindHost), config.port), 0).apply { createContext("/v1") { handle(it) }; executor = java.util.concurrent.Executors.newCachedThreadPool(); start() }
        LOGGER.info("AI bridge listening on {}:{}", config.bindHost, config.port)
    }

    private fun handle(exchange: HttpExchange) {
        val body = exchange.requestBody.readBytes().toString(Charsets.UTF_8)
        if (!exchange.remoteAddress.address.isLoopbackAddress || !authorized(exchange, body)) return reply(exchange, 401, errorResponse("unauthorized"))
        try {
            val response = when (exchange.requestURI.path) {
                "/v1/status" -> onServer { snapshots.status() }
                "/v1/players" -> onServer { JsonObject().apply { add("players", snapshots.players()) } }
                "/v1/player" -> onServer { snapshots.player(player(query(exchange)["name"] ?: throw IllegalArgumentException("missing_name"))) }
                "/v1/party" -> onServer { JsonObject().apply { add("party", snapshots.party(player(query(exchange)["name"] ?: throw IllegalArgumentException("missing_name")))) } }
                "/v1/pc" -> onServer { snapshots.pc(player(query(exchange)["name"] ?: throw IllegalArgumentException("missing_name"))) }
                "/v1/bases" -> onServer { JsonObject().apply { add("bases", snapshots.bases()) } }
                "/v1/base" -> onServer { snapshots.base(query(exchange)["name"] ?: throw IllegalArgumentException("missing_name")) }
                "/v1/answers" -> {
                    require(exchange.requestMethod == "POST") { "method_not_allowed" }
                    val answer = gson.fromJson(body, JsonObject::class.java)
                    acceptAnswer(answer["requestId"].asString, answer["answer"].asString)
                    JsonObject().apply { addProperty("accepted", true) }
                }
                else -> errorResponse("not_found")
            }
            reply(exchange, 200, response)
        } catch (error: Exception) { reply(exchange, 400, errorResponse(error.message ?: "request_failed")) }
    }

    private fun onServer(action: () -> JsonObject): JsonObject = java.util.concurrent.CompletableFuture.supplyAsync({ action() }, server).get(5, java.util.concurrent.TimeUnit.SECONDS)
    private fun player(name: String): ServerPlayer = server.playerList.getPlayerByName(name) ?: throw IllegalArgumentException("player_offline")
    private fun authorized(exchange: HttpExchange, body: String): Boolean {
        val timestamp = exchange.requestHeaders.getFirst("X-ZCN-Timestamp") ?: return false; val nonce = exchange.requestHeaders.getFirst("X-ZCN-Nonce") ?: return false
        if (runCatching { kotlin.math.abs(Instant.now().epochSecond - timestamp.toLong()) <= 60 }.getOrDefault(false).not()) return false
        return BridgeSecurity.matches(BridgeSecurity.sign(config.sharedSecret, timestamp, nonce, exchange.requestMethod, exchange.requestURI.path, body), exchange.requestHeaders.getFirst("X-ZCN-Signature"))
    }
    private fun query(exchange: HttpExchange) = (exchange.requestURI.rawQuery ?: "").split("&").filter { it.isNotBlank() }.associate { val pair = it.split("=", limit = 2); URLDecoder.decode(pair[0], Charsets.UTF_8) to URLDecoder.decode(pair.getOrElse(1) { "" }, Charsets.UTF_8) }
    private fun reply(exchange: HttpExchange, code: Int, value: JsonObject) { val bytes = gson.toJson(value).toByteArray(); exchange.responseHeaders.add("Content-Type", "application/json"); exchange.sendResponseHeaders(code, bytes.size.toLong()); exchange.responseBody.use { it.write(bytes) } }
    private fun errorResponse(message: String) = JsonObject().apply { addProperty("error", message) }

    private fun registerCommands(dispatcher: com.mojang.brigadier.CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("zcn")
                .requires(::canManageBases)
                .then(Commands.literal("base")
                    .then(Commands.literal("create").then(Commands.argument("name", StringArgumentType.greedyString()).executes { context -> baseAction(context.source) { baseSelections.create(it, StringArgumentType.getString(context, "name")) } }))
                    .then(Commands.literal("select").then(Commands.argument("name", StringArgumentType.greedyString()).executes { context -> baseAction(context.source) { baseSelections.select(it, StringArgumentType.getString(context, "name")) } }))
                    .then(Commands.literal("status").executes { context -> baseAction(context.source) { baseSelections.status(it) } })
                    .then(Commands.literal("add").executes { context -> baseAction(context.source) { baseSelections.add(it) } })
                    .then(Commands.literal("clear").executes { context -> baseAction(context.source) { baseSelections.clear(it) } }))
        )
        dispatcher.register(
            Commands.literal("goto")
                .requires(::canGoto)
                .then(Commands.argument("player", EntityArgument.player()).executes { context ->
                    val traveler = context.source.playerOrException
                    val destination = EntityArgument.getPlayer(context, "player")
                    traveler.teleportTo(destination.serverLevel(), destination.x, destination.y, destination.z, destination.yRot, destination.xRot)
                    context.source.sendSuccess({ Component.literal("已传送至 ${destination.gameProfile.name}") }, false)
                    1
                })
                .then(Commands.argument("location", Vec3Argument.vec3()).executes { context ->
                    val traveler = context.source.playerOrException
                    val destination = Vec3Argument.getVec3(context, "location")
                    traveler.teleportTo(traveler.serverLevel(), destination.x, destination.y, destination.z, traveler.yRot, traveler.xRot)
                    context.source.sendSuccess({ Component.literal("已传送至 %.1f, %.1f, %.1f".format(destination.x, destination.y, destination.z)) }, false)
                    1
                })
        )
        dispatcher.register(
            Commands.literal("suicide")
                .requires { config.suicideEnabled && it.entity is ServerPlayer }
                .executes { context ->
                    val player = context.source.playerOrException
                    context.source.sendSuccess({ Component.literal("ouch.... that looks hurt") }, false)
                    player.kill()
                    1
                }
        )
        dispatcher.register(Commands.literal("ai")
            .then(Commands.literal("status").executes { context -> respond(context.source) { snapshots.status() } })
            .then(Commands.literal("players").executes { context ->
                if (!canRead(context.source, shared = true)) deny(context.source) else respond(context.source) { JsonObject().apply { add("players", snapshots.players()) } }
            })
            .then(Commands.literal("player").then(Commands.argument("name", StringArgumentType.word()).executes { context ->
                val name = StringArgumentType.getString(context, "name")
                if (!canRead(context.source, name)) deny(context.source) else respond(context.source) { snapshots.player(player(name)) }
            }))
            .then(Commands.literal("party").then(Commands.argument("name", StringArgumentType.word()).executes { context ->
                val name = StringArgumentType.getString(context, "name")
                if (!canRead(context.source, name)) deny(context.source) else respond(context.source) { JsonObject().apply { add("party", snapshots.party(player(name))) } }
            }))
            .then(Commands.literal("base").then(Commands.argument("name", StringArgumentType.word()).executes { context ->
                if (!canRead(context.source, shared = true)) deny(context.source) else respond(context.source) { snapshots.base(StringArgumentType.getString(context, "name")) }
            }))
            .then(Commands.literal("question").then(Commands.argument("text", StringArgumentType.greedyString()).executes { context ->
                if (!canQuestion(context.source)) deny(context.source) else ask(context.source.playerOrException, StringArgumentType.getString(context, "text")); 1
            })))
    }
    private fun send(source: CommandSourceStack, value: JsonObject) { source.sendSuccess({ Component.literal(gson.toJson(value)) }, false) }
    private fun respond(source: CommandSourceStack, action: () -> JsonObject): Int {
        return runCatching { action() }.fold({ value -> send(source, value); 1 }, { error -> source.sendFailure(Component.literal("[AI] ${error.message ?: "查询失败"}")); 0 })
    }
    private fun canRead(source: CommandSourceStack, requestedName: String? = null, shared: Boolean = false): Boolean = when (config.accessMode.lowercase()) {
        "admin_only" -> source.hasPermission(2)
        "self_and_admin" -> source.hasPermission(2) || (!shared && requestedName != null && (source.entity as? ServerPlayer)?.gameProfile?.name.equals(requestedName, true))
        else -> true
    }
    private fun canGoto(source: CommandSourceStack): Boolean {
        if (source.hasPermission(2)) return true
        val player = source.entity as? ServerPlayer ?: return false
        return config.gotoAllowedPlayers.any { it.equals(player.gameProfile.name, ignoreCase = true) }
    }
    private fun canManageBases(source: CommandSourceStack): Boolean = source.hasPermission(2) && source.entity is ServerPlayer
    private fun baseAction(source: CommandSourceStack, action: (ServerPlayer) -> String): Int = runCatching { action(source.playerOrException) }.fold(
        { message -> source.sendSuccess({ Component.literal("[基地] $message") }, false); 1 },
        { error -> source.sendFailure(Component.literal("[基地] ${error.message ?: "操作失败"}")); 0 },
    )
    private fun canQuestion(source: CommandSourceStack): Boolean = config.accessMode.lowercase() != "admin_only" || source.hasPermission(2)
    private fun deny(source: CommandSourceStack): Int { source.sendFailure(Component.literal("[AI] 当前 accessMode 不允许此查询。")); return 0 }
    private fun ask(player: ServerPlayer, question: String) {
        require(question.length <= 500) { "问题最多 500 字" }; val id = UUID.randomUUID().toString(); pending[id] = player.uuid
        val body = JsonObject().apply { addProperty("requestId", id); addProperty("player", player.gameProfile.name); addProperty("playerUuid", player.uuid.toString()); addProperty("question", question) }.toString()
        val now = Instant.now().epochSecond.toString(); val nonce = UUID.randomUUID().toString(); val uri = URI.create(config.gatewayQuestionUrl)
        val request = HttpRequest.newBuilder(uri).header("Content-Type", "application/json").header("X-ZCN-Timestamp", now).header("X-ZCN-Nonce", nonce).header("X-ZCN-Signature", BridgeSecurity.sign(config.sharedSecret, now, nonce, "POST", uri.path, body)).POST(HttpRequest.BodyPublishers.ofString(body)).build()
        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString()).whenComplete { response, failure ->
            server.execute {
                when {
                    failure != null -> player.sendSystemMessage(Component.literal("[AI] 提交失败：${failure.message ?: "无法连接 AI 网关"}"))
                    response.statusCode() == 202 -> player.sendSystemMessage(Component.literal("[AI] 已提交问题，回答会通过系统消息返回。"))
                    else -> {
                        pending.remove(id)
                        player.sendSystemMessage(Component.literal("[AI] 提交失败（${response.statusCode()}）：${response.body().take(200)}"))
                    }
                }
            }
        }
    }
    private fun acceptAnswer(requestId: String, answer: String) { pending.remove(requestId)?.let { uuid -> server.playerList.getPlayer(uuid)?.sendSystemMessage(Component.literal("[AI] ${answer.take(2_000)}")) } }
    companion object { private val LOGGER = org.slf4j.LoggerFactory.getLogger("zyu-cobblemon-note") }
}
