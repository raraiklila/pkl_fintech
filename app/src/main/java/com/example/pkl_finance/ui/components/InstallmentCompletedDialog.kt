package com.example.pkl_finance.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.pkl_finance.ui.theme.*
import kotlinx.coroutines.isActive
import java.util.Locale
import kotlin.random.Random

private data class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var color: Color,
    var rotation: Float,
    var rotationSpeed: Float,
    var alpha: Float = 1f
)

@Composable
fun ConfettiCanvas(modifier: Modifier = Modifier) {
    val particles = remember {
        val colors = listOf(
            Color(0xFFF59E0B), // Gold
            Color(0xFFD97706), // Dark Gold
            Color(0xFF10B981), // Emerald Green
            Color(0xFF2563EB), // Royal Blue
            Color(0xFFEC4899), // Pink
            Color(0xFF8B5CF6)  // Purple
        )
        List(40) {
            ConfettiParticle(
                x = Random.nextFloat() * 600f,
                y = Random.nextFloat() * -300f,
                vx = (Random.nextFloat() - 0.5f) * 4f,
                vy = Random.nextFloat() * 4f + 3f,
                size = Random.nextFloat() * 10f + 6f,
                color = colors[Random.nextInt(colors.size)],
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 8f
            )
        }
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameNanos { _ ->
                particles.forEach { p ->
                    p.x += p.vx
                    p.y += p.vy
                    p.rotation += p.rotationSpeed
                    if (p.y > 1000f) {
                        p.y = -50f
                        p.x = Random.nextFloat() * 600f
                    }
                }
            }
        }
    }

    Canvas(modifier = modifier) {
        particles.forEach { p ->
            rotate(p.rotation, Offset(p.x, p.y)) {
                drawRect(
                    color = p.color,
                    topLeft = Offset(p.x - p.size / 2, p.y - p.size / 2),
                    size = Size(p.size, p.size * 0.6f),
                    alpha = p.alpha
                )
            }
        }
    }
}

@Composable
fun InstallmentCompletedDialog(
    targetWeight: Double,
    onDismiss: () -> Unit,
    onViewGold: () -> Unit,
    onStartNew: () -> Unit = {}
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dialogInstallmentScale"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Confetti animation layer
            ConfettiCanvas(modifier = Modifier.fillMaxSize())

            // Main Dialog Card (Matching Design System exactly)
            Card(
                colors = CardDefaults.cardColors(containerColor = AppWhite),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .scale(scale)
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Icon Badge
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(GoldAccentLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🪙", fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    Text(
                        text = "Cicilan Emas Lunas!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkNavy,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Subtitle
                    Text(
                        text = "Target tabungan emas Anda sudah tercapai 100%",
                        fontSize = 12.sp,
                        color = SlateGray,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Detail items
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Target Emas", fontSize = 13.sp, color = SlateGray)
                        Text(
                            text = String.format(Locale.US, "%.4f Gram", targetWeight),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkNavy
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status Kontrak", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                        Text(
                            text = "Lunas 100%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Yellow Info Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Emas telah otomatis ditambahkan ke Saldo Emas Anda.",
                            fontSize = 11.sp,
                            color = Color(0xFFB45309),
                            modifier = Modifier.padding(12.dp),
                            lineHeight = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action buttons (1 single row with concise button text)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppButton(
                            text = "Tutup",
                            onClick = onDismiss,
                            variant = AppButtonVariant.Secondary,
                            horizontalPadding = 8.dp,
                            modifier = Modifier.weight(1f)
                        )

                        AppButton(
                            text = "Lihat Emas",
                            onClick = {
                                onDismiss()
                                onViewGold()
                            },
                            variant = AppButtonVariant.Primary,
                            horizontalPadding = 8.dp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
