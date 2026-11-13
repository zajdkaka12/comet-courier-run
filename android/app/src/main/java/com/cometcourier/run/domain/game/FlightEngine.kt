package com.cometcourier.run.domain.game

import com.cometcourier.run.domain.model.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class FlightEngine(initial: FlightSnapshot) {
    var snapshot: FlightSnapshot = initial
        private set

    init {
        if (initial.objects.isEmpty() && initial.elapsed == 0.0) {
            snapshot = initial.copy(objects = listOf(
                FlightObject(0, ObjectKind.Asteroid, 1, 1.2, distanceAt(1.2)),
                FlightObject(1, ObjectKind.Asteroid, 1, 2.6, distanceAt(2.6))
            ))
        }
        plan()
        project()
    }

    fun move(direction: Int): Boolean {
        val s = snapshot
        if (s.ended || s.moveStart >= 0.0 || s.lane + direction !in 0..2) return false
        snapshot = s.copy(lane = s.lane + direction, moveFrom = s.shipPosition, moveStart = s.elapsed)
        return true
    }

    fun step(seconds: Double, date: String): FlightSnapshot {
        if (snapshot.ended) return snapshot
        val target = snapshot.elapsed + seconds
        val due = snapshot.objects.filter { it.arrival <= target + 0.000000001 }.sortedWith(compareBy<FlightObject> { it.arrival }.thenBy { it.kind.ordinal })
        for (obj in due) {
            advance(obj.arrival)
            snapshot = snapshot.copy(objects = snapshot.objects.filterNot { it.id == obj.id })
            if (abs(snapshot.shipPosition - obj.lane) < 0.42) cross(obj, date)
            if (snapshot.ended) break
        }
        if (!snapshot.ended) advance(target)
        plan()
        project()
        return snapshot
    }

    private fun advance(time: Double) {
        val s = snapshot
        val progress = if (s.moveStart < 0.0) 1.0 else ((time - s.moveStart) / 0.18).coerceIn(0.0, 1.0)
        val eased = progress * progress * (3.0 - 2.0 * progress)
        snapshot = s.copy(elapsed = time, distance = distanceAt(time), shipPosition = s.moveFrom + (s.lane - s.moveFrom) * eased,
            moveStart = if (progress >= 1.0) -1.0 else s.moveStart,
            moveFrom = if (progress >= 1.0) s.lane.toDouble() else s.moveFrom)
    }

    private fun cross(obj: FlightObject, date: String) {
        val s = snapshot
        snapshot = when (obj.kind) {
            ObjectKind.Parcel -> if (s.cargo < 3) s.copy(cargo = s.cargo + 1, status = FlightStatus.Parcel, statusSequence = s.statusSequence + 1)
                else s.copy(status = FlightStatus.CargoFull, statusSequence = s.statusSequence + 1)
            ObjectKind.Shield -> s.copy(shield = true, status = FlightStatus.ShieldReady, statusSequence = s.statusSequence + 1)
            ObjectKind.Ring -> if (s.cargo > 0) {
                val event = DeliveryEvent(s.runId, s.deliverySequence + 1, date, s.cargo)
                s.copy(cargo = 0, deliveries = s.deliveries + s.cargo, dockings = s.dockings + 1, deliverySequence = event.sequence,
                    deliveryEvents = s.deliveryEvents + event, station = obj.station, status = FlightStatus.Delivery, statusSequence = s.statusSequence + 1)
            } else s
            ObjectKind.Asteroid -> when {
                s.elapsed < s.immuneUntil -> s
                s.shield -> s.copy(shield = false, immuneUntil = s.elapsed + 0.85, status = FlightStatus.ShieldHit, statusSequence = s.statusSequence + 1)
                else -> s.copy(ended = true, status = FlightStatus.Complete, statusSequence = s.statusSequence + 1)
            }
        }
    }

    private fun randomLane(): Int {
        val next = (snapshot.randomState * 1664525L + 1013904223L) and 0xffffffffL
        snapshot = snapshot.copy(randomState = next)
        return ((next ushr 8) % 3).toInt()
    }

    private fun add(kind: ObjectKind, lane: Int, time: Double, station: Int = 0) {
        val s = snapshot
        snapshot = s.copy(nextObjectId = s.nextObjectId + 1, objects = s.objects + FlightObject(s.nextObjectId, kind, lane, time, distanceAt(time), station))
    }

    private fun safeLane(preferred: Int, time: Double, margin: Double): Int? =
        (listOf(preferred) + (0..2).filter { it != preferred }).firstOrNull { lane ->
            snapshot.objects.none { it.kind == ObjectKind.Asteroid && it.lane == lane && abs(it.arrival - time) < margin }
        }

    private fun plan() {
        val horizon = snapshot.elapsed + 12.0
        while (snapshot.nextHazard <= horizon + 2.0) {
            val time = snapshot.nextHazard
            var lane = randomLane()
            if (lane == snapshot.lastHazardLane) lane = (lane + 1) % 3
            val candidate = (listOf(lane) + (0..2).filter { it != lane }).firstOrNull { l ->
                snapshot.objects.none { it.kind == ObjectKind.Ring && it.lane == l && (abs(it.arrival - time) < 1.2 || abs(it.arrival - time - 1.4) < 1.2) }
            } ?: lane
            add(ObjectKind.Asteroid, candidate, time)
            add(ObjectKind.Asteroid, candidate, time + 1.4)
            snapshot = snapshot.copy(lastHazardLane = candidate, nextHazard = time + max(2.8, 4.2 - 0.015 * time))
        }
        while (snapshot.nextParcel <= horizon) {
            val time = snapshot.nextParcel
            val lane = safeLane(if (time < 1.0) 0 else randomLane(), time, 1.0)
            if (lane == null) snapshot = snapshot.copy(nextParcel = time + 1.5)
            else { add(ObjectKind.Parcel, lane, time); snapshot = snapshot.copy(nextParcel = time + 2.0) }
        }
        while (snapshot.nextRing <= horizon) {
            val time = snapshot.nextRing
            val lane = safeLane(randomLane(), time, 1.2)
            if (lane == null) snapshot = snapshot.copy(nextRing = time + 1.5)
            else { add(ObjectKind.Ring, lane, time, ((time / 6).toInt() - 1) % 3); snapshot = snapshot.copy(nextRing = time + 6.0) }
        }
        while (snapshot.nextShield <= horizon) {
            val time = snapshot.nextShield
            val lane = safeLane(randomLane(), time, 1.0)
            if (lane == null) snapshot = snapshot.copy(nextShield = time + 1.5)
            else { add(ObjectKind.Shield, lane, time); snapshot = snapshot.copy(nextShield = time + 20.0) }
        }
    }

    private fun project() {
        snapshot = snapshot.copy(objects = snapshot.objects.map { it.copy(y = (0.8 - min(120.0, it.crossingDistance - snapshot.distance) / 120.0 * 0.7).toFloat()) })
    }

    companion object {
        fun distanceAt(t: Double): Double = if (t <= 90.0) 14.0 * t + 0.10 * t * t else 2070.0 + 32.0 * (t - 90.0)
    }
}
