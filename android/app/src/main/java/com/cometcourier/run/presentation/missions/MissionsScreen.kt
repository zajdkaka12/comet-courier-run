package com.cometcourier.run.presentation.missions

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.cometcourier.run.R
import com.cometcourier.run.core.theme.*
import com.cometcourier.run.core.ui.*

@Composable
fun MissionsScreen(state: MissionsUiState, onClaim: (String) -> Unit, onMenu: () -> Unit, onRetry: () -> Unit) {
    val p = state.profile
    AppPage(actions = {
        Text(stringResource(R.string.midnight_hint), color = Ink, style = MaterialTheme.typography.bodyMedium)
        SecondaryButton(R.string.menu, onMenu, Modifier.fillMaxWidth(), icon = R.drawable.icon_home)
    }) {
        ScreenHeader(R.string.daily_missions, trailing = { WalletChip(p?.points ?: 0) })
        Text(stringResource(R.string.date_value, state.date), color = Ink, style = MaterialTheme.typography.bodyMedium)
        StatusMessage(state.readError, state.saveError, onRetry)
        OrbitSurface(Modifier.fillMaxWidth(), light = false) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Image(painterResource(R.drawable.sprite_2), null, Modifier.size(64.dp), contentScale = ContentScale.Fit)
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.today_post), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.mission_intro), color = SoftMint, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        if (state.loading) CircularProgressIndicator(color = CourierGreen)
        if (p?.mission?.claimed?.size == 3) Text(stringResource(R.string.dispatch_complete), color = Ink, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        state.missions.forEach { mission ->
            val progress = if (p != null && p.mission.date == state.date) { if (mission.countsDockings) p.mission.dockings else p.mission.parcels } else 0
            val claimed = p != null && p.mission.date == state.date && mission.id in p.mission.claimed
            val ready = progress >= mission.target
            OrbitSurface(Modifier.fillMaxWidth()) {
                Text(stringResource(missionNameId(mission.id)), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(if (mission.countsDockings) R.string.rings_goal else R.string.parcels_goal, number(mission.target)), style = MaterialTheme.typography.bodyMedium)
                GoalProgress(progress, mission.target, mission.countsDockings)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.reward_points, number(mission.reward)), style = MaterialTheme.typography.labelLarge)
                    SecondaryButton(if (claimed) R.string.claimed else R.string.claim, { onClaim(mission.id) },
                        enabled = ready && !claimed && state.pendingId == null && !state.saveError && !state.loading)
                }
                if (state.pendingId == mission.id) LinearProgressIndicator(Modifier.fillMaxWidth(), color = CourierGreen)
                if (claimed) Text(stringResource(R.string.reward_claimed), style = MaterialTheme.typography.labelSmall, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            }
        }
    }
}
