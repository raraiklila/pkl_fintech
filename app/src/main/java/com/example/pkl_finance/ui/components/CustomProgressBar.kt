package com.example.pkl_finance.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CustomProgressBar(
    progress: Float, // 0f to 1f
    color: Color,
    trackColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        
        // Inner dimensions of the actual line
        val lineThickness = 6.dp.toPx()
        val radius = height / 2f
        val startX = radius
        val endX = width - radius
        val activeWidth = endX - startX
        
        // Draw track
        drawRoundRect(
            color = trackColor,
            topLeft = Offset(startX, centerY - lineThickness / 2f),
            size = Size(activeWidth, lineThickness),
            cornerRadius = CornerRadius(lineThickness / 2f, lineThickness / 2f)
        )
        
        if (progress > 0f) {
            val progressWidth = activeWidth * progress
            
            // Draw progress line
            drawRoundRect(
                color = color,
                topLeft = Offset(startX, centerY - lineThickness / 2f),
                size = Size(progressWidth, lineThickness),
                cornerRadius = CornerRadius(lineThickness / 2f, lineThickness / 2f)
            )
            
            // Draw thumb circle at the end of progress line
            drawCircle(
                color = color,
                radius = radius,
                center = Offset(startX + progressWidth, centerY)
            )
        }
    }
}
