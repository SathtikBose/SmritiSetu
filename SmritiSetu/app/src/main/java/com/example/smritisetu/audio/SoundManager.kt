package com.example.smritisetu.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.example.smritisetu.R

object SoundManager {
    private const val TAG = "SoundManager"

    private var soundPool: SoundPool? = null
    private var flipSoundId: Int = 0
    private var correctSoundId: Int = 0
    private var wrongSoundId: Int = 0
    private var isInitialized: Boolean = false

    @Synchronized
    fun init(context: Context) {
        if (isInitialized) return

        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(audioAttributes)
                .build()

            soundPool?.let { pool ->
                flipSoundId = pool.load(context.applicationContext, R.raw.flipcard_sound, 1)
                correctSoundId = pool.load(context.applicationContext, R.raw.correct_sound, 1)
                wrongSoundId = pool.load(context.applicationContext, R.raw.wrong_sound, 1)
            }
            isInitialized = true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to initialize SoundPool: ${e.message}")
        }
    }

    fun playFlip(context: Context) {
        ensureInit(context)
        try {
            if (flipSoundId != 0) {
                soundPool?.play(flipSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error playing flip sound: ${e.message}")
        }
    }

    fun playCorrect(context: Context) {
        ensureInit(context)
        try {
            if (correctSoundId != 0) {
                soundPool?.play(correctSoundId, 1.0f, 1.0f, 2, 0, 1.0f)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error playing correct sound: ${e.message}")
        }
    }

    fun playWrong(context: Context) {
        ensureInit(context)
        try {
            if (wrongSoundId != 0) {
                soundPool?.play(wrongSoundId, 1.0f, 1.0f, 2, 0, 1.0f)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error playing wrong sound: ${e.message}")
        }
    }

    private fun ensureInit(context: Context) {
        if (!isInitialized || soundPool == null) {
            init(context)
        }
    }
}
