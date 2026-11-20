package com.antigravity.sandadventure.physics

import com.antigravity.sandadventure.terrain.SandTerrain
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class PhysicsEngine(
    val worldWidth: Float = 1000.0f,
    val worldHeight: Float = 1800.0f
) {
    val balls = ArrayList<Ball>()
    val staticSegments = ArrayList<CollisionBody.Segment>()
    val rotatingPaddles = ArrayList<CollisionBody.RotatingPaddle>()
    val multiplierGates = ArrayList<CollisionBody.MultiplierGate>()
    val portalPairs = ArrayList<CollisionBody.PortalPair>()
    val bouncyBumpers = ArrayList<CollisionBody.BouncyBumper>()
    val tntCrates = ArrayList<CollisionBody.TntCrate>()

    private val pendingClones = ArrayList<Ball>()
    private var ballIdCounter = 1000

    val spatialGrid = SpatialHashGrid(worldWidth, worldHeight, cellSize = 45.0f)

    var gravity = Vector2D(0.0f, -1450.0f)
    var restitution = 0.16f
    var linearDamping = 0.996f

    // Callbacks
    var onBallActivated: ((Ball) -> Unit)? = null
    var onBallCollected: ((Ball) -> Unit)? = null
    var onBallCloned: ((Ball) -> Unit)? = null
    var onPortalUsed: (() -> Unit)? = null
    var onBumperHit: ((CollisionBody.BouncyBumper) -> Unit)? = null
    var onTntExploded: ((CollisionBody.TntCrate) -> Unit)? = null

    // Truck collection zone: [minX, maxX, minY, maxY]
    var truckZoneLeft: Float = 340.0f
    var truckZoneRight: Float = 660.0f
    var truckZoneTop: Float = 160.0f
    var truckZoneBottom: Float = 40.0f

    fun clear() {
        balls.clear()
        staticSegments.clear()
        rotatingPaddles.clear()
        multiplierGates.clear()
        portalPairs.clear()
        bouncyBumpers.clear()
        tntCrates.clear()
        pendingClones.clear()
        spatialGrid.clear()
    }

    fun addBall(ball: Ball) {
        balls.add(ball)
    }

    fun step(dt: Float, subSteps: Int = 6, sandTerrain: SandTerrain? = null) {
        if (balls.isEmpty()) return
        val subDt = dt / subSteps

        // Update dynamic obstacles
        for (paddle in rotatingPaddles) {
            paddle.update(dt)
        }
        for (bumper in bouncyBumpers) {
            if (bumper.flashTimer > 0f) bumper.flashTimer -= dt
        }

        for (s in 0 until subSteps) {
            integrateVerlet(subDt)
            updateSpatialGrid()
            resolveBallBallCollisions()
            if (sandTerrain != null) {
                resolveSandCollisions(sandTerrain)
            }
            resolveObstacleCollisions()
            if (sandTerrain != null) {
                resolveSandCollisions(sandTerrain)
            }
            resolveWorldBoundaries()
        }

        if (pendingClones.isNotEmpty()) {
            balls.addAll(pendingClones)
            pendingClones.clear()
        }

        checkTruckCollection()
    }

    private fun resolveSandCollisions(sandTerrain: SandTerrain) {
        for (i in 0 until balls.size) {
            val b = balls[i]
            if (b.isCollected || b.isDestroyed || b.isDormant) continue
            sandTerrain.resolveBallCollision(b)
        }
    }

    private fun integrateVerlet(dt: Float) {
        for (i in 0 until balls.size) {
            val b = balls[i]
            if (b.isCollected || b.isDestroyed || b.isDormant) continue

            // Integrate acceleration (gravity)
            b.velocity.x += gravity.x * dt
            b.velocity.y += gravity.y * dt

            // Apply damping
            b.velocity.x *= linearDamping
            b.velocity.y *= linearDamping

            // Store previous position
            b.prevPosition.set(b.position)

            // Update position
            b.position.x += b.velocity.x * dt
            b.position.y += b.velocity.y * dt
        }
    }

    private fun updateSpatialGrid() {
        spatialGrid.clear()
        for (i in 0 until balls.size) {
            val b = balls[i]
            if (!b.isCollected && !b.isDestroyed) {
                spatialGrid.insert(b)
            }
        }
    }

    private fun resolveBallBallCollisions() {
        for (i in 0 until balls.size) {
            val b1 = balls[i]
            if (b1.isCollected || b1.isDestroyed) continue

            spatialGrid.forEachNeighbor(b1) { b2 ->
                val dx = b2.position.x - b1.position.x
                val dy = b2.position.y - b1.position.y
                val distSq = dx * dx + dy * dy
                val minDist = b1.radius + b2.radius

                if (distSq < minDist * minDist && distSq > 0.0001f) {
                    val dist = sqrt(distSq)
                    val overlap = minDist - dist
                    val nx = dx / dist
                    val ny = dy / dist

                    // Infection propagation: colored ball meets dormant white ball
                    if (!b1.isDormant && b2.isDormant) {
                        b2.activate()
                        onBallActivated?.invoke(b2)
                    } else if (b1.isDormant && !b2.isDormant) {
                        b1.activate()
                        onBallActivated?.invoke(b1)
                    }

                    // Separation of overlapping balls
                    val weight1 = if (b1.isDormant) 0.0f else if (b2.isDormant) 1.0f else 0.5f
                    val weight2 = if (b2.isDormant) 0.0f else if (b1.isDormant) 1.0f else 0.5f

                    b1.position.x -= nx * overlap * weight1
                    b1.position.y -= ny * overlap * weight1
                    b2.position.x += nx * overlap * weight2
                    b2.position.y += ny * overlap * weight2

                    // Relative velocity impulse
                    val rvx = b2.velocity.x - b1.velocity.x
                    val rvy = b2.velocity.y - b1.velocity.y
                    val velAlongNormal = rvx * nx + rvy * ny

                    if (velAlongNormal < 0.0f) {
                        val impulse = -(1.0f + restitution) * velAlongNormal * 0.5f
                        b1.velocity.x -= nx * impulse
                        b1.velocity.y -= ny * impulse
                        b2.velocity.x += nx * impulse
                        b2.velocity.y += ny * impulse
                    }
                }
            }
        }
    }

    private fun resolveObstacleCollisions() {
        for (i in 0 until balls.size) {
            val b = balls[i]
            if (b.isCollected || b.isDestroyed || b.isDormant) continue

            // 1. Static segments
            for (j in 0 until staticSegments.size) {
                val seg = staticSegments[j]
                collideBallWithSegment(b, seg.p1.x, seg.p1.y, seg.p2.x, seg.p2.y, seg.restitution)
            }

            // 2. Rotating paddles
            for (j in 0 until rotatingPaddles.size) {
                val (ep1, ep2) = rotatingPaddles[j].getEndpoints()
                collideBallWithSegment(b, ep1.x, ep1.y, ep2.x, ep2.y, 0.35f)
            }

            // 3. Bouncy Bumpers (Pinball Mushroom)
            for (j in 0 until bouncyBumpers.size) {
                val bumper = bouncyBumpers[j]
                val dx = b.position.x - bumper.x
                val dy = b.position.y - bumper.y
                val distSq = dx * dx + dy * dy
                val minDist = bumper.radius + b.radius
                if (distSq < minDist * minDist && distSq > 0.0001f) {
                    val dist = sqrt(distSq)
                    val nx = dx / dist
                    val ny = dy / dist
                    val overlap = minDist - dist
                    b.position.x += nx * overlap
                    b.position.y += ny * overlap
                    // Launch ball outward with joyful bouncy impulse!
                    b.velocity.x = nx * 850.0f
                    b.velocity.y = ny * 850.0f
                    bumper.flashTimer = 0.22f
                    onBumperHit?.invoke(bumper)
                }
            }

            // 4. Portal Pairs (Teleporters)
            for (j in 0 until portalPairs.size) {
                val portal = portalPairs[j]
                val dx = b.position.x - portal.inX
                val dy = b.position.y - portal.inY
                val distSq = dx * dx + dy * dy
                if (distSq < portal.radius * portal.radius) {
                    // Teleport to portal exit with downward speed boost!
                    b.position.x = portal.outX + (kotlin.random.Random.nextFloat() - 0.5f) * 20.0f
                    b.position.y = portal.outY - 25.0f
                    if (b.velocity.y > -350.0f) b.velocity.y = -450.0f
                    onPortalUsed?.invoke()
                }
            }

            // 5. Multiplier Gates
            for (j in 0 until multiplierGates.size) {
                val gate = multiplierGates[j]
                if (kotlin.math.abs(b.position.x - gate.x) < gate.width / 2.0f &&
                    kotlin.math.abs(b.position.y - gate.y) < gate.height / 2.0f
                ) {
                    if (!b.passedGateIds.contains(gate.id)) {
                        b.passedGateIds.add(gate.id)
                        val spawnCount = (gate.multiplier - 1).coerceIn(1, 3)
                        for (m in 1..spawnCount) {
                            val clone = Ball(
                                id = ballIdCounter++,
                                position = Vector2D(b.position.x + (m * 9.0f), b.position.y),
                                prevPosition = Vector2D(b.position.x, b.position.y),
                                velocity = Vector2D(b.velocity.x + (m * 45.0f), b.velocity.y),
                                radius = b.radius,
                                color = Ball.randomColor(),
                                isDormant = false
                            )
                            clone.passedGateIds.add(gate.id)
                            pendingClones.add(clone)
                        }
                        onBallCloned?.invoke(b)
                    }
                }
            }

            // 6. TNT Toy Crates
            for (j in 0 until tntCrates.size) {
                val crate = tntCrates[j]
                if (!crate.isExploded) {
                    val halfS = crate.size / 2.0f + b.radius
                    if (kotlin.math.abs(b.position.x - crate.x) < halfS &&
                        kotlin.math.abs(b.position.y - crate.y) < halfS
                    ) {
                        crate.isExploded = true
                        onTntExploded?.invoke(crate)
                        // Repel nearby balls with blast wave
                        for (k in 0 until balls.size) {
                            val other = balls[k]
                            val bdx = other.position.x - crate.x
                            val bdy = other.position.y - crate.y
                            val bDist = sqrt(bdx * bdx + bdy * bdy)
                            if (bDist < 260.0f && bDist > 0.001f) {
                                val push = (260.0f - bDist) * 4.5f
                                other.velocity.x += (bdx / bDist) * push
                                other.velocity.y += (bdy / bDist) * push
                            }
                        }
                    }
                }
            }
        }
    }

    private fun collideBallWithSegment(
        b: Ball,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        segRestitution: Float
    ) {
        val segDx = x2 - x1
        val segDy = y2 - y1
        val segLenSq = segDx * segDx + segDy * segDy
        if (segLenSq < 0.0001f) return

        // Project ball center onto segment clamped to [0, 1]
        val t = max(0.0f, min(1.0f, ((b.position.x - x1) * segDx + (b.position.y - y1) * segDy) / segLenSq))
        val projX = x1 + t * segDx
        val projY = y1 + t * segDy

        val dx = b.position.x - projX
        val dy = b.position.y - projY
        val distSq = dx * dx + dy * dy

        if (distSq < b.radius * b.radius && distSq > 0.0001f) {
            val dist = sqrt(distSq)
            val overlap = b.radius - dist
            val nx = dx / dist
            val ny = dy / dist

            b.position.x += nx * overlap
            b.position.y += ny * overlap

            val velDotN = b.velocity.x * nx + b.velocity.y * ny
            if (velDotN < 0.0f) {
                val impulse = -(1.0f + segRestitution) * velDotN
                b.velocity.x += nx * impulse
                b.velocity.y += ny * impulse
            }
        }
    }

    private fun resolveWorldBoundaries() {
        val margin = 20.0f
        for (i in 0 until balls.size) {
            val b = balls[i]
            if (b.isCollected || b.isDestroyed || b.isDormant) continue

            // Left boundary
            if (b.position.x - b.radius < margin) {
                b.position.x = margin + b.radius
                if (b.velocity.x < 0.0f) b.velocity.x *= -restitution
            }
            // Right boundary
            if (b.position.x + b.radius > worldWidth - margin) {
                b.position.x = worldWidth - margin - b.radius
                if (b.velocity.x > 0.0f) b.velocity.x *= -restitution
            }
            // Bottom floor (outside truck)
            if (b.position.y - b.radius < 0.0f) {
                b.position.y = b.radius
                b.velocity.y = 0.0f
                b.velocity.x *= 0.8f
            }
        }
    }

    private fun checkTruckCollection() {
        for (i in 0 until balls.size) {
            val b = balls[i]
            if (!b.isCollected && !b.isDestroyed && !b.isDormant) {
                if (b.position.x in truckZoneLeft..truckZoneRight &&
                    b.position.y in truckZoneBottom..truckZoneTop
                ) {
                    b.isCollected = true
                    b.velocity.set(0.0f, 0.0f)
                    onBallCollected?.invoke(b)
                }
            }
        }
    }
}
