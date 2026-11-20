package com.antigravity.sandadventure

import com.antigravity.sandadventure.data.SandAdventureLevel
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class LevelParsingTest {

    private val sampleJson = """
    {
      "levelId": 1,
      "worldId": 1,
      "title": "First Dig",
      "quota": 10,
      "totalInitialBalls": 20,
      "bounds": { "width": 1000.0, "height": 1800.0 },
      "sandRegions": [
        { "x": 0.0, "y": 300.0, "width": 1000.0, "height": 1200.0, "carvable": true }
      ],
      "ballSpawners": [
        { "x": 500.0, "y": 1650.0, "count": 20, "type": "colored", "spreadRadius": 120.0 }
      ],
      "obstacles": [
        { "id": "funnel_left", "type": "funnel_bumper", "x": 250.0, "y": 260.0, "width": 240.0, "height": 40.0, "rotationDegrees": -25.0 }
      ],
      "collectibles": [],
      "truck": { "x": 500.0, "y": 120.0, "width": 320.0, "height": 150.0, "quota": 10 }
    }
    """.trimIndent()

    @Test
    fun testLevelParsingSuccess() {
        val level = SandAdventureLevel.fromJson(sampleJson)
        assertEquals(1, level.levelId)
        assertEquals(1, level.worldId)
        assertEquals("First Dig", level.title)
        assertEquals(10, level.quota)
        assertEquals(20, level.totalInitialBalls)
        assertEquals(1000.0f, level.bounds.width, 0.01f)
        assertEquals(1800.0f, level.bounds.height, 0.01f)
        assertEquals(1, level.sandRegions.size)
        assertEquals(1, level.ballSpawners.size)
        assertEquals(20, level.ballSpawners[0].count)
        assertEquals("colored", level.ballSpawners[0].type)
        assertEquals(1, level.obstacles.size)
        assertEquals(500.0f, level.truck.x, 0.01f)
        assertEquals(10, level.truck.quota)
    }

    @Test
    fun testAll100LevelsIntegrity() {
        val assetsDir = File("src/main/assets/levels")
        val files = assetsDir.listFiles { _, name -> name.endsWith(".json") }
        assertNotNull("Assets directory must exist", files)
        assertEquals("Must contain exactly 100 level files", 100, files!!.size)

        for (i in 1..100) {
            val file = File(assetsDir, "level_$i.json")
            assertTrue("Level file level_$i.json must exist", file.exists())

            val content = file.readText(Charsets.UTF_8)
            val level = SandAdventureLevel.fromJson(content)

            assertEquals("Level ID must match filename", i, level.levelId)
            val expectedWorld = ((i - 1) / 10) + 1
            assertEquals("World ID must match formula", expectedWorld, level.worldId)
            assertTrue("Quota must be > 0", level.quota > 0)
            assertTrue("Total balls must be >= quota", level.totalInitialBalls >= level.quota)
            assertTrue("Ball spawners must not be empty", level.ballSpawners.isNotEmpty())
            assertTrue("Obstacles must contain bottom funnels", level.obstacles.size >= 2)
            assertNotNull("Truck must be defined", level.truck)
            assertEquals("Truck quota must match level quota", level.quota, level.truck.quota)
        }
    }
}
