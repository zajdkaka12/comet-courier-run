package com.cometcourier.run.presentation.gameover

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cometcourier.run.R
import com.cometcourier.run.core.theme.*
import com.cometcourier.run.core.ui.*
import com.cometcourier.run.domain.model.RunEndReason
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameOverScreen(state: GameOverUiState, onAgain: () -> Unit, onMenu: () -> Unit, onMissions: () -> Unit, onRetry: () -> Unit) {
    val result = state.result
    val reveal = remember(result?.id) { Animatable(0f) }
    val motion = LocalMotionEnabled.current
    LaunchedEffect(result?.id) {
        if (result != null && motion) reveal.animateTo(1f, tween(600)) else reveal.snapTo(1f)
    }
    AppPage(actions = {
        Text(stringResource(R.string.summary_hint), color = Ink, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        if (result != null) PrimaryButton(R.string.play_again, onAgain, enabled = !state.saveError)
        SecondaryButton(R.string.menu, onMenu, Modifier.fillMaxWidth(), icon = R.drawable.icon_home)
    }) {
        ScreenHeader(R.string.run_summary, R.string.route_receipt)
        StatusMessage(false, state.saveError, onRetry)
        if (state.loading) CircularProgressIndicator(color = CourierGreen)
        else if (result == null) OrbitSurface(Modifier.fillMaxWidth()) { Text(stringResource(R.string.summary_unavailable)) }
        else {
            Text(stringResource(if (result.reason == RunEndReason.Interrupted) R.string.interrupted_flight else if (result.deliveries > 0) R.string.good_day else R.string.next_route_ready), color = Ink, style = MaterialTheme.typography.bodyLarge)
            OrbitSurface(Modifier.fillMaxWidth().graphicsLayer {
                val entry = (reveal.value / 0.37f).coerceIn(0f, 1f)
                alpha = entry
                scaleX = 0.96f + entry * 0.04f
                scaleY = scaleX
            }.semantics { liveRegion = LiveRegionMode.Polite }, light = false) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.distance_flown), color = SoftMint, style = MaterialTheme.typography.labelSmall)
                        Text(stringResource(R.string.meters_value, number(result.distance.toInt())), fontSize = 40.sp, fontWeight = FontWeight.Bold, lineHeight = 44.sp)
                    }
                    CourierArtwork(result.hullId, 104.dp)
                }
                if (result.newBest) Text(stringResource(R.string.new_personal_best), color = TrailCyan, style = MaterialTheme.typography.labelLarge)
                if (result.newBest && motion && reveal.value < 1f) Canvas(Modifier.fillMaxWidth().height(14.dp)) {
                    repeat(12) { index ->
                        val angle = index * Math.PI * 2 / 12
                        val radius = size.width * 0.36f * reveal.value
                        drawCircle(if (index % 2 == 0) TrailCyan.copy(alpha = 1f - reveal.value) else CourierGreen.copy(alpha = 1f - reveal.value),
                            2.dp.toPx(), Offset(size.width / 2 + cos(angle).toFloat() * radius, size.height / 2 + sin(angle).toFloat() * radius * 0.2f))
                    }
                }
            }
            if (result.deliveries > 0 && result.totalPoints > 0) Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(R.string.deliveries, stringResource(R.string.count_value, number(result.deliveries)), Modifier.weight(1f))
                StatCard(R.string.run_points, stringResource(R.string.count_value, number(result.totalPoints)), Modifier.weight(1f))
            } else OrbitSurface(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.collect_then_deliver), style = MaterialTheme.typography.bodyLarge)
                Text(stringResource(if (result.totalPoints == 0) R.string.no_points else R.string.run_points_value, number(result.totalPoints)), style = MaterialTheme.typography.bodyMedium)
            }
            OrbitSurface(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.delivery_receipt), style = MaterialTheme.typography.titleLarge)
                ReceiptLine(R.string.delivery_points, result.deliveryPoints)
                ReceiptLine(R.string.distance_points, result.distancePoints)
                HorizontalDivider(color = SoftMint)
                ReceiptLine(R.string.total_run_points, result.totalPoints)
                Text(stringResource(R.string.added_to_points), style = MaterialTheme.typography.bodyMedium)
                WalletChip(state.profile?.points ?: 0)
            }
            if (result.deliveries > 0) OrbitSurface(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.mission_advanced), style = MaterialTheme.typography.bodyLarge)
                SecondaryButton(R.string.missions, onMissions, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ReceiptLine(label: Int, value: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(label), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.points_value, number(value)), style = MaterialTheme.typography.labelLarge)
    }
}
