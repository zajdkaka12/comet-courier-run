package com.cometcourier.run.domain.usecase

import com.cometcourier.run.domain.model.*
import com.cometcourier.run.domain.repository.ProgressRepository

class RecordDeliveryUseCase(private val progress: ProgressRepository) {
    suspend operator fun invoke(event: DeliveryEvent, snapshot: FlightSnapshot) = progress.recordDelivery(event, snapshot)
}
class FinalizeRunUseCase(private val progress: ProgressRepository) {
    suspend operator fun invoke(snapshot: FlightSnapshot, reason: RunEndReason) = progress.finalize(snapshot, reason)
}
class UnlockHullUseCase(private val progress: ProgressRepository) {
    suspend operator fun invoke(hull: CourierHull) = progress.unlock(hull)
}
class ClaimMissionUseCase(private val progress: ProgressRepository) {
    suspend operator fun invoke(mission: DailyMission, date: String) = progress.claim(mission, date)
}
