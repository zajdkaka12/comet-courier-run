package com.cometcourier.run.presentation.game

import com.cometcourier.run.domain.model.*

enum class FlightPhase { Preparing, Running, Paused, Finishing, Completed }
data class GameUiState(val snapshot: FlightSnapshot? = null, val phase: FlightPhase = FlightPhase.Preparing, val pauseDialog: Boolean = false, val saveError: Boolean = false, val settings: CourierSettings = CourierSettings(), val announcedDistance: Int = 0)
