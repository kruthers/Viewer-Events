package com.kruthers.piggles.queuecore.events

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * An event triggered every time someone joins the queue
 * @param player The player who was added to the queue
 * @param startWeight The starting weight to be applied if they have not joined the queue before
 */
class QueueJoinEvent(val player: Player, var startWeight: Int, var message: Component?): Event(), Cancellable {
    companion object {
        private val handlers = HandlerList()

        fun getHandlers(): HandlerList {
            return handlers
        }
    }

    override fun getHandlers(): HandlerList {
        return QueueJoinEvent.handlers
    }

    override fun isCancelled(): Boolean {
        return false
    }

    override fun setCancelled(cancel: Boolean) {

    }


}