package io.github.vincentzyu233.cobblemonnote

import com.google.gson.GsonBuilder

import net.fabricmc.loader.api.FabricLoader

import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

data class BridgeConfig(
    var bindHost: String = "127.0.0.1",
    var port: Int = 25931,
    var sharedSecret: String = "",
    var gatewayQuestionUrl: String = "http://127.0.0.1:25932/v1/questions",
    var accessMode: String = "public_full",
    var gotoAllowedPlayers: MutableList<String> = mutableListOf(),
    var suicideEnabled: Boolean = true,
    var regionWandItem: String = "minecraft:golden_hoe",
    var maxRegionBlocks: Int = 32768,
    var bases: MutableList<BaseDefinition> = mutableListOf()
) {
    fun hasSecret() = sharedSecret.isNotBlank()

    companion object {
        private val gson = GsonBuilder().setPrettyPrinting().create()
        private fun path() = FabricLoader.getInstance().configDir.resolve("zyu-cobblemon-note.json")

        fun load(): BridgeConfig {
            val configPath = path()
            if (Files.notExists(configPath)) {
                val config = BridgeConfig()
                Files.writeString(configPath, gson.toJson(config))
                return config
            }
            return gson.fromJson(Files.readString(configPath), BridgeConfig::class.java) ?: BridgeConfig()
        }

        fun save(config: BridgeConfig) {
            val configPath = path()
            val temporaryPath = configPath.resolveSibling("${configPath.fileName}.tmp")
            Files.writeString(temporaryPath, gson.toJson(config))
            try {
                Files.move(temporaryPath, configPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(temporaryPath, configPath, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }
}

data class BaseDefinition(
    var name: String = "",
    var regions: MutableList<BaseRegion> = mutableListOf(),
    var targets: MutableList<ContainerTarget> = mutableListOf()
)
data class BaseRegion(var dimension: String = "minecraft:overworld", var minX: Int = 0, var minY: Int = 0, var minZ: Int = 0, var maxX: Int = 0, var maxY: Int = 0, var maxZ: Int = 0)
data class ContainerTarget(var dimension: String = "minecraft:overworld", var x: Int = 0, var y: Int = 0, var z: Int = 0, var label: String = "")
