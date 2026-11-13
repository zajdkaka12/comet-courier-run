package com.cometcourier.run.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

@Composable
fun OrbitSurface(modifier: Modifier = Modifier, light: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier, shape = RoundedCornerShape(18.dp), color = if (light) MintBackground.copy(alpha = 0.95f) else SpaceSurface.copy(alpha = 0.92f),
        contentColor = if (light) Ink else MintBackground, border = BorderStroke(1.dp, if (light) SoftMint else TrailCyan.copy(alpha = 0.22f)), shadowElevation = 4.dp) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
fun AppPage(light: Boolean = true, actions: @Composable ColumnScope.() -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize()) {
        SpaceBackground(light = light)
        Column(Modifier.fillMaxSize().safeDrawingPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.widthIn(max = 480.dp).fillMaxSize().padding(horizontal = 16.dp)) {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 20.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
                Column(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = actions)
            }
        }
    }
}

@Composable
fun ScreenHeader(title: Int, eyebrow: Int = R.string.orbital_service, light: Boolean = true, trailing: @Composable () -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(eyebrow), color = if (light) Ink.copy(alpha = 0.7f) else SoftMint, style = MaterialTheme.typography.labelSmall)
            Text(stringResource(title), color = if (light) Ink else MintBackground, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
        }
        trailing()
    }
}

@Composable
fun StatusMessage(readError: Boolean, saveError: Boolean, onRetry: () -> Unit, light: Boolean = true) {
    if (readError || saveError) OrbitSurface(light = light, modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }) {
        Text(stringResource(if (readError) R.string.read_error else R.string.save_error), style = MaterialTheme.typography.bodyMedium)
        SecondaryButton(if (readError) R.string.retry else R.string.retry_save, onRetry, light = light)
    }
}
