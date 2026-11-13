package com.cometcourier.run.presentation.dialog

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.cometcourier.run.R
import com.cometcourier.run.core.theme.*

@Composable
fun HowToPlayDialog(onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.how_to_play_title)) },
        text = { Text(stringResource(R.string.rules)) }, containerColor = MintBackground, titleContentColor = Ink, textContentColor = Ink,
        confirmButton = { TextButton(onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = Ink)) { Text(stringResource(R.string.got_it)) } })
}
