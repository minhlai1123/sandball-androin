package com.antigravity.sandadventure.physics

import kotlin.math.floor

class SpatialHashGrid(
    private val worldWidth: Float = 1000.0f,
    private val worldHeight: Float = 1800.0f,
    val cellSize: Float = 50.0f
) {
    val cols = (worldWidth / cellSize).toInt() + 1
    val rows = (worldHeight / cellSize).toInt() + 1
    val numBuckets = cols * rows

    @Suppress("UNCHECKED_CAST")
    val buckets: Array<ArrayList<Ball>> = Array(numBuckets) { ArrayList(8) }

    fun clear() {
        for (i in 0 until numBuckets) {
            buckets[i].clear()
        }
    }

    private fun getCellIndex(x: Float, y: Float): Int {
        val col = floor(x / cellSize).toInt().coerceIn(0, cols - 1)
        val row = floor(y / cellSize).toInt().coerceIn(0, rows - 1)
        return row * cols + col
    }

    fun insert(ball: Ball) {
        val idx = getCellIndex(ball.position.x, ball.position.y)
        buckets[idx].add(ball)
    }

    /**
     * Finds potential collision candidates within neighboring 3x3 cells.
     */
    fun forEachNeighbor(ball: Ball, action: (Ball) -> Unit) {
        val col = floor(ball.position.x / cellSize).toInt()
        val row = floor(ball.position.y / cellSize).toInt()

        val minCol = (col - 1).coerceAtLeast(0)
        val maxCol = (col + 1).coerceAtMost(cols - 1)
        val minRow = (row - 1).coerceAtLeast(0)
        val maxRow = (row + 1).coerceAtMost(rows - 1)

        for (r in minRow..maxRow) {
            val rowOffset = r * cols
            for (c in minCol..maxCol) {
                val bucket = buckets[rowOffset + c]
                for (i in 0 until bucket.size) {
                    val other = bucket[i]
                    if (other.id > ball.id) { // Avoid duplicate pair checks (A-B and B-A)
                        action(other)
                    }
                }
            }
        }
    }
}
