package com.kruthers.piggles.queuecore.commands

import cloud.commandframework.ArgumentDescription
import cloud.commandframework.Command
import cloud.commandframework.CommandManager
import cloud.commandframework.context.CommandContext
import com.kruthers.piggles.queuecore.ConfigMessages
import com.kruthers.piggles.queuecore.QueueCore
import com.kruthers.piggles.queuecore.classes.QueueData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class ListCommands(manager: CommandManager<CommandSender>, coreCmd: Command.Builder<CommandSender>) {
    init {
        manager.command(coreCmd
            .literal("list")
            .literal("queue", ArgumentDescription.of("Lists everyone in the queue currently"))
            .permission("queue.list.queued")
            .handler(this::listQueueCommand)
        )
        manager.command(coreCmd
            .literal("list")
            .literal("participants", ArgumentDescription.of("Lists everyone currently participating"))
            .permission("queue.list.participants")
            .handler(this::listParticipantsCommand)
        )
    }

    private fun listQueueCommand(ctx: CommandContext<CommandSender>) {
        val queue = QueueCore.queue
        var list: Component = Component.empty()

        queue.getQueue().forEach { player ->
            val data = queue.getQueueData(player)
            if (data != null) {
                val tags = this.getQueueDataTags(data)
                list = list.append(Component.newline()).append(ConfigMessages.COMMANDS.LIST.QUEUE.ROW.getComponent(tags))
            }
        }

        ctx.sender.sendMessage(
            ConfigMessages.COMMANDS.LIST.QUEUE.HEADER.getComponent(
                TagResolver.resolver(
            Placeholder.component("list", list)
        )))
    }

    private fun listParticipantsCommand(ctx: CommandContext<CommandSender>) {
        val queue = QueueCore.queue
        var list = Component.empty()

        queue.getParticipants().forEachIndexed { i, player ->
            val tags = TagResolver.resolver(
                ConfigMessages.getPlayerTags(player),
                Placeholder.parsed("pos", "$i"),
                Placeholder.parsed("position", "$i")
            )
            list = list.append(Component.newline()).append(ConfigMessages.COMMANDS.LIST.PARTICIPANTS.ROW.getComponent(tags))
        }

        ctx.sender.sendMessage(
            ConfigMessages.COMMANDS.LIST.PARTICIPANTS.HEADER.getComponent(
                TagResolver.resolver(
            Placeholder.component("list", list)
        )))
    }


    private fun getQueueDataTags(data: QueueData): TagResolver {
        var tags = ConfigMessages.getPlayerTags(data.player)
        return TagResolver.resolver(
            tags,
            Placeholder.parsed("weight", "${data.weight}"),
            Placeholder.parsed("pos", "${data.position}"),
            Placeholder.parsed("position", "${data.position}"),
        )
    }
}