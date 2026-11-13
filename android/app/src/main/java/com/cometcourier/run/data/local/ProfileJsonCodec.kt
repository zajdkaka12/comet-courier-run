package com.cometcourier.run.data.local

import com.cometcourier.run.domain.model.*
import org.json.JSONArray
import org.json.JSONObject

class ProfileJsonCodec {
    private fun strings(items: Set<String>) = JSONArray(items.toList())
    private fun set(array: JSONArray): Set<String> = (0 until array.length()).map { array.getString(it) }.toSet()
    private fun objects(array: JSONArray): List<JSONObject> = (0 until array.length()).map { array.getJSONObject(it) }

    fun encode(p: PlayerProfile): String = JSONObject().apply {
        put("version", 1); put("revision", p.revision); put("points", p.points); put("owned", strings(p.owned)); put("selected", p.selected)
        put("best", p.best); put("lifetime", p.lifetimeDeliveries)
        put("settings", JSONObject().apply { put("buttons", p.settings.buttonsOnly); put("sound", p.settings.sound); put("haptics", p.settings.haptics); put("motion", p.settings.reducedMotion) })
        put("mission", JSONObject().apply { put("date", p.mission.date); put("parcels", p.mission.parcels); put("dockings", p.mission.dockings); put("claimed", strings(p.mission.claimed)) })
        put("active", p.active?.let(::flight) ?: JSONObject.NULL)
        put("results", JSONArray().apply { p.results.forEach { r -> put(JSONObject().apply {
            put("id", r.id); put("hull", r.hullId); put("distance", r.distance); put("deliveries", r.deliveries); put("dockings", r.dockings)
            put("deliveryPoints", r.deliveryPoints); put("distancePoints", r.distancePoints); put("newBest", r.newBest); put("reason", r.reason.name)
        }) } })
        put("deliveryLedger", strings(p.deliveryLedger)); put("finalized", strings(p.finalized)); put("claimLedger", strings(p.claimLedger))
    }.toString()

    fun decode(raw: String): PlayerProfile {
        val j = JSONObject(raw)
        require(j.getInt("version") == 1)
        val s = j.getJSONObject("settings")
        val m = j.getJSONObject("mission")
        val p = PlayerProfile(j.getLong("revision"), j.getInt("points"), set(j.getJSONArray("owned")), j.getString("selected"), j.getDouble("best"), j.getInt("lifetime"),
            CourierSettings(s.getBoolean("buttons"), s.getBoolean("sound"), s.getBoolean("haptics"), s.getBoolean("motion")),
            MissionProgress(m.getString("date"), m.getInt("parcels"), m.getInt("dockings"), set(m.getJSONArray("claimed"))),
            j.optJSONObject("active")?.let(::readFlight), objects(j.getJSONArray("results")).map { r ->
                RunResult(r.getString("id"), r.getString("hull"), r.getDouble("distance"), r.getInt("deliveries"), r.getInt("dockings"), r.getInt("deliveryPoints"), r.getInt("distancePoints"), r.getBoolean("newBest"), RunEndReason.valueOf(r.getString("reason")))
            }, set(j.getJSONArray("deliveryLedger")), set(j.getJSONArray("finalized")), set(j.getJSONArray("claimLedger")))
        require(p.points >= 0 && p.best >= 0 && p.best.isFinite() && p.selected in p.owned && "sprout" in p.owned)
        require(p.owned.all { it in setOf("sprout", "manta", "cherry", "nova") })
        return p
    }

    private fun flight(s: FlightSnapshot) = JSONObject().apply {
        put("id", s.runId); put("hull", s.hullId); put("seed", s.seed); put("elapsed", s.elapsed); put("distance", s.distance)
        put("lane", s.lane); put("position", s.shipPosition); put("from", s.moveFrom); put("moveStart", s.moveStart); put("cargo", s.cargo)
        put("shield", s.shield); put("immune", s.immuneUntil); put("deliveries", s.deliveries); put("dockings", s.dockings); put("sequence", s.deliverySequence)
        put("events", JSONArray().apply { s.deliveryEvents.forEach { e -> put(JSONObject().apply { put("run", e.runId); put("seq", e.sequence); put("date", e.date); put("parcels", e.parcels) }) } })
        put("objects", JSONArray().apply { s.objects.forEach { o -> put(JSONObject().apply {
            put("id", o.id); put("kind", o.kind.name); put("lane", o.lane); put("arrival", o.arrival); put("distance", o.crossingDistance); put("station", o.station)
        }) } })
        put("nextParcel", s.nextParcel); put("nextRing", s.nextRing); put("nextShield", s.nextShield); put("nextHazard", s.nextHazard)
        put("lastHazard", s.lastHazardLane); put("random", s.randomState); put("nextId", s.nextObjectId); put("status", s.status.name)
        put("statusSeq", s.statusSequence); put("station", s.station); put("ended", s.ended)
    }

    private fun readFlight(j: JSONObject) = FlightSnapshot(
        runId = j.getString("id"), hullId = j.getString("hull"), seed = j.getLong("seed"), elapsed = j.getDouble("elapsed"), distance = j.getDouble("distance"),
        lane = j.getInt("lane"), shipPosition = j.getDouble("position"), moveFrom = j.getDouble("from"), moveStart = j.getDouble("moveStart"), cargo = j.getInt("cargo"),
        shield = j.getBoolean("shield"), immuneUntil = j.getDouble("immune"), deliveries = j.getInt("deliveries"), dockings = j.getInt("dockings"), deliverySequence = j.getInt("sequence"),
        deliveryEvents = objects(j.getJSONArray("events")).map { DeliveryEvent(it.getString("run"), it.getInt("seq"), it.getString("date"), it.getInt("parcels")) },
        objects = objects(j.getJSONArray("objects")).map { FlightObject(it.getInt("id"), ObjectKind.valueOf(it.getString("kind")), it.getInt("lane"), it.getDouble("arrival"), it.getDouble("distance"), it.getInt("station")) },
        nextParcel = j.getDouble("nextParcel"), nextRing = j.getDouble("nextRing"), nextShield = j.getDouble("nextShield"), nextHazard = j.getDouble("nextHazard"),
        lastHazardLane = j.getInt("lastHazard"), randomState = j.getLong("random"), nextObjectId = j.getInt("nextId"), status = FlightStatus.valueOf(j.getString("status")),
        statusSequence = j.getInt("statusSeq"), station = j.getInt("station"), ended = j.getBoolean("ended")
    )
}
