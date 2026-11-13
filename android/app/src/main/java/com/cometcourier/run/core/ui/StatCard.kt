package com.cometcourier.run.core.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.cometcourier.run.R
import com.cometcourier.run.core.theme.*
import java.text.NumberFormat

fun number(value: Number): String = NumberFormat.getIntegerInstance().format(value)

@Composable
fun StatCard(label: Int, value: String, modifier: Modifier = Modifier, light: Boolean = true) {
    OrbitSurface(modifier, light) {
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(stringResource(label), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun WalletChip(points: Int, light: Boolean = true) {
    val pulse = remember { Animatable(1f) }
    var lastPoints by remember { mutableIntStateOf(points) }
    val motion = LocalMotionEnabled.current
    LaunchedEffect(points) {
        if (points != lastPoints && motion) pulse.animateTo(1f, keyframes { durationMillis = 200; 1.05f at 100 })
        lastPoints = points
    }
    Surface(Modifier.scale(pulse.value), shape = RoundedCornerShape(14.dp), color = if (light) Ink else MintBackground, contentColor = if (light) MintBackground else Ink,
        border = BorderStroke(1.dp, SoftMint.copy(alpha = 0.4f))) {
        Text(stringResource(R.string.points_value, number(points)), Modifier.padding(horizontal = 12.dp, vertical = 10.dp), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun GoalProgress(current: Int, target: Int, rings: Boolean = false) {
    val label = stringResource(if (rings) R.string.rings_progress else R.string.parcels_progress, number(current), number(target))
    Column(Modifier.semantics { stateDescription = label }, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        LinearProgressIndicator(progress = { (current.toFloat() / target).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(6.dp), color = CourierGreen, trackColor = SoftMint)
    }
}
