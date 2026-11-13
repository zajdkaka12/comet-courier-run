package com.cometcourier.run.presentation.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometcourier.run.core.navigation.Screen
import com.cometcourier.run.core.navigation.UiEvent
import com.cometcourier.run.domain.repository.ProgressRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AppViewModel(private val progress: ProgressRepository) : ViewModel() {
    private val current = MutableStateFlow<Screen>(Screen.Splash)
    val screen = current.asStateFlow()
    private val backStack = mutableListOf<Screen>()
    private val channel = Channel<UiEvent>(Channel.BUFFERED)
    val events = channel.receiveAsFlow()

    init { viewModelScope.launch { progress.load() } }

    fun finishSplash() {
        if (current.value == Screen.Splash) current.value = Screen.Menu
    }

    fun navigate(target: Screen) {
        if (current.value == target || current.value == Screen.Splash) return
        backStack.add(current.value)
        current.value = target
    }

    fun start(runId: String) {
        backStack.clear()
        backStack.add(Screen.Menu)
        current.value = Screen.Game(runId)
    }

    fun menu() { backStack.clear(); current.value = Screen.Menu }

    fun back() {
        if (current.value == Screen.Splash) return
        if (current.value == Screen.Menu) viewModelScope.launch { channel.send(UiEvent.Exit) }
        else current.value = backStack.removeLastOrNull() ?: Screen.Menu
    }
}
