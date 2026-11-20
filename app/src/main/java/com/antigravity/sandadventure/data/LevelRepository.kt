package com.antigravity.sandadventure.data

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

interface LevelRepository {
    fun getLevel(levelId: Int): SandAdventureLevel
    fun getAvailableLevelCount(): Int
}

class AssetLevelRepository(private val context: Context) : LevelRepository {

    override fun getLevel(levelId: Int): SandAdventureLevel {
        val fileName = "levels/level_$levelId.json"
        val jsonStr = try {
            context.assets.open(fileName).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            // Fallback default level if asset not found
            getDefaultFallbackLevel(levelId)
        }
        return SandAdventureLevel.fromJson(jsonStr)
    }

    override fun getAvailableLevelCount(): Int = 100

    private fun getDefaultFallbackLevel(levelId: Int): String {
        return """
        {
          "levelId": $levelId,
          "worldId": 1,
          "title": "Level $levelId",
          "quota": 10,
          "totalInitialBalls": 20,
          "bounds": { "width": 1000.0, "height": 1800.0 },
          "sandRegions": [{ "x": 0.0, "y": 300.0, "width": 1000.0, "height": 1200.0, "carvable": true }],
          "ballSpawners": [{ "x": 500.0, "y": 1650.0, "count": 20, "type": "colored", "spreadRadius": 120.0 }],
          "obstacles": [
            { "id": "funnel_left", "type": "funnel_bumper", "x": 250.0, "y": 260.0, "width": 240.0, "height": 40.0, "rotationDegrees": -25.0 },
            { "id": "funnel_right", "type": "funnel_bumper", "x": 750.0, "y": 260.0, "width": 240.0, "height": 40.0, "rotationDegrees": 25.0 }
          ],
          "collectibles": [],
          "truck": { "x": 500.0, "y": 120.0, "width": 320.0, "height": 150.0, "quota": 10 }
        }
        """.trimIndent()
    }
}
