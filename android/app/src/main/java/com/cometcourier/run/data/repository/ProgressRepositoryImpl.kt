package com.cometcourier.run.data.repository

import com.cometcourier.run.data.local.SharedPreferencesStore
import com.cometcourier.run.domain.game.ProgressRules
import com.cometcourier.run.domain.model.*
import com.cometcourier.run.domain.repository.ProgressRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

class ProgressRepositoryImpl(private val store: SharedPreferencesStore) : ProgressRepository {
    private val mutable = MutableStateFlow(ProgressState())
    override val state = mutable.asStateFlow()
    private val mutex = Mutex()

    override suspend fun load(): Unit = mutex.withLock {
        if (mutable.value.profile != null) return@withLock
        mutable.update { it.copy(loading = true, readError = false) }
        runCatching { store.read() }.onSuccess { loaded ->
            var p = loaded
            loaded.active?.let { p = ProgressRules.finalize(p, it, RunEndReason.Interrupted) }
            p = ProgressRules.day(p, LocalDate.now().toString())
            mutable.update { it.copy(profile = loaded, loading = false, readError = false) }
            if (p != loaded) persist(p.copy(revision = loaded.revision + 1))
        }.onFailure { mutable.update { it.copy(loading = false, readError = true) } }
    }

    private suspend fun persist(p: PlayerProfile): Boolean = withContext(NonCancellable) {
        val previous = mutable.value.profile
        if (store.write(p, previous)) {
            mutable.update { it.copy(profile = p, pending = null, saveError = false, loading = false) }
            true
        } else {
            mutable.update { it.copy(pending = p, saveError = true, loading = false) }
            false
        }
    }

    private suspend fun mutate(allowPending: Boolean = false, block: (PlayerProfile) -> PlayerProfile): Boolean = mutex.withLock {
        val state = mutable.value
        if (state.saveError && !allowPending) return@withLock false
        val p = state.pending ?: state.profile ?: return@withLock false
        val next = block(p)
        if (next == p) return@withLock true
        persist(next.copy(revision = p.revision + 1))
    }

    override suspend fun retry() {
        if (mutable.value.readError) load()
        else mutex.withLock { mutable.value.pending?.let { persist(it) } }
    }

    override suspend fun checkpoint(snapshot: FlightSnapshot) {
        mutate(true) { p ->
            if (snapshot.runId in p.finalized || (p.active?.runId == snapshot.runId && p.active.elapsed > snapshot.elapsed)) p
            else p.copy(active = snapshot)
        }
    }

    override suspend fun recordDelivery(event: DeliveryEvent, snapshot: FlightSnapshot) {
        mutate(true) { p ->
            if (snapshot.runId in p.finalized) p
            else {
                val latest = p.active?.takeIf { it.runId == snapshot.runId && it.elapsed > snapshot.elapsed } ?: snapshot
                ProgressRules.delivery(p, event).copy(active = latest)
            }
        }
    }

    override suspend fun finalize(snapshot: FlightSnapshot, reason: RunEndReason) {
        mutate(true) { ProgressRules.finalize(it, snapshot, reason) }
    }

    override suspend fun unlock(hull: CourierHull): Boolean = mutate { ProgressRules.unlock(it, hull) }

    override suspend fun select(hullId: String) { mutate { if (hullId in it.owned) it.copy(selected = hullId) else it } }

    override suspend fun claim(mission: DailyMission, date: String): Boolean = mutate { ProgressRules.claim(it, mission, LocalDate.now().toString(), date) }

    override suspend fun refreshDay(date: String) { mutate { ProgressRules.day(it, date) } }
    override suspend fun settings(key: SettingKey, value: Boolean) {
        mutate { p -> p.copy(settings = when (key) {
            SettingKey.ButtonsOnly -> p.settings.copy(buttonsOnly = value)
            SettingKey.Sound -> p.settings.copy(sound = value)
            SettingKey.Haptics -> p.settings.copy(haptics = value)
            SettingKey.ReducedMotion -> p.settings.copy(reducedMotion = value)
        }) }
    }
}
