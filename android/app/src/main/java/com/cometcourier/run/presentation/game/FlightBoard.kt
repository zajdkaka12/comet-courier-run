package com.cometcourier.run.presentation.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.cometcourier.run.R
import com.cometcourier.run.core.theme.*
import com.cometcourier.run.core.ui.CourierArtwork
import com.cometcourier.run.domain.model.*
import kotlin.math.abs

@Composable
fun FlightBoard(snapshot: FlightSnapshot?, enabled: Boolean, buttonsOnly: Boolean, compact: Boolean, onMove: (Int) -> Unit, onPause: () -> Unit, modifier: Modifier = Modifier) {
    val latestMove by rememberUpdatedState(onMove)
    val pickupScale = remember { Animatable(1f) }
    val motion = LocalMotionEnabled.current
    LaunchedEffect(snapshot?.statusSequence) {
        if (motion && snapshot?.status == FlightStatus.Parcel) pickupScale.animateTo(1f, keyframes { durationMillis = 180; 0.88f at 80 })
        if (motion && snapshot?.status == FlightStatus.Delivery) pickupScale.animateTo(1f, keyframes { durationMillis = 200; 1.08f at 100 })
    }
    val threshold = with(LocalDensity.current) { 24.dp.toPx() }
    val moveLeft = stringResource(R.string.move_left)
    val moveRight = stringResource(R.string.move_right)
    val pause = stringResource(R.string.pause_flight)
    val routeDescription = stringResource(R.string.route_description)
    Surface(modifier.clip(RoundedCornerShape(18.dp)).semantics {
        contentDescription = routeDescription
        if (enabled) customActions = listOf(
            CustomAccessibilityAction(moveLeft) { latestMove(-1); true },
            CustomAccessibilityAction(moveRight) { latestMove(1); true },
            CustomAccessibilityAction(pause) { onPause(); true }
        )
    }.pointerInput(enabled, buttonsOnly) {
        if (enabled && !buttonsOnly) {
            var horizontal = 0f
            var vertical = 0f
            detectDragGestures(onDragStart = { horizontal = 0f; vertical = 0f }, onDragCancel = { horizontal = 0f; vertical = 0f },
                onDragEnd = { if (abs(horizontal) >= threshold && abs(horizontal) > abs(vertical) * 1.5f) latestMove(if (horizontal < 0) -1 else 1) },
                onDrag = { change, amount -> change.consume(); horizontal += amount.x; vertical += amount.y })
        }
    }, color = SpaceTop.copy(alpha = 0.32f), shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, TrailCyan.copy(alpha = 0.32f)), shadowElevation = 8.dp) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val boardWidth = maxWidth
            val boardHeight = maxHeight
            Canvas(Modifier.fillMaxSize()) {
                drawRect(Brush.horizontalGradient(listOf(SpaceTop.copy(alpha = 0.5f), TrailCyan.copy(alpha = 0.035f), SpaceTop.copy(alpha = 0.5f))))
                for (lane in 1..2) drawLine(TrailCyan.copy(alpha = 0.28f), Offset(size.width * lane / 3f, 0f), Offset(size.width * lane / 3f, size.height),
                    strokeWidth = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12.dp.toPx(), 12.dp.toPx()), (snapshot?.distance?.toFloat() ?: 0f) * 4f))
                drawLine(SoftMint.copy(alpha = 0.10f), Offset(0f, size.height * 0.8f), Offset(size.width, size.height * 0.8f), strokeWidth = 1.dp.toPx())
                val shipX = ((snapshot?.shipPosition?.toFloat() ?: 1f) + 0.5f) / 3f * size.width
                drawLine(Brush.verticalGradient(listOf(TrailCyan.copy(alpha = 0.8f), TrailCyan.copy(alpha = 0f))), Offset(shipX, size.height * 0.82f), Offset(shipX, size.height), strokeWidth = 8.dp.toPx())
                if (snapshot?.shield != false) drawCircle(TrailCyan.copy(alpha = 0.4f), if (compact) 23.dp.toPx() else 34.dp.toPx(), Offset(shipX, size.height * 0.8f), style = Stroke(1.dp.toPx()))
            }
            snapshot?.objects?.filter { it.crossingDistance - snapshot.distance <= 120.0 }?.forEach { obj ->
                val spriteSize = if (compact) 40.dp else when (obj.kind) { ObjectKind.Asteroid -> 60.dp; ObjectKind.Parcel -> 32.dp; ObjectKind.Ring -> 80.dp; ObjectKind.Shield -> 36.dp }
                val art = when (obj.kind) { ObjectKind.Asteroid -> R.drawable.sprite_5; ObjectKind.Parcel -> R.drawable.sprite_2; ObjectKind.Ring -> R.drawable.sprite_4; ObjectKind.Shield -> R.drawable.sprite_3 }
                Box(Modifier.offset(x = boardWidth * ((obj.lane + 0.5f) / 3f) - spriteSize / 2, y = boardHeight * obj.y - spriteSize / 2).size(spriteSize)) {
                    if (obj.kind == ObjectKind.Ring) Canvas(Modifier.fillMaxSize()) { drawCircle(TrailCyan.copy(alpha = 0.8f), size.minDimension * 0.46f, style = Stroke(3.dp.toPx())) }
                    Image(painterResource(art), null, Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                }
            }
            val shipSize = if (compact) 40.dp else 52.dp
            CourierArtwork(snapshot?.hullId ?: "sprout", shipSize,
                Modifier.offset(x = boardWidth * (((snapshot?.shipPosition?.toFloat() ?: 1f) + 0.5f) / 3f) - shipSize / 2, y = boardHeight * 0.8f - shipSize / 2).scale(pickupScale.value), decorative = true)
        }
    }
}
