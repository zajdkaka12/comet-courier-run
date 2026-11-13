package com.cometcourier.run.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cometcourier.run.core.theme.*

@Composable
fun SecondaryButton(text: Int, onClick: () -> Unit, modifier: Modifier = Modifier, light: Boolean = true, enabled: Boolean = true, icon: Int? = null) {
    OutlinedButton(onClick, modifier.heightIn(min = 48.dp), enabled = enabled, shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (light) Ink.copy(alpha = 0.18f) else SoftMint.copy(alpha = 0.3f)),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = if (light) MintBackground.copy(alpha = 0.9f) else SpaceSurface.copy(alpha = 0.9f),
            contentColor = if (light) Ink else MintBackground, disabledContainerColor = SoftMint, disabledContentColor = Ink.copy(alpha = 0.65f)),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)) {
        if (icon != null) {
            Image(painterResource(icon), null, Modifier.size(22.dp), contentScale = ContentScale.Fit)
            Spacer(Modifier.width(6.dp))
        }
        Text(stringResource(text), style = MaterialTheme.typography.labelLarge)
    }
}
