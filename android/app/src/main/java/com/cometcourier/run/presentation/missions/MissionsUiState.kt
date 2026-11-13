package com.cometcourier.run.presentation.missions

import com.cometcourier.run.domain.model.*

data class MissionsUiState(val profile: PlayerProfile? = null, val missions: List<DailyMission> = emptyList(), val date: String = "", val loading: Boolean = true, val readError: Boolean = false, val saveError: Boolean = false, val pendingId: String? = null)
