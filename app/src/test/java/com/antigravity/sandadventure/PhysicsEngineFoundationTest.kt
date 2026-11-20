package com.antigravity.sandadventure

import com.antigravity.sandadventure.physics.Ball
import com.antigravity.sandadventure.physics.PhysicsEngine
import com.antigravity.sandadventure.physics.Vector2D
import org.junit.Assert.*
import org.junit.Test

class PhysicsEngineFoundationTest {

    @Test
    fun testPhysicsSimulationStabilityWith100Balls() {
        val engine = PhysicsEngine(1000.0f, 1800.0f)

        // Spawn 100 balls
        for (i in 1..100) {
            val b = Ball(
                id = i,
                position = Vector2D(400.0f + (i % 10) * 20.0f, 1500.0f + (i / 10) * 20.0f),
                radius = 12.0f
            )
            engine.addBall(b)
        }

        assertEquals(100, engine.balls.size)

        // Run 60 frames of physics simulation with 4 sub-steps each (240 sub-steps)
        for (f in 0 until 60) {
            engine.step(0.0166f, subSteps = 4)
        }

        // Verify all balls remain within valid world boundaries and positions are valid finite numbers
        for (b in engine.balls) {
            assertFalse("Ball position X must not be NaN", b.position.x.isNaN())
            assertFalse("Ball position Y must not be NaN", b.position.y.isNaN())
            assertTrue("Ball position X must be inside world boundaries", b.position.x in 0.0f..1000.0f)
            assertTrue("Ball position Y must be >= 0", b.position.y >= 0.0f)
        }
    }

    @Test
    fun testDormantBallConversionOnContact() {
        val engine = PhysicsEngine(1000.0f, 1800.0f)

        // Active colored ball falling downward
        val activeBall = Ball(
            id = 1,
            position = Vector2D(500.0f, 1030.0f),
            radius = 15.0f,
            isDormant = false,
            color = 0xFFFF4757.toInt()
        )
        activeBall.velocity.set(0.0f, -100.0f)

        // Dormant white ball at resting position
        val dormantBall = Ball(
            id = 2,
            position = Vector2D(500.0f, 1000.0f),
            radius = 15.0f,
            isDormant = true,
            color = Ball.COLOR_DORMANT
        )

        engine.addBall(activeBall)
        engine.addBall(dormantBall)

        var activated = false
        engine.onBallActivated = { ball ->
            if (ball.id == 2) {
                activated = true
            }
        }

        // Step physics to induce collision
        for (i in 0 until 5) {
            engine.step(0.0166f, subSteps = 4)
        }

        assertTrue("Dormant ball must be converted to active upon collision", activated)
        assertFalse("Dormant ball flag must be false", dormantBall.isDormant)
        assertNotEquals(Ball.COLOR_DORMANT, dormantBall.color)
    }

    @Test
    fun testTruckCollectionDetection() {
        val engine = PhysicsEngine(1000.0f, 1800.0f)

        val ball = Ball(
            id = 1,
            position = Vector2D(500.0f, 100.0f), // Inside truck zone (X: 340-660, Y: 40-160)
            radius = 14.0f,
            isDormant = false
        )
        engine.addBall(ball)

        var collected = false
        engine.onBallCollected = { b ->
            collected = true
        }

        engine.step(0.0166f, subSteps = 1)

        assertTrue("Ball inside truck zone must be collected", collected)
        assertTrue("Ball isCollected flag must be true", ball.isCollected)
    }

    @Test
    fun testBallSupportedBySolidSandAndFallsWhenCarved() {
        val engine = PhysicsEngine(1000.0f, 1800.0f)
        val sand = com.antigravity.sandadventure.terrain.SandTerrain(1000.0f, 1800.0f)
        sand.reset(sandBottom = 260.0f, sandTop = 1750.0f)

        // Carve pocket from y=1450 to 1600 (bottom edge of carved pocket is at y=1390)
        sand.carveCapsule(500.0f, 1600.0f, 500.0f, 1450.0f, 60.0f)

        val ball = Ball(
            id = 1,
            position = Vector2D(500.0f, 1460.0f),
            radius = 14.0f,
            isDormant = false
        )
        engine.addBall(ball)

        // Run 30 frames with gravity (-1200). Solid sand underneath must support the ball!
        for (f in 0 until 30) {
            engine.step(0.0166f, subSteps = 6, sandTerrain = sand)
        }

        assertTrue("Ball must be supported by solid sand and not fall through", ball.position.y >= 1380.0f)

        // Now carve a tunnel through the sand underneath the ball
        sand.carveCapsule(500.0f, 1450.0f, 500.0f, 1000.0f, 60.0f)

        // Run another 30 frames
        for (f in 0 until 30) {
            engine.step(0.0166f, subSteps = 6, sandTerrain = sand)
        }

        assertTrue("Ball must fall down through carved tunnel", ball.position.y < 1350.0f)
    }
}
