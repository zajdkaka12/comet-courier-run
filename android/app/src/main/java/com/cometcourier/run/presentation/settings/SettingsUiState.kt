package com.cometcourier.run.presentation.settings

import com.cometcourier.run.domain.model.CourierSettings
import com.cometcourier.run.domain.model.SettingKey

data class SettingsUiState(val settings: CourierSettings = CourierSettings(), val loading: Boolean = true, val readError: Boolean = false, val saveError: Boolean = false, val savingKeys: Set<SettingKey> = emptySet(), val showRules: Boolean = false)
