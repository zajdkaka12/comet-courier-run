package com.cometcourier.run.data.sample

import com.cometcourier.run.domain.model.CourierHull
import com.cometcourier.run.domain.model.DailyMission

object SampleData {
    val hulls = listOf(CourierHull("sprout", 0), CourierHull("manta", 120), CourierHull("cherry", 300), CourierHull("nova", 600))
    val missions = listOf(DailyMission("first_post", 6, 30), DailyMission("ring_route", 4, 45, true), DailyMission("express_shift", 12, 60))
    val stationIds = listOf("juniper_dock", "pebble_outpost", "aurora_post")
}
