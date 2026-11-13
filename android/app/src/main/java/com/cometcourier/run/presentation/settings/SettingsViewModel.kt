package com.cometcourier.run.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometcourier.run.domain.model.CourierSettings
import com.cometcourier.run.domain.model.SettingKey
import com.cometcourier.run.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(private val progress: ProgressRepository) : ViewModel() {
    private val mutable = MutableStateFlow(SettingsUiState())
    val state = mutable.asStateFlow()
    init { viewModelScope.launch { progress.state.collect { p -> mutable.update { it.copy(settings = p.profile?.settings ?: CourierSettings(), loading = p.loading, readError = p.readError, saveError = p.saveError) } } } }
    fun change(settings: CourierSettings) {
        val state = mutable.value
        if (state.saveError || state.loading || state.readError) return
        val old = state.settings
        val change = when {
            settings.buttonsOnly != old.buttonsOnly -> SettingKey.ButtonsOnly to settings.buttonsOnly
            settings.sound != old.sound -> SettingKey.Sound to settings.sound
            settings.haptics != old.haptics -> SettingKey.Haptics to settings.haptics
            settings.reducedMotion != old.reducedMotion -> SettingKey.ReducedMotion to settings.reducedMotion
            else -> return
        }
        if (change.first in state.savingKeys) return
        mutable.update { it.copy(savingKeys = it.savingKeys + change.first) }
        viewModelScope.launch {
            progress.settings(change.first, change.second)
            mutable.update { it.copy(savingKeys = it.savingKeys - change.first) }
        }
    }
    fun rules(show: Boolean) { mutable.update { it.copy(showRules = show) } }
    fun retry() { viewModelScope.launch { progress.retry() } }
}
