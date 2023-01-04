package com.kruthers.piggles.queuecore.classes

import com.kruthers.piggles.queuecore.ConfigMessages
import com.kruthers.piggles.queuecore.QueueCore
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import kotlin.math.round

class MainRunable(val plugin: QueueCore): Runnable {
    val mm = MiniMessage.miniMessage()

    /**
     * When an object implementing interface `Runnable` is used
     * to create a thread, starting the thread causes the object's
     * `run` method to be called in that separately executing
     * thread.
     *
     *
     * The general contract of the method `run` is that it may
     * take any action whatsoever.
     *
     * @see java.lang.Thread.run
     */
    override fun run() {
        if (plugin.config.getBoolean("action_bar.enabled")) {
            val queue = QueueCore.queue
            val message = when(QueueCore.queue.mode) {
                QueueType.WEIGHTED -> plugin.config.getString("action_bar.weighted")?: "<light_purple>In Queue <gray>You currently have <percent>% at being picked"
                QueueType.LOGICAL -> plugin.config.getString("action_bar.logical")?: "<light_purple>In Queue <gray>You currently are <position>/<queue_size> in the queue"
                QueueType.RANDOM -> plugin.config.getString("action_bar.random")?: "<light_purple>In Queue <gray>You currently have <percent>% at being picked. There are <queue_size> other people in the queue"
            }

            val maxWeight = QueueCore.queue.getTotalWeight()
            val queueSize = queue.getQueue().size

            queue.getQueue().forEach { player ->
                QueueCore.queue.getQueueData(player)?.let { data ->
                    val percent = round((data.weight / maxWeight * 10000F)) / 100F
                    player.sendActionBar(mm.deserialize(message, getTags(data, queueSize, percent)))
                }
            }
        }
    }

    private fun getTags(data: QueueData, queueSize: Int, percent: Float): TagResolver {
        return TagResolver.resolver(
            ConfigMessages.getPlayerTags(data.player),
            Placeholder.parsed("weight", "${data.weight}"),
            Placeholder.parsed("pos", "${data.position}"),
            Placeholder.parsed("position", "${data.position}"),
            Placeholder.parsed("queue_size", "$queueSize"),
            Placeholder.parsed("percent", "$percent"),
        )
    }
}