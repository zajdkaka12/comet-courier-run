package com.cometcourier.run.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.cometcourier.run.R
import com.cometcourier.run.core.theme.*

@Composable
fun SpaceBackground(light: Boolean = false, splash: Boolean = false) {
    Image(painterResource(R.drawable.bg_game), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
    val colors = when {
        light -> listOf(MintBackground.copy(alpha = 0.97f), SoftMint.copy(alpha = 0.91f), MintBackground.copy(alpha = 0.98f))
        splash -> listOf(SpaceTop.copy(alpha = 0.94f), SpaceIndigo.copy(alpha = 0.87f), SpaceSurface.copy(alpha = 0.96f))
        else -> listOf(SpaceTop.copy(alpha = 0.65f), SpaceIndigo.copy(alpha = 0.48f), SpaceSurface.copy(alpha = 0.83f))
    }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(colors)))
    Canvas(Modifier.fillMaxSize()) {
        val tint = if (light) CourierGreen.copy(alpha = 0.08f) else TrailCyan.copy(alpha = 0.10f)
        listOf(0.55f, 0.85f, 1.2f).forEach { factor ->
            drawCircle(tint, size.width * factor, Offset(size.width * 0.88f, size.height * 0.27f), style = Stroke(1.2f))
        }
        if (!light) repeat(18) { i ->
            drawCircle(SoftMint.copy(alpha = 0.18f + (i % 3) * 0.1f), (i % 2 + 1).toFloat(), Offset(size.width * ((i * 37 % 97) / 97f), size.height * ((i * 23 % 89) / 89f)))
        }
    }
}
