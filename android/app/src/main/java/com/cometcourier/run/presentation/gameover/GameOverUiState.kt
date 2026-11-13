package com.cometcourier.run.presentation.gameover

import com.cometcourier.run.domain.model.*

data class GameOverUiState(val result: RunResult? = null, val profile: PlayerProfile? = null, val loading: Boolean = true, val saveError: Boolean = false)
