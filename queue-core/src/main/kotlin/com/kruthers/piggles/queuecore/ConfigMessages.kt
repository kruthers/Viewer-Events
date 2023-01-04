package com.kruthers.piggles.queuecore

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.configuration.file.FileConfiguration

class ConfigMessages {
    companion object {
        private val messages: HashMap<String, String?> = hashMapOf()

        val COMMANDS = CommandMessages
        object CommandMessages {
            val SELECT: PassFailMessage = PassFailMessage(
                "commands.select",
                "<red>Unable to find a player to select from the queue",
                "<green>Selected a player from the queue"
            )
            val REMOVE: PassFailMessage = PassFailMessage(
                "commands.remove",
                "<red>No online participant found. </red><grey><i>If they have left the game they will be swapped/ removed automatically",
                "<grey><player> Has been removed from the Event."
            )
            val CYCLE = SwapCommand("cycle")
            val LIST = ListCommands
            object ListCommands {
                val QUEUE = ListQueueCommand
                object ListQueueCommand {
                    val HEADER = ConfigMessage("commands.list.queue.header", "<hover:show_text:'Format:<br><format>'><Gold>Currently queued players:</hover><gray><list>")
                    val ROW = ConfigMessage("commands.list.queue.header", "<aqua>[</aqua><light_purple><position></light_purple><aqua>]</aqua> <player> <b>-</b><aqua> <weight></aqua>")
                }
                val PARTICIPANTS = ListParticipantsCommand
                object ListParticipantsCommand {
                    val HEADER = ConfigMessage("commands.list.participants.header", "<hover:show_text:'Format:<br><format>'><Gold>Current participants:</hover><gray><list>")
                    val ROW = ConfigMessage("commands.list.participants.header", "<aqua>[</aqua><light_purple><position></light_purple><aqua>]</aqua> <player>")
                }
            }
            val SWAP = SwapCommand("swap")
            val REMOVE_ALL = ConfigMessage("commands.remove_all", "<b><red>All participants have been returned to the queue")
            val SELECT_ALL = ConfigMessage("commands.select_all", "<b><green>Everyone in the queue has now been added as a participant")
            object SettingsCommands {
                val MODE = ConfigMessage("commands.settings.mode", "<green>New selection method selected: <type>")
                val STARTING_WEIGHT = ConfigMessage("commands.settings.starting_weight", "<green>New queue starting weight selected: <input>")
                val INCREMENT_WEIGHT = ConfigMessage("commands.settings.increment_weight", "<green>New queue increment weight selected: <input>")
                val AUTO_REPLACE = ConfigMessage("commands.settings.auto_replace", "<green>Queue will auto replace on death/ logout: <input>")
                val INVENTORY_HANDLING = ConfigMessage("commands.settings.inventory_handling", "<green>New inventory handling method selected: <type>")
                val RELOAD = ConfigMessage("commands.settings.reload", "<gold>Queue plugin config reload")
            }
            val SETTINGS = SettingsCommands
        }

        val QUEUE_MESSAGES = QueueMessages
        object QueueMessages {
            val select_private: ConfigMessage = ConfigMessage("queue.select.private","<green>You have been selected to take part in Piggles' Event!")
            val select_public: ConfigMessage = ConfigMessage("queue.select.public","<b><gold>Queue</b><green> A new player has been selected from the queue: <player>")
            val join_queue: ConfigMessage = ConfigMessage("queue.join","<light_purple>You have been added to the queue")
            val participantRemovePrivate: ConfigMessage = ConfigMessage("queue.remove.private","<red>You are no longer a participant in the event")
            val participantRemovePublic: ConfigMessage = ConfigMessage("queue.remove.public","<b><gold>Queue</b><red> <player> is no longer a participant")
        }


        class ConfigMessage(val key: String, val default: String) {
            init {
                messages[key] = default
            }

            /**
             * Converts the raw message into a formatted component
             * @param tags The optional tags to apply to the mini message
             * @return The text component to use
             */
            fun getComponent(tags: TagResolver = TagResolver.empty()): Component {
                return MiniMessage.miniMessage().deserialize(this.getRaw(), tags)
            }

            /**
             * Gets the raw string message with no formatting#
             * @return The raw string message
             */
            fun getRaw(): String {
                return messages[key] ?: default
            }

            override fun toString(): String {
                return this.getRaw()
            }

        }

        //commons
        class PassFailMessage(key: String, failMessage: String, passMessage: String) {
            val FAILED = ConfigMessage("${key}.failed", failMessage)
            val PASSED = ConfigMessage("${key}.passed", passMessage)
        }
        data class SwapCommand(
            private val command: String,
            val NO_OLD: ConfigMessage = ConfigMessage(
                "command.$command.no_old",
                "<red>No online participant found. </red><grey><i>If they have left the game they will be swapped/ removed automatically"
            ),
            val NO_NEW: ConfigMessage = ConfigMessage(
                "command.$command.no_new",
                "<red>Unable to find a new player in the queue to swap for <player>"
            ),
            val SWAPPED: ConfigMessage = ConfigMessage(
                "command.$command.swapped",
                "<green>Swapped <old> for <new>"
            )
        )

        //functions

        /**
         * Gets the resolver tags for a player
         * @param player The player for whom to get the tags
         * @return The tags resolver
         */
        fun getPlayerTags(player: OfflinePlayer): TagResolver {
            var tags = TagResolver.resolver(
                Placeholder.parsed("player", player.name?: "Unknown"),
                Placeholder.parsed("name", player.name?: "Unknown"),
                Placeholder.parsed("uuid", player.uniqueId.toString()),
            )
            player.player?.let { tags = TagResolver.resolver(
                tags,
                Placeholder.component("display_name", it.displayName()),
            ) }
            return tags
        }

        /**
         * Reloads all the messages from a given config file
         * @param config The config to use to reload them
         */
        fun reload(config: FileConfiguration) {
            messages.forEach{ (key) ->
                messages[key] = config.getString("message.${key}")
            }
        }

    }
}