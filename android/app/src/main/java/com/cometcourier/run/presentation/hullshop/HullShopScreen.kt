package com.cometcourier.run.presentation.hullshop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.cometcourier.run.R
import com.cometcourier.run.core.theme.*
import com.cometcourier.run.core.ui.*

@Composable
fun HullShopScreen(state: HullShopUiState, onChoose: (String) -> Unit, onMenu: () -> Unit, onRetry: () -> Unit) {
    val p = state.profile
    AppPage(light = false, actions = {
        Text(stringResource(R.string.hull_hint), color = SoftMint, style = MaterialTheme.typography.bodyMedium)
        SecondaryButton(R.string.menu, onMenu, Modifier.fillMaxWidth(), light = false, icon = R.drawable.icon_home)
    }) {
        ScreenHeader(R.string.hull_shop_title, light = false, trailing = { WalletChip(p?.points ?: 0, light = false) })
        StatusMessage(state.readError, state.saveError, onRetry, light = false)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            CourierArtwork(p?.selected ?: "sprout", 112.dp)
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.fleet_collection), color = SoftMint, style = MaterialTheme.typography.labelSmall)
                Text(stringResource(if (p?.owned?.size == 4) R.string.fleet_complete else R.string.find_your_style), color = MintBackground, style = MaterialTheme.typography.titleLarge)
            }
        }
        if (state.loading) CircularProgressIndicator(color = TrailCyan)
        state.catalog.forEach { hull ->
            val selected = p?.selected == hull.id
            val owned = p?.owned?.contains(hull.id) == true
            val affordable = (p?.points ?: 0) >= hull.price
            val pending = state.pendingHullId == hull.id
            val name = stringResource(hullNameId(hull.id))
            val status = stringResource(if (selected) R.string.selected else if (owned) R.string.owned else R.string.locked)
            Surface(Modifier.fillMaxWidth().semantics { stateDescription = "$name, $status" }, shape = RoundedCornerShape(18.dp), color = MintBackground, contentColor = Ink,
                border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) CourierGreen else SoftMint), shadowElevation = 4.dp) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CourierArtwork(hull.id, 80.dp)
                        Column(Modifier.weight(1f)) {
                            Text(name, style = MaterialTheme.typography.titleLarge)
                            Text(stringResource(hullDescriptionId(hull.id)), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (owned) status else stringResource(R.string.points_value, number(hull.price)), style = MaterialTheme.typography.labelLarge)
                        SecondaryButton(if (selected) R.string.selected else if (owned) R.string.select else R.string.unlock,
                            { onChoose(hull.id) }, enabled = !selected && (owned || affordable) && state.pendingHullId == null && !state.saveError && p != null)
                    }
                    if (!owned && !affordable) Text(stringResource(R.string.earn_more, number(hull.price - (p?.points ?: 0))), style = MaterialTheme.typography.bodyMedium)
                    if (pending) { LinearProgressIndicator(Modifier.fillMaxWidth(), color = CourierGreen); Text(stringResource(R.string.saving_hull)) }
                }
            }
        }
    }
}
