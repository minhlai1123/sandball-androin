package com.antigravity.sandadventure.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.antigravity.sandadventure.gameplay.GameEngine
import com.antigravity.sandadventure.gameplay.GameSurfaceView

@Composable
fun GameScreen(
    gameEngine: GameEngine,
    levelId: Int,
    onMenu: () -> Unit,
    onNextLevel: (Int) -> Unit
) {
    // Whenever levelId changes, ensure gameEngine loads the target level
    LaunchedEffect(levelId) {
        if (gameEngine.currentLevelId != levelId || gameEngine.currentLevel == null) {
            gameEngine.loadLevel(levelId)
        }
    }

    // Auto-advance callback from GameEngine
    DisposableEffect(gameEngine, levelId) {
        gameEngine.onLevelCompleted = { nextId ->
            onNextLevel(nextId)
        }
        onDispose {
            gameEngine.onLevelCompleted = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // High-performance native SurfaceView
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                GameSurfaceView(context, gameEngine).apply {
                    if (gameEngine.currentLevel == null) {
                        gameEngine.loadLevel(levelId)
                    }
                }
            },
            update = { _ ->
                // Level updates handled directly via gameEngine.loadLevel
            }
        )

        // Compose HUD overlay
        GameHUD(
            gameEngine = gameEngine,
            onRestart = {
                gameEngine.loadLevel(gameEngine.currentLevelId)
            },
            onMenu = onMenu,
            onNextLevel = {
                onNextLevel(gameEngine.currentLevelId)
            }
        )
    }
}
