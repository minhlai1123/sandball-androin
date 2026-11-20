package com.antigravity.sandadventure.gameplay

import android.view.MotionEvent
import com.antigravity.sandadventure.terrain.CapsuleCarver
import kotlin.math.max
import kotlin.math.min

class InputHandler(
    private val carver: CapsuleCarver,
    var viewportWidth: Float = 1080.0f,
    var viewportHeight: Float = 1920.0f
) {
    var worldWidth = 1000.0f
    var worldHeight = 1800.0f

    fun handleTouchEvent(event: MotionEvent): Boolean {
        // Map screen pixels to virtual world units
        val scaleX = worldWidth / viewportWidth
        val scaleY = worldHeight / viewportHeight

        val worldX = event.x * scaleX
        // Invert Y: Screen Y (0 at top) -> World Y (1800 at top, 0 at bottom)
        val worldY = worldHeight - (event.y * scaleY)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                carver.onTouchDown(worldX, worldY)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                carver.onTouchMove(worldX, worldY)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                carver.onTouchUp(worldX, worldY)
                return true
            }
        }
        return false
    }
}
