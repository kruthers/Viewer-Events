package com.kruthers.piggles.queuecore.classes

import cloud.commandframework.captions.Caption

class QueueExceptionCaptions {
    companion object {
        val PLAYER_NOT_IN_QUEUE: Caption = Caption.of("argument.parse.failure.queue.not_in_queued")
        val PLAYER_NOT_PARTICIPATING: Caption = Caption.of("argument.parse.failure.queue.not_participating")
        val INVALID_TYPE: Caption = Caption.of("argument.parse.failure.queue.setting.invalid_type")
    }

}