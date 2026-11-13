package com.cometcourier.run.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometcourier.run.core.config.GameConfig
import com.cometcourier.run.domain.repository.ProgressRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SplashViewModel(progress: ProgressRepository) : ViewModel() {
    private val mutable = MutableStateFlow(SplashUiState())
    val state = mutable.asStateFlow()
    private val _finished = MutableStateFlow(false)
    val finished = _finished.asStateFlow()
    init {
        viewModelScope.launch {
            delay(GameConfig.LOADER_DURATION_MS)
            _finished.value = true
            mutable.update { it.copy(isFinished = true) }
        }
        viewModelScope.launch { progress.state.collect { p -> mutable.update { it.copy(reducedMotion = p.profile?.settings?.reducedMotion ?: false) } } }
    }
}
