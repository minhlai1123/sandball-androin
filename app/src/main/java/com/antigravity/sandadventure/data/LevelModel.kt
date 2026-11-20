package com.antigravity.sandadventure.data

import org.json.JSONArray
import org.json.JSONObject

data class Bounds(
    val width: Float = 1000.0f,
    val height: Float = 1800.0f
)

data class SandRegion(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val carvable: Boolean = true
)

data class BallSpawner(
    val x: Float,
    val y: Float,
    val count: Int,
    val type: String, // "colored" or "dormant_white"
    val spreadRadius: Float = 100.0f
)

data class ObstacleDef(
    val id: String,
    val type: String, // "static_beam", "rotating_paddle", "funnel_bumper", "multiplier_gate", "portal_pair", "bouncy_bumper", "tnt_crate"
    val x: Float,
    val y: Float,
    val width: Float = 0.0f,
    val height: Float = 0.0f,
    val rotationDegrees: Float = 0.0f,
    val motorSpeed: Float = 0.0f,
    val restitution: Float = 0.3f,
    val multiplier: Int = 2,
    val exitX: Float = 500.0f,
    val exitY: Float = 500.0f
)

data class CollectibleDef(
    val id: String,
    val type: String, // "gold_key"
    val x: Float,
    val y: Float
)

data class TruckDef(
    val x: Float,
    val y: Float,
    val width: Float = 320.0f,
    val height: Float = 150.0f,
    val quota: Int
)

data class SandAdventureLevel(
    val levelId: Int,
    val worldId: Int,
    val title: String,
    val quota: Int,
    val totalInitialBalls: Int,
    val bounds: Bounds,
    val sandRegions: List<SandRegion>,
    val ballSpawners: List<BallSpawner>,
    val obstacles: List<ObstacleDef>,
    val collectibles: List<CollectibleDef>,
    val truck: TruckDef
) {
    companion object {
        fun fromJson(jsonStr: String): SandAdventureLevel {
            val json = JSONObject(jsonStr)
            val levelId = json.getInt("levelId")
            val worldId = json.optInt("worldId", 1)
            val title = json.optString("title", "Level $levelId")
            val quota = json.getInt("quota")
            val totalBalls = json.optInt("totalInitialBalls", quota)

            val boundsObj = json.optJSONObject("bounds")
            val bounds = if (boundsObj != null) {
                Bounds(
                    width = boundsObj.optDouble("width", 1000.0).toFloat(),
                    height = boundsObj.optDouble("height", 1800.0).toFloat()
                )
            } else {
                Bounds()
            }

            val sandRegions = mutableListOf<SandRegion>()
            val sandArr = json.optJSONArray("sandRegions") ?: JSONArray()
            for (i in 0 until sandArr.length()) {
                val obj = sandArr.getJSONObject(i)
                sandRegions.add(
                    SandRegion(
                        x = obj.getDouble("x").toFloat(),
                        y = obj.getDouble("y").toFloat(),
                        width = obj.getDouble("width").toFloat(),
                        height = obj.getDouble("height").toFloat(),
                        carvable = obj.optBoolean("carvable", true)
                    )
                )
            }

            val spawners = mutableListOf<BallSpawner>()
            val spawnerArr = json.optJSONArray("ballSpawners") ?: JSONArray()
            for (i in 0 until spawnerArr.length()) {
                val obj = spawnerArr.getJSONObject(i)
                spawners.add(
                    BallSpawner(
                        x = obj.getDouble("x").toFloat(),
                        y = obj.getDouble("y").toFloat(),
                        count = obj.getInt("count"),
                        type = obj.optString("type", "colored"),
                        spreadRadius = obj.optDouble("spreadRadius", 100.0).toFloat()
                    )
                )
            }

            val obstacles = mutableListOf<ObstacleDef>()
            val obstacleArr = json.optJSONArray("obstacles") ?: JSONArray()
            for (i in 0 until obstacleArr.length()) {
                val obj = obstacleArr.getJSONObject(i)
                obstacles.add(
                    ObstacleDef(
                        id = obj.optString("id", "obstacle_$i"),
                        type = obj.optString("type", "static_beam"),
                        x = obj.optDouble("x", 0.0).toFloat(),
                        y = obj.optDouble("y", 0.0).toFloat(),
                        width = obj.optDouble("width", 0.0).toFloat(),
                        height = obj.optDouble("height", 0.0).toFloat(),
                        rotationDegrees = obj.optDouble("rotationDegrees", 0.0).toFloat(),
                        motorSpeed = obj.optDouble("motorSpeed", 0.0).toFloat(),
                        restitution = obj.optDouble("restitution", 0.3).toFloat(),
                        multiplier = obj.optInt("multiplier", 2),
                        exitX = obj.optDouble("exitX", 500.0).toFloat(),
                        exitY = obj.optDouble("exitY", 500.0).toFloat()
                    )
                )
            }

            val collectibles = mutableListOf<CollectibleDef>()
            val collArr = json.optJSONArray("collectibles") ?: JSONArray()
            for (i in 0 until collArr.length()) {
                val obj = collArr.getJSONObject(i)
                collectibles.add(
                    CollectibleDef(
                        id = obj.optString("id", "collectible_$i"),
                        type = obj.optString("type", "gold_key"),
                        x = obj.getDouble("x").toFloat(),
                        y = obj.getDouble("y").toFloat()
                    )
                )
            }

            val truckObj = json.getJSONObject("truck")
            val truck = TruckDef(
                x = truckObj.getDouble("x").toFloat(),
                y = truckObj.getDouble("y").toFloat(),
                width = truckObj.optDouble("width", 320.0).toFloat(),
                height = truckObj.optDouble("height", 150.0).toFloat(),
                quota = truckObj.optInt("quota", quota)
            )

            return SandAdventureLevel(
                levelId = levelId,
                worldId = worldId,
                title = title,
                quota = quota,
                totalInitialBalls = totalBalls,
                bounds = bounds,
                sandRegions = sandRegions,
                ballSpawners = spawners,
                obstacles = obstacles,
                collectibles = collectibles,
                truck = truck
            )
        }
    }
}
