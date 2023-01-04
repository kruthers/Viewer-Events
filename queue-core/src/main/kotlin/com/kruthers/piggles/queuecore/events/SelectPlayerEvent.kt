package com.kruthers.piggles.queuecore.events

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class SelectPlayerEvent(val player: Player, var private_msg: Component?, var public_msg: Component?): Event(), Cancellable {
    companion object {
        private val handlers = HandlerList()

        fun getHandlers(): HandlerList {
            return handlers
        }
    }

    private var cancelled = false

    override fun getHandlers(): HandlerList {
        return SelectPlayerEvent.handlers
    }

    override fun isCancelled(): Boolean {
        return this.cancelled
    }

    override fun setCancelled(cancel: Boolean) {
        this.cancelled = cancel
    }
}