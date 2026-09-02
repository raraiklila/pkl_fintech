package com.example.pkl_finance.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pkl_finance.ui.theme.*

enum class AppButtonVariant { Primary, Secondary, Tertiary, Danger, Disabled }

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppButtonVariant = AppButtonVariant.Primary,
    leadingIcon: ImageVector? = null,
    height: Dp = 50.dp,
    horizontalPadding: Dp = 16.dp,
    isLoading: Boolean = false,
) {
    val isDisabled = variant == AppButtonVariant.Disabled || isLoading

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && !isDisabled) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "expressiveButtonScale"
    )

    val containerColor: Color
    val contentColor: Color
    val borderColor: Color?
    val gradient: Brush?

    when (variant) {
        AppButtonVariant.Primary -> {
            gradient = Brush.horizontalGradient(
                colors = if (isPressed) listOf(Blue600, Blue700)
                         else listOf(Blue500, Blue600)
            )
            containerColor = Blue500
            contentColor   = ColorWhite
            borderColor    = null
        }
        AppButtonVariant.Secondary -> {
            gradient       = null
            containerColor = ColorWhite
            contentColor   = Blue500
            borderColor    = Blue500
        }
        AppButtonVariant.Tertiary -> {
            gradient       = null
            containerColor = Blue50
            contentColor   = Blue700
            borderColor    = Blue200
        }
        AppButtonVariant.Danger -> {
            gradient = Brush.horizontalGradient(
                colors = if (isPressed) listOf(Color(0xFFDC2626), Color(0xFFB91C1C))
                         else listOf(Color(0xFFEF4444), Color(0xFFDC2626))
            )
            containerColor = Color(0xFFEF4444)
            contentColor   = ColorWhite
            borderColor    = null
        }
        AppButtonVariant.Disabled -> {
            gradient       = null
            containerColor = ColorBorder
            contentColor   = ColorWhite
            borderColor    = null
        }
    }

    val animatedContainer by animateColorAsState(
        targetValue = containerColor,
        animationSpec = tween(150),
        label = "containerColor"
    )

    Box(
        modifier = modifier
            .height(height)
            .scale(scale)
            .then(
                if (variant == AppButtonVariant.Primary && !isDisabled)
                    Modifier.shadow(6.dp, CircleShape, spotColor = Blue500.copy(alpha = 0.35f))
                else Modifier
            )
            .clip(CircleShape)
            .then(
                if (gradient != null && variant == AppButtonVariant.Primary)
                    Modifier.background(gradient)
                else
                    Modifier.background(animatedContainer)
            )
            .then(
                if (borderColor != null)
                    Modifier.border(1.5.dp, borderColor, CircleShape)
                else Modifier
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isDisabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            ExpressiveBouncyDotsLoader(
                color = contentColor
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = horizontalPadding)
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    letterSpacing = 0.2.sp,
                    maxLines = 1,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
