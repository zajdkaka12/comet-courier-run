package com.cometcourier.run.presentation.game

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cometcourier.run.R
import com.cometcourier.run.core.theme.*
import com.cometcourier.run.core.ui.*
import com.cometcourier.run.domain.model.FlightStatus
import com.cometcourier.run.presentation.dialog.PauseDialog

@Composable
fun GameScreen(state: GameUiState, onMove: (Int) -> Unit, onPause: () -> Unit, onResume: () -> Unit, onEnd: () -> Unit, onMenu: () -> Unit, onDismissPause: () -> Unit, onSummary: () -> Unit, onRetry: () -> Unit) {
    val snapshot = state.snapshot
    val running = state.phase == FlightPhase.Running
    BoxWithConstraints(Modifier.fillMaxSize()) {
        SpaceBackground()
        val compact = maxHeight < 500.dp
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 12.dp, vertical = if (compact) 4.dp else 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.widthIn(max = 480.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 8.dp)) {
                Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onMenu, Modifier.heightIn(min = 48.dp), colors = ButtonDefaults.textButtonColors(contentColor = SoftMint)) { Text(stringResource(R.string.menu)) }
                    val distance = stringResource(R.string.meters_value, number(snapshot?.distance?.toInt() ?: 0))
                    val distanceAnnouncement = stringResource(R.string.meters_value, number(state.announcedDistance))
                    Text(distance, color = MintBackground, fontWeight = FontWeight.Bold, fontSize = if (compact) 22.sp else 26.sp,
                        modifier = Modifier.clearAndSetSemantics { contentDescription = distanceAnnouncement; liveRegion = LiveRegionMode.Polite })
                    SecondaryButton(R.string.pause, onPause, light = false, enabled = running, icon = if (compact) null else R.drawable.icon_pause)
                }
                FlightHud(snapshot, compact)
                Box(Modifier.fillMaxWidth().weight(1f)) {
                    FlightBoard(snapshot, running, state.settings.buttonsOnly, compact, onMove, onPause, Modifier.fillMaxSize())
                    if (state.phase != FlightPhase.Running) Column(Modifier.align(Alignment.Center).padding(12.dp).widthIn(max = 300.dp)) {
                        OrbitSurface(Modifier.fillMaxWidth(), light = false) {
                            Text(stringResource(when (state.phase) {
                                FlightPhase.Preparing -> R.string.preparing_route
                                FlightPhase.Paused -> R.string.flight_paused
                                else -> R.string.flight_complete
                            }), style = MaterialTheme.typography.titleLarge)
                            when (state.phase) {
                                FlightPhase.Preparing, FlightPhase.Finishing -> CircularProgressIndicator(Modifier.size(28.dp), color = TrailCyan)
                                FlightPhase.Paused -> {
                                    if (!state.saveError) PrimaryButton(R.string.resume, onResume)
                                    else SecondaryButton(R.string.retry_save, onRetry, light = false)
                                }
                                FlightPhase.Completed -> PrimaryButton(R.string.view_summary, onSummary)
                                else -> Unit
                            }
                        }
                    }
                }
                if (state.saveError) Text(stringResource(R.string.save_error), color = SoftMint, style = MaterialTheme.typography.labelSmall)
                val status = when (snapshot?.status) {
                    FlightStatus.Parcel -> R.string.parcel_collected
                    FlightStatus.CargoFull -> R.string.cargo_full
                    FlightStatus.Delivery -> R.string.delivery_complete
                    FlightStatus.ShieldHit -> R.string.shield_hit
                    FlightStatus.ShieldReady -> R.string.shield_restored
                    FlightStatus.Complete -> R.string.flight_complete
                    else -> R.string.route_hint
                }
                val statusText = if (snapshot?.status == FlightStatus.Delivery) stringResource(R.string.delivery_status, stringResource(stationNameId(snapshot.station)), number((snapshot.deliveryEvents.lastOrNull()?.parcels ?: 0) * 10)) else stringResource(status)
                if (!compact) Text(statusText, Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }, color = SoftMint, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                OrbitSurface(Modifier.fillMaxWidth()) {
                    if (!compact) Text(stringResource(if (state.settings.buttonsOnly) R.string.tap_to_move else R.string.swipe_hint), style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val leftDescription = stringResource(if (snapshot?.lane == 0) R.string.left_edge else R.string.move_left)
                        val rightDescription = stringResource(if (snapshot?.lane == 2) R.string.right_edge else R.string.move_right)
                        SecondaryButton(R.string.left, { onMove(-1) }, Modifier.weight(1f).semantics { stateDescription = leftDescription }, enabled = running && snapshot?.lane != 0)
                        SecondaryButton(R.string.right, { onMove(1) }, Modifier.weight(1f).semantics { stateDescription = rightDescription }, enabled = running && snapshot?.lane != 2)
                    }
                    if (!compact) {
                        val laneDescription = stringResource(R.string.lane_value, (snapshot?.lane ?: 1) + 1)
                        Text(laneDescription, Modifier.fillMaxWidth().semantics { stateDescription = laneDescription }, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
    if (state.pauseDialog) PauseDialog(onResume, onEnd, onMenu, onDismissPause)
}
