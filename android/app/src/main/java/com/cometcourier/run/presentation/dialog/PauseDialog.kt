package com.cometcourier.run.presentation.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.*
import androidx.compose.ui.res.stringResource
import com.cometcourier.run.R
import com.cometcourier.run.core.ui.*

@Composable
fun PauseDialog(onResume: () -> Unit, onEnd: () -> Unit, onMenu: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        OrbitSurface(Modifier.fillMaxWidth(), light = false) {
            Text(stringResource(R.string.flight_paused), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.pause_hint), style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            PrimaryButton(R.string.resume, onResume)
            SecondaryButton(R.string.end_run, onEnd, Modifier.fillMaxWidth(), light = false)
            SecondaryButton(R.string.menu, onMenu, Modifier.fillMaxWidth(), light = false)
        }
    }
}
