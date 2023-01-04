package com.kruthers.piggles.queuecore.listeners

import com.kruthers.piggles.queuecore.QueueCore
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import org.bukkit.scheduler.BukkitTask
import java.util.*

class ConnectionEvents(private val plugin: QueueCore): Listener {
    private val logouts: HashMap<UUID, BukkitTask> = HashMap()

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val tags = event.player.scoreboardTags
        if (!tags.contains("queue_bypass")) {
            //Add the player to the queue if they are not a queue master or participating
            if (!QueueCore.queue.inQueue(event.player) && !QueueCore.queue.isParticipating(event.player)) {
                QueueCore.queue.add(event.player)
            } else {
                event.player.sendMessage("In queue: ${QueueCore.queue.inQueue(event.player)} | Participating: ${QueueCore.queue.isParticipating(event.player)}")
            }
        }

        //Make sure their inventory is clear
        if (QueueCore.queue.inQueue(event.player) &&
            (this.plugin.config.getString("handling.inventory_handling") == "pass_on" ||
                    this.plugin.config.getString("handling.inventory_handling") == "CLEAR"
                    )
        ) {
            event.player.inventory.clear()
        }

        //remove them from the logouts queue
        logouts.remove(event.player.uniqueId)
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        val queue = QueueCore.queue
        val player = event.player
        if (queue.inQueue(player)) {
            if (this.plugin.config.getLong("logout_timeouts.queue") > 0L) {
                val task = Bukkit.getScheduler().runTaskLater(this.plugin, Runnable {
                    this.removeFromQueue(player)
                }, this.plugin.config.getLong("logout_timeouts.queue"))
                this.logouts[player.uniqueId] = task
            } else if (this.plugin.config.getLong("logout_timeouts.queue") == 0L) {
                this.removeFromQueue(player)
            }
        } else if (queue.isParticipating(player)) {
            if (this.plugin.config.getLong("logout_timeouts.queue") > 0L) {
                val task = Bukkit.getScheduler().runTaskLater(this.plugin, Runnable {
                    this.removeParticipant(player, player.inventory)
                }, this.plugin.config.getLong("logout_timeouts.queue"))
                this.logouts[player.uniqueId] = task
            } else if (this.plugin.config.getLong("logout_timeouts.queue") == 0L) {
                this.removeParticipant(player, player.inventory)
            }
        }
    }

    private fun removeFromQueue(player: OfflinePlayer) {
        QueueCore.queue.remove(player)

        //remove them from the logouts queue
        logouts.remove(player.uniqueId)
    }

    private fun removeParticipant(player: OfflinePlayer, inv: Inventory) {
        QueueCore.queue.removeParticipating(player)

        if (this.plugin.config.getBoolean("handling.auto_replace")) {
            val new = QueueCore.queue.select()
            if (this.plugin.config.getString("handling.inventory_handling") == "pass_on" && new != null) {
                new.inventory.contents = inv.contents
            }
        }

        //remove them from the logouts queue
        logouts.remove(player.uniqueId)
    }

}