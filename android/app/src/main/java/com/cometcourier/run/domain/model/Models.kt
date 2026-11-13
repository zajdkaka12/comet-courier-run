package com.cometcourier.run.domain.model

data class CourierHull(val id: String, val price: Int)
data class DailyMission(val id: String, val target: Int, val reward: Int, val countsDockings: Boolean = false)
enum class SettingKey { ButtonsOnly, Sound, Haptics, ReducedMotion }
data class CourierSettings(val buttonsOnly: Boolean = false, val sound: Boolean = true, val haptics: Boolean = true, val reducedMotion: Boolean = false)
data class MissionProgress(val date: String = "", val parcels: Int = 0, val dockings: Int = 0, val claimed: Set<String> = emptySet())
enum class ObjectKind { Shield, Parcel, Ring, Asteroid }
enum class RunEndReason { Collision, Ended, Left, Interrupted }
data class FlightObject(val id: Int, val kind: ObjectKind, val lane: Int, val arrival: Double, val crossingDistance: Double, val station: Int = 0, val y: Float = 0f)
data class DeliveryEvent(val runId: String, val sequence: Int, val date: String, val parcels: Int)
data class FlightSnapshot(
    val runId: String,
    val hullId: String,
    val seed: Long,
    val elapsed: Double = 0.0,
    val distance: Double = 0.0,
    val lane: Int = 1,
    val shipPosition: Double = 1.0,
    val moveFrom: Double = 1.0,
    val moveStart: Double = -1.0,
    val cargo: Int = 0,
    val shield: Boolean = true,
    val immuneUntil: Double = 0.0,
    val deliveries: Int = 0,
    val dockings: Int = 0,
    val deliverySequence: Int = 0,
    val deliveryEvents: List<DeliveryEvent> = emptyList(),
    val objects: List<FlightObject> = emptyList(),
    val nextParcel: Double = 0.8,
    val nextRing: Double = 6.0,
    val nextShield: Double = 15.0,
    val nextHazard: Double = 7.0,
    val lastHazardLane: Int = 1,
    val randomState: Long = seed,
    val nextObjectId: Int = 2,
    val status: FlightStatus = FlightStatus.Ready,
    val statusSequence: Int = 0,
    val station: Int = 0,
    val ended: Boolean = false
)
enum class FlightStatus { Ready, Parcel, CargoFull, Delivery, ShieldHit, ShieldReady, Complete }
data class RunResult(
    val id: String,
    val hullId: String,
    val distance: Double,
    val deliveries: Int,
    val dockings: Int,
    val deliveryPoints: Int,
    val distancePoints: Int,
    val newBest: Boolean,
    val reason: RunEndReason
) { val totalPoints: Int get() = deliveryPoints + distancePoints }
data class PlayerProfile(
    val revision: Long = 0,
    val points: Int = 0,
    val owned: Set<String> = setOf("sprout"),
    val selected: String = "sprout",
    val best: Double = 0.0,
    val lifetimeDeliveries: Int = 0,
    val settings: CourierSettings = CourierSettings(),
    val mission: MissionProgress = MissionProgress(),
    val active: FlightSnapshot? = null,
    val results: List<RunResult> = emptyList(),
    val deliveryLedger: Set<String> = emptySet(),
    val finalized: Set<String> = emptySet(),
    val claimLedger: Set<String> = emptySet()
)
data class ProgressState(val profile: PlayerProfile? = null, val loading: Boolean = true, val readError: Boolean = false, val saveError: Boolean = false, val pending: PlayerProfile? = null)
