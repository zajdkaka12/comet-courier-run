package com.cometcourier.run.domain.repository

import com.cometcourier.run.domain.model.*
import kotlinx.coroutines.flow.StateFlow

interface ProgressRepository {
    val state: StateFlow<ProgressState>
    suspend fun load()
    suspend fun retry()
    suspend fun checkpoint(snapshot: FlightSnapshot)
    suspend fun recordDelivery(event: DeliveryEvent, snapshot: FlightSnapshot)
    suspend fun finalize(snapshot: FlightSnapshot, reason: RunEndReason)
    suspend fun unlock(hull: CourierHull): Boolean
    suspend fun select(hullId: String)
    suspend fun claim(mission: DailyMission, date: String): Boolean
    suspend fun refreshDay(date: String)
    suspend fun settings(key: SettingKey, value: Boolean)
}
