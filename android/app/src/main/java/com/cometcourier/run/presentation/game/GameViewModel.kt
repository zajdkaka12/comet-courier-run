package com.cometcourier.run.presentation.game

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometcourier.run.core.config.GameConfig
import com.cometcourier.run.core.navigation.*
import com.cometcourier.run.domain.feedback.FeedbackPlayer
import com.cometcourier.run.domain.game.FlightEngine
import com.cometcourier.run.domain.model.*
import com.cometcourier.run.domain.repository.ProgressRepository
import com.cometcourier.run.domain.usecase.*
import java.time.LocalDate
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameViewModel(private val progress: ProgressRepository, private val record: RecordDeliveryUseCase, private val finalize: FinalizeRunUseCase, private val feedback: FeedbackPlayer) : ViewModel() {
    private val mutable = MutableStateFlow(GameUiState())
    val state = mutable.asStateFlow()
    private val channel = Channel<UiEvent>(Channel.BUFFERED)
    val events = channel.receiveAsFlow()
    private var engine: FlightEngine? = null
    private var loop: Job? = null
    private var finishing: Job? = null
    private var checkpointAt = 0.0
    init { viewModelScope.launch { progress.state.collect { p ->
        mutable.update { it.copy(saveError = p.saveError, settings = p.profile?.settings ?: CourierSettings()) }
        if (p.saveError) pause()
    } } }

    fun start(runId: String) {
        if (engine?.snapshot?.runId == runId) return
        loop?.cancel()
        val profile = progress.state.value.profile ?: return
        engine = FlightEngine(FlightSnapshot(runId, profile.selected, runId.hashCode().toLong()))
        checkpointAt = 0.0
        mutable.update { it.copy(snapshot = engine?.snapshot, phase = FlightPhase.Preparing, pauseDialog = false, announcedDistance = 0) }
        viewModelScope.launch {
            engine?.snapshot?.let { progress.checkpoint(it) }
            if (mutable.value.phase == FlightPhase.Preparing) {
                if (!progress.state.value.saveError) resume() else mutable.update { it.copy(phase = FlightPhase.Paused) }
            }
        }
    }

    fun resume() {
        if (mutable.value.phase == FlightPhase.Completed || mutable.value.phase == FlightPhase.Finishing || mutable.value.saveError || engine == null) return
        if (loop?.isActive == true) return
        mutable.update { it.copy(phase = FlightPhase.Running, pauseDialog = false) }
        loop = viewModelScope.launch {
            var last = SystemClock.elapsedRealtimeNanos()
            var accumulator = 0.0
            while (isActive && mutable.value.phase == FlightPhase.Running) {
                delay(16)
                val now = SystemClock.elapsedRealtimeNanos()
                val delta = (now - last) / 1_000_000_000.0
                last = now
                if (delta > 0.25) { pause(); break }
                accumulator += delta
                var steps = 0
                while (accumulator >= GameConfig.STEP_SECONDS && steps < 4 && mutable.value.phase == FlightPhase.Running) {
                    accumulator -= GameConfig.STEP_SECONDS
                    steps++
                    val e = engine ?: break
                    val old = e.snapshot
                    val s = e.step(GameConfig.STEP_SECONDS, LocalDate.now().toString())
                    mutable.update { it.copy(snapshot = s, announcedDistance = if (s.elapsed.toInt() != old.elapsed.toInt()) s.distance.toInt() else it.announcedDistance) }
                    if (s.statusSequence != old.statusSequence) {
                        if (s.status == FlightStatus.Parcel || s.status == FlightStatus.Delivery) {
                            if (mutable.value.settings.sound) feedback.play(s.status == FlightStatus.Delivery)
                        }
                        if (mutable.value.settings.haptics && (s.status == FlightStatus.Delivery || s.status == FlightStatus.ShieldHit)) channel.send(UiEvent.Haptic)
                    }
                    if (s.deliverySequence > old.deliverySequence) s.deliveryEvents.filter { it.sequence > old.deliverySequence }.forEach { record(it, s) }
                    else if (s.cargo != old.cargo || s.shield != old.shield || s.elapsed - checkpointAt >= 5.0) {
                        checkpointAt = s.elapsed
                        progress.checkpoint(s)
                    }
                    if (s.ended) { finish(RunEndReason.Collision); break }
                }
                accumulator = accumulator.coerceAtMost(GameConfig.STEP_SECONDS * 4)
            }
        }
    }

    fun move(direction: Int) {
        if (mutable.value.phase != FlightPhase.Running) return
        if (engine?.move(direction) == true) {
            mutable.update { it.copy(snapshot = engine?.snapshot) }
            if (mutable.value.settings.haptics) viewModelScope.launch { channel.send(UiEvent.Haptic) }
        }
    }

    fun pause(showDialog: Boolean = false) {
        if (mutable.value.phase != FlightPhase.Running && mutable.value.phase != FlightPhase.Preparing) return
        loop?.cancel(); loop = null
        mutable.update { it.copy(phase = FlightPhase.Paused, pauseDialog = showDialog) }
        val s = engine?.snapshot
        if (s != null) viewModelScope.launch { progress.checkpoint(s) }
        feedback.release()
    }

    fun dismissPause() { mutable.update { it.copy(pauseDialog = false) } }
    fun finish(reason: RunEndReason = RunEndReason.Ended, destination: UiEvent? = null) {
        if (mutable.value.phase == FlightPhase.Completed) {
            viewModelScope.launch { channel.send(destination ?: UiEvent.Navigate(Screen.GameOver(engine?.snapshot?.runId ?: return@launch))) }
            return
        }
        if (finishing?.isActive == true) return
        loop?.cancel(); loop = null
        val s = engine?.snapshot ?: run { viewModelScope.launch { channel.send(destination ?: UiEvent.Menu) }; return }
        mutable.update { it.copy(phase = FlightPhase.Finishing, pauseDialog = false) }
        finishing = viewModelScope.launch {
            finalize(s, reason)
            mutable.update { it.copy(phase = FlightPhase.Completed) }
            feedback.release()
            channel.send(destination ?: UiEvent.Navigate(Screen.GameOver(s.runId)))
        }
    }
    fun leave(toMenu: Boolean) { finish(RunEndReason.Left, if (toMenu) UiEvent.Menu else UiEvent.Back) }
    fun summary() { engine?.snapshot?.let { s -> viewModelScope.launch { channel.send(UiEvent.Navigate(Screen.GameOver(s.runId))) } } }
    fun retry() { viewModelScope.launch { progress.retry() } }
    override fun onCleared() { feedback.release(); super.onCleared() }
}
