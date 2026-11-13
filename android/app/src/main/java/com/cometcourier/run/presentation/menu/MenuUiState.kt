package com.cometcourier.run.presentation.menu

import com.cometcourier.run.domain.model.PlayerProfile
import com.cometcourier.run.domain.model.DailyMission

data class MenuUiState(val profile: PlayerProfile? = null, val loading: Boolean = true, val readError: Boolean = false, val saveError: Boolean = false, val starting: Boolean = false, val dailyMission: DailyMission? = null, val dailyCount: Int = 0)
