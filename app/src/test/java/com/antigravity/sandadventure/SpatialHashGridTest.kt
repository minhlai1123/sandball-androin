package com.antigravity.sandadventure

import com.antigravity.sandadventure.physics.Ball
import com.antigravity.sandadventure.physics.SpatialHashGrid
import com.antigravity.sandadventure.physics.Vector2D
import org.junit.Assert.*
import org.junit.Test

class SpatialHashGridTest {

    @Test
    fun testSpatialHash100BallsInsertionAndQuery() {
        val grid = SpatialHashGrid(1000.0f, 1800.0f, cellSize = 50.0f)

        // Insert 100 balls across the grid
        val balls = ArrayList<Ball>()
        for (i in 1..100) {
            val b = Ball(
                id = i,
                position = Vector2D((i * 9).toFloat() % 1000.0f, (i * 17).toFloat() % 1800.0f),
                radius = 13.0f
            )
            balls.add(b)
            grid.insert(b)
        }

        // Query neighbors for ball 1
        var neighborChecks = 0
        grid.forEachNeighbor(balls[0]) {
            neighborChecks++
        }

        // Spatial hashing must dramatically reduce pair checks compared to full N*(N-1)/2 = 4950
        assertTrue("Neighbor checks should be far less than 4950", neighborChecks < 100)

        // Test clear
        grid.clear()
        var postClearChecks = 0
        grid.forEachNeighbor(balls[0]) {
            postClearChecks++
        }
        assertEquals(0, postClearChecks)
    }
}
