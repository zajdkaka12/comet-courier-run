package com.cometcourier.run.domain.feedback

interface FeedbackPlayer {
    fun play(delivery: Boolean)
    fun release()
}
