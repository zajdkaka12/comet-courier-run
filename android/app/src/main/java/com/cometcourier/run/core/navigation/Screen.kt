package com.cometcourier.run.core.navigation

sealed interface Screen {
    data object Splash : Screen
    data object Menu : Screen
    data class Game(val runId: String) : Screen
    data class GameOver(val resultId: String) : Screen
    data object HullShop : Screen
    data object Missions : Screen
    data object Settings : Screen
}
sealed interface UiEvent {
    data class Navigate(val screen: Screen) : UiEvent
    data class Start(val runId: String) : UiEvent
    data object Menu : UiEvent
    data object Back : UiEvent
    data object Exit : UiEvent
    data object Haptic : UiEvent
}
