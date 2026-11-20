package com.antigravity.sandadventure.physics

import kotlin.math.sqrt

data class Vector2D(
    var x: Float = 0.0f,
    var y: Float = 0.0f
) {
    fun set(newX: Float, newY: Float) {
        x = newX
        y = newY
    }

    fun set(other: Vector2D) {
        x = other.x
        y = other.y
    }

    fun add(dx: Float, dy: Float) {
        x += dx
        y += dy
    }

    fun add(other: Vector2D) {
        x += other.x
        y += other.y
    }

    fun sub(other: Vector2D) {
        x -= other.x
        y -= other.y
    }

    fun scale(factor: Float) {
        x *= factor
        y *= factor
    }

    fun lengthSquared(): Float = x * x + y * y

    fun length(): Float = sqrt(lengthSquared())

    fun normalize(): Vector2D {
        val len = length()
        if (len > 0.0001f) {
            x /= len
            y /= len
        }
        return this
    }

    fun distanceSquared(other: Vector2D): Float {
        val dx = x - other.x
        val dy = y - other.y
        return dx * dx + dy * dy
    }

    fun distanceTo(other: Vector2D): Float = sqrt(distanceSquared(other))

    fun dot(other: Vector2D): Float = x * other.x + y * other.y
}
