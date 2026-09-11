package io.github.vincentzyu233.cobblemonnote

import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

private data class SelectionPoint(val dimension: String, val position: BlockPos)
private data class PlayerSelection(
    var first: SelectionPoint? = null,
    var second: SelectionPoint? = null,
    var baseName: String? = null,
)

class BaseSelectionService(private val config: BridgeConfig) {
    private val selections = ConcurrentHashMap<UUID, PlayerSelection>()

    fun isWand(stack: ItemStack): Boolean = runCatching {
        ResourceLocation.parse(config.regionWandItem) == BuiltInRegistries.ITEM.getKey(stack.item)
    }.getOrDefault(false)

    fun setFirst(player: ServerPlayer, dimension: String, position: BlockPos) {
        val selection = selections.computeIfAbsent(player.uuid) { PlayerSelection() }
        selection.first = SelectionPoint(dimension, position.immutable())
        player.sendSystemMessage(Component.literal("[基地] 第一角：${dimension} ${position.x} ${position.y} ${position.z}"))
    }

    fun setSecond(player: ServerPlayer, dimension: String, position: BlockPos) {
        val selection = selections.computeIfAbsent(player.uuid) { PlayerSelection() }
        selection.second = SelectionPoint(dimension, position.immutable())
        player.sendSystemMessage(Component.literal("[基地] 第二角：${dimension} ${position.x} ${position.y} ${position.z}"))
    }

    fun create(player: ServerPlayer, name: String): String {
        require(name.isNotBlank()) { "基地名称不能为空" }
        require(findBase(name) == null) { "基地已存在：$name" }
        config.bases.add(BaseDefinition(name = name))
        selection(player).baseName = name
        BridgeConfig.save(config)
        return "已创建并选中基地：$name"
    }

    fun select(player: ServerPlayer, name: String): String {
        val base = findBase(name) ?: throw IllegalArgumentException("未知基地：$name")
        selection(player).baseName = base.name
        return "当前基地：${base.name}"
    }

    fun status(player: ServerPlayer): String {
        val selection = selections[player.uuid]
        val base = selection?.baseName ?: "未选择"
        val first = selection?.first?.display() ?: "未设置"
        val second = selection?.second?.display() ?: "未设置"
        val volume = selection?.let { volume(it.first, it.second) }?.toString() ?: "-"
        return "当前基地：$base；第一角：$first；第二角：$second；体积：$volume"
    }

    fun add(player: ServerPlayer): String {
        val selection = selection(player)
        val baseName = selection.baseName ?: throw IllegalArgumentException("先执行 /zcn base select <名称>")
        val first = selection.first ?: throw IllegalArgumentException("先用配置棒左键设置第一角")
        val second = selection.second ?: throw IllegalArgumentException("再用配置棒右键设置第二角")
        require(first.dimension == second.dimension) { "两个角必须在同一维度" }
        val volume = volume(first, second) ?: throw IllegalArgumentException("无法计算选区体积")
        require(volume <= config.maxRegionBlocks.toLong()) {
            "选区体积 $volume 超过 maxRegionBlocks=${config.maxRegionBlocks}"
        }
        val base = findBase(baseName) ?: throw IllegalArgumentException("当前基地已被移除：$baseName")
        val region = BaseRegion(
            dimension = first.dimension,
            minX = minOf(first.position.x, second.position.x),
            minY = minOf(first.position.y, second.position.y),
            minZ = minOf(first.position.z, second.position.z),
            maxX = maxOf(first.position.x, second.position.x),
            maxY = maxOf(first.position.y, second.position.y),
            maxZ = maxOf(first.position.z, second.position.z),
        )
        require(base.regions.none { it.sameAs(region) }) { "该区域已登记" }
        base.regions.add(region)
        BridgeConfig.save(config)
        selection.first = null
        selection.second = null
        return "已向 ${base.name} 增加区域，体积 $volume 格"
    }

    fun clear(player: ServerPlayer): String {
        val selection = selection(player)
        selection.first = null
        selection.second = null
        return "已清除两角选区"
    }

    fun remove(player: ServerPlayer) {
        selections.remove(player.uuid)
    }

    private fun findBase(name: String) = config.bases.firstOrNull {
        it.name.equals(name, ignoreCase = true)
    }

    private fun selection(player: ServerPlayer) = selections.computeIfAbsent(player.uuid) { PlayerSelection() }

    private fun volume(first: SelectionPoint?, second: SelectionPoint?): Long? {
        if (first == null || second == null || first.dimension != second.dimension) return null
        return (kotlin.math.abs(first.position.x - second.position.x) + 1L) *
            (kotlin.math.abs(first.position.y - second.position.y) + 1L) *
            (kotlin.math.abs(first.position.z - second.position.z) + 1L)
    }

    private fun SelectionPoint.display() = "$dimension ${position.x} ${position.y} ${position.z}"

    private fun BaseRegion.sameAs(other: BaseRegion) =
        dimension == other.dimension && minX == other.minX && minY == other.minY && minZ == other.minZ &&
            maxX == other.maxX && maxY == other.maxY && maxZ == other.maxZ
}
