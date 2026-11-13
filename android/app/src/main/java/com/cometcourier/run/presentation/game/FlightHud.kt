package com.cometcourier.run.presentation.game

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
import com.cometcourier.run.domain.model.FlightSnapshot

@Composable
fun FlightHud(snapshot: FlightSnapshot?, compact: Boolean) {
    val cargo = stringResource(R.string.cargo_value, snapshot?.cargo ?: 0)
    val shield = stringResource(if (snapshot?.shield != false) R.string.shield_ready else R.string.shield_empty)
    Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = if (compact) 2.dp else 8.dp).semantics { stateDescription = "$cargo, $shield" },
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.sprite_2), null, Modifier.size(if (compact) 22.dp else 28.dp), contentScale = ContentScale.Fit)
            Text(cargo, color = MintBackground, style = MaterialTheme.typography.labelSmall)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(shield, color = if (snapshot?.shield != false) TrailCyan else SoftMint, style = MaterialTheme.typography.labelSmall)
            if (!compact) Text(stringResource(R.string.deliveries_value, number(snapshot?.deliveries ?: 0)), color = SoftMint, style = MaterialTheme.typography.labelSmall)
        }
    }
}
