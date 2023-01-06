package com.kruthers.piggles.permslink

import com.kruthers.piggles.queuecore.QueueCore
import com.kruthers.piggles.queuecore.QueueManager
import com.kruthers.piggles.queuecore.events.QueueJoinEvent
import com.kruthers.piggles.queuecore.events.QueueMasterAddEvent
import com.kruthers.piggles.queuecore.events.QueueMasterRemoveEvent
import com.kruthers.piggles.queuecore.events.SelectPlayerEvent
import net.milkbowl.vault.permission.Permission
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin

class QueuePermsLInk: JavaPlugin(), Listener {
    private var perms: Permission? = null
    private var queue: QueueManager? = null

    override fun onEnable() {
        this.logger.info("Setting up Queue Link, checking for required plugins")
        if (this.server.pluginManager.getPlugin("QueueCore") == null) {
            this.logger.severe("Queue Core not installed, not loading plugin")
            this.server.pluginManager.disablePlugin(this)
            return
        }
        queue = QueueCore.queue
        if (!setupPermissions()) {
            this.logger.severe("Vault not installed, not loading plugin")
            this.server.pluginManager.disablePlugin(this)
            return
        }
        this.logger.info("Loading config")
        this.config.options().copyDefaults(true)
        this.saveConfig()
        this.logger.info("Loading events")
        this.server.pluginManager.registerEvents(this,this)
        this.logger.info("Plugin Loaded")
    }

    private fun setupPermissions(): Boolean {
        val rsp: RegisteredServiceProvider<Permission>? = server.servicesManager.getRegistration(
            Permission::class.java
        )
        this.perms = rsp?.provider
        return this.perms != null
    }

    @EventHandler
    private fun onQueueJoin(event: QueueJoinEvent) {
        //remove participant group
        this.perms?.playerRemoveGroup(null, event.player, this.config.getString("group.participant")?: "participant")
        //add them to the queue group
        this.perms?.playerAddGroup(null, event.player, this.config.getString("group.queued")?: "queued")
    }

    @EventHandler
    private fun onSelectEvent(event: SelectPlayerEvent) {
        //remove queue group
        this.perms?.playerRemoveGroup(null, event.player, this.config.getString("group.queued")?: "queued")
        //add them to the participant group
        this.perms?.playerAddGroup(null, event.player, this.config.getString("group.participant")?: "participant")
    }

    @EventHandler
    private fun onQueueMasterAdd(event: QueueMasterAddEvent) {
        //add them to the participant group
        this.perms?.playerAddGroup(null, event.player, this.config.getString("group.queue_master")?: "queue_master")
    }

    @EventHandler
    private fun onQueueMasterRemove(event: QueueMasterRemoveEvent) {
        //add them to the participant group
        this.perms?.playerRemoveGroup(null, event.player, this.config.getString("group.queue_master")?: "queue_master")
    }
}