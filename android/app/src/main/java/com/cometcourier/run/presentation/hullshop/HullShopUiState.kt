package com.cometcourier.run.presentation.hullshop

import com.cometcourier.run.domain.model.*

data class HullShopUiState(val profile: PlayerProfile? = null, val catalog: List<CourierHull> = emptyList(), val loading: Boolean = true, val readError: Boolean = false, val saveError: Boolean = false, val pendingHullId: String? = null)
