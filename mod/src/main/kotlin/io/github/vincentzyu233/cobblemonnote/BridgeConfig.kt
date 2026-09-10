package io.github.vincentzyu233.cobblemonnote

import com.google.gson.GsonBuilder

import net.fabricmc.loader.api.FabricLoader

import java.nio.file.Files

data class BridgeConfig(
    var bindHost: String = "127.0.0.1",
    var port: Int = 25931,
    var sharedSecret: String = "",
    var gatewayQuestionUrl: String = "http://127.0.0.1:25932/v1/questions",
    var accessMode: String = "public_full",
    var maxRegionBlocks: Int = 32768,
    var bases: MutableList<BaseDefinition> = mutableListOf()
) {
    fun hasSecret() = sharedSecret.isNotBlank()

    companion object {
        private val gson = GsonBuilder().setPrettyPrinting().create()
        fun load(): BridgeConfig {
            val path = FabricLoader.getInstance().configDir.resolve("zyu-cobblemon-note.json")
            if (Files.notExists(path)) {
                val config = BridgeConfig()
                Files.writeString(path, gson.toJson(config))
                return config
            }
            return gson.fromJson(Files.readString(path), BridgeConfig::class.java) ?: BridgeConfig()
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
