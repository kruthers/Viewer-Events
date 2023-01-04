package com.kruthers.piggles.queuecore.events

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class QueueMasterAddEvent(val player: Player): Event(), Cancellable {
    companion object {
        private val handlers = HandlerList()

        fun getHandlers(): HandlerList {
            return handlers
        }
    }

    override fun getHandlers(): HandlerList {
        return QueueMasterAddEvent.handlers
    }

    /**
     * Gets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins
     * @return false always as event can't be canceled
     */
    override fun isCancelled(): Boolean {
        return false
    }

    /**
     * Event cannot be canceled
     * @param cancel true if you wish to cancel this event
     */
    override fun setCancelled(cancel: Boolean) {

    }
}