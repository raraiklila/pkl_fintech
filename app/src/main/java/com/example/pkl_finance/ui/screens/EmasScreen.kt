package com.example.pkl_finance.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.pkl_finance.R
import com.example.pkl_finance.data.AppState
import com.example.pkl_finance.data.Screen
import com.example.pkl_finance.data.Transaction
import com.example.pkl_finance.ui.theme.*
import com.example.pkl_finance.ui.components.*

data class GoldPriceInfo(
    val price: Long,
    val change: Long,
    val percent: Double,
    val trend: String  // "up" or "down"
)

@Composable
fun EmasScreen(
    appState: AppState,
    modifier: Modifier = Modifier
) {
    var comingSoonFeature by remember { mutableStateOf<String?>(null) }
    var showVerificationDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp)
                .background(AppWhite)
                .padding(vertical = 16.dp, horizontal = 24.dp)
        ) {
            Text(
                text = "Dashboard Emas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkNavy,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Scrollable Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Hero Card
            // Simulasi respons API backend harga emas (harga Antam 12 Juli 2026)
            val goldPrice = remember(appState.goldPriceRate, appState.goldPriceTrend, appState.goldPricePercent) {
                GoldPriceInfo(
                    price = appState.goldPriceRate.toLong(),
                    change = appState.goldPriceChange.toLong(),
                    percent = appState.goldPricePercent,
                    trend = appState.goldPriceTrend
                )
            }
            val goldPriceFormatted = appState.formatRupiah(appState.goldPriceRate)
            val isUp = appState.goldPriceTrend == "up"
            val trendArrow = if (isUp) "▲" else "▼"
            val trendSign = if (isUp) "+" else ""
            val trendColor = if (isUp) SuccessGreen else Color(0xFFEF4444)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = Color(0xFFFAA137),
                        spotColor = Color(0xFFFAA137)
                    )
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFF0C735), Color(0xFFD98F39))
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clip(RoundedCornerShape(20.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.coin_gold),
                    contentDescription = "Gold Coin",
                    contentScale = ContentScale.Fit,
                    alpha = 0.35f,
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.CenterEnd)
                        .offset(x = 20.dp, y = 38.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 20.dp, bottom = 20.dp, end = 100.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Emas yang dimiliki",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${String.format(java.util.Locale.US, "%.4f", appState.goldBalance)}g",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column {
                        Text(
                            text = "Harga Emas",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$goldPriceFormatted / g",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$trendArrow $trendSign${String.format("%.2f", goldPrice.percent)}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = trendColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Actions 
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                EmasActionItem(
                    label = "Cicil Emas",
                    iconResId = R.drawable.ic_menu_cicil,
                    onClick = {
                        if (!appState.isVerified) {
                            showVerificationDialog = true
                        } else {
                            appState.previousScreen = Screen.Emas
                            appState.navigateTo(Screen.ConfigCicilEmas)
                        }
                    }
                )
                EmasActionItem(
                    label = "Beli Emas",
                    iconResId = R.drawable.ic_menu_beli,
                    onClick = {
                        if (!appState.isVerified) {
                            showVerificationDialog = true
                        } else {
                            appState.previousScreen = Screen.Emas
                            appState.navigateTo(Screen.BeliEmas)
                        }
                    }
                )
                EmasActionItem(
                    label = "Jual Emas",
                    iconResId = R.drawable.ic_menu_jual,
                    onClick = {
                        if (!appState.isVerified) {
                            showVerificationDialog = true
                        } else {
                            comingSoonFeature = "Jual Emas"
                        }
                    }
                )
                EmasActionItem(
                    label = "Cetak Emas",
                    iconResId = R.drawable.ic_menu_cetak,
                    onClick = {
                        if (!appState.isVerified) {
                            showVerificationDialog = true
                        } else {
                            comingSoonFeature = "Cetak Emas"
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Perkembangan Emas header with "Lihat Laporan" link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Perkembangan Emas",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkNavy
                )
                Text(
                    text = "Lihat Laporan",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    modifier = Modifier.clickable { comingSoonFeature = "Laporan Emas" }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Line Chart 
            EmasAreaChart(appState = appState)

            Spacer(modifier = Modifier.height(24.dp))

            // Riwayat Emas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Riwayat Emas",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkNavy
                )
                Text(
                    text = "Lihat Semua",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    modifier = Modifier.clickable { appState.navigateTo(Screen.RiwayatEmasDetail) }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Filter all transaction history specifically for gold items
            val goldTransactions = remember(appState.transactionHistory) {
                appState.transactionHistory.filter { it.type == "GOLD_BUY" || it.goldAmount > 0.0 }
            }

            if (goldTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada riwayat emas",
                        fontSize = 12.sp,
                        color = SlateGray
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Display top 5 gold items
                    goldTransactions.take(5).forEach { trans ->
                        GoldHistoryItem(
                            trans = trans,
                            appState = appState,
                            onClick = {
                                appState.selectedTransactionReceipt = trans
                                appState.navigateTo(Screen.ReceiptDetail)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = Color(0xFF10B981).copy(alpha = 0.15f),
                        spotColor = Color(0xFF10B981).copy(alpha = 0.15f)
                    )
                    .background(
                        color = PrimaryBlueLight,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(BorderStroke(1.dp, Blue400), RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shield icon circle
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = PrimaryBlue,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield Icon",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Aset Kamu Aman",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlueDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Emas disimpan oleh mitra kustodian terpercaya sesuai ketentuan yang berlaku.",
                            fontSize = 11.sp,
                            color = PrimaryBlue,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp)) // navbar spacing
        }
    }

    if (comingSoonFeature != null) {
        ComingSoonDialog(
            featureName = comingSoonFeature,
            onDismiss = { comingSoonFeature = null }
        )
    }

    if (showVerificationDialog) {
        VerificationRequiredDialog(
            onVerify = {
                showVerificationDialog = false
                appState.navigateTo(Screen.VerifikasiKyc)
            },
            onDismiss = {
                showVerificationDialog = false
            }
        )
    }
}

@Composable
fun EmasAreaChart(
    appState: AppState,
    modifier: Modifier = Modifier
) {
    val currentGram = appState.goldBalance
    val grams = remember(currentGram) {
        if (currentGram <= 0.0) {
            listOf(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
        } else {
            listOf(
                (currentGram * 0.15).toFloat(),
                (currentGram * 0.35).toFloat(),
                (currentGram * 0.55).toFloat(),
                (currentGram * 0.70).toFloat(),
                (currentGram * 0.85).toFloat(),
                currentGram.toFloat()
            )
        }
    }
    val maxGram = (grams.maxOrNull() ?: 1.0f).coerceAtLeast(0.1f)

    Card(
        colors = CardDefaults.cardColors(containerColor = AppWhite),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Layout Y-axis labels + Canvas Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                // Y-Axis Labels
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(32.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    val yLabels = listOf(
                        String.format(java.util.Locale.US, "%.1f", maxGram),
                        String.format(java.util.Locale.US, "%.1f", maxGram * 0.8f),
                        String.format(java.util.Locale.US, "%.1f", maxGram * 0.6f),
                        String.format(java.util.Locale.US, "%.1f", maxGram * 0.4f),
                        String.format(java.util.Locale.US, "%.1f", maxGram * 0.2f),
                        "0.0"
                    )
                    yLabels.forEach { label ->
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateGray
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Graph area (Smooth Area Bezier)
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    val w = size.width
                    val h = size.height

                    // Grid lines (horizontal)
                    val horizontalLines = 5
                    for (i in 0..horizontalLines) {
                        val y = i * (h / horizontalLines)
                        drawLine(
                            color = Color(0xFFF1F5F9),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                    }

                    val coords = grams.mapIndexed { index, gram ->
                        Offset(index * (w / 5f), h - (gram / maxGram) * h)
                    }

                    // Draw Gradient Area under curve
                    val areaPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(0f, h)
                        lineTo(coords[0].x, coords[0].y)
                        
                        for (i in 0 until coords.size - 1) {
                            val p0 = coords[i]
                            val p1 = coords[i + 1]
                            val conX1 = p0.x + (p1.x - p0.x) / 2f
                            val conY1 = p0.y
                            val conX2 = p0.x + (p1.x - p0.x) / 2f
                            val conY2 = p1.y
                            cubicTo(conX1, conY1, conX2, conY2, p1.x, p1.y)
                        }
                        
                        lineTo(w, h)
                        close()
                    }

                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(PrimaryBlue.copy(alpha = 0.25f), Color.Transparent),
                            startY = 0f,
                            endY = h
                        )
                    )

                    // Draw Curve Stroke
                    val strokePath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(coords[0].x, coords[0].y)
                        for (i in 0 until coords.size - 1) {
                            val p0 = coords[i]
                            val p1 = coords[i + 1]
                            val conX1 = p0.x + (p1.x - p0.x) / 2f
                            val conY1 = p0.y
                            val conX2 = p0.x + (p1.x - p0.x) / 2f
                            val conY2 = p1.y
                            cubicTo(conX1, conY1, conX2, conY2, p1.x, p1.y)
                        }
                    }

                    drawPath(
                        path = strokePath,
                        color = PrimaryBlue,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )

                    // Draw circles at data points
                    coords.forEach { pt ->
                        drawCircle(
                            color = PrimaryBlue,
                            radius = 4.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // X-Axis Labels (Months)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp), // align with canvas start
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val months = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun")
                months.forEach { month ->
                    Text(
                        text = month,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateGray
                    )
                }
            }
        }
    }
}

@Composable
fun EmasActionItem(
    label: String,
    iconResId: Int,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = Color.Black,
                    spotColor = Color.Black.copy(alpha = 0.25f)
                )
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(BorderStroke(1.dp, Color(0xFFF1F5F9)), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = label,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = DarkNavy
        )
    }
}

@Composable
fun GoldHistoryItem(
    trans: Transaction,
    appState: AppState,
    onClick: () -> Unit = {}
) {
    val isBuy = trans.type == "GOLD_BUY"
    val titleText = if (isBuy) "Beli Emas Fisik" else "Autosplit Emas"
    val subText = if (isBuy) "Dari Saldo Merchant" else "Ke Cicilan Emas"
    val weightText = "+" + String.format("%.4f", trans.goldWeightAdded) + " Gram"
    val rupiahText = appState.formatRupiah(if (isBuy) -trans.totalAmount else trans.goldAmount)

    Card(
        colors = CardDefaults.cardColors(containerColor = AppWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Yellow100, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Paid,
                    contentDescription = "Coin Icon",
                    tint = GoldAccent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titleText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkNavy
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subText,
                    fontSize = 12.sp,
                    color = SlateGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = trans.date,
                    fontSize = 11.sp,
                    color = SlateGray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = weightText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = rupiahText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkNavy
                )
            }
        }
    }
}
