package com.kruthers.piggles.queuecore.commands.arguments

import cloud.commandframework.ArgumentDescription
import cloud.commandframework.arguments.CommandArgument
import cloud.commandframework.arguments.parser.ArgumentParseResult
import cloud.commandframework.arguments.parser.ArgumentParser
import cloud.commandframework.bukkit.parsers.PlayerArgument.PlayerParseException
import cloud.commandframework.captions.CaptionVariable
import cloud.commandframework.context.CommandContext
import cloud.commandframework.exceptions.parsing.NoInputProvidedException
import cloud.commandframework.exceptions.parsing.ParserException
import com.kruthers.piggles.queuecore.QueueCore
import com.kruthers.piggles.queuecore.classes.QueueExceptionCaptions
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import java.util.function.BiFunction

class QueuedPlayerArgument<C : Any>(
    required: Boolean,
    name: String,
    defaultValue: String,
    suggestionsProvider: BiFunction<CommandContext<C>, String, MutableList<String>>?,
    defaultDescription: ArgumentDescription
) : CommandArgument<C, Player>(
    required,
    name,
    QueuedPlayerParser(),
    defaultValue,
    Player::class.java,
    suggestionsProvider,
    defaultDescription
) {
    companion object {
        fun <C : Any> newBuilder(name: String): Builder<C> {
            return Builder(name)
        }

        fun <C : Any> of(name: String): CommandArgument<C, Player> {
            return newBuilder<C>(name).asRequired().build()
        }

        fun <C: Any> optional(name: String): CommandArgument<C, Player> {
            return newBuilder<C>(name).asOptional().build()
        }

        fun <C: Any> optional(name: String, defaultPlayer: String): CommandArgument<C, Player> {
            return newBuilder<C>(name).asOptionalWithDefault(defaultPlayer).build()
        }

        class Builder<C : Any> (name: String) : CommandArgument.Builder<C, Player>(Player::class.java, name) {
            override fun build(): CommandArgument<C, Player> {
                return QueuedPlayerArgument(this.isRequired, this.name, this.defaultValue,
                    this.suggestionsProvider, this.defaultDescription
                )
            }
        }

        class QueuedPlayerParser<C: Any>: ArgumentParser<C, Player> {
            override fun parse(
                commandContext: CommandContext<C>,
                inputQueue: Queue<String>
            ): ArgumentParseResult<Player> {
                val input = inputQueue.peek()
                    ?: return ArgumentParseResult.failure(NoInputProvidedException(this::class.java, commandContext))

                val player = Bukkit.getPlayer(input)
                    ?: return ArgumentParseResult.failure(PlayerParseException(input, commandContext))

                QueueCore.queue.getQueueData(player)
                    ?: return ArgumentParseResult.failure(NotInQueueException(player, commandContext))

                return ArgumentParseResult.success(player)
            }

            override fun suggestions(commandContext: CommandContext<C>, input: String): MutableList<String> {
                return QueueCore.queue.getQueue().filter { it.name.contains(input, true) }.map{ it.name }.toMutableList()
            }
        }

        class NotInQueueException( val player: Player, context: CommandContext<*>) : ParserException(
            ParticipatingPlayerArgument::class.java,
            context,
            QueueExceptionCaptions.PLAYER_NOT_IN_QUEUE,
            CaptionVariable.of("name", player.name)
        )
    }
}

