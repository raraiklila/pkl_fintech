package com.example.pkl_finance.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.pkl_finance.ui.theme.GoldAccent
import com.example.pkl_finance.ui.theme.PrimaryBlue
import kotlin.math.sin

/**
 * Material 3 Expressive Shape Morphing Loading Indicator
 * Custom Canvas-rendered morphing indicator with fluid spring shapes & rotating track.
 */
@Composable
fun ExpressiveLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = PrimaryBlue
) {
    FallbackExpressiveMorphingLoader(
        modifier = modifier,
        size = size,
        primaryColor = color
    )
}

/**
 * Custom Material 3 Expressive Wavy Circular Progress Indicator
 * As seen in M3 Expressive design specs (Circular progress with wavy dynamic track/stroke)
 */
@Composable
fun ExpressiveWavyCircularProgressIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    color: Color = PrimaryBlue,
    trackColor: Color = color.copy(alpha = 0.15f),
    strokeWidth: Dp = 3.5.dp,
    waveAmplitude: Float = 4f,
    waveFrequency: Int = 8
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wavyCircular")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Canvas(
        modifier = modifier
            .size(size)
            .rotate(rotation)
    ) {
        val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
        val baseRadius = (size.toPx() - strokeWidth.toPx() * 2) / 2f

        // Draw track circle
        drawCircle(
            color = trackColor,
            radius = baseRadius,
            center = center,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )

        // Draw Wavy Progress Arc
        val wavyPath = Path()
        val steps = 120
        val arcDegrees = 270f
        val startAngleRad = 0.0

        for (i in 0..steps) {
            val progressFraction = i.toFloat() / steps
            val angleRad = startAngleRad + Math.toRadians((arcDegrees * progressFraction).toDouble())
            val waveOffset = sin(angleRad * waveFrequency + wavePhase) * waveAmplitude
            val currentRadius = baseRadius + waveOffset

            val x = center.x + (currentRadius * kotlin.math.cos(angleRad)).toFloat()
            val y = center.y + (currentRadius * kotlin.math.sin(angleRad)).toFloat()

            if (i == 0) {
                wavyPath.moveTo(x, y)
            } else {
                wavyPath.lineTo(x, y)
            }
        }

        drawPath(
            path = wavyPath,
            color = color,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
    }
}

/**
 * Material 3 Expressive Wavy Linear Progress Indicator
 * Renders an animated sine wave active progress line (as seen in M3 Expressive design)
 */
@Composable
fun ExpressiveWavyLinearProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = PrimaryBlue,
    trackColor: Color = color.copy(alpha = 0.15f),
    strokeWidth: Dp = 4.dp,
    waveAmplitude: Float = 6f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wavyLinear")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (4 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    val progressX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "progressX"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(strokeWidth * 3)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        // Draw track
        drawLine(
            color = trackColor,
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = strokeWidth.toPx(),
            cap = StrokeCap.Round
        )

        // Draw animated Wavy line
        val wavePath = Path()
        val activeWidth = width * 0.45f
        val startX = (width - activeWidth) * progressX
        val endX = startX + activeWidth
        val steps = 60

        for (i in 0..steps) {
            val x = startX + (activeWidth * (i.toFloat() / steps))
            val waveAngle = (x / activeWidth) * (3 * Math.PI).toFloat() + phase
            val y = centerY + (sin(waveAngle) * waveAmplitude)

            if (i == 0) wavePath.moveTo(x, y) else wavePath.lineTo(x, y)
        }

        drawPath(
            path = wavePath,
            color = color,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
    }
}

/**
 * Fallback Expressive Morphing Loader with pulsing scaling shapes
 */
@Composable
private fun FallbackExpressiveMorphingLoader(
    modifier: Modifier = Modifier,
    size: Dp,
    primaryColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "expressiveFallback")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotation)
        ) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val radius = (size.toPx() - 12f) / 2f

            drawCircle(
                color = primaryColor.copy(alpha = 0.15f),
                radius = radius,
                center = center,
                style = Stroke(width = 6f)
            )

            val wavyPath = Path()
            val steps = 80
            for (i in 0..steps) {
                val angleRad = (i.toDouble() / steps) * (1.5 * Math.PI)
                val r = radius + sin(angleRad * 6).toFloat() * 3f
                val x = center.x + r * kotlin.math.cos(angleRad).toFloat()
                val y = center.y + r * kotlin.math.sin(angleRad).toFloat()
                if (i == 0) wavyPath.moveTo(x, y) else wavyPath.lineTo(x, y)
            }

            drawPath(
                path = wavyPath,
                color = primaryColor,
                style = Stroke(width = 8f, cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * Material 3 Expressive Bouncy Dots Loader
 */
@Composable
fun ExpressiveBouncyDotsLoader(
    modifier: Modifier = Modifier,
    dotSize: Dp = 8.dp,
    color: Color = Color.White
) {
    val infiniteTransition = rememberInfiniteTransition(label = "expressiveDots")

    @Composable
    fun animateDotOffset(delayMs: Int): Float {
        val offsetY by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -6f,
            animationSpec = infiniteRepeatable(
                animation = tween(350, delayMillis = delayMs, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dotOffset"
        )
        return offsetY
    }

    val offset1 = animateDotOffset(0)
    val offset2 = animateDotOffset(120)
    val offset3 = animateDotOffset(240)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(dotSize)
                .offset(y = offset1.dp)
                .background(color, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(dotSize)
                .offset(y = offset2.dp)
                .background(color, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(dotSize)
                .offset(y = offset3.dp)
                .background(color, CircleShape)
        )
    }
}

