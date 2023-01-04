package com.kruthers.piggles.queuecore.commands

import cloud.commandframework.ArgumentDescription
import cloud.commandframework.CommandManager
import cloud.commandframework.arguments.standard.BooleanArgument
import cloud.commandframework.arguments.standard.EnumArgument
import cloud.commandframework.arguments.standard.IntegerArgument
import cloud.commandframework.context.CommandContext
import com.kruthers.piggles.queuecore.ConfigMessages
import com.kruthers.piggles.queuecore.QueueCore
import com.kruthers.piggles.queuecore.classes.InventoryHandling
import com.kruthers.piggles.queuecore.classes.QueueType
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender

class ConfigCommands(private val plugin: QueueCore, manager: CommandManager<CommandSender>) {

    init {
        val command = manager.commandBuilder("queuecore", ArgumentDescription.of("Manage the settings for the queue"), "qc")

        manager.command(command
            .literal("setting")
            .literal("mode")
            .argument(EnumArgument.of(QueueType::class.java, "type"))
            .permission("queue.setting.mode")
            .handler(this::modeSettingQueue)
        )
        manager.command(command
            .literal("setting")
            .literal("weight")
            .literal("starting")
            .argument(IntegerArgument.of("weight"))
            .permission("queue.setting.weights")
            .handler(this::startingWeightSettingQueue)
        )
        manager.command(command
            .literal("setting")
            .literal("weight")
            .literal("increment")
            .argument(IntegerArgument.of("weight"))
            .permission("queue.setting.weights")
            .handler(this::incrementWeightSettingQueue)
        )
        manager.command(command
            .literal("setting")
            .literal("handling")
            .literal("auto_replace")
            .argument(BooleanArgument.of("state"))
            .permission("queue.setting.weights")
            .handler(this::autoReplaceSettingQueue)
        )
        manager.command(command
            .literal("setting")
            .literal("handling")
            .literal("inventory_handling")
            .argument(EnumArgument.of(InventoryHandling::class.java, "type"))
            .permission("queue.setting.weights")
            .handler(this::inventoryHandlingSettingQueue)
        )
        manager.command(command
            .literal("setting")
            .literal("reload")
            .permission("queue.setting.reload")
            .handler(this::reloadCommand)
        )
    }


    private fun modeSettingQueue(ctx: CommandContext<CommandSender>) {
        val type: QueueType = ctx.get("type")
        QueueCore.queue.mode = type
        plugin.config.set("queue.type", type.name)
        plugin.saveConfig()
        ctx.sender.sendMessage(ConfigMessages.COMMANDS.SETTINGS.MODE.getComponent(SingleTag("type", type.name)))
    }
    private fun startingWeightSettingQueue(ctx: CommandContext<CommandSender>) {
        val weight: Int = ctx.get("weight")
        QueueCore.queue.startWeight = weight
        plugin.config.set("queue.starting_weight", weight)
        plugin.saveConfig()
        ctx.sender.sendMessage(ConfigMessages.COMMANDS.SETTINGS.STARTING_WEIGHT.getComponent(SingleTag("input", "$weight")))
    }
    private fun incrementWeightSettingQueue(ctx: CommandContext<CommandSender>) {
        val weight: Int = ctx.get("weight")
        QueueCore.queue.startWeight = weight
        plugin.config.set("queue.weight_increment", weight)
        plugin.saveConfig()
        ctx.sender.sendMessage(ConfigMessages.COMMANDS.SETTINGS.INCREMENT_WEIGHT.getComponent(SingleTag("input", "$weight")))
    }
    private fun autoReplaceSettingQueue(ctx: CommandContext<CommandSender>) {
        val state: Boolean = ctx.get("type")
        plugin.config.set("handling.auto_replace", state)
        plugin.saveConfig()
        ctx.sender.sendMessage(ConfigMessages.COMMANDS.SETTINGS.AUTO_REPLACE.getComponent(SingleTag("input", "$state")))
    }
    private fun inventoryHandlingSettingQueue(ctx: CommandContext<CommandSender>) {
        val type: InventoryHandling = ctx.get("type")
        QueueCore.queue.invHandling = type
        plugin.config.set("handling.inventory_handling", type.name)
        plugin.saveConfig()
        ctx.sender.sendMessage(ConfigMessages.COMMANDS.SETTINGS.INVENTORY_HANDLING.getComponent(SingleTag("type", type.name)))
    }

    private fun reloadCommand(ctx: CommandContext<CommandSender>) {
        plugin.updateConfig()
        ctx.sender.sendMessage(ConfigMessages.COMMANDS.SETTINGS.RELOAD.getComponent())
    }

    fun SingleTag(name: String, value: String): TagResolver {
        return TagResolver.resolver(
            Placeholder.parsed(name, value)
        )
    }

}