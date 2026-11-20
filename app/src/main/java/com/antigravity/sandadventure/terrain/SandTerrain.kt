package com.antigravity.sandadventure.terrain

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import com.antigravity.sandadventure.physics.Ball
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class SandTerrain(
    val worldWidth: Float = 1000.0f,
    val worldHeight: Float = 1800.0f,
    val gridCellSize: Float = 20.0f
) {
    val gridCols = (worldWidth / gridCellSize).toInt() + 1
    val gridRows = (worldHeight / gridCellSize).toInt() + 1

    // true = solid sand, false = carved empty space
    val occupancyGrid = BooleanArray(gridCols * gridRows) { false }

    // Visual sand mask bitmap (rendered at 1/2 scale for hardware performance)
    val maskWidth = 500
    val maskHeight = 900
    private var maskBitmap: Bitmap? = null
    private var maskCanvas: Canvas? = null

    private val clearPaint by lazy {
        try {
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            }
        } catch (_: Throwable) {
            Paint()
        }
    }

    private val strokePaint by lazy {
        try {
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
            }
        } catch (_: Throwable) {
            Paint()
        }
    }

    private val sandFillPaint by lazy {
        try {
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFD152.toInt() // Radiant golden sunny beach sand
            }
        } catch (_: Throwable) {
            Paint()
        }
    }

    init {
        reset()
    }

    fun reset(
        sandBottom: Float = 260.0f,
        sandTop: Float = 1750.0f,
        color: Int = 0xFFFFD152.toInt()
    ) {
        // 1. Reset occupancy grid: solid only between sandBottom and sandTop
        for (r in 0 until gridRows) {
            val cellY = (r + 0.5f) * gridCellSize
            val isSand = cellY in sandBottom..sandTop
            val rowOffset = r * gridCols
            for (c in 0 until gridCols) {
                occupancyGrid[rowOffset + c] = isSand
            }
        }

        // 2. Reset visual mask bitmap
        try {
            if (maskBitmap == null) {
                maskBitmap = Bitmap.createBitmap(maskWidth, maskHeight, Bitmap.Config.ARGB_8888)
                maskCanvas = Canvas(maskBitmap!!)
            }
            maskCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

            sandFillPaint.color = color
            val scaleY = maskHeight / worldHeight
            val screenTop = (worldHeight - sandTop) * scaleY
            val screenBottom = (worldHeight - sandBottom) * scaleY
            maskCanvas?.drawRect(0.0f, screenTop, maskWidth.toFloat(), screenBottom, sandFillPaint)
        } catch (_: Throwable) {
            // Memory fallback
        }
    }

    fun carveCapsule(x1: Float, y1: Float, x2: Float, y2: Float, radius: Float) {
        // 1. Update occupancy grid
        val minX = max(0.0f, min(x1, x2) - radius)
        val maxX = min(worldWidth, max(x1, x2) + radius)
        val minY = max(0.0f, min(y1, y2) - radius)
        val maxY = min(worldHeight, max(y1, y2) + radius)

        val minCol = (minX / gridCellSize).toInt().coerceIn(0, gridCols - 1)
        val maxCol = (maxX / gridCellSize).toInt().coerceIn(0, gridCols - 1)
        val minRow = (minY / gridCellSize).toInt().coerceIn(0, gridRows - 1)
        val maxRow = (maxY / gridCellSize).toInt().coerceIn(0, gridRows - 1)

        val segDx = x2 - x1
        val segDy = y2 - y1
        val segLenSq = segDx * segDx + segDy * segDy
        val rSq = radius * radius

        for (r in minRow..maxRow) {
            val cellY = (r + 0.5f) * gridCellSize
            val rowOffset = r * gridCols
            for (c in minCol..maxCol) {
                val idx = rowOffset + c
                if (!occupancyGrid[idx]) continue

                val cellX = (c + 0.5f) * gridCellSize
                val distSq = if (segLenSq < 0.0001f) {
                    val dx = cellX - x1
                    val dy = cellY - y1
                    dx * dx + dy * dy
                } else {
                    val t = max(0.0f, min(1.0f, ((cellX - x1) * segDx + (cellY - y1) * segDy) / segLenSq))
                    val projX = x1 + t * segDx
                    val projY = y1 + t * segDy
                    val dx = cellX - projX
                    val dy = cellY - projY
                    dx * dx + dy * dy
                }

                if (distSq <= rSq) {
                    occupancyGrid[idx] = false
                }
            }
        }

        // 2. Update visual bitmap
        maskCanvas?.let { canvas ->
            val scaleX = maskWidth / worldWidth
            val scaleY = maskHeight / worldHeight

            val mx1 = x1 * scaleX
            val my1 = (worldHeight - y1) * scaleY // Invert Y for screen space
            val mx2 = x2 * scaleX
            val my2 = (worldHeight - y2) * scaleY
            val mr = radius * scaleX

            strokePaint.strokeWidth = mr * 2.0f
            canvas.drawLine(mx1, my1, mx2, my2, strokePaint)
            canvas.drawCircle(mx1, my1, mr, clearPaint)
            canvas.drawCircle(mx2, my2, mr, clearPaint)
        }
    }

    fun isSolidAt(x: Float, y: Float): Boolean {
        val col = (x / gridCellSize).toInt()
        val row = (y / gridCellSize).toInt()
        if (col !in 0 until gridCols || row !in 0 until gridRows) return false
        return occupancyGrid[row * gridCols + col]
    }

    /**
     * Resolves collisions between a ball and remaining solid sand cells.
     * Uses robust box-circle clamping and boundary ejection so balls NEVER tunnel through solid sand.
     */
    fun resolveBallCollision(ball: Ball) {
        val minCol = ((ball.position.x - ball.radius) / gridCellSize).toInt().coerceIn(0, gridCols - 1)
        val maxCol = ((ball.position.x + ball.radius) / gridCellSize).toInt().coerceIn(0, gridCols - 1)
        val minRow = ((ball.position.y - ball.radius) / gridCellSize).toInt().coerceIn(0, gridRows - 1)
        val maxRow = ((ball.position.y + ball.radius) / gridCellSize).toInt().coerceIn(0, gridRows - 1)

        for (r in minRow..maxRow) {
            val rowOffset = r * gridCols
            val boxBottom = r * gridCellSize
            val boxTop = boxBottom + gridCellSize

            for (c in minCol..maxCol) {
                if (!occupancyGrid[rowOffset + c]) continue

                val boxLeft = c * gridCellSize
                val boxRight = boxLeft + gridCellSize

                val clampX = ball.position.x.coerceIn(boxLeft, boxRight)
                val clampY = ball.position.y.coerceIn(boxBottom, boxTop)

                val dx = ball.position.x - clampX
                val dy = ball.position.y - clampY
                val distSq = dx * dx + dy * dy

                if (distSq > 0.0001f) {
                    if (distSq < ball.radius * ball.radius) {
                        val dist = sqrt(distSq)
                        val overlap = ball.radius - dist
                        val nx = dx / dist
                        val ny = dy / dist

                        ball.position.x += nx * overlap
                        ball.position.y += ny * overlap

                        val velDotN = ball.velocity.x * nx + ball.velocity.y * ny
                        if (velDotN < 0.0f) {
                            // Cancel inward penetration velocity cleanly
                            ball.velocity.x -= nx * velDotN
                            ball.velocity.y -= ny * velDotN
                            // Extremely low rolling resistance so balls flow like water down carved slopes
                            ball.velocity.x *= 0.995f
                            ball.velocity.y *= 0.995f
                        }
                    }
                } else {
                    // Ball center penetrated inside the solid cell - push out to nearest face bordering empty space
                    val dLeft = ball.position.x - boxLeft
                    val dRight = boxRight - ball.position.x
                    val dBottom = ball.position.y - boxBottom
                    val dTop = boxTop - ball.position.y

                    var bestDir = 0
                    var minD = dTop

                    if (dBottom < minD && (r > 0 && !occupancyGrid[(r - 1) * gridCols + c])) {
                        minD = dBottom
                        bestDir = 1
                    }
                    if (dLeft < minD && (c > 0 && !occupancyGrid[rowOffset + (c - 1)])) {
                        minD = dLeft
                        bestDir = 2
                    }
                    if (dRight < minD && (c < gridCols - 1 && !occupancyGrid[rowOffset + (c + 1)])) {
                        minD = dRight
                        bestDir = 3
                    }

                    when (bestDir) {
                        0 -> {
                            ball.position.y = boxTop + ball.radius
                            if (ball.velocity.y < 0.0f) ball.velocity.y = 0.0f
                        }
                        1 -> {
                            ball.position.y = boxBottom - ball.radius
                            if (ball.velocity.y > 0.0f) ball.velocity.y = 0.0f
                        }
                        2 -> {
                            ball.position.x = boxLeft - ball.radius
                            if (ball.velocity.x > 0.0f) ball.velocity.x = 0.0f
                        }
                        3 -> {
                            ball.position.x = boxRight + ball.radius
                            if (ball.velocity.x < 0.0f) ball.velocity.x = 0.0f
                        }
                    }
                }
            }
        }
    }

    fun getMaskBitmap(): Bitmap? = maskBitmap
}
