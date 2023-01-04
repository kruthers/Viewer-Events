package com.kruthers.piggle.queuedimension.lobbyworld

import org.bukkit.block.Biome
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.WorldInfo

class LobbyBiomeProvider: BiomeProvider() {
    /**
     * Return the Biome which should be present at the provided location.
     *
     *
     * Notes:
     *
     *
     * This method **must** be completely thread safe and able to handle
     * multiple concurrent callers.
     *
     *
     * This method should only return biomes which are present in the list
     * returned by [.getBiomes]
     *
     *
     * This method should **never** return [Biome.CUSTOM].
     *
     * @param worldInfo The world info of the world the biome will be used for
     * @param x The X-coordinate from world origin
     * @param y The Y-coordinate from world origin
     * @param z The Z-coordinate from world origin
     * @return Biome for the given location
     */
    override fun getBiome(worldInfo: WorldInfo, x: Int, y: Int, z: Int): Biome {
        return Biome.PLAINS
    }

    /**
     * Returns a list with every biome the [BiomeProvider] will use for
     * the given world.
     *
     *
     * Notes:
     *
     *
     * This method only gets called once, when the world is loaded. Returning
     * another list or modifying the values from the initial returned list later
     * one, are not respected.
     *
     *
     * This method should **never** return a list which contains
     * [Biome.CUSTOM].
     *
     * @param worldInfo The world info of the world the list will be used for
     * @return A list with every biome the [BiomeProvider] uses
     */
    override fun getBiomes(worldInfo: WorldInfo): MutableList<Biome> {
        return mutableListOf(Biome.PLAINS)
    }
}