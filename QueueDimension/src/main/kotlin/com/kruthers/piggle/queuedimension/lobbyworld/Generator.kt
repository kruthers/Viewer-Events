package com.kruthers.piggle.queuedimension.lobbyworld

import org.bukkit.Material
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import java.util.*

class Generator: ChunkGenerator() {

    override fun shouldGenerateNoise(): Boolean {
        return false
    }

    override fun shouldGenerateCaves(): Boolean {
        return false
    }

    override fun shouldGenerateDecorations(): Boolean {
        return false
    }

    override fun shouldGenerateMobs(): Boolean {
        return false
    }

    override fun shouldGenerateStructures(): Boolean {
        return false
    }

    override fun generateSurface(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int, chunkData: ChunkData) {
        chunkData.setRegion(0,chunkData.minHeight,0,15,chunkData.maxHeight,0,Material.AIR)
//        super.generateSurface(worldInfo, random, chunkX, chunkZ, chunkData)
    }

}