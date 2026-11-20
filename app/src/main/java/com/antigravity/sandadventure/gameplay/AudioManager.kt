package com.antigravity.sandadventure.gameplay

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.antigravity.sandadventure.data.ProgressManager

class AudioManager(
    private val context: Context,
    private val progressManager: ProgressManager
) {
    private var mediaPlayer: MediaPlayer? = null
    private var currentBgmTrack: String? = null

    private val soundPool: SoundPool
    private val soundMap = HashMap<String, Int>()

    private var digStreamId: Int = 0

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()

        loadSounds()
    }

    private fun loadSounds() {
        val sfxList = listOf(
            "sfx_victory_fanfare.wav",
            "sfx_dig_sand.wav",
            "sfx_ball_pop.wav",
            "sfx_ball_clack.wav",
            "sfx_boing.wav",
            "sfx_truck_drop.wav",
            "sfx_truck_horn.wav",
            "sfx_star_twinkle.wav",
            "sfx_btn_tap.wav",
            "sfx_portal.wav",
            "sfx_multiplier.wav",
            "sfx_tnt_boom.wav",
            "sfx_cheer_kids.wav"
        )

        for (sfx in sfxList) {
            try {
                val afd = context.assets.openFd("audio/$sfx")
                val soundId = soundPool.load(afd, 1)
                soundMap[sfx] = soundId
            } catch (_: Throwable) {
            }
        }
    }

    fun playBgm(trackName: String) {
        if (!progressManager.musicEnabled) {
            stopBgm()
            return
        }
        if (currentBgmTrack == trackName && mediaPlayer?.isPlaying == true) {
            return
        }

        stopBgm()

        try {
            val afd: AssetFileDescriptor = context.assets.openFd("audio/$trackName")
            mediaPlayer = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                isLooping = true
                setVolume(0.65f, 0.65f)
                prepare()
                start()
            }
            currentBgmTrack = trackName
        } catch (_: Throwable) {
        }
    }

    fun stopBgm() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Throwable) {
        }
        mediaPlayer = null
        currentBgmTrack = null
    }

    fun pauseBgm() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (_: Throwable) {
        }
    }

    fun resumeBgm() {
        if (progressManager.musicEnabled) {
            try {
                mediaPlayer?.start()
            } catch (_: Throwable) {
            }
        }
    }

    private fun playSfx(name: String, volume: Float = 1.0f, rate: Float = 1.0f): Int {
        if (!progressManager.soundEnabled) return 0
        val soundId = soundMap[name] ?: return 0
        return soundPool.play(soundId, volume, volume, 1, 0, rate)
    }

    fun playVictory() {
        playSfx("sfx_victory_fanfare.wav", volume = 1.0f)
    }

    fun playBallPop() {
        playSfx("sfx_ball_pop.wav", volume = 0.8f, rate = 0.95f + (Math.random().toFloat() * 0.1f))
    }

    fun playBallClack() {
        playSfx("sfx_ball_clack.wav", volume = 0.4f, rate = 0.9f + (Math.random().toFloat() * 0.2f))
    }

    fun playBoing() {
        playSfx("sfx_boing.wav", volume = 0.85f)
    }

    fun playTruckDrop() {
        playSfx("sfx_truck_drop.wav", volume = 0.75f)
    }

    fun playTruckHorn() {
        playSfx("sfx_truck_horn.wav", volume = 0.9f)
    }

    fun playStarTwinkle() {
        playSfx("sfx_star_twinkle.wav", volume = 0.85f)
    }

    fun playButtonTap() {
        playSfx("sfx_btn_tap.wav", volume = 0.7f)
    }

    fun playPortal() {
        playSfx("sfx_portal.wav", volume = 0.85f)
    }

    fun playMultiplier() {
        playSfx("sfx_multiplier.wav", volume = 0.9f)
    }

    fun playTntBlast() {
        playSfx("sfx_tnt_boom.wav", volume = 0.95f)
    }

    fun playKidsCheer() {
        playSfx("sfx_cheer_kids.wav", volume = 0.85f)
    }

    fun playWorldBgm(worldId: Int) {
        val track = when (worldId) {
            1, 8 -> "bgm_gameplay_kids.wav"
            2, 5, 7, 10 -> "bgm_candy_world.wav"
            3, 6 -> "bgm_jungle_fun.wav"
            4, 9 -> "bgm_icecream_waltz.wav"
            else -> "bgm_gameplay_kids.wav"
        }
        playBgm(track)
    }

    fun startDigSound() {
        if (!progressManager.soundEnabled) return
        if (digStreamId == 0) {
            val soundId = soundMap["sfx_dig_sand.wav"] ?: return
            digStreamId = soundPool.play(soundId, 0.4f, 0.4f, 1, -1, 1.0f)
        }
    }

    fun stopDigSound() {
        if (digStreamId != 0) {
            soundPool.stop(digStreamId)
            digStreamId = 0
        }
    }

    fun release() {
        stopBgm()
        stopDigSound()
        soundPool.release()
    }
}
