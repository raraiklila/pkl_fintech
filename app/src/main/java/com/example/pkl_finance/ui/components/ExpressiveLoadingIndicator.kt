package com.example.pkl_finance.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.pkl_finance.ui.theme.GoldAccent
import com.example.pkl_finance.ui.theme.PrimaryBlue

/**
 * Material 3 Expressive Loading Indicator
 * Features a smooth rotating gradient arc and spring-pulsing core.
 */
@Composable
fun ExpressiveLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = PrimaryBlue,
    accentColor: Color = GoldAccent
) {
    val infiniteTransition = rememberInfiniteTransition(label = "expressiveLoading")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scalePulse"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Rotating Outer Ring
        CircularProgressIndicator(
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotation),
            color = color,
            strokeWidth = (size.value * 0.08f).dp,
            trackColor = color.copy(alpha = 0.15f)
        )

        // Pulsing Expressive Center Core
        Box(
            modifier = Modifier
                .size(size * 0.35f)
                .scale(scalePulse)
                .background(accentColor, CircleShape)
        )
    }
}

/**
 * Material 3 Expressive Bouncy Dots Loader
 * Uses 3 spring-animated dots jumping in rhythm.
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
