package com.example.pkl_finance.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pkl_finance.data.AppState
import com.example.pkl_finance.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrisDownloadTemplate(
    appState: AppState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val seed = remember(appState.shopName) {
        if (appState.shopName.isEmpty()) 42L else appState.shopName.hashCode().toLong()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE2E8F0)) // slate blue gray overlay bg
        ) {
        // Toolbar header
        TopAppBar(
            title = {
                Text(
                    text = "Preview Cetak QRIS",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkNavy
                )
            },
            navigationIcon = {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = DarkNavy
                    )
                }
            },
            actions = {
                TextButton(
                    onClick = {
                        Toast.makeText(context, "QRIS berhasil disimpan ke Galeri!", Toast.LENGTH_LONG).show()
                        onDismiss()
                    }
                ) {
                    Text(
                        text = "Simpan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        // Scrollable Print Sheet viewport
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            // Printable A4/Stand Card ratio container
            Box(
                modifier = Modifier
                    .width(360.dp)
                    .height(512.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
            ) {
                // 1. Watermark Concentric Squares Graphics (Left and Bottom Border)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    // concentric tiles drawer
                    fun DrawScope.drawWatermarkTile(x: Float, y: Float, size: Float) {
                        val steps = 3
                        for (i in 0 until steps) {
                            val offset = i * (size / 8f)
                            val rectSize = size - 2 * offset
                            drawRect(
                                color = Color.Black.copy(alpha = 0.04f),
                                topLeft = Offset(x + offset, y + offset),
                                size = Size(rectSize, rectSize),
                                style = Stroke(width = 1.dp.toPx())
                            )
                        }
                    }

                    // Left margins watermark vertical strip
                    val tileSize = 54.dp.toPx()
                    var yPos = 0f
                    while (yPos < height) {
                        drawWatermarkTile(0f, yPos, tileSize)
                        yPos += tileSize
                    }

                    // Bottom margins watermark horizontal strip
                    var xPos = 0f
                    while (xPos < width) {
                        drawWatermarkTile(xPos, height - tileSize, tileSize)
                        xPos += tileSize
                    }
                }

                // 2. Red Decal Path overlays around QR
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    // Left rose-red border chevron
                    val leftPath = Path().apply {
                        moveTo(0f, height * 0.22f)
                        lineTo(width * 0.15f, height * 0.35f)
                        lineTo(0f, height * 0.48f)
                        close()
                    }
                    drawPath(path = leftPath, color = Color(0xFFE11D48))

                    // Bottom-right corner rose-red angled polygon
                    val rightPath = Path().apply {
                        moveTo(width, height * 0.72f)
                        lineTo(width * 0.65f, height)
                        lineTo(width, height)
                        close()
                    }
                    drawPath(path = rightPath, color = Color(0xFFE11D48))
                }

                // 3. Branded Header row: [QRIS] standard + GPN Wings logo
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // QRIS Text and brackets logo
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⌈",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "QRIS",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "⌉",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(
                                text = "QR Code Standar",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "Pembayaran Nasional",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }

                    // GPN Logo (Red bird wing + GPN text)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(42.dp)
                    ) {
                        Canvas(modifier = Modifier.size(24.dp)) {
                            // Wing shape
                            val wingPath = Path().apply {
                                moveTo(size.width * 0.1f, size.height * 0.7f)
                                cubicTo(size.width * 0.2f, size.height * 0.1f, size.width * 0.8f, size.height * 0.05f, size.width * 0.9f, size.height * 0.2f)
                                cubicTo(size.width * 0.6f, size.height * 0.3f, size.width * 0.5f, size.height * 0.5f, size.width * 0.7f, size.height * 0.5f)
                                cubicTo(size.width * 0.4f, size.height * 0.6f, size.width * 0.3f, size.height * 0.8f, size.width * 0.1f, size.height * 0.7f)
                                close()
                            }
                            drawPath(path = wingPath, color = Color(0xFFEF4444))
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "GPN",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF003087)
                        )
                    }
                }

                // 4. Content Area (Merchant Shop name, NMID, Terminal)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 74.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (appState.shopName.isNotEmpty()) appState.shopName.uppercase() else "RARAWR STYLE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "NMID: ID102030405060",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "A01",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // QR Card Canvas
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .background(Color.White)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val width = size.width
                            val gridCount = 29
                            val cellSize = width / gridCount

                            fun drawBlock(col: Int, row: Int) {
                                drawRect(
                                    color = Color.Black,
                                    topLeft = Offset(col * cellSize, row * cellSize),
                                    size = Size(cellSize + 0.5f, cellSize + 0.5f)
                                )
                            }

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

                // 5. Instruction Bottom Banner Area (Cara Pembayaran QRIS)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    // Right-bottom white detailed logs
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 12.dp, start = 16.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = "Dicetak oleh: 93600914",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateGray
                        )
                        Text(
                            text = "Versi cetak: V0.0.2025.09.26",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateGray
                        )
                    }

                    // Overlay Instructions Row
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 12.dp, end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Cara pembayaran QRIS",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                PaymentStepIcon(icon = "📱", label = "Buka Aplikasi\nBerlogo QRIS")
                                PaymentStepIcon(icon = "🔍", label = "Scan dan cek")
                                PaymentStepIcon(icon = "✅", label = "Bayar")
                            }
                        }
                    }
                }

                // ASPI QRIS validation sub-labels
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 110.dp)
                ) {
                    Text(
                        text = "SATU QRIS UNTUK SEMUA",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                    Text(
                        text = "Cek aplikasi penyelenggara di: www.aspi-qris.id",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateGray
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentStepIcon(icon: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 6.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            lineHeight = 7.sp,
            textAlign = TextAlign.Center
        )
    }
}
