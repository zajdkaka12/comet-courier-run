package com.cometcourier.run.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometcourier.run.core.navigation.*
import com.cometcourier.run.domain.repository.ProgressRepository
import com.cometcourier.run.domain.model.DailyMission
import java.util.UUID
import java.time.LocalDate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MenuViewModel(private val progress: ProgressRepository, private val missions: List<DailyMission>) : ViewModel() {
    private val mutable = MutableStateFlow(MenuUiState())
    val state = mutable.asStateFlow()
    private val channel = Channel<UiEvent>(Channel.BUFFERED)
    val events = channel.receiveAsFlow()
    init { viewModelScope.launch { progress.state.collect { p ->
        val mission = missions.firstOrNull { it.id !in (p.profile?.mission?.claimed ?: emptySet()) }
        val count = if (mission?.countsDockings == true) p.profile?.mission?.dockings ?: 0 else p.profile?.mission?.parcels ?: 0
        mutable.update { it.copy(profile = p.profile, loading = p.loading, readError = p.readError, saveError = p.saveError, dailyMission = mission, dailyCount = count) }
    } } }
    fun visible() { mutable.update { it.copy(starting = false) }; viewModelScope.launch { progress.refreshDay(LocalDate.now().toString()) } }
    fun play() {
        val s = mutable.value
        if (s.profile == null || s.loading || s.readError || s.saveError || s.starting) return
        mutable.update { it.copy(starting = true) }
        viewModelScope.launch { channel.send(UiEvent.Start(UUID.randomUUID().toString())) }
    }
    fun open(screen: Screen) { viewModelScope.launch { channel.send(UiEvent.Navigate(screen)) } }
    fun cycle(direction: Int) { viewModelScope.launch {
        val p = mutable.value.profile ?: return@launch
        val owned = listOf("sprout", "manta", "cherry", "nova").filter { it in p.owned }
        progress.select(owned[(owned.indexOf(p.selected) + direction + owned.size) % owned.size])
    } }
    fun retry() { viewModelScope.launch { progress.retry() } }
}
