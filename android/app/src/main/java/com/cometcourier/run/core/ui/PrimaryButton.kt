package com.cometcourier.run.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cometcourier.run.core.theme.*

@Composable
fun PrimaryButton(text: Int, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && LocalMotionEnabled.current) 0.97f else 1f, spring(dampingRatio = 0.75f, stiffness = 500f), label = "press")
    val shape = RoundedCornerShape(18.dp)
    val colors = if (enabled) listOf(ActionCherry, OrbitViolet) else listOf(SoftMint, SoftMint)
    Button(onClick, modifier.fillMaxWidth().heightIn(min = 60.dp).scale(scale).shadow(if (enabled) 6.dp else 0.dp, shape, ambientColor = ActionCherry, spotColor = ActionCherry)
        .clip(shape).background(Brush.horizontalGradient(colors)).background(SpaceTop.copy(alpha = if (enabled) 0.10f else 0f)),
        enabled = enabled, shape = shape, interactionSource = interaction,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MintBackground, disabledContainerColor = Color.Transparent, disabledContentColor = Ink),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)) {
        Text(stringResource(text), style = MaterialTheme.typography.labelLarge)
    }
}
