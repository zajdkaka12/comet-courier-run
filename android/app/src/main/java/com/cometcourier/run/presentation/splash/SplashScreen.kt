package com.cometcourier.run.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cometcourier.run.R
import com.cometcourier.run.core.theme.*
import com.cometcourier.run.core.ui.*
import kotlinx.coroutines.flow.StateFlow

@Composable
fun SplashScreen(state: SplashUiState, finished: StateFlow<Boolean>, onFinished: () -> Unit) {
    val done by finished.collectAsStateWithLifecycle()
    val latestFinished by rememberUpdatedState(onFinished)
    LaunchedEffect(done) { if (done) latestFinished() }
    val motion = LocalMotionEnabled.current && !state.reducedMotion
    val entry = remember { Animatable(0f) }
    LaunchedEffect(motion) { if (motion) entry.animateTo(1f, tween(250)) else entry.snapTo(1f) }
    val transition = rememberInfiniteTransition(label = "orbit")
    val bob by transition.animateFloat(-8f, 8f, infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "ship")
    val orbit by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(4800, easing = LinearEasing)), label = "revolution")
    val stars by transition.animateFloat(0.2f, 0.6f, infiniteRepeatable(tween(2400), RepeatMode.Reverse), label = "stars")
    Box(Modifier.fillMaxSize()) {
        SpaceBackground(splash = true)
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.weight(0.7f))
            Text(stringResource(R.string.orbital_service), style = MaterialTheme.typography.labelSmall, color = TrailCyan)
            Spacer(Modifier.height(28.dp))
            Box(Modifier.size(244.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.size(230.dp).background(Brush.radialGradient(listOf(TrailCyan.copy(alpha = 0.17f), SpaceTop.copy(alpha = 0f)))))
                Canvas(Modifier.fillMaxSize().rotate(if (motion) orbit else 0f).alpha(if (motion) stars else 0.4f)) {
                    drawCircle(TrailCyan, 4.dp.toPx(), Offset(size.width * 0.16f, size.height * 0.22f))
                    drawCircle(SoftMint, 3.dp.toPx(), Offset(size.width * 0.84f, size.height * 0.30f))
                    drawCircle(CourierGreen, 5.dp.toPx(), Offset(size.width * 0.63f, size.height * 0.88f))
                }
                Image(painterResource(R.drawable.logo), null, Modifier.size(176.dp).alpha(0.3f).rotate(if (motion) orbit * 0.15f else 0f), contentScale = ContentScale.Fit)
                CourierArtwork("sprout", 176.dp, Modifier.graphicsLayer {
                    translationY = if (motion) bob.dp.toPx() else 0f
                    alpha = entry.value
                    scaleX = 0.94f + entry.value * 0.06f
                    scaleY = scaleX
                }, decorative = true)
            }
            Spacer(Modifier.height(20.dp))
            Text(stringResource(R.string.splash_title), modifier = Modifier.alpha(entry.value), color = MintBackground, style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.tagline), color = SoftMint, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            Spacer(Modifier.weight(1f))
            val preparing = stringResource(R.string.preparing_hangar)
            CircularProgressIndicator(Modifier.size(32.dp).semantics { contentDescription = preparing }, color = TrailCyan, strokeWidth = 3.dp)
            Spacer(Modifier.height(16.dp))
            Text(preparing, style = MaterialTheme.typography.labelSmall, color = SoftMint)
            Spacer(Modifier.height(28.dp))
        }
    }
}
