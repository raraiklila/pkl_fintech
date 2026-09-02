package com.example.pkl_finance.ui.screens

import androidx.compose.animation.core.*
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
import com.example.pkl_finance.ui.theme.AppWhite
import com.example.pkl_finance.ui.theme.BackgroundLight
import com.example.pkl_finance.ui.theme.Blue400
import com.example.pkl_finance.ui.theme.Blue50
import com.example.pkl_finance.ui.theme.DarkNavy
import com.example.pkl_finance.ui.theme.GoldAccent
import com.example.pkl_finance.ui.theme.PrimaryBlue
import com.example.pkl_finance.ui.theme.PrimaryBlueDark
import com.example.pkl_finance.ui.theme.PrimaryBlueLight
import com.example.pkl_finance.ui.theme.SlateGray
import com.example.pkl_finance.ui.theme.SuccessGreen
import com.example.pkl_finance.ui.theme.Yellow100
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
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Emas yang dimiliki",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${String.format(java.util.Locale.US, "%.4f", appState.goldBalance)}g",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.25f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Harga Beli",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black.copy(alpha = 0.65f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${appState.formatRupiah(appState.goldBuyPrice)}/g",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Column {
                            Text(
                                text = "Harga Jual (Buyback)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black.copy(alpha = 0.65f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${appState.formatRupiah(appState.goldSellPrice)}/g",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        val badgeBg = if (goldPrice.trend == "up") Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                        val badgeText = if (goldPrice.trend == "up") Color(0xFF15803D) else Color(0xFFB91C1C)

                        Box(
                            modifier = Modifier
                                .background(badgeBg, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$trendArrow $trendSign${String.format(java.util.Locale.US, "%.2f", goldPrice.percent)}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeText
                            )
                        }
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
                            appState.previousScreen = Screen.Emas
                            appState.navigateTo(Screen.JualEmas)
                        }
                    }
                )
                EmasActionItem(
                    label = "Riwayat Emas",
                    iconResId = R.drawable.ic_menu_cetak,
                    onClick = {
                        appState.navigateTo(Screen.RiwayatEmasDetail)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Perkembangan Emas header (tanpa Lihat Laporan)
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
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Line Chart 
            EmasAreaChart(appState = appState)

            Spacer(modifier = Modifier.height(24.dp))

            // Riwayat Emas (tanpa Lihat Semua)
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
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Filter all transaction history specifically for gold items
            val goldTransactions = remember(appState.transactionHistory) {
                appState.transactionHistory.filter {
                    it.type == "GOLD_BUY" || it.type == "GOLD_SELL" || it.goldAmount > 0.0
                }
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
    val history = appState.goldPriceHistory
    val currentPrice = appState.goldPriceRate.toFloat()

    // Build price list: use real history if available, else simulate from current price
    val prices = remember(history, currentPrice) {
        if (history.size >= 2) {
            history.map { it.price.toFloat() }
        } else {
            // Fallback: simulate 6 points trending to current price
            listOf(
                currentPrice * 0.960f,
                currentPrice * 0.972f,
                currentPrice * 0.981f,
                currentPrice * 0.989f,
                currentPrice * 0.995f,
                currentPrice
            )
        }
    }

    val minPrice = (prices.minOrNull() ?: currentPrice) * 0.998f
    val maxPrice = (prices.maxOrNull() ?: currentPrice) * 1.002f
    val priceRange = (maxPrice - minPrice).coerceAtLeast(1000f)

    // Build x-axis labels from history updated_at or generic labels
    val xLabels = remember(history) {
        if (history.size >= 2) {
            history.map { point ->
                val raw = point.updated_at ?: ""
                try {
                    val datePart = raw.substringBefore("T")
                    val parts = datePart.split("-")
                    if (parts.size == 3) "${parts[2]}/${parts[1]}" else raw.take(5)
                } catch (_: Exception) { raw.take(5) }
            }
        } else {
            listOf("T-5", "T-4", "T-3", "T-2", "T-1", "Hari ini")
        }
    }

    val chartProgress = remember { Animatable(0f) }
    LaunchedEffect(prices) {
        chartProgress.animateTo(1f, animationSpec = tween(1000, easing = FastOutSlowInEasing))
    }

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
                // Y-Axis Labels (in juta / millions for compact display)
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(40.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    val steps = 5
                    (steps downTo 0).forEach { i ->
                        val labelVal = minPrice + (priceRange / steps) * i
                        val labelJuta = labelVal / 1_000_000f
                        Text(
                            text = String.format(java.util.Locale.US, "%.2fJ", labelJuta),
                            fontSize = 9.sp,
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
                    val n = prices.size

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

                    val coords = prices.mapIndexed { index, price ->
                        val x = if (n > 1) index * (w / (n - 1).toFloat()) else w / 2f
                        val y = h - ((price - minPrice) / priceRange) * h
                        Offset(x, y)
                    }

                    // Draw Gradient Area under curve
                    val areaPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(coords[0].x, h)
                        lineTo(coords[0].x, coords[0].y)

                        for (i in 0 until coords.size - 1) {
                            val p0 = coords[i]
                            val p1 = coords[i + 1]
                            val conX1 = p0.x + (p1.x - p0.x) / 2f
                            val conX2 = conX1
                            cubicTo(conX1, p0.y, conX2, p1.y, p1.x, p1.y)
                        }

                        lineTo(coords.last().x, h)
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
                            val conX2 = conX1
                            cubicTo(conX1, p0.y, conX2, p1.y, p1.x, p1.y)
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

                    // Draw circles at data points with Expressive Glowing Halo on latest point
                    coords.forEachIndexed { idx, pt ->
                        if (idx == coords.lastIndex) {
                            // Expressive Halo Ring on latest price point
                            drawCircle(
                                color = PrimaryBlue.copy(alpha = (0.25f * chartProgress.value).coerceIn(0f, 1f)),
                                radius = 9.dp.toPx(),
                                center = pt
                            )
                        }
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

            // X-Axis Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp), // align with canvas start
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                xLabels.forEach { label ->
                    Text(
                        text = label,
                        fontSize = 9.sp,
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
    val isSell = trans.type == "GOLD_SELL"
    val titleText = when {
        isBuy -> "Beli Emas Digital"
        isSell -> "Jual Emas Digital"
        else -> "Autosplit Emas"
    }
    val subText = when {
        isBuy -> "Dari Saldo Merchant"
        isSell -> "Ke Saldo Merchant"
        else -> "Ke Cicilan Emas"
    }
    val weightText = if (isSell) {
        "-" + String.format("%.4f", kotlin.math.abs(trans.goldWeightAdded)) + " Gram"
    } else {
        "+" + String.format("%.4f", trans.goldWeightAdded) + " Gram"
    }
    val weightColor = if (isSell) Color(0xFFEF4444) else SuccessGreen
    val rupiahText = if (isSell) {
        "+" + appState.formatRupiah(trans.totalAmount).removePrefix("Rp ").let { "Rp $it" }
    } else {
        appState.formatRupiah(if (isBuy) -trans.totalAmount else trans.goldAmount)
    }

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
                    color = weightColor
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
