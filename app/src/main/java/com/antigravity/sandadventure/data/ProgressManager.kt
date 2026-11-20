package com.antigravity.sandadventure.data

import android.content.Context
import android.content.SharedPreferences

class ProgressManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sand_adventure_progress", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_HIGHEST_UNLOCKED = "highest_unlocked_level"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_MUSIC_ENABLED = "music_enabled"
        private const val KEY_HAPTICS_ENABLED = "haptics_enabled"
        private const val PREFIX_STARS = "stars_level_"
    }

    var highestUnlockedLevel: Int
        get() = prefs.getInt(KEY_HIGHEST_UNLOCKED, 1)
        set(value) {
            if (value > highestUnlockedLevel) {
                prefs.edit().putInt(KEY_HIGHEST_UNLOCKED, value).apply()
            }
        }

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

    var musicEnabled: Boolean
        get() = prefs.getBoolean(KEY_MUSIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_MUSIC_ENABLED, value).apply()

    var hapticsEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTICS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, value).apply()

    fun getStarsForLevel(levelId: Int): Int {
        return prefs.getInt(PREFIX_STARS + levelId, 0)
    }

    fun setStarsForLevel(levelId: Int, stars: Int) {
        val current = getStarsForLevel(levelId)
        if (stars > current) {
            prefs.edit().putInt(PREFIX_STARS + levelId, stars.coerceIn(1, 3)).apply()
        }
        if (levelId >= highestUnlockedLevel) {
            highestUnlockedLevel = levelId + 1
        }
    }

    fun getTotalStars(): Int {
        var total = 0
        for (i in 1..100) {
            total += getStarsForLevel(i)
        }
        return total
    }

    fun resetProgress() {
        prefs.edit().clear().apply()
    }
}
