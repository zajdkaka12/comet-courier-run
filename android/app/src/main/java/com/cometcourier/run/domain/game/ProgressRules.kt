package com.cometcourier.run.domain.game

import com.cometcourier.run.domain.model.*
import kotlin.math.floor

object ProgressRules {
    fun day(profile: PlayerProfile, date: String): PlayerProfile = if (profile.mission.date == date) profile else profile.copy(mission = MissionProgress(date))

    fun delivery(profile: PlayerProfile, event: DeliveryEvent): PlayerProfile {
        val key = "${event.runId}:${event.sequence}"
        if (key in profile.deliveryLedger || event.runId in profile.finalized || event.parcels !in 1..3 || event.sequence < 1) return profile
        val p = day(profile, event.date)
        return p.copy(points = p.points + event.parcels * 10, lifetimeDeliveries = p.lifetimeDeliveries + event.parcels,
            deliveryLedger = p.deliveryLedger + key,
            mission = p.mission.copy(parcels = p.mission.parcels + event.parcels, dockings = p.mission.dockings + 1))
    }

    fun unlock(profile: PlayerProfile, hull: CourierHull): PlayerProfile =
        if (hull.id in profile.owned || hull.price < 0 || profile.points < hull.price) profile
        else profile.copy(points = profile.points - hull.price, owned = profile.owned + hull.id, selected = hull.id)

    fun claim(profile: PlayerProfile, mission: DailyMission, today: String, requestedDate: String): PlayerProfile {
        val p = day(profile, today)
        val count = if (mission.countsDockings) p.mission.dockings else p.mission.parcels
        val key = "$today:${mission.id}"
        return if (requestedDate != today || count < mission.target || key in p.claimLedger) p
        else p.copy(points = p.points + mission.reward, mission = p.mission.copy(claimed = p.mission.claimed + mission.id), claimLedger = p.claimLedger + key)
    }

    fun finalize(profile: PlayerProfile, snapshot: FlightSnapshot, reason: RunEndReason): PlayerProfile {
        if (snapshot.runId in profile.finalized) return profile
        var p = profile
        snapshot.deliveryEvents.forEach { p = delivery(p, it) }
        val distancePoints = floor(snapshot.distance / 100.0).toInt()
        val result = RunResult(snapshot.runId, snapshot.hullId, snapshot.distance, snapshot.deliveries, snapshot.dockings,
            snapshot.deliveries * 10, distancePoints, snapshot.distance > p.best, reason)
        return p.copy(points = p.points + distancePoints, best = maxOf(p.best, snapshot.distance),
            active = if (p.active?.runId == snapshot.runId) null else p.active, results = (listOf(result) + p.results).take(20), finalized = p.finalized + snapshot.runId)
    }
}
