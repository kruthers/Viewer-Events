package com.kruthers.piggles.queuecore.commands.arguments

import cloud.commandframework.ArgumentDescription
import cloud.commandframework.arguments.CommandArgument
import cloud.commandframework.arguments.parser.ArgumentParseResult
import cloud.commandframework.arguments.parser.ArgumentParser
import cloud.commandframework.captions.CaptionVariable
import cloud.commandframework.context.CommandContext
import cloud.commandframework.exceptions.parsing.NoInputProvidedException
import cloud.commandframework.exceptions.parsing.ParserException
import com.kruthers.piggles.queuecore.QueueCore
import com.kruthers.piggles.queuecore.classes.QueueExceptionCaptions
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.*
import java.util.function.BiFunction

class ParticipatingPlayerArgument<C : Any>(
    required: Boolean,
    name: String,
    defaultValue: String,
    suggestionsProvider: BiFunction<CommandContext<C>, String, MutableList<String>>?,
    defaultDescription: ArgumentDescription
) : CommandArgument<C, OfflinePlayer>(
    required,
    name,
    ParticipatingPlayerParser(),
    defaultValue,
    OfflinePlayer::class.java,
    suggestionsProvider,
    defaultDescription
) {
    companion object {
        fun <C : Any> newBuilder(name: String): Builder<C> {
            return Builder(name)
        }

        fun <C : Any> of(name: String): CommandArgument<C, OfflinePlayer> {
            return newBuilder<C>(name).asRequired().build()
        }

        fun <C: Any> optional(name: String): CommandArgument<C, OfflinePlayer> {
            return newBuilder<C>(name).asOptional().build()
        }

        fun <C: Any> optional(name: String, defaultPlayer: String): CommandArgument<C, OfflinePlayer> {
            return newBuilder<C>(name).asOptionalWithDefault(defaultPlayer).build()
        }

        class Builder<C : Any> (name: String) : CommandArgument.Builder<C, OfflinePlayer>(OfflinePlayer::class.java, name) {
            override fun build(): CommandArgument<C, OfflinePlayer> {
                return ParticipatingPlayerArgument(this.isRequired, this.name, this.defaultValue,
                    this.suggestionsProvider, this.defaultDescription
                )
            }
        }

        class ParticipatingPlayerParser<C: Any>: ArgumentParser<C, OfflinePlayer> {
            override fun parse(
                commandContext: CommandContext<C>,
                inputQueue: Queue<String>
            ): ArgumentParseResult<OfflinePlayer> {
                val input = inputQueue.peek()
                    ?: return ArgumentParseResult.failure(NoInputProvidedException(this::class.java, commandContext))

                val player = Bukkit.getOfflinePlayer(input)

                return if (QueueCore.queue.isParticipating(player)) {
                    ArgumentParseResult.success(player)
                } else {
                    ArgumentParseResult.failure(NotParticipatingException(input, commandContext))
                }
            }

            override fun suggestions(commandContext: CommandContext<C>, input: String): MutableList<String> {
                return QueueCore.queue.getQueue().filter { it.name.equals(input, true) }.map{ it.name }.toMutableList()
            }
        }

        class NotParticipatingException(name: String, context: CommandContext<*>) : ParserException(
            ParticipatingPlayerParser::class.java,
            context,
            QueueExceptionCaptions.PLAYER_NOT_PARTICIPATING,
            CaptionVariable.of("name", name)
        )
    }
}