package com.kruthers.piggles.queuecore.commands

import cloud.commandframework.ArgumentDescription
import cloud.commandframework.Command
import cloud.commandframework.CommandManager
import cloud.commandframework.context.CommandContext
import com.kruthers.piggles.queuecore.ConfigMessages
import com.kruthers.piggles.queuecore.QueueCore
import com.kruthers.piggles.queuecore.commands.arguments.ParticipatingPlayerArgument
import com.kruthers.piggles.queuecore.commands.arguments.QueuedPlayerArgument
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class ControlCommands(manager: CommandManager<CommandSender>, coreCmd: Command.Builder<CommandSender>) {

    init {
        manager.command(coreCmd
            .permission("queue.control")
            .literal("select", ArgumentDescription.of("Select a player from the queue, can be forced"))
            .argument(QueuedPlayerArgument.optional("player"))
            .handler(this::selectCommand)
        )
        manager.command(coreCmd
            .permission("queue.control")
            .literal("cycle", ArgumentDescription.of("Replaces the longest running participant with someone from the queue"))
            .argument(QueuedPlayerArgument.optional("player"))
            .handler(this::cycleCommand)
        )
        manager.command(coreCmd
            .permission("queue.control")
            .literal("remove", ArgumentDescription.of("Removes a participating player, or the longest running one"))
            .argument(ParticipatingPlayerArgument.optional("player"))
            .handler(this::removeCommand)
        )
    }

    private fun selectCommand(ctx: CommandContext<CommandSender>) {
        val forcedPlayer: Optional<Player> = ctx.getOptional("player")
        val player = if (forcedPlayer.isPresent) {
            if (QueueCore.queue.select(forcedPlayer.get())) {
                forcedPlayer.get()
            } else {
                null
            }
        } else {
            QueueCore.queue.select()
        }

        if (player == null) {
            ctx.sender.sendMessage(ConfigMessages.COMMANDS.SELECT.FAILED.getComponent())
        } else {
            ctx.sender.sendMessage(ConfigMessages.COMMANDS.SELECT.PASSED.getComponent(ConfigMessages.getPlayerTags(player)))
        }
    }

    private fun cycleCommand(ctx: CommandContext<CommandSender>) {
        val forcedPlayer: Optional<Player> = ctx.getOptional("player")
        val queue = QueueCore.queue

        //gets the old player
        val oldPlayer = if (forcedPlayer.isPresent) {
            forcedPlayer.get()
        } else {
            val participants = queue.getParticipants().filter { it.isOnline }.map { it.player!! }
            if (participants.isEmpty()) {
                null
            } else {
                participants[0]
            }
        }

        //If no old player is found, don't do anything
        if (oldPlayer == null) {
            ctx.sender.sendMessage(ConfigMessages.COMMANDS.CYCLE.NO_OLD.getComponent())
            return
        }

        //gets a new player
        val newPlayer = queue.select()

        if (newPlayer != null) {
            queue.swap(oldPlayer, newPlayer)
            ctx.sender.sendMessage(ConfigMessages.COMMANDS.CYCLE.SWAPPED.getComponent(TagResolver.resolver(
                Placeholder.parsed("new", newPlayer.name),
                Placeholder.parsed("old",oldPlayer.name)
            )))
        } else {
            ctx.sender.sendMessage(ConfigMessages.COMMANDS.CYCLE.NO_NEW.getComponent())
        }
    }

    private fun removeCommand(ctx: CommandContext<CommandSender>) {
        val forcedPlayer: Optional<OfflinePlayer> = ctx.getOptional("player")
        val queue = QueueCore.queue

        //gets the old player
        val player = if (forcedPlayer.isPresent) {
            forcedPlayer.get()
        } else {
            val participants = queue.getParticipants()
            if (participants.isEmpty()) {
                null
            } else {
                participants[0]
            }
        }

        //If no old player is found, don't do anything
        if (player == null) {
            ctx.sender.sendMessage(ConfigMessages.COMMANDS.REMOVE.FAILED.getComponent())
        } else {
            queue.removeParticipating(player)
            player.player?.let { queue.add(it) }
            ctx.sender.sendMessage(ConfigMessages.COMMANDS.REMOVE.PASSED.getComponent(ConfigMessages.getPlayerTags(player)))
        }

    }

}