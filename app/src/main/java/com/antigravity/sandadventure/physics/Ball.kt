package com.antigravity.sandadventure.physics

import kotlin.random.Random

enum class BallExpression {
    NORMAL,
    SURPRISED, // Wide eyes when falling fast
    HAPPY      // Smiling eyes when saved in truck
}

data class Ball(
    val id: Int,
    val position: Vector2D = Vector2D(),
    val prevPosition: Vector2D = Vector2D(),
    val velocity: Vector2D = Vector2D(),
    val radius: Float = 14.0f,
    val mass: Float = 1.0f,
    var color: Int = 0xFFFF4757.toInt(),
    var isDormant: Boolean = false,
    var isCollected: Boolean = false,
    var isDestroyed: Boolean = false,

    // Child-friendly cartoon attributes
    var expression: BallExpression = BallExpression.NORMAL,
    var blinkTimer: Float = Random.nextFloat() * 3.0f,
    var isBlinking: Boolean = false,
    val passedGateIds: HashSet<String> = HashSet()
) {
    companion object {
        // Cheerful, child-friendly candy pastel palette
        val PALETTE = intArrayOf(
            0xFFFF4757.toInt(), // Bubblegum Coral Red
            0xFFFFA502.toInt(), // Sunny Candy Orange
            0xFF00D2D3.toInt(), // Bright Sky Cyan
            0xFF2ED573.toInt(), // Fresh Mint Green
            0xFF5F27CD.toInt(), // Sweet Lavender Purple
            0xFFFF6B81.toInt(), // Strawberry Pink
            0xFFFDCB6E.toInt(), // Lemon Yellow
            0xFF0984E3.toInt()  // Ocean Blue
        )
        const val COLOR_DORMANT = 0xFFF1F2F6.toInt()

        fun randomColor(): Int {
            return PALETTE[Random.nextInt(PALETTE.size)]
        }
    }

    fun updateAnimation(dt: Float) {
        blinkTimer -= dt
        if (blinkTimer <= 0.0f) {
            isBlinking = !isBlinking
            blinkTimer = if (isBlinking) 0.12f else (2.0f + Random.nextFloat() * 3.0f)
        }

        expression = when {
            isCollected -> BallExpression.HAPPY
            velocity.y < -380.0f -> BallExpression.SURPRISED
            else -> BallExpression.NORMAL
        }
    }

    fun activate(assignRandomColor: Boolean = true) {
        if (isDormant) {
            isDormant = false
            if (assignRandomColor) {
                color = randomColor()
            }
            expression = BallExpression.HAPPY
            // Fun pop scattering impulse
            velocity.x += (Random.nextFloat() - 0.5f) * 60.0f
            velocity.y += 30.0f + Random.nextFloat() * 40.0f
        }
    }
}
