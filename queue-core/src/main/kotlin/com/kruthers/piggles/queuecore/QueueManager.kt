package com.kruthers.piggles.queuecore

import com.kruthers.piggles.queuecore.classes.InventoryHandling
import com.kruthers.piggles.queuecore.classes.QueueData
import com.kruthers.piggles.queuecore.classes.QueueType
import com.kruthers.piggles.queuecore.events.ParticipantRemoveEvent
import com.kruthers.piggles.queuecore.events.QueueJoinEvent
import com.kruthers.piggles.queuecore.events.SelectPlayerEvent
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import kotlin.math.round

/**
 * Creates a queue
 * @param startWeight The amount of default weight to give a person when they first join a queue
 * @param mode The mode to use when selecting a player from the queue
 * @param weightIncrement How much to increase everyone in the queues weight by when they are not selected
 * @param invHandling How to handle inventory's when a player is selected
 */
class QueueManager(
    var startWeight: Int = 5, var mode: QueueType = QueueType.WEIGHTED, var weightIncrement: Int = 1,
    var invHandling: InventoryHandling = InventoryHandling.PASS_ON
) {
    private val queueWeight: LinkedHashMap<OfflinePlayer, Int> = LinkedHashMap()
    private var queuePos: ArrayList<OfflinePlayer> = arrayListOf()
    private val inQueue: HashSet<Player> = hashSetOf()
    private var participants: ArrayList<OfflinePlayer> = arrayListOf()

    private val messages = ConfigMessages.QUEUE_MESSAGES

    fun getParticipants(): ArrayList<OfflinePlayer> {
        return this.participants
    }

    /**
     * Get the queueing data for a player
     * @param player The player to check for
     * @return The players queue data if they are in the queue, else null
     */
    fun getQueueData(player: Player): QueueData? {
        if (this.inQueue(player)) {
            return QueueData(
                player,
                this.queuePos.indexOf(player),
                this.queueWeight[player] ?: -1
            )
        }
        return null
    }

    /**
     * Checks if a player is in the queue
     * @param player The player for whom to check
     * @return True if they are in the queue, false if not
     */
    fun inQueue(player: Player): Boolean {
        return this.inQueue.contains(player) && this.queueWeight.containsKey(player) && this.queuePos.contains(player)
    }

    /**
     * Gets the combined weight of the whole queue
     * @return The combined weight
     */
    fun getTotalWeight(): Int {
        var max = 0
        this.queueWeight.filter { it.key.isOnline }.forEach { max += it.value }
        return max
    }

    /**
     * Select a new player from the queue
     * @return A player of null if no player was found
     */
    fun select(): Player? {
        if (this.inQueue.isEmpty()) return null

        var player: Player? = when(this.mode) {
            QueueType.RANDOM -> this.inQueue.random()
            QueueType.LOGICAL -> {
                for (player in this.queuePos) {
                    if (player.isOnline && player.player != null) {
                        player.player
                    }
                }

                null
            }
            QueueType.WEIGHTED -> {
                val players = this.queueWeight.filter { it.key.isOnline }
                val max = getTotalWeight()
                val response = if (max < 1) {
                    null
                } else {
                    val selected = round(Math.random() * max)
                    var player: Player? = null
                    var count = 0
                    players.forEach { (offlinePlayer,weight) ->
                        val countNext = count + weight
                        if (selected > count && selected <= countNext) {
                            player = offlinePlayer.player
                        }
                        count = countNext
                    }
                    player
                }

                response
            }
        }

        if ( player != null ) {
            if ( !this.select(player!!) ) {
                player = null
            }
        }

        return player
    }

    /**
     * Selects a player
     * @return True if the event does not cancel it
     */
    fun select(player: Player): Boolean {
        val event = SelectPlayerEvent(
            player,
            this.messages.select_private.getComponent(ConfigMessages.getPlayerTags(player)),
            this.messages.select_public.getComponent(ConfigMessages.getPlayerTags(player))
        )
        Bukkit.getServer().pluginManager.callEvent(event)

        //execute event
        if (event.isCancelled) {
            return false
        } else {
            this.remove(player)
            event.private_msg?.let { player.sendMessage(it) }
            event.public_msg?.let { Bukkit.broadcast(it) }

            //handle inventory
            if (this.invHandling == InventoryHandling.CLEAR) {
                player.inventory.clear()
            }
        }

        //increase queue weight
        this.queueWeight.forEach { (player, weight) ->
            this.queueWeight[player] = weight + this.weightIncrement
        }

        this.participants.plus(player)

        return true
    }


    /**
     * Adds a player into the queue
     * @param player The player to add
     */
    fun add(player: Player) {
        val event = QueueJoinEvent(
            player,
            this.startWeight,
            this.messages.join_queue.getComponent(ConfigMessages.getPlayerTags(player))
        )
        Bukkit.getServer().pluginManager.callEvent(event)
        if (!event.isCancelled) {
            //removes them as a participant
            if (this.participants.contains(player)) this.removeParticipating(player)

            //set the queue weight, if they are in it then set it to 0, else set it to the start weight
            if (this.queueWeight.containsKey(player)) {
                this.queueWeight[player] = 0
            } else {
                this.queueWeight[player] = event.startWeight
            }

            //Add them to the end of the queue
            if (this.queuePos.contains(player)) {
                this.queuePos = ArrayList(this.queuePos.filter { player.uniqueId != it.uniqueId })
            }
            this.queuePos.add(player)

            //actually add them to the queue
            this.inQueue.add(player)

            //send message
            event.message?.let { player.sendMessage(it) }
        }
    }

    /**
     * Removes a player from the queue
     * @param player The player to remove
     */
    fun remove(player: OfflinePlayer) {
        this.inQueue.remove(player)
        this.queueWeight[player] = -1
        this.queuePos = ArrayList(this.queuePos.filter { player.uniqueId != it.uniqueId })

    }

    /**
     * Gets everyone currently in the queue
     * @return The current queue of players
     */
    fun getQueue(): Set<Player> {
        return this.inQueue
    }

    /**
     * Checks if a player is a participant
     * @param player The player to check
     */
    fun isParticipating(player: OfflinePlayer): Boolean {
        return this.participants.contains(player)
    }

    /**
     * Remove a player from the participating queue
     * @param player The player to remove
     * @return If the event was cancelled or not
     */
    fun removeParticipating(player: OfflinePlayer): Boolean {
        val event = ParticipantRemoveEvent(
            player,
            this.messages.participantRemovePrivate.getComponent(ConfigMessages.getPlayerTags(player)),
            this.messages.participantRemovePublic.getComponent(ConfigMessages.getPlayerTags(player))
        )
        Bukkit.getServer().pluginManager.callEvent(event)
        if (!event.isCancelled) {
            //remove them form the participants list
            this.participants = ArrayList(this.participants.filter { it.uniqueId != player.uniqueId })

            player.player?.let { onlinePlayer ->
                //clear their inv if handling is set to clear
                if (this.invHandling == InventoryHandling.CLEAR) {
                    onlinePlayer.inventory.clear()
                }
                event.private_msg?.let { onlinePlayer.sendMessage(it) }
            }
            event.public_msg?.let { Bukkit.broadcast(it) }
        }
        return event.isCancelled
    }

    /**
     * Swaps 2 people from in to out of the queue
     * @param old The old player to remove as a participant
     * @param new The new player to replace them (Must already have been selected)
     */
    fun swap(old: Player, new: Player) {
        //adds the old player to the queue
        this.removeParticipating(old)
        this.add(old)

        //handles inventory
        if (this.invHandling == InventoryHandling.PASS_ON) {
            new.inventory.contents = old.inventory.contents
            old.inventory.clear()
        }
    }



}