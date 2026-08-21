package com.example.pkl_finance.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.pkl_finance.R
import com.example.pkl_finance.ui.theme.AppWhite

@Composable
fun QrisMockCode(
    shopName: String,
    modifier: Modifier = Modifier
) {
    val seed = remember(shopName) {
        if (shopName.isEmpty()) 42L else shopName.hashCode().toLong()
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = AppWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Centered QRIS Branding Logo on top
            Icon(
                painter = painterResource(id = R.drawable.ic_qris_logo_branding),
                contentDescription = "QRIS Logo",
                tint = Color.Unspecified,
                modifier = Modifier
                    .height(28.dp)
                    .wrapContentWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // White box containing QR Code + red corners template background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Canvas for red corner templates
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Left red decal (slanted bar on left edge)
                    val leftPath = Path().apply {
                        moveTo(0f, h * 0.12f)
                        lineTo(w * 0.05f, h * 0.12f)
                        lineTo(w * 0.05f, h * 0.65f)
                        lineTo(0f, h * 0.72f)
                        close()
                    }
                    drawPath(path = leftPath, color = Color(0xFFEF4444))

                    // Bottom-right red decal (L-shaped corner border)
                    val rightPath = Path().apply {
                        moveTo(w * 0.55f, h)
                        lineTo(w * 0.62f, h * 0.94f)
                        lineTo(w * 0.94f, h * 0.94f)
                        lineTo(w * 0.94f, h * 0.62f)
                        lineTo(w, h * 0.55f)
                        lineTo(w, h)
                        close()
                    }
                    drawPath(path = rightPath, color = Color(0xFFEF4444))
                }

                // QR modules drawing Canvas (with padding to prevent overlapping red decals)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val gridCount = 29 // 29x29 detailed modules grid
                        val cellSize = width / gridCount

                        // Helper to draw black square blocks
                        fun drawBlock(col: Int, row: Int) {
                            drawRect(
                                color = Color.Black,
                                topLeft = Offset(col * cellSize, row * cellSize),
                                size = Size(cellSize + 0.5f, cellSize + 0.5f)
                            )
                        }

                        // Draw Finder Pattern (Top-Left, Top-Right, Bottom-Left)
                        fun drawFinderPattern(startCol: Int, startRow: Int) {
                            for (i in 0 until 7) {
                                for (j in 0 until 7) {
                                    if (i == 0 || i == 6 || j == 0 || j == 6) {
                                        drawBlock(startCol + i, startRow + j)
                                    }
                                }
                            }
                            for (i in 2..4) {
                                for (j in 2..4) {
                                    drawBlock(startCol + i, startRow + j)
                                }
                            }
                        }

                        drawFinderPattern(0, 0)
                        drawFinderPattern(gridCount - 7, 0)
                        drawFinderPattern(0, gridCount - 7)

                        for (i in 7 until gridCount - 7) {
                            if (i % 2 == 0) {
                                drawBlock(i, 6)
                                drawBlock(6, i)
                            }
                        }

                        val random = java.util.Random(seed)
                        for (row in 0 until gridCount) {
                            for (col in 0 until gridCount) {
                                val inTopLeft = col < 8 && row < 8
                                val inTopRight = col >= gridCount - 8 && row < 8
                                val inBottomLeft = col < 8 && row >= gridCount - 8
                                if (inTopLeft || inTopRight || inBottomLeft) continue
                                if (row == 6 || col == 6) continue

                                if (random.nextBoolean()) {
                                    drawBlock(col, row)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
