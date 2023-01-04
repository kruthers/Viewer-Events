package com.kruthers.piggles.queuecore.listeners

import com.kruthers.piggles.queuecore.QueueCore
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class DeathListener(val plugin: QueueCore): Listener {

    @EventHandler
    private fun onDeath(event: PlayerDeathEvent) {
        if (plugin.config.getBoolean("handling.auto_replace") && QueueCore.queue.isParticipating(event.player)) {
            val new = QueueCore.queue.select()
            if (new != null) {
                QueueCore.queue.swap(event.player, new)
            }
        }
    }

}