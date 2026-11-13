package com.cometcourier.run.presentation.missions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometcourier.run.domain.model.DailyMission
import com.cometcourier.run.domain.repository.ProgressRepository
import com.cometcourier.run.domain.usecase.ClaimMissionUseCase
import java.time.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MissionsViewModel(private val progress: ProgressRepository, private val claim: ClaimMissionUseCase, catalog: List<DailyMission>) : ViewModel() {
    private val mutable = MutableStateFlow(MissionsUiState(missions = catalog))
    val state = mutable.asStateFlow()
    private var midnight: Job? = null
    init { viewModelScope.launch { progress.state.collect { p -> mutable.update { it.copy(profile = p.profile, loading = p.loading, readError = p.readError, saveError = p.saveError) } } } }
    fun visible() {
        midnight?.cancel()
        midnight = viewModelScope.launch {
            while (isActive) {
                val now = ZonedDateTime.now()
                val date = now.toLocalDate().toString()
                progress.refreshDay(date)
                mutable.update { it.copy(date = date) }
                val next = now.toLocalDate().plusDays(1).atStartOfDay(now.zone)
                delay(Duration.between(now, next).toMillis().coerceAtLeast(1))
            }
        }
    }
    fun hidden() { midnight?.cancel(); midnight = null }
    fun claim(id: String) {
        if (mutable.value.pendingId != null || mutable.value.saveError) return
        val mission = mutable.value.missions.find { it.id == id } ?: return
        mutable.update { it.copy(pendingId = id) }
        viewModelScope.launch {
            claim(mission, mutable.value.date)
            mutable.update { it.copy(pendingId = null) }
        }
    }
    fun retry() { viewModelScope.launch { progress.retry() } }
}
