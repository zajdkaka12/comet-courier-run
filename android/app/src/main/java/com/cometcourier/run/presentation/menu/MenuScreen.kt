package com.cometcourier.run.presentation.menu

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cometcourier.run.R
import com.cometcourier.run.core.navigation.Screen
import com.cometcourier.run.core.theme.*
import com.cometcourier.run.core.ui.*

@Composable
fun MenuScreen(state: MenuUiState, onPlay: () -> Unit, onOpen: (Screen) -> Unit, onCycle: (Int) -> Unit, onRetry: () -> Unit) {
    val profile = state.profile
    val bobTransition = rememberInfiniteTransition(label = "hangar")
    val bob by bobTransition.animateFloat(-4f, 4f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "docked")
    val motion = LocalMotionEnabled.current
    AppPage(actions = {
        Text(stringResource(R.string.play_hint), color = Ink.copy(alpha = 0.78f), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth())
        PrimaryButton(R.string.play, onPlay, enabled = profile != null && !state.loading && !state.readError && !state.saveError && !state.starting)
    }) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Image(painterResource(R.drawable.logo), stringResource(R.string.app_emblem), Modifier.size(28.dp), contentScale = ContentScale.Fit)
            Text(stringResource(R.string.app_name), color = Ink, style = MaterialTheme.typography.labelLarge)
        }
        ScreenHeader(R.string.hangar, R.string.ready_dispatch, trailing = { if (profile != null) WalletChip(profile.points) })
        StatusMessage(state.readError, state.saveError, onRetry)
        OrbitSurface(Modifier.fillMaxWidth(), light = false) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.your_courier), color = SoftMint, style = MaterialTheme.typography.labelSmall)
                    Text(stringResource(hullNameId(profile?.selected ?: "sprout")), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.ready_for_route), color = SoftMint, style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.selected_badge), color = TrailCyan, style = MaterialTheme.typography.labelSmall)
                }
                Crossfade(profile?.selected ?: "sprout", animationSpec = tween(if (motion) 180 else 120), label = "hull") { hull ->
                    val selectedState = stringResource(R.string.selected)
                    CourierArtwork(hull, 132.dp, Modifier.graphicsLayer { translationY = if (motion) bob.dp.toPx() else 0f }
                        .semantics { stateDescription = selectedState; liveRegion = LiveRegionMode.Polite })
                }
            }
            if (state.loading) {
                LinearProgressIndicator(Modifier.fillMaxWidth(), color = TrailCyan)
                Text(stringResource(R.string.preparing_hangar), style = MaterialTheme.typography.bodyMedium)
            }
        }
        if ((profile?.owned?.size ?: 0) > 1) Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton(R.string.previous, { onCycle(-1) }, Modifier.weight(1f), enabled = !state.saveError)
            SecondaryButton(R.string.next, { onCycle(1) }, Modifier.weight(1f), enabled = !state.saveError)
        }
        if (profile != null && profile.best > 0 && profile.lifetimeDeliveries > 0) Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(R.string.best_distance, stringResource(R.string.meters_value, number(profile.best.toInt())), Modifier.weight(1f))
            StatCard(R.string.deliveries, stringResource(R.string.count_value, number(profile.lifetimeDeliveries)), Modifier.weight(1f))
        } else OrbitSurface(Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.first_route), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.first_route_hint), style = MaterialTheme.typography.bodyMedium)
        }
        OrbitSurface(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.daily_dispatch), style = MaterialTheme.typography.labelSmall)
                    Text(stringResource(state.dailyMission?.let { missionNameId(it.id) } ?: R.string.dispatch_complete), style = MaterialTheme.typography.titleLarge)
                }
                TextButton({ onOpen(Screen.Missions) }, Modifier.heightIn(min = 48.dp), colors = ButtonDefaults.textButtonColors(contentColor = Ink)) { Text(stringResource(R.string.missions)) }
            }
            state.dailyMission?.let { GoalProgress(state.dailyCount, it.target, it.countsDockings) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton(R.string.hull_shop, { onOpen(Screen.HullShop) }, Modifier.weight(1f))
            SecondaryButton(R.string.settings, { onOpen(Screen.Settings) }, Modifier.weight(1f))
        }
        profile?.results?.firstOrNull()?.let { result -> SecondaryButton(R.string.last_run, { onOpen(Screen.GameOver(result.id)) }, Modifier.fillMaxWidth()) }
    }
}
