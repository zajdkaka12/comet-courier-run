package com.cometcourier.run.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.cometcourier.run.R

fun hullArt(id: String): Int = when (id) {
    "manta" -> R.drawable.sprite_2
    "cherry" -> R.drawable.sprite_3
    "nova" -> R.drawable.sprite_4
    else -> R.drawable.sprite_1
}
fun hullNameId(id: String): Int = when (id) {
    "manta" -> R.string.hull_manta
    "cherry" -> R.string.hull_cherry
    "nova" -> R.string.hull_nova
    else -> R.string.hull_sprout
}
fun hullDescriptionId(id: String): Int = when (id) {
    "manta" -> R.string.manta_description
    "cherry" -> R.string.cherry_description
    "nova" -> R.string.nova_description
    else -> R.string.sprout_description
}
fun missionNameId(id: String): Int = when (id) {
    "ring_route" -> R.string.ring_route
    "express_shift" -> R.string.express_shift
    else -> R.string.first_post
}
fun stationNameId(index: Int): Int = when (index) {
    1 -> R.string.pebble_outpost
    2 -> R.string.aurora_post
    else -> R.string.juniper_dock
}
@Composable
fun CourierArtwork(hull: String, size: Dp, modifier: Modifier = Modifier, decorative: Boolean = false) {
    Image(painterResource(hullArt(hull)), if (decorative) null else stringResource(R.string.courier_description, stringResource(hullNameId(hull))), modifier.size(size), contentScale = ContentScale.Fit)
}
