package com.kruthers.piggles.queuecore

import cloud.commandframework.ArgumentDescription
import cloud.commandframework.captions.FactoryDelegatingCaptionRegistry
import cloud.commandframework.execution.CommandExecutionCoordinator
import cloud.commandframework.minecraft.extras.AudienceProvider
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler
import cloud.commandframework.paper.PaperCommandManager
import com.kruthers.piggles.queuecore.classes.InventoryHandling
import com.kruthers.piggles.queuecore.classes.MainRunable
import com.kruthers.piggles.queuecore.classes.QueueExceptionCaptions
import com.kruthers.piggles.queuecore.classes.QueueType
import com.kruthers.piggles.queuecore.commands.AdminCommands
import com.kruthers.piggles.queuecore.commands.ControlCommands
import com.kruthers.piggles.queuecore.commands.ListCommands
import com.kruthers.piggles.queuecore.listeners.ConnectionEvents
import com.kruthers.piggles.queuecore.listeners.DeathListener
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import java.util.function.Function


class QueueCore: JavaPlugin() {

    companion object {
        val queue: QueueManager = QueueManager()
    }

    @Override
    override fun onEnable() {
        this.logger.info("Loading config")
        //process config
        this.config.options().copyDefaults(true)
        this.saveConfig()
        this.updateConfig()

        this.logger.info("Loading listeners")
        Bukkit.getPluginManager().registerEvents(ConnectionEvents(this), this)
        Bukkit.getPluginManager().registerEvents(DeathListener(this), this)

        this.logger.info("Loading commands")
        val cmdManager: PaperCommandManager<CommandSender> = PaperCommandManager(
            this,
            CommandExecutionCoordinator.simpleCoordinator(),
            Function.identity(),
            Function.identity()
        )
        try {
            cmdManager.registerBrigadier()
        } catch (e: Exception) {
            this.logger.warning("Failed to initialize Brigadier support: " + e.message)
        }
        MinecraftExceptionHandler<CommandSender>()
            .withArgumentParsingHandler()
            .withInvalidSenderHandler()
            .withInvalidSyntaxHandler()
            .withNoPermissionHandler()
            .withCommandExecutionHandler()
            .withDecorator { component ->
                Component.text()
                    .append(component)
                    .build()
            }
            .apply(cmdManager, AudienceProvider.nativeAudience())
        val registry = cmdManager.captionRegistry()
        if (registry is FactoryDelegatingCaptionRegistry<*>) {
            registry.registerMessageFactory(
                QueueExceptionCaptions.PLAYER_NOT_IN_QUEUE
            ) { _, _ -> "Player '{name}' is not in currently in the queue" }
            registry.registerMessageFactory(
                QueueExceptionCaptions.PLAYER_NOT_PARTICIPATING
            ) { _, _ -> "Player '{name}' is not in currently participating in the event" }
            registry.registerMessageFactory(
                QueueExceptionCaptions.INVALID_TYPE
            ) { _, _ -> "Type '{type}' is not a valid type of {types}" }
        }
        val coreCommand = cmdManager.commandBuilder(
            "queue",
            ArgumentDescription.of("The main command to manage the player queue"),
            "q"
        )

        ControlCommands(cmdManager, coreCommand)
        ListCommands(cmdManager, coreCommand)
        AdminCommands(cmdManager, coreCommand)

        //start runnable
        Bukkit.getScheduler().runTaskTimer(this, MainRunable(this), 100, 10)
    }

    fun updateConfig() {
        //update the queue
        queue.startWeight = this.config.getInt("queue.starting_weight")
        queue.weightIncrement = this.config.getInt("queue.weight_increment")
        queue.mode = try {
            QueueType.valueOf(this.config.getString("queue.type")?: "weighted")
        } catch (_: IllegalArgumentException) {
            QueueType.WEIGHTED
        }
        queue.invHandling = try {
            InventoryHandling.valueOf(this.config.getString("handling.inventory_handling")?: "pass_on")
        } catch (_: IllegalArgumentException) {
            InventoryHandling.PASS_ON
        }

        //Load messages
        ConfigMessages.reload(this.config)
    }

}