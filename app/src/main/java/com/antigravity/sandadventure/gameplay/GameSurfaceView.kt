package com.antigravity.sandadventure.gameplay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.antigravity.sandadventure.data.getWorldTheme
import com.antigravity.sandadventure.physics.Ball
import com.antigravity.sandadventure.physics.BallExpression
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class GameSurfaceView(
    context: Context,
    val gameEngine: GameEngine
) : SurfaceView(context), SurfaceHolder.Callback, Runnable {

    private var renderThread: Thread? = null
    @Volatile
    private var isRunning = false

    private val bgPaint = Paint()
    private var bgGradient: LinearGradient? = null
    private var lastViewW = 0
    private var lastViewH = 0

    private val ballPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ballHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xAAFFFFFF.toInt()
    }
    private val blushPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x77FF6B81.toInt() // Soft rosy cartoon cheeks
    }
    private val eyeWhitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val eyePupilPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF1E293B.toInt() }
    private val eyeSparklePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val mouthPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF1E293B.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 2.8f
        strokeCap = Paint.Cap.ROUND
    }
    private val mouthFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF1E293B.toInt()
        style = Paint.Style.FILL
    }
    private val tonguePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFF7675.toInt() // Cute pink tongue
    }

    private val obstaclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 18.0f
        strokeCap = Paint.Cap.ROUND
    }
    private val obstacleStripePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 5.0f
        strokeCap = Paint.Cap.ROUND
    }

    private val truckBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFF4757.toInt() }
    private val truckRoofPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF2ED573.toInt() }
    private val truckWheelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF2F3542.toInt() }
    private val truckRimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFF1F2F6.toInt() }
    private val truckFaceEyePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val truckFacePupilPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF00D2D3.toInt() }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 58.0f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val confettiPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFF1C40F.toInt() }

    // Interactive Elements Paints
    private val gateBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xCC8E44AD.toInt()
        style = Paint.Style.FILL
    }
    private val gateBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF00D2D3.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 6.0f
    }
    private val gateTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }

    private val portalInPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF0984E3.toInt()
        style = Paint.Style.FILL
    }
    private val portalInRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF00CEC9.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 7.0f
    }
    private val portalOutPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE17055.toInt()
        style = Paint.Style.FILL
    }
    private val portalOutRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFDCB6E.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 7.0f
    }

    private val bumperBasePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFF4757.toInt()
        style = Paint.Style.FILL
    }
    private val bumperRimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 8.0f
    }
    private val bumperDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val tntCratePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE67E22.toInt()
        style = Paint.Style.FILL
    }
    private val tntBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFD35400.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 5.0f
    }
    private val tntLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFC0392B.toInt()
        style = Paint.Style.FILL
    }
    private val tntTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }

    private val destRect = RectF()
    private val starPath = Path()

    // Pre-allocated colors for truck-bed collected balls
    private val collectedBallColors = intArrayOf(
        0xFFFF4757.toInt(), 0xFF2ED573.toInt(), 0xFF1E90FF.toInt(),
        0xFFFFA502.toInt(), 0xFF9B59B6.toInt(), 0xFFFF6B81.toInt()
    )

    init {
        holder.addCallback(this)
        isFocusable = true
        initStarPath()
    }

    private fun initStarPath() {
        starPath.reset()
        val rOuter = 18.0f
        val rInner = 8.0f
        for (i in 0 until 10) {
            val angle = i * Math.PI / 5.0 - Math.PI / 2.0
            val r = if (i % 2 == 0) rOuter else rInner
            val x = (cos(angle) * r).toFloat()
            val y = (sin(angle) * r).toFloat()
            if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
        }
        starPath.close()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        isRunning = true
        renderThread = Thread(this, "GameRenderThread").apply { start() }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        gameEngine.inputHandler.viewportWidth = width.toFloat()
        gameEngine.inputHandler.viewportHeight = height.toFloat()
        destRect.set(0f, 0f, width.toFloat(), height.toFloat())
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isRunning = false
        gameEngine.audioManager.stopDigSound()
        var retry = true
        while (retry) {
            try {
                renderThread?.join(500)
                retry = false
            } catch (_: InterruptedException) {
            }
        }
        renderThread = null
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                gameEngine.audioManager.startDigSound()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                gameEngine.audioManager.stopDigSound()
            }
        }
        return gameEngine.inputHandler.handleTouchEvent(event)
    }

    override fun run() {
        var lastTime = System.nanoTime()

        while (isRunning) {
            val now = System.nanoTime()
            val dt = ((now - lastTime) / 1_000_000_000.0f).coerceIn(0.001f, 0.033f)
            lastTime = now

            // Update game logic
            gameEngine.update(dt)

            // Render frame
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    renderFrame(canvas)
                }
            } catch (_: Throwable) {
            } finally {
                if (canvas != null) {
                    try {
                        holder.unlockCanvasAndPost(canvas)
                    } catch (_: Throwable) {
                    }
                }
            }

            try {
                Thread.sleep(8)
            } catch (_: InterruptedException) {
            }
        }
    }

    private fun renderFrame(canvas: Canvas) {
        val viewW = width.toFloat()
        val viewH = height.toFloat()
        if (viewW <= 0f || viewH <= 0f) return

        val scaleX = viewW / 1000.0f
        val scaleY = viewH / 1800.0f

        val theme = getWorldTheme(gameEngine.currentWorldId)

        // 1. Cheerful, warm excavated background gradient
        if (bgGradient == null || lastViewW != width || lastViewH != height) {
            lastViewW = width
            lastViewH = height
            bgGradient = LinearGradient(
                0f, 0f, 0f, viewH,
                theme.bgTopColor, theme.bgBottomColor,
                Shader.TileMode.CLAMP
            )
            bgPaint.shader = bgGradient
        }
        canvas.drawRect(0f, 0f, viewW, viewH, bgPaint)

        // 2. Sand surface mask (rendered with transparent carved tunnels)
        gameEngine.sandTerrain.getMaskBitmap()?.let { mask ->
            canvas.drawBitmap(mask, null, destRect, null)
        }

        // 3. Obstacles (cheerful candy-striped ramps)
        obstaclePaint.color = theme.obstacleColor
        for (seg in gameEngine.physicsEngine.staticSegments) {
            val sx1 = seg.p1.x * scaleX
            val sy1 = (1800.0f - seg.p1.y) * scaleY
            val sx2 = seg.p2.x * scaleX
            val sy2 = (1800.0f - seg.p2.y) * scaleY
            canvas.drawLine(sx1, sy1, sx2, sy2, obstaclePaint)
            canvas.drawLine(sx1, sy1, sx2, sy2, obstacleStripePaint)
        }

        // 4. Rotating paddles
        for (paddle in gameEngine.physicsEngine.rotatingPaddles) {
            val (ep1, ep2) = paddle.getEndpoints()
            val px1 = ep1.x * scaleX
            val py1 = (1800.0f - ep1.y) * scaleY
            val px2 = ep2.x * scaleX
            val py2 = (1800.0f - ep2.y) * scaleY
            canvas.drawLine(px1, py1, px2, py2, obstaclePaint)
            canvas.drawLine(px1, py1, px2, py2, obstacleStripePaint)
        }

        // 4b. Multiplier Gates
        renderMultiplierGates(canvas, scaleX, scaleY)

        // 4c. Portals
        renderPortals(canvas, scaleX, scaleY)

        // 4d. Bouncy Bumpers
        renderBouncyBumpers(canvas, scaleX, scaleY)

        // 4e. TNT Toy Crates
        renderTntCrates(canvas, scaleX, scaleY)

        // 5. Balls with Cute Kawaii Cartoon Faces
        val balls = gameEngine.physicsEngine.balls
        for (i in 0 until balls.size) {
            val b = balls[i]
            if (b.isCollected || b.isDestroyed) continue

            val bx = b.position.x * scaleX
            val by = (1800.0f - b.position.y) * scaleY
            val br = b.radius * scaleX

            // Candy ball body
            ballPaint.color = b.color
            canvas.drawCircle(bx, by, br, ballPaint)

            // Glossy 3D specular highlight
            canvas.drawCircle(bx - br * 0.32f, by - br * 0.32f, br * 0.40f, ballHighlightPaint)

            // Render Cartoon Face
            if (!b.isDormant) {
                renderKawaiiBallFace(canvas, bx, by, br, b)
            } else {
                renderSleepingDormantFace(canvas, bx, by, br)
            }
        }

        // 6. Cute Bouncy Toy Truck at bottom
        renderCuteTruck(canvas, scaleX, scaleY)

        // 7. Confetti & Celebration Stars on victory
        for (c in gameEngine.confettiParticles) {
            confettiPaint.color = c.color
            confettiPaint.alpha = (c.alpha * 255).toInt().coerceIn(0, 255)
            val cx = c.x * scaleX
            val cy = (1800.0f - c.y) * scaleY
            canvas.save()
            canvas.rotate(c.rotation, cx, cy)
            canvas.drawRect(cx - 10f, cy - 6f, cx + 10f, cy + 6f, confettiPaint)
            canvas.restore()
        }

        for (s in gameEngine.floatingStars) {
            starPaint.alpha = (s.alpha * 255).toInt().coerceIn(0, 255)
            val sx = s.x * scaleX
            val sy = (1800.0f - s.y) * scaleY
            canvas.save()
            canvas.translate(sx, sy)
            canvas.scale(s.scale * scaleX, s.scale * scaleY)
            canvas.drawPath(starPath, starPaint)
            canvas.restore()
        }
    }

    private fun renderKawaiiBallFace(canvas: Canvas, bx: Float, by: Float, br: Float, b: Ball) {
        val eyeSpacing = br * 0.34f
        val eyeY = by - br * 0.10f
        val eyeRadius = br * 0.28f

        // 1. Rosy pink blush cheeks
        canvas.drawCircle(bx - br * 0.50f, by + br * 0.12f, br * 0.20f, blushPaint)
        canvas.drawCircle(bx + br * 0.50f, by + br * 0.12f, br * 0.20f, blushPaint)

        // 2. Eyes
        if (b.isBlinking) {
            // Cute closed smiling eye arcs (⌒ ‿ ⌒)
            canvas.drawArc(
                bx - eyeSpacing - eyeRadius, eyeY - eyeRadius * 0.6f,
                bx - eyeSpacing + eyeRadius, eyeY + eyeRadius * 0.6f,
                0f, 180f, false, mouthPaint
            )
            canvas.drawArc(
                bx + eyeSpacing - eyeRadius, eyeY - eyeRadius * 0.6f,
                bx + eyeSpacing + eyeRadius, eyeY + eyeRadius * 0.6f,
                0f, 180f, false, mouthPaint
            )
        } else {
            // White sclera
            canvas.drawCircle(bx - eyeSpacing, eyeY, eyeRadius, eyeWhitePaint)
            canvas.drawCircle(bx + eyeSpacing, eyeY, eyeRadius, eyeWhitePaint)

            // Glossy black pupil
            val isFast = b.velocity.y < -350.0f || abs(b.velocity.x) > 300.0f
            val pupilR = if (isFast) eyeRadius * 0.50f else eyeRadius * 0.62f
            canvas.drawCircle(bx - eyeSpacing, eyeY, pupilR, eyePupilPaint)
            canvas.drawCircle(bx + eyeSpacing, eyeY, pupilR, eyePupilPaint)

            // Sparkle 1: Big sparkle glint (top-left)
            canvas.drawCircle(bx - eyeSpacing - pupilR * 0.35f, eyeY - pupilR * 0.35f, pupilR * 0.38f, eyeSparklePaint)
            canvas.drawCircle(bx + eyeSpacing - pupilR * 0.35f, eyeY - pupilR * 0.35f, pupilR * 0.38f, eyeSparklePaint)

            // Sparkle 2: Small secondary twinkle (bottom-right)
            canvas.drawCircle(bx - eyeSpacing + pupilR * 0.30f, eyeY + pupilR * 0.30f, pupilR * 0.18f, eyeSparklePaint)
            canvas.drawCircle(bx + eyeSpacing + pupilR * 0.30f, eyeY + pupilR * 0.30f, pupilR * 0.18f, eyeSparklePaint)
        }

        // 3. Mouth
        val mouthY = by + br * 0.28f
        val isFast = b.velocity.y < -350.0f || abs(b.velocity.x) > 300.0f
        if (isFast) {
            // Surprised "O" mouth when rushing down
            canvas.drawCircle(bx, mouthY + br * 0.05f, br * 0.22f, mouthFillPaint)
            canvas.drawCircle(bx, mouthY + br * 0.05f, br * 0.12f, tonguePaint)
        } else {
            // Cheerful happy open smile
            canvas.drawArc(bx - br * 0.28f, mouthY - br * 0.18f, bx + br * 0.28f, mouthY + br * 0.22f, 15f, 150f, false, mouthPaint)
            canvas.drawArc(bx - br * 0.18f, mouthY, bx + br * 0.18f, mouthY + br * 0.20f, 20f, 140f, true, tonguePaint)
        }
    }

    private fun renderSleepingDormantFace(canvas: Canvas, bx: Float, by: Float, br: Float) {
        val eyeSpacing = br * 0.32f
        val eyeY = by - br * 0.05f

        // Cute lavender/pink blush
        canvas.drawCircle(bx - br * 0.48f, by + br * 0.10f, br * 0.18f, blushPaint)
        canvas.drawCircle(bx + br * 0.48f, by + br * 0.10f, br * 0.18f, blushPaint)

        // Sleeping closed crescent eyes (- ‿ -)
        canvas.drawLine(bx - eyeSpacing - br * 0.15f, eyeY, bx - eyeSpacing + br * 0.15f, eyeY, mouthPaint)
        canvas.drawLine(bx + eyeSpacing - br * 0.15f, eyeY, bx + eyeSpacing + br * 0.15f, eyeY, mouthPaint)

        // Tiny sleeping smile
        canvas.drawArc(bx - br * 0.16f, eyeY + br * 0.18f, bx + br * 0.16f, eyeY + br * 0.32f, 20f, 140f, false, mouthPaint)
    }

    private fun renderCuteTruck(canvas: Canvas, scaleX: Float, scaleY: Float) {
        val level = gameEngine.currentLevel ?: return
        val truck = level.truck

        // Suspension bounce effect
        val bounceY = gameEngine.truckBounceY * scaleY

        val tx = truck.x * scaleX
        val ty = (1800.0f - truck.y) * scaleY + bounceY
        val tw = truck.width * scaleX
        val th = truck.height * scaleY

        // 1. Asphalt platform base
        val platformPaint = Paint().apply { color = 0xFF334155.toInt() }
        canvas.drawRoundRect(tx - tw * 0.65f, ty + th * 0.35f - bounceY, tx + tw * 0.65f, ty + th * 0.85f - bounceY, 16f, 16f, platformPaint)

        // 2. Cute Pickup Truck Bed
        val bedLeft = tx - tw * 0.48f
        val bedRight = tx + tw * 0.15f
        val bedTop = ty - th * 0.35f
        val bedBottom = ty + th * 0.35f
        canvas.drawRoundRect(bedLeft, bedTop, bedRight, bedBottom, 14f, 14f, truckBodyPaint)

        // Draw collected balls physically piling up inside the truck bed!
        val collected = gameEngine.collectedCount
        val ballsToShow = collected.coerceAtMost(24)
        if (ballsToShow > 0) {
            val ballR = 8.5f * scaleX
            for (i in 0 until ballsToShow) {
                val row = i / 6
                val col = i % 6
                val cx = bedLeft + (col + 0.8f) * (ballR * 2.1f)
                val cy = bedBottom - (row + 0.6f) * (ballR * 1.9f)
                ballPaint.color = collectedBallColors[i % collectedBallColors.size]
                canvas.drawCircle(cx, cy, ballR, ballPaint)
                canvas.drawCircle(cx - ballR * 0.3f, cy - ballR * 0.3f, ballR * 0.35f, ballHighlightPaint)
            }
        }

        // 3. Truck Cab (with cute curved roof)
        val cabLeft = tx + tw * 0.12f
        val cabRight = tx + tw * 0.50f
        val cabTop = ty - th * 0.42f
        val cabBottom = ty + th * 0.35f
        canvas.drawRoundRect(cabLeft, cabTop, cabRight, cabBottom, 18f, 18f, truckRoofPaint)

        // 4. Cute Cartoon Headlight Eyes
        val headlightY = ty - th * 0.10f
        val headlightR = th * 0.16f
        canvas.drawCircle(cabRight - 4f, headlightY, headlightR, truckFaceEyePaint)
        canvas.drawCircle(cabRight - 2f, headlightY, headlightR * 0.6f, truckFacePupilPaint)
        canvas.drawCircle(cabRight - 4f, headlightY - 3f, headlightR * 0.25f, eyeSparklePaint)

        // 5. Friendly Grille Smile
        val grilleY = ty + th * 0.18f
        canvas.drawArc(cabRight - 18f, grilleY - 8f, cabRight - 2f, grilleY + 8f, 20f, 140f, false, mouthPaint)

        // 6. Oversized Bouncy Cartoon Wheels
        val wheelR = th * 0.20f
        val wheel1X = bedLeft + tw * 0.14f
        val wheel2X = cabRight - tw * 0.12f
        val wheelY = bedBottom + 6f

        canvas.drawCircle(wheel1X, wheelY, wheelR, truckWheelPaint)
        canvas.drawCircle(wheel2X, wheelY, wheelR, truckWheelPaint)

        canvas.drawCircle(wheel1X, wheelY, wheelR * 0.5f, truckRimPaint)
        canvas.drawCircle(wheel2X, wheelY, wheelR * 0.5f, truckRimPaint)

        // 7. Quota badge below platform
        textPaint.textSize = 50.0f * scaleX
        val quotaStr = if (gameEngine.isVictory) "★ FULL! ★" else "🎯 ${gameEngine.collectedCount} / ${level.quota}"
        canvas.drawText(quotaStr, tx, ty + th * 0.70f - bounceY, textPaint)
    }

    private fun renderMultiplierGates(canvas: Canvas, scaleX: Float, scaleY: Float) {
        gateTextPaint.textSize = 24.0f * scaleX
        for (gate in gameEngine.physicsEngine.multiplierGates) {
            val gx = gate.x * scaleX
            val gy = (1800.0f - gate.y) * scaleY
            val gw = gate.width * scaleX
            val gh = gate.height * scaleY

            val rect = RectF(gx - gw / 2f, gy - gh / 2f, gx + gw / 2f, gy + gh / 2f)
            canvas.drawRoundRect(rect, 14f, 14f, gateBgPaint)
            canvas.drawRoundRect(rect, 14f, 14f, gateBorderPaint)

            // Neon Multiply Label
            val label = "★ ×${gate.multiplier} MULTIPLY ★"
            canvas.drawText(label, gx, gy + gateTextPaint.textSize * 0.35f, gateTextPaint)
        }
    }

    private fun renderPortals(canvas: Canvas, scaleX: Float, scaleY: Float) {
        val portalTime = (System.currentTimeMillis() % 2000) / 2000.0f * 360f
        for (portal in gameEngine.physicsEngine.portalPairs) {
            val inX = portal.inX * scaleX
            val inY = (1800.0f - portal.inY) * scaleY
            val outX = portal.outX * scaleX
            val outY = (1800.0f - portal.outY) * scaleY
            val r = portal.radius * scaleX

            // Entry Portal (Cyan/Blue vortex)
            canvas.drawCircle(inX, inY, r, portalInPaint)
            canvas.drawCircle(inX, inY, r, portalInRingPaint)
            // Inner pulsing swirl dot
            val swirlX1 = inX + cos(Math.toRadians(portalTime.toDouble())).toFloat() * (r * 0.45f)
            val swirlY1 = inY + sin(Math.toRadians(portalTime.toDouble())).toFloat() * (r * 0.45f)
            canvas.drawCircle(swirlX1, swirlY1, r * 0.25f, eyeWhitePaint)

            // Exit Portal (Orange/Gold vortex)
            canvas.drawCircle(outX, outY, r, portalOutPaint)
            canvas.drawCircle(outX, outY, r, portalOutRingPaint)
            val swirlX2 = outX - cos(Math.toRadians(portalTime.toDouble())).toFloat() * (r * 0.45f)
            val swirlY2 = outY - sin(Math.toRadians(portalTime.toDouble())).toFloat() * (r * 0.45f)
            canvas.drawCircle(swirlX2, swirlY2, r * 0.25f, eyeWhitePaint)
        }
    }

    private fun renderBouncyBumpers(canvas: Canvas, scaleX: Float, scaleY: Float) {
        for (bumper in gameEngine.physicsEngine.bouncyBumpers) {
            val bx = bumper.x * scaleX
            val by = (1800.0f - bumper.y) * scaleY
            val pulse = if (bumper.flashTimer > 0f) 1.25f else 1.0f
            val r = bumper.radius * scaleX * pulse

            // Outer bumper ring
            canvas.drawCircle(bx, by, r, bumperBasePaint)
            canvas.drawCircle(bx, by, r, bumperRimPaint)

            // Cute polka dots on mushroom bumper
            val dotR = r * 0.20f
            canvas.drawCircle(bx, by, dotR * 1.2f, bumperDotPaint)
            canvas.drawCircle(bx - r * 0.45f, by - r * 0.20f, dotR, bumperDotPaint)
            canvas.drawCircle(bx + r * 0.45f, by - r * 0.20f, dotR, bumperDotPaint)
            canvas.drawCircle(bx, by + r * 0.45f, dotR, bumperDotPaint)
        }
    }

    private fun renderTntCrates(canvas: Canvas, scaleX: Float, scaleY: Float) {
        tntTextPaint.textSize = 22.0f * scaleX
        for (crate in gameEngine.physicsEngine.tntCrates) {
            if (crate.isExploded) continue
            val cx = crate.x * scaleX
            val cy = (1800.0f - crate.y) * scaleY
            val s = crate.size * scaleX
            val halfS = s / 2.0f

            // Wooden Crate body
            val rect = RectF(cx - halfS, cy - halfS, cx + halfS, cy + halfS)
            canvas.drawRoundRect(rect, 8f, 8f, tntCratePaint)
            canvas.drawRoundRect(rect, 8f, 8f, tntBorderPaint)

            // Diagonal slats
            canvas.drawLine(cx - halfS, cy - halfS, cx + halfS, cy + halfS, tntBorderPaint)
            canvas.drawLine(cx - halfS, cy + halfS, cx + halfS, cy - halfS, tntBorderPaint)

            // Center Red TNT Badge
            val badgeRect = RectF(cx - halfS * 0.75f, cy - halfS * 0.45f, cx + halfS * 0.75f, cy + halfS * 0.45f)
            canvas.drawRoundRect(badgeRect, 6f, 6f, tntLabelPaint)
            canvas.drawText("TNT 💣", cx, cy + tntTextPaint.textSize * 0.35f, tntTextPaint)
        }
    }
}
