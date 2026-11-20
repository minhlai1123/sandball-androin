package com.antigravity.sandadventure.terrain

class CapsuleCarver(
    private val terrain: SandTerrain,
    var carveRadius: Float = 42.0f
) {
    private var isDragging = false
    private var lastX = 0.0f
    private var lastY = 0.0f

    fun onTouchDown(x: Float, y: Float) {
        isDragging = true
        lastX = x
        lastY = y
        terrain.carveCapsule(x, y, x, y, carveRadius)
    }

    fun onTouchMove(x: Float, y: Float) {
        if (!isDragging) {
            onTouchDown(x, y)
            return
        }
        terrain.carveCapsule(lastX, lastY, x, y, carveRadius)
        lastX = x
        lastY = y
    }

    fun onTouchUp(x: Float, y: Float) {
        if (isDragging) {
            terrain.carveCapsule(lastX, lastY, x, y, carveRadius)
            isDragging = false
        }
    }
}
