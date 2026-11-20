package com.antigravity.sandadventure.physics

import kotlin.math.cos
import kotlin.math.sin

sealed class CollisionBody {
    data class Segment(
        val p1: Vector2D,
        val p2: Vector2D,
        val restitution: Float = 0.3f,
        val isHazard: Boolean = false
    ) : CollisionBody()

    data class Box(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val restitution: Float = 0.3f
    ) : CollisionBody()

    data class RotatingPaddle(
        val pivot: Vector2D,
        val length: Float,
        val thickness: Float,
        var currentAngle: Float = 0.0f,
        val motorSpeed: Float = 0.0f
    ) : CollisionBody() {
        fun update(dt: Float) {
            if (motorSpeed != 0.0f) {
                currentAngle += motorSpeed * dt
            }
        }

        fun getEndpoints(): Pair<Vector2D, Vector2D> {
            val rad = Math.toRadians(currentAngle.toDouble()).toFloat()
            val halfLen = length / 2.0f
            val dx = cos(rad) * halfLen
            val dy = sin(rad) * halfLen
            return Pair(
                Vector2D(pivot.x - dx, pivot.y - dy),
                Vector2D(pivot.x + dx, pivot.y + dy)
            )
        }
    }

    data class MultiplierGate(
        val id: String,
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float = 30.0f,
        val multiplier: Int = 2
    ) : CollisionBody()

    data class PortalPair(
        val id: String,
        val inX: Float,
        val inY: Float,
        val outX: Float,
        val outY: Float,
        val radius: Float = 38.0f
    ) : CollisionBody()

    data class BouncyBumper(
        val id: String,
        val x: Float,
        val y: Float,
        val radius: Float = 36.0f,
        var flashTimer: Float = 0.0f
    ) : CollisionBody()

    data class TntCrate(
        val id: String,
        val x: Float,
        val y: Float,
        val size: Float = 55.0f,
        var isExploded: Boolean = false
    ) : CollisionBody()
}
