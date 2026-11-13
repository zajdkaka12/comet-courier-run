package com.cometcourier.run.presentation.gameover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometcourier.run.core.navigation.*
import com.cometcourier.run.domain.repository.ProgressRepository
import java.util.UUID
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GameOverViewModel(private val progress: ProgressRepository) : ViewModel() {
    private val mutable = MutableStateFlow(GameOverUiState())
    val state = mutable.asStateFlow()
    private var resultId: String? = null
    private var starting = false
    private val channel = Channel<UiEvent>(Channel.BUFFERED)
    val events = channel.receiveAsFlow()
    init { viewModelScope.launch { progress.state.collect { refresh() } } }
    fun show(id: String) { resultId = id; starting = false; refresh() }
    private fun refresh() {
        val p = progress.state.value
        val available = p.pending ?: p.profile
        mutable.update { it.copy(result = available?.results?.find { r -> r.id == resultId }, profile = p.profile, loading = p.loading, saveError = p.saveError) }
    }
    fun again() {
        if (starting || mutable.value.saveError || mutable.value.result == null) return
        starting = true
        viewModelScope.launch { channel.send(UiEvent.Start(UUID.randomUUID().toString())) }
    }
    fun open(screen: Screen) { viewModelScope.launch { channel.send(UiEvent.Navigate(screen)) } }
    fun menu() { viewModelScope.launch { channel.send(UiEvent.Menu) } }
    fun retry() { viewModelScope.launch { progress.retry() } }
}
