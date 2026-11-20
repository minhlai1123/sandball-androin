package com.antigravity.sandadventure.gameplay

enum class GameState {
    MENU,
    LEVEL_SELECT,
    PLAYING,
    LEVEL_COMPLETE,
    LEVEL_FAILED,
    SETTINGS
}

class SceneManager(
    initialState: GameState = GameState.MENU
) {
    var currentState: GameState = initialState
        private set

    var currentLevelId: Int = 1
        private set

    private val listeners = ArrayList<(GameState, Int) -> Unit>()

    fun addStateListener(listener: (GameState, Int) -> Unit) {
        listeners.add(listener)
    }

    fun transitionTo(newState: GameState, levelId: Int = currentLevelId) {
        currentState = newState
        currentLevelId = levelId
        for (i in 0 until listeners.size) {
            listeners[i].invoke(newState, levelId)
        }
    }

    fun startLevel(levelId: Int) {
        transitionTo(GameState.PLAYING, levelId)
    }

    fun completeLevel() {
        transitionTo(GameState.LEVEL_COMPLETE, currentLevelId)
    }

    fun failLevel() {
        transitionTo(GameState.LEVEL_FAILED, currentLevelId)
    }

    fun openMenu() {
        transitionTo(GameState.MENU)
    }

    fun openLevelSelect() {
        transitionTo(GameState.LEVEL_SELECT)
    }

    fun openSettings() {
        transitionTo(GameState.SETTINGS)
    }
}
