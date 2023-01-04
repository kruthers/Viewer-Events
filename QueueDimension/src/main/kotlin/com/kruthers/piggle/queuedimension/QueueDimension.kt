package com.kruthers.piggle.queuedimension

import com.kruthers.piggle.queuedimension.lobbyworld.Generator
import com.kruthers.piggle.queuedimension.lobbyworld.LobbyBiomeProvider
import org.bukkit.*
import org.bukkit.plugin.java.JavaPlugin

class QueueDimension: JavaPlugin() {

    @Override
    override fun onEnable() {
        getQueueWorld()
    }

    fun getQueueWorld(): World? {
        val creator: WorldCreator = WorldCreator(NamespacedKey.fromString("queue:limbo")!!)
            .type(WorldType.FLAT)
            .environment(World.Environment.NORMAL)
            .generator(Generator())
            .biomeProvider(LobbyBiomeProvider())

        return creator.createWorld()
    }

}