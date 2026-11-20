package com.antigravity.sandadventure.gameplay

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.antigravity.sandadventure.data.AssetLevelRepository
import com.antigravity.sandadventure.data.ProgressManager
import com.antigravity.sandadventure.data.SandAdventureLevel
import com.antigravity.sandadventure.data.getWorldTheme
import com.antigravity.sandadventure.physics.Ball
import com.antigravity.sandadventure.physics.CollisionBody
import com.antigravity.sandadventure.physics.PhysicsEngine
import com.antigravity.sandadventure.physics.Vector2D
import com.antigravity.sandadventure.terrain.CapsuleCarver
import com.antigravity.sandadventure.terrain.SandTerrain
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class GameEngine(
    private val context: Context,
    val sceneManager: SceneManager,
    val progressManager: ProgressManager,
    val audioManager: AudioManager = AudioManager(context, progressManager)
) {
    val levelRepo = AssetLevelRepository(context)

    var currentLevelId by mutableIntStateOf(1)
    var currentLevel by mutableStateOf<SandAdventureLevel?>(null)
    var currentWorldId by mutableIntStateOf(1)

    val physicsEngine = PhysicsEngine(1000.0f, 1800.0f)
    val sandTerrain = SandTerrain(1000.0f, 1800.0f, gridCellSize = 20.0f)
    val capsuleCarver = CapsuleCarver(sandTerrain, carveRadius = 55.0f)
    val inputHandler = InputHandler(capsuleCarver)

    var remainingQuota by mutableIntStateOf(10)
    var collectedCount by mutableIntStateOf(0)
    var isVictory by mutableStateOf(false)
    var isDefeat by mutableStateOf(false)
    var earnedStars by mutableIntStateOf(0)
    var victoryTimer by mutableFloatStateOf(0.0f)
    val autoAdvanceDelay: Float = 2.4f
    var hasTriggeredAutoAdvance: Boolean = false

    var onLevelCompleted: ((Int) -> Unit)? = null

    fun advanceToNextLevel() {
        val nextId = currentLevelId + 1
        if (nextId <= 100) {
            loadLevel(nextId)
            sceneManager.startLevel(nextId)
            onLevelCompleted?.invoke(nextId)
        } else {
            sceneManager.openLevelSelect()
        }
    }

    // Truck bounce suspension animation
    var truckBounceY: Float = 0.0f
    private var truckBounceVelocity: Float = 0.0f

    // Confetti and celebratory floating stars
    val confettiParticles = ArrayList<Confetti>()
    val floatingStars = ArrayList<CelebrationStar>()

    data class Confetti(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        val color: Int,
        var rotation: Float,
        var vRot: Float,
        var alpha: Float = 1.0f
    )

    data class CelebrationStar(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var scale: Float,
        var alpha: Float = 1.0f
    )

    init {
        physicsEngine.onBallCollected = { ball ->
            onBallReachedTruck(ball)
        }
        physicsEngine.onBallActivated = {
            audioManager.playBallPop()
        }
        physicsEngine.onBallCloned = {
            audioManager.playMultiplier()
        }
        physicsEngine.onPortalUsed = {
            audioManager.playPortal()
        }
        physicsEngine.onBumperHit = {
            audioManager.playBoing()
        }
        physicsEngine.onTntExploded = { crate ->
            audioManager.playTntBlast()
            // Blow open a large crater in the sand!
            sandTerrain.carveCapsule(crate.x, crate.y, crate.x, crate.y, 140.0f)
            // Spawn explosion stars & confetti
            for (i in 0 until 24) {
                floatingStars.add(
                    CelebrationStar(
                        x = crate.x + (Random.nextFloat() - 0.5f) * 60f,
                        y = crate.y + (Random.nextFloat() - 0.5f) * 60f,
                        vx = (Random.nextFloat() - 0.5f) * 550f,
                        vy = (Random.nextFloat() - 0.5f) * 550f,
                        scale = 0.8f + Random.nextFloat() * 0.6f
                    )
                )
            }
        }
    }

    fun loadLevel(levelId: Int) {
        currentLevelId = levelId
        currentWorldId = ((levelId - 1) / 10) + 1
        val level = levelRepo.getLevel(levelId)
        currentLevel = level

        physicsEngine.clear()

        // Match sand color to current world theme
        val theme = getWorldTheme(currentWorldId)
        sandTerrain.reset(sandBottom = 260.0f, sandTop = 1750.0f, color = theme.sandColor)
        confettiParticles.clear()
        floatingStars.clear()

        remainingQuota = level.quota
        collectedCount = 0
        isVictory = false
        isDefeat = false
        earnedStars = 0
        victoryTimer = 0.0f
        hasTriggeredAutoAdvance = false
        truckBounceY = 0.0f
        truckBounceVelocity = 0.0f

        // Play joyful cartoon gameplay BGM matched to world theme
        audioManager.playWorldBgm(currentWorldId)

        // 1. Setup truck trigger zone
        val truck = level.truck
        physicsEngine.truckZoneLeft = truck.x - truck.width / 2.0f
        physicsEngine.truckZoneRight = truck.x + truck.width / 2.0f
        physicsEngine.truckZoneBottom = truck.y - truck.height / 2.0f
        physicsEngine.truckZoneTop = truck.y + truck.height / 2.0f

        // 2. Setup obstacles
        for (obs in level.obstacles) {
            when (obs.type) {
                "static_beam", "funnel_bumper" -> {
                    val halfW = obs.width / 2.0f
                    val rad = Math.toRadians(obs.rotationDegrees.toDouble()).toFloat()
                    val dx = cos(rad) * halfW
                    val dy = sin(rad) * halfW
                    physicsEngine.staticSegments.add(
                        CollisionBody.Segment(
                            p1 = Vector2D(obs.x - dx, obs.y - dy),
                            p2 = Vector2D(obs.x + dx, obs.y + dy),
                            restitution = obs.restitution
                        )
                    )
                }
                "rotating_paddle" -> {
                    physicsEngine.rotatingPaddles.add(
                        CollisionBody.RotatingPaddle(
                            pivot = Vector2D(obs.x, obs.y),
                            length = obs.width,
                            thickness = obs.height,
                            currentAngle = obs.rotationDegrees,
                            motorSpeed = obs.motorSpeed
                        )
                    )
                }
                "multiplier_gate" -> {
                    physicsEngine.multiplierGates.add(
                        CollisionBody.MultiplierGate(
                            id = obs.id,
                            x = obs.x,
                            y = obs.y,
                            width = obs.width.coerceAtLeast(140.0f),
                            height = obs.height.coerceAtLeast(36.0f),
                            multiplier = obs.multiplier
                        )
                    )
                }
                "portal_pair" -> {
                    physicsEngine.portalPairs.add(
                        CollisionBody.PortalPair(
                            id = obs.id,
                            inX = obs.x,
                            inY = obs.y,
                            outX = obs.exitX,
                            outY = obs.exitY,
                            radius = if (obs.width > 0f) obs.width / 2f else 38.0f
                        )
                    )
                }
                "bouncy_bumper" -> {
                    physicsEngine.bouncyBumpers.add(
                        CollisionBody.BouncyBumper(
                            id = obs.id,
                            x = obs.x,
                            y = obs.y,
                            radius = if (obs.width > 0f) obs.width / 2f else 38.0f
                        )
                    )
                }
                "tnt_crate" -> {
                    physicsEngine.tntCrates.add(
                        CollisionBody.TntCrate(
                            id = obs.id,
                            x = obs.x,
                            y = obs.y,
                            size = if (obs.width > 0f) obs.width else 55.0f
                        )
                    )
                }
            }
        }

        // 3. Pre-carve initial bowl opening where colored balls start
        sandTerrain.carveCapsule(500.0f, 1720.0f, 500.0f, 1560.0f, 120.0f)

        // 3b. Pre-carve enclosed chambers for dormant white balls
        for (spawner in level.ballSpawners) {
            if (spawner.type == "dormant_white") {
                sandTerrain.carveCapsule(
                    spawner.x, spawner.y,
                    spawner.x, spawner.y,
                    spawner.spreadRadius + 12.0f
                )
            }
        }

        // 4. Spawn balls
        var ballIdCounter = 1
        for (spawner in level.ballSpawners) {
            val isDormant = spawner.type == "dormant_white"
            for (i in 0 until spawner.count) {
                val angle = Random.nextFloat() * 6.28318f
                val dist = Random.nextFloat() * spawner.spreadRadius
                val bx = spawner.x + cos(angle) * dist
                val by = spawner.y + sin(angle) * dist

                val ball = Ball(
                    id = ballIdCounter++,
                    position = Vector2D(bx, by),
                    prevPosition = Vector2D(bx, by),
                    radius = 14.0f,
                    isDormant = isDormant,
                    color = if (isDormant) Ball.COLOR_DORMANT else Ball.randomColor()
                )
                physicsEngine.addBall(ball)
            }
        }
    }

    private fun onBallReachedTruck(ball: Ball) {
        collectedCount++
        if (remainingQuota > 0) {
            remainingQuota--
        }

        // Play drop sound and bounce truck suspension
        audioManager.playTruckDrop()
        truckBounceVelocity += 18.0f

        val level = currentLevel ?: return
        if (collectedCount >= level.quota && !isVictory) {
            triggerVictory()
        }
    }

    private fun triggerVictory() {
        val level = currentLevel ?: return
        isVictory = true

        val totalBalls = if (level.totalInitialBalls > 0) level.totalInitialBalls else 1
        val ratio = collectedCount.toFloat() / totalBalls.toFloat()
        earnedStars = if (ratio >= 0.85f) {
            3
        } else if (ratio >= 0.65f) {
            2
        } else {
            1
        }

        progressManager.setStarsForLevel(level.levelId, earnedStars)

        // Play round victory celebration fanfare + friendly truck horn!
        audioManager.playVictory()
        audioManager.playTruckHorn()

        // Spawn colorful rainbow confetti
        for (i in 0 until 80) {
            confettiParticles.add(
                Confetti(
                    x = 500.0f + (Random.nextFloat() - 0.5f) * 250.0f,
                    y = 220.0f,
                    vx = (Random.nextFloat() - 0.5f) * 700.0f,
                    vy = 350.0f + Random.nextFloat() * 600.0f,
                    color = Ball.randomColor(),
                    rotation = Random.nextFloat() * 360.0f,
                    vRot = (Random.nextFloat() - 0.5f) * 900.0f
                )
            )
        }

        // Spawn floating star particles
        for (i in 0 until 24) {
            floatingStars.add(
                CelebrationStar(
                    x = 500.0f + (Random.nextFloat() - 0.5f) * 300.0f,
                    y = 200.0f + Random.nextFloat() * 100.0f,
                    vx = (Random.nextFloat() - 0.5f) * 300.0f,
                    vy = 150.0f + Random.nextFloat() * 400.0f,
                    scale = 0.8f + Random.nextFloat() * 0.7f
                )
            )
        }
    }

    fun update(dt: Float) {
        if (sceneManager.currentState != GameState.PLAYING) return

        // Step physics with sand terrain collision sub-stepping
        physicsEngine.step(dt, subSteps = 6, sandTerrain = sandTerrain)

        // Update ball animations & cartoon facial expressions
        for (i in 0 until physicsEngine.balls.size) {
            val b = physicsEngine.balls[i]
            b.updateAnimation(dt)
        }

        // Auto-advance to next harder level after round victory celebration
        if (isVictory) {
            victoryTimer += dt
            if (victoryTimer >= autoAdvanceDelay && !hasTriggeredAutoAdvance) {
                hasTriggeredAutoAdvance = true
                advanceToNextLevel()
            }
        }

        // Update truck suspension bounce (spring damper)
        truckBounceVelocity -= truckBounceY * 30.0f * dt
        truckBounceVelocity *= 0.88f
        truckBounceY += truckBounceVelocity * dt
        if (kotlin.math.abs(truckBounceY) < 0.1f && kotlin.math.abs(truckBounceVelocity) < 0.1f) {
            truckBounceY = 0.0f
        }

        // Update confetti
        if (confettiParticles.isNotEmpty()) {
            val iter = confettiParticles.iterator()
            while (iter.hasNext()) {
                val c = iter.next()
                c.x += c.vx * dt
                c.y += c.vy * dt
                c.vy -= 750.0f * dt
                c.rotation += c.vRot * dt
                c.alpha -= 0.32f * dt
                if (c.alpha <= 0.0f || c.y < 0.0f) {
                    iter.remove()
                }
            }
        }

        // Update floating celebration stars
        if (floatingStars.isNotEmpty()) {
            val iter = floatingStars.iterator()
            while (iter.hasNext()) {
                val s = iter.next()
                s.x += s.vx * dt
                s.y += s.vy * dt
                s.vy -= 200.0f * dt
                s.alpha -= 0.35f * dt
                if (s.alpha <= 0.0f) {
                    iter.remove()
                }
            }
        }
    }
}
