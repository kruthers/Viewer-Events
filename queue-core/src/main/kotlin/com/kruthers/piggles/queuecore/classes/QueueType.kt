package com.kruthers.piggles.queuecore.classes

/**
 * The type of sorting to use when selecting players from the queue
 */
enum class QueueType {
    /**
     * Completely random, ignores weights and when they joined the queue
     */
    RANDOM,
    /**
     * Logical, based of when you were added, first come, first served
     */
    LOGICAL,
    /**
     * Similar to weighted but uses a weighted system to give thoughts who have weighted longest the best shot
     */
    WEIGHTED
}