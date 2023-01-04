package com.kruthers.piggles.queuecore.commands

import cloud.commandframework.Command
import cloud.commandframework.CommandManager
import cloud.commandframework.context.CommandContext
import cloud.commandframework.kotlin.extension.argumentDescription
import com.kruthers.piggles.queuecore.ConfigMessages
import com.kruthers.piggles.queuecore.QueueCore
import com.kruthers.piggles.queuecore.commands.arguments.ParticipatingPlayerArgument
import com.kruthers.piggles.queuecore.commands.arguments.QueuedPlayerArgument
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class AdminCommands(manager: CommandManager<CommandSender>, coreCmd: Command.Builder<CommandSender>) {

    init {
        manager.command(coreCmd
            .literal("swap", argumentDescription("Swaps a player from the queue with a participant"))
            .argument(QueuedPlayerArgument.of("queued_player"))
            .argument(ParticipatingPlayerArgument.of("participant"))
            .permission("queue.swap")
            .handler(this::swapCommand)
        )
        manager.command(coreCmd
            .literal("remove_all", argumentDescription("Clears all participants"))
            .permission("queue.remove_all")
            .handler(this::removeAllCommand)
        )
        manager.command(coreCmd
            .literal("select_all", argumentDescription("Makes everyone in the queue a participant"))
            .permission("queue.select_all")
            .handler(this::selectAllCommand)
        )
    }

    private fun swapCommand(ctx: CommandContext<CommandSender>) {
        val new: Player = ctx.get("queued_player")
        val old: OfflinePlayer = ctx.get("participant")

        if (old.isOnline) {
            if (QueueCore.queue.select(new)) {
                QueueCore.queue.swap(old.player!!, new)
                ctx.sender.sendMessage(ConfigMessages.COMMANDS.SWAP.SWAPPED.getComponent(
                    TagResolver.resolver(
                    Placeholder.parsed("new", new.name),
                    Placeholder.parsed("old", old.player!!.name)
                )))
            } else {
                ctx.sender.sendMessage(ConfigMessages.COMMANDS.SWAP.NO_NEW.getComponent())
            }
        } else {
            ctx.sender.sendMessage(ConfigMessages.COMMANDS.SWAP.NO_OLD.getComponent())
        }
    }

    private fun removeAllCommand(ctx: CommandContext<CommandSender>) {
        QueueCore.queue.getParticipants().forEach {
            QueueCore.queue.removeParticipating(it)
        }

        ctx.sender.sendMessage(ConfigMessages.COMMANDS.REMOVE_ALL.getComponent())
    }

    private fun selectAllCommand(ctx: CommandContext<CommandSender>) {
        QueueCore.queue.getQueue().forEach {
            QueueCore.queue.select(it)
        }

        ctx.sender.sendMessage(ConfigMessages.COMMANDS.SELECT_ALL.getComponent())
    }

}