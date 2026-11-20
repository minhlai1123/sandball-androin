package com.antigravity.sandadventure

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.antigravity.sandadventure.data.ProgressManager
import com.antigravity.sandadventure.gameplay.AudioManager
import com.antigravity.sandadventure.gameplay.GameEngine
import com.antigravity.sandadventure.gameplay.GameState
import com.antigravity.sandadventure.gameplay.SceneManager
import com.antigravity.sandadventure.ui.GameScreen
import com.antigravity.sandadventure.ui.LevelSelectScreen
import com.antigravity.sandadventure.ui.MainMenuScreen
import com.antigravity.sandadventure.ui.SettingsScreen

class MainActivity : ComponentActivity() {

    private lateinit var progressManager: ProgressManager
    private lateinit var audioManager: AudioManager
    private lateinit var sceneManager: SceneManager
    private lateinit var gameEngine: GameEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        progressManager = ProgressManager(this)
        audioManager = AudioManager(this, progressManager)
        sceneManager = SceneManager(GameState.MENU)
        gameEngine = GameEngine(this, sceneManager, progressManager, audioManager)

        // Start playful menu music
        audioManager.playBgm("bgm_menu_kids.wav")

        setContent {
            var currentScreen by remember { mutableStateOf(GameState.MENU) }
            var activeLevelId by remember { mutableIntStateOf(1) }

            DisposableEffect(Unit) {
                sceneManager.addStateListener { state, levelId ->
                    currentScreen = state
                    activeLevelId = levelId

                    if (state == GameState.MENU || state == GameState.LEVEL_SELECT || state == GameState.SETTINGS) {
                        audioManager.playBgm("bgm_menu_kids.wav")
                    } else if (state == GameState.PLAYING) {
                        audioManager.playBgm("bgm_gameplay_kids.wav")
                    }
                }
                onDispose { }
            }

            when (currentScreen) {
                GameState.MENU -> {
                    MainMenuScreen(
                        progressManager = progressManager,
                        onPlay = {
                            audioManager.playButtonTap()
                            val targetLevel = progressManager.highestUnlockedLevel
                            sceneManager.startLevel(targetLevel)
                        },
                        onLevelSelect = {
                            audioManager.playButtonTap()
                            sceneManager.openLevelSelect()
                        },
                        onSettings = {
                            audioManager.playButtonTap()
                            sceneManager.openSettings()
                        }
                    )
                }

                GameState.LEVEL_SELECT -> {
                    LevelSelectScreen(
                        progressManager = progressManager,
                        onLevelSelected = { levelId ->
                            audioManager.playButtonTap()
                            sceneManager.startLevel(levelId)
                        },
                        onBack = {
                            audioManager.playButtonTap()
                            sceneManager.openMenu()
                        }
                    )
                }

                GameState.SETTINGS -> {
                    SettingsScreen(
                        progressManager = progressManager,
                        onBack = {
                            audioManager.playButtonTap()
                            sceneManager.openMenu()
                        }
                    )
                }

                GameState.PLAYING, GameState.LEVEL_COMPLETE, GameState.LEVEL_FAILED -> {
                    GameScreen(
                        gameEngine = gameEngine,
                        levelId = activeLevelId,
                        onMenu = {
                            audioManager.playButtonTap()
                            sceneManager.openMenu()
                        },
                        onNextLevel = { nextId ->
                            audioManager.playButtonTap()
                            if (nextId <= 100) {
                                sceneManager.startLevel(nextId)
                            } else {
                                sceneManager.openLevelSelect()
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::audioManager.isInitialized) {
            audioManager.resumeBgm()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::audioManager.isInitialized) {
            audioManager.pauseBgm()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::audioManager.isInitialized) {
            audioManager.release()
        }
    }
}
