package com.cometcourier.run.data.feedback

import android.media.AudioManager
import android.media.ToneGenerator
import com.cometcourier.run.domain.feedback.FeedbackPlayer

class LocalFeedbackPlayer : FeedbackPlayer {
    private var tone: ToneGenerator? = null
    override fun play(delivery: Boolean) {
        runCatching {
            val generator = tone ?: ToneGenerator(AudioManager.STREAM_MUSIC, 35).also { tone = it }
            generator.startTone(if (delivery) ToneGenerator.TONE_PROP_ACK else ToneGenerator.TONE_PROP_BEEP, if (delivery) 120 else 55)
        }
    }
    override fun release() { tone?.release(); tone = null }
}
