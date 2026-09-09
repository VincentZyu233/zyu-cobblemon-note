package io.github.vincentzyu233.cobblemonnote

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container

class SnapshotService(private val server: MinecraftServer, private val config: BridgeConfig) {
    fun status() = JsonObject().apply {
        val mspt = server.averageTickTimeNanos / 1_000_000.0
        addProperty("onlinePlayers", server.playerCount); addProperty("maxPlayers", server.maxPlayers)
        addProperty("mspt", "%.2f".format(mspt).toDouble()); addProperty("tps", "%.2f".format(minOf(20.0, 1000.0 / maxOf(mspt, 1.0))).toDouble())
        addProperty("cobblemonAvailable", runCatching { Class.forName("com.cobblemon.mod.common.Cobblemon") }.isSuccess)
    }
    fun players() = JsonArray().also { result -> server.playerList.players.forEach { player -> result.add(playerSummary(player)) } }
    fun player(player: ServerPlayer) = playerSummary(player).apply {
        add("inventory", inventory(player.inventory)); add("party", cobblemonParty(player))
    }
    fun party(player: ServerPlayer) = cobblemonParty(player)
    fun pc(player: ServerPlayer): JsonObject = JsonObject().apply {
        val pc = cobblemonStorage()?.let { invoke(it, "getPC", player) } ?: throw IllegalStateException("Cobblemon unavailable")
        val values = (pc as? Iterable<*>)?.toList().orEmpty()
        addProperty("pokemonCount", values.size); add("sample", JsonArray().also { array -> values.take(12).forEach { value -> value?.let { array.add(pokemon(it)) } } })
    }
    fun bases() = JsonArray().also { array -> config.bases.forEach { base -> array.add(JsonObject().apply { addProperty("name", base.name); addProperty("regions", base.regions.size); addProperty("targets", base.targets.size) }) } }
    fun base(name: String): JsonObject {
        val base = config.bases.firstOrNull { it.name.equals(name, true) } ?: throw IllegalArgumentException("Unknown base: $name")
        val containers = JsonArray()
        base.targets.forEach { target -> containers.add(container(target.dimension, target.x, target.y, target.z, target.label)) }
        base.regions.forEach { region -> scanRegion(region).forEach(containers::add) }
        return JsonObject().apply { addProperty("name", base.name); add("containers", containers) }
    }
    private fun playerSummary(player: ServerPlayer) = JsonObject().apply {
        addProperty("name", player.gameProfile.name); addProperty("uuid", player.uuid.toString()); addProperty("dimension", player.level().dimension().location().toString())
        addProperty("x", player.x); addProperty("y", player.y); addProperty("z", player.z); addProperty("health", player.health); addProperty("food", player.foodData.foodLevel)
    }
    private fun inventory(container: Container) = JsonArray().also { array ->
        val totals = linkedMapOf<String, Int>(); repeat(container.containerSize) { slot -> container.getItem(slot).takeUnless { it.isEmpty }?.let { totals.merge(it.item.toString(), it.count, Int::plus) } }
        totals.forEach { (item, count) -> array.add(JsonObject().apply { addProperty("item", item); addProperty("count", count) }) }
    }
    private fun container(dimension: String, x: Int, y: Int, z: Int, label: String) = JsonObject().apply {
        val position = BlockPos(x, y, z); addProperty("position", "$dimension:$x:$y:$z"); if (label.isNotBlank()) addProperty("label", label)
        val level = server.getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimension)))
        if (level == null || !level.hasChunkAt(position)) { addProperty("state", "unloaded"); return@apply }
        val entity = level.getBlockEntity(position); if (entity !is Container) { addProperty("state", "not_container"); return@apply }
        addProperty("state", "loaded"); addProperty("block", level.getBlockState(position).block.descriptionId); add("items", inventory(entity))
    }
    private fun scanRegion(region: BaseRegion): List<JsonObject> {
        val volume = (kotlin.math.abs(region.maxX - region.minX) + 1).toLong() * (kotlin.math.abs(region.maxY - region.minY) + 1) * (kotlin.math.abs(region.maxZ - region.minZ) + 1)
        require(volume <= config.maxRegionBlocks) { "Region exceeds maxRegionBlocks: $volume" }
        return (minOf(region.minX, region.maxX)..maxOf(region.minX, region.maxX)).flatMap { x -> (minOf(region.minY, region.maxY)..maxOf(region.minY, region.maxY)).flatMap { y -> (minOf(region.minZ, region.maxZ)..maxOf(region.minZ, region.maxZ)).map { z -> container(region.dimension, x, y, z, "") } } }.filter { it.get("state").asString != "not_container" }
    }
    private fun cobblemonParty(player: ServerPlayer): JsonArray = JsonArray().also { array ->
        val party = cobblemonStorage()?.let { runCatching { invoke(it, "getParty", player) }.getOrNull() } ?: return@also
        (runCatching { invoke(party, "toGappyList") }.getOrNull() as? Iterable<*>)?.forEachIndexed { slot, value -> array.add(JsonObject().apply { addProperty("slot", slot); if (value == null) addProperty("empty", true) else add("pokemon", pokemon(value)) }) }
    }
    private fun pokemon(value: Any) = JsonObject().apply {
        val name = runCatching { invoke(value, "getDisplayName")?.let { invoke(it, "getString") } }.getOrNull()?.toString() ?: "Unknown"
        addProperty("name", name)
        addProperty("level", (runCatching { invoke(value, "getLevel") }.getOrNull() as? Number)?.toInt() ?: 0)
    }
    private fun cobblemonStorage(): Any? = runCatching { invoke(Class.forName("com.cobblemon.mod.common.Cobblemon").getField("INSTANCE").get(null), "getStorage") }.getOrNull()
    private fun invoke(target: Any, name: String, vararg args: Any): Any? = target.javaClass.methods.firstOrNull { it.name == name && it.parameterCount == args.size && it.parameterTypes.withIndex().all { (i, type) -> type.isAssignableFrom(args[i].javaClass) } }?.invoke(target, *args) ?: throw IllegalStateException("Cobblemon API missing $name")
}
