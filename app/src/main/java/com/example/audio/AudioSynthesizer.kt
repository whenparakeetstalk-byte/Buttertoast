package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class AudioSynthesizer {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val sampleRate = 22050
    var isEnabled: Boolean = true

    private fun playTone(samples: ShortArray) {
        if (!isEnabled) return
        scope.launch {
            try {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(samples, 0, samples.size)
                track.play()
                // Auto release after sound plays
                scope.launch {
                    kotlinx.coroutines.delay((samples.size * 1000L / sampleRate) + 50)
                    try {
                        track.stop()
                        track.release()
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {}
        }
    }

    fun playSpreadSound(intensity: Float = 0.5f) {
        if (!isEnabled) return
        val durationMs = 60
        val numSamples = (sampleRate * durationMs / 1000)
        val samples = ShortArray(numSamples)
        var lowPass = 0f
        val alpha = 0.15f

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val envelope = sin(progress * PI.toFloat()).coerceAtLeast(0f)
            val whiteNoise = (Random.nextFloat() * 2f - 1f)
            lowPass += alpha * (whiteNoise - lowPass)
            val wave = (lowPass * envelope * 0.45f * intensity.coerceIn(0.2f, 1f))
            samples[i] = (wave * Short.MAX_VALUE).toInt().toShort()
        }
        playTone(samples)
    }

    fun playScrapeSound() {
        if (!isEnabled) return
        val durationMs = 80
        val numSamples = (sampleRate * durationMs / 1000)
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val envelope = exp(-progress * 4f)
            val noise = (Random.nextFloat() * 2f - 1f)
            val tone = sin(2 * PI * 850.0 * i / sampleRate).toFloat() * 0.2f
            val wave = ((noise * 0.4f + tone) * envelope * 0.35f)
            samples[i] = (wave * Short.MAX_VALUE).toInt().toShort()
        }
        playTone(samples)
    }

    fun playSliceSound() {
        if (!isEnabled) return
        val durationMs = 70
        val numSamples = (sampleRate * durationMs / 1000)
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val envelope = (1f - progress) * (1f - progress)
            val freq = 480f - progress * 200f
            val wave = sin(2 * PI * freq * i / sampleRate).toFloat() * envelope * 0.4f
            samples[i] = (wave * Short.MAX_VALUE).toInt().toShort()
        }
        playTone(samples)
    }

    fun playTearSound() {
        if (!isEnabled) return
        val durationMs = 120
        val numSamples = (sampleRate * durationMs / 1000)
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val envelope = exp(-progress * 6f)
            val noise = if (i % 3 == 0) (Random.nextFloat() * 2f - 1f) else 0f
            val pop = sin(2 * PI * 180.0 * i / sampleRate).toFloat() * 0.5f
            val wave = ((noise * 0.6f + pop) * envelope * 0.5f)
            samples[i] = (wave * Short.MAX_VALUE).toInt().toShort()
        }
        playTone(samples)
    }

    fun playStarSound(starIndex: Int) {
        if (!isEnabled) return
        val durationMs = 150
        val numSamples = (sampleRate * durationMs / 1000)
        val samples = ShortArray(numSamples)
        val baseFreq = when (starIndex) {
            1 -> 523.25f // C5
            2 -> 659.25f // E5
            else -> 783.99f // G5
        }

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val envelope = exp(-progress * 3.5f)
            val wave = sin(2 * PI * baseFreq * i / sampleRate).toFloat() * envelope * 0.5f
            samples[i] = (wave * Short.MAX_VALUE).toInt().toShort()
        }
        playTone(samples)
    }
}
