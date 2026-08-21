package com.example.pkl_finance.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.pkl_finance.data.Screen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pkl_finance.data.AppState
import com.example.pkl_finance.ui.theme.*
import java.util.Locale
import kotlin.math.abs

private data class ChartPoint(
    val label: String,
    val gram: Float,
    val portfolioValue: Long,
    val goldPrice: Long,
    val gain: Long
)

private data class ActivityEntry(
    val date: String,
    val type: String,
    val gram: Double,
    val amount: Long,
    val priceChange: Double = 0.0
)

private val periods = listOf("Hari Ini", "7 Hari", "1 Bulan", "3 Bulan", "Semua")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanEmasScreen(appState: AppState) {
    var selectedPeriod by remember { mutableStateOf("Hari Ini") }
    var selectedPoint by remember { mutableStateOf<ChartPoint?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }

    val dynamicChartPoints = remember(appState.goldBalance, appState.goldPriceRate) {
        val currentGram = appState.goldBalance
        val price = appState.goldPriceRate
        if (currentGram <= 0.0) {
            listOf(
                ChartPoint("Jan", 0.0f, 0, price.toLong(), 0),
                ChartPoint("Feb", 0.0f, 0, price.toLong(), 0),
                ChartPoint("Mar", 0.0f, 0, price.toLong(), 0),
                ChartPoint("Apr", 0.0f, 0, price.toLong(), 0),
                ChartPoint("Mei", 0.0f, 0, price.toLong(), 0),
                ChartPoint("Saat Ini", 0.0f, 0, price.toLong(), 0)
            )
        } else {
            val p1 = (currentGram * 0.15).toFloat()
            val p2 = (currentGram * 0.35).toFloat()
            val p3 = (currentGram * 0.55).toFloat()
            val p4 = (currentGram * 0.70).toFloat()
            val p5 = (currentGram * 0.85).toFloat()
            val p6 = currentGram.toFloat()

            listOf(
                ChartPoint("Jan", p1, (p1 * price).toLong(), price.toLong(), (p1 * 50_000).toLong()),
                ChartPoint("Feb", p2, (p2 * price).toLong(), price.toLong(), (p2 * 50_000).toLong()),
                ChartPoint("Mar", p3, (p3 * price).toLong(), price.toLong(), (p3 * 50_000).toLong()),
                ChartPoint("Apr", p4, (p4 * price).toLong(), price.toLong(), (p4 * 50_000).toLong()),
                ChartPoint("Mei", p5, (p5 * price).toLong(), price.toLong(), (p5 * 50_000).toLong()),
                ChartPoint("Saat Ini", p6, (p6 * price).toLong(), price.toLong(), (p6 * 100_000).toLong())
            )
        }
    }

    val chartPoints = remember(selectedPeriod, dynamicChartPoints) {
        when (selectedPeriod) {
            "Hari Ini" -> dynamicChartPoints.takeLast(1)
            "7 Hari"  -> dynamicChartPoints.takeLast(2)
            "1 Bulan" -> dynamicChartPoints.takeLast(3)
            "3 Bulan" -> dynamicChartPoints.takeLast(4)
            else      -> dynamicChartPoints
        }
    }

    val activities = remember(selectedPeriod, appState.transactionHistory) {
        val real = appState.transactionHistory.map { tx ->
            ActivityEntry(
                date = tx.date,
                type = tx.type,
                gram = tx.goldWeightAdded,
                amount = tx.totalAmount.toLong()
            )
        }
        if (real.isNotEmpty()) real else emptyList()
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // Header
            item { LaporanHeader(onClose = { appState.navigateTo(Screen.Emas) }) }

            // 1. Portfolio Summary
            item { PortfolioSummaryCard(appState = appState) }

            // 2. Period Filter
            item {
                PeriodFilterRow(selected = selectedPeriod, onSelect = { selectedPeriod = it })
            }

            // 3. Interactive Chart
            item {
                InteractiveChartCard(points = chartPoints, onPointTap = { pt ->
                    selectedPoint = pt
                    showBottomSheet = true
                })
            }

            // 5. Statistics
            item { StatisticsCard(appState = appState) }

            // 6. Timeline title + items
            // item { SectionTitle("Timeline Aktivitas") }
            // items(activities.size) { i ->
            //    ActivityTimelineItem(
            //        entry = activities[i],
            //        isLast = i == activities.lastIndex
            //    )
            // }

            // 7. Activity summary
            item { ActivitySummaryCard(appState = appState) }

            // 8. Insight
            item { InsightComingSoonCard() }
        }
    }

    // 4. Bottom Sheet
    if (showBottomSheet && selectedPoint != null) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            ChartPointDetailSheet(
                point = selectedPoint!!,
                appState = appState
            )
        }
    }
}

@Composable
private fun LaporanHeader(onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(DarkNavy, Color(0xFF1E3A5F))
                )
            )
            .padding(horizontal = 8.dp)
            .padding(top = 52.dp, bottom = 24.dp)
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Kembali", tint = Color.White)
        }
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Laporan Emas", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Perkembangan aset kamu", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun PortfolioSummaryCard(appState: AppState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .shadow(
                8.dp, RoundedCornerShape(20.dp),
                ambientColor = Color(0xFFFAA137).copy(alpha = 0.25f),
                spotColor = Color(0xFFFAA137).copy(alpha = 0.25f)
            )
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFF0C735), Color(0xFFD98F39))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column {
            Text("Nilai Portofolio Saat Ini", fontSize = 11.sp,
                color = Color.Black.copy(alpha = 0.55f), fontWeight = FontWeight.Medium)
            Text(appState.formatRupiah(2_845_000.0), fontSize = 30.sp,
                fontWeight = FontWeight.Black, color = Color.White)

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.25f))
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Keuntungan sejak mulai", fontSize = 10.sp,
                        color = Color.Black.copy(alpha = 0.55f))
                    Text("+Rp245.000 (+9.42%)", fontSize = 13.sp,
                        fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Emas Dimiliki", fontSize = 10.sp,
                        color = Color.Black.copy(alpha = 0.55f))
                    Text("${String.format(java.util.Locale.US, "%.4f", appState.goldBalance)} gram", fontSize = 13.sp,
                        fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun PeriodFilterRow(selected: String, onSelect: (String) -> Unit) {
    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(periods.size) { index ->
            val period = periods[index]
            val isSelected = period == selected
            Box(
                modifier = Modifier
                    .background(
                        color = if (isSelected) PrimaryBlue else Color.White,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onSelect(period) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = period,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) Color.White else SlateGray
                )
            }
        }
    }
}

@Composable
private fun InteractiveChartCard(
    points: List<ChartPoint>,
    onPointTap: (ChartPoint) -> Unit
) {
    var tappedIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Grafik Perkembangan", fontSize = 13.sp,
                    fontWeight = FontWeight.Bold, color = DarkNavy)
                Text("Tekan titik untuk detail", fontSize = 10.sp, color = SlateGray)
            }
            Spacer(Modifier.height(16.dp))

            if (points.size < 2) {
                Box(
                    Modifier.fillMaxWidth().height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum cukup data untuk periode ini", fontSize = 12.sp, color = SlateGray)
                }
            } else {
                var canvasW by remember { mutableFloatStateOf(0f) }
                var canvasH by remember { mutableFloatStateOf(0f) }
                val tapRadiusPx = 60f

                Row(Modifier.fillMaxWidth().height(220.dp)) {
                    // Y-axis
                    val maxGram = points.maxOf { it.gram }
                    Column(
                        Modifier.fillMaxHeight().width(36.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        listOf(
                            String.format("%.1f", maxGram),
                            String.format("%.1f", maxGram * 0.75f),
                            String.format("%.1f", maxGram * 0.5f),
                            String.format("%.1f", maxGram * 0.25f),
                            "0.0"
                        ).forEach { Text(it, fontSize = 9.sp, color = SlateGray) }
                    }
                    Spacer(Modifier.width(8.dp))

                    Canvas(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .pointerInput(points) {
                                detectTapGestures { offset ->
                                    if (canvasW == 0f || canvasH == 0f) return@detectTapGestures
                                    val step = if (points.size > 1)
                                        canvasW / (points.size - 1) else canvasW
                                    val maxG = points.maxOf { it.gram }
                                    points.forEachIndexed { i, pt ->
                                        val cx = i * step
                                        val cy = canvasH - (pt.gram / maxG) * canvasH
                                        val dist = abs(offset.x - cx) + abs(offset.y - cy)
                                        if (dist < tapRadiusPx) {
                                            tappedIndex = i
                                            onPointTap(pt)
                                        }
                                    }
                                }
                            }
                    ) {
                        val w = size.width; val h = size.height
                        canvasW = w; canvasH = h
                        val maxG = points.maxOf { it.gram }
                        val step = if (points.size > 1) w / (points.size - 1) else w

                        val coords = points.mapIndexed { i, pt ->
                            Offset(i * step, h - (pt.gram / maxG) * h)
                        }

                        // Grid
                        repeat(5) { i ->
                            drawLine(Color(0xFFF1F5F9), Offset(0f, i * h / 4), Offset(w, i * h / 4), 1f)
                        }

                        // Area fill
                        val area = Path().apply {
                            moveTo(0f, h); lineTo(coords.first().x, coords.first().y)
                            for (i in 0 until coords.size - 1) {
                                val cx = (coords[i].x + coords[i+1].x) / 2f
                                cubicTo(cx, coords[i].y, cx, coords[i+1].y, coords[i+1].x, coords[i+1].y)
                            }
                            lineTo(w, h); close()
                        }
                        drawPath(area, Brush.verticalGradient(
                            listOf(PrimaryBlue.copy(0.25f), Color.Transparent), 0f, h
                        ))

                        // Line
                        val line = Path().apply {
                            moveTo(coords.first().x, coords.first().y)
                            for (i in 0 until coords.size - 1) {
                                val cx = (coords[i].x + coords[i+1].x) / 2f
                                cubicTo(cx, coords[i].y, cx, coords[i+1].y, coords[i+1].x, coords[i+1].y)
                            }
                        }
                        drawPath(line, PrimaryBlue, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))

                        // Dots
                        coords.forEachIndexed { i, pt ->
                            val isTapped = tappedIndex == i
                            if (isTapped) drawCircle(PrimaryBlue.copy(0.2f), 14.dp.toPx(), pt)
                            drawCircle(PrimaryBlue, if (isTapped) 6.dp.toPx() else 4.dp.toPx(), pt)
                            drawCircle(Color.White, if (isTapped) 3.dp.toPx() else 2.dp.toPx(), pt)
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth().padding(start = 44.dp),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    points.forEach {
                        Text(it.label, fontSize = 10.sp, color = SlateGray, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartPointDetailSheet(point: ChartPoint, appState: AppState) {
    val isProfit = point.gain >= 0
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp)
    ) {
        Box(Modifier.width(40.dp).height(4.dp)
            .background(Color(0xFFE2E8F0), CircleShape)
            .align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(16.dp))

        Text("${point.label} 2026", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailBox("Nilai Portofolio", appState.formatRupiah(point.portfolioValue.toDouble()), Modifier.weight(1f))
            DetailBox("Total Gram", "${String.format("%.2f", point.gram)} gram", Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailBox("Harga Emas", appState.formatRupiah(point.goldPrice.toDouble()) + " / g", Modifier.weight(1f))
            DetailBox(
                "Keuntungan",
                (if (isProfit) "+" else "") + appState.formatRupiah(point.gain.toDouble()),
                Modifier.weight(1f),
                if (isProfit) SuccessGreen else Color(0xFFEF4444)
            )
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = Color(0xFFE2E8F0))
        Spacer(Modifier.height(16.dp))

        Text("Aktivitas Bulan Ini", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
        Spacer(Modifier.height(12.dp))

        listOf(
            Triple("Autosplit QRIS", "+0.06 gram", SuccessGreen),
            Triple("Cicilan Emas", "+0.02 gram", SuccessGreen)
        ).forEach { (name, gram, color) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).background(color, CircleShape))
                    Spacer(Modifier.width(10.dp))
                    Text(name, fontSize = 13.sp, color = DarkNavy)
                }
                Text(gram, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = color)
            }
        }
    }
}

@Composable
private fun DetailBox(label: String, value: String, modifier: Modifier, valueColor: Color = DarkNavy) {
    Column(
        modifier = modifier
            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(label, fontSize = 10.sp, color = SlateGray)
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun StatisticsCard(appState: AppState) {
    val currentGoldValue = appState.goldBalance * appState.goldPriceRate

    val goldTransactions = appState.transactionHistory.filter { it.type == "GOLD_BUY" || it.goldAmount > 0 }
    val totalInvestment = if (goldTransactions.isNotEmpty()) {
        goldTransactions.sumOf { if (it.type == "GOLD_BUY") it.totalAmount else it.goldAmount }
    } else {
        if (appState.goldBalance > 0) appState.goldBalance * 2_500_000.0 else 0.0
    }

    val profit = currentGoldValue - totalInvestment
    val returnPercent = if (totalInvestment > 0) (profit / totalInvestment) * 100 else 0.0

    val profitSign = if (profit >= 0) "+" else ""
    val profitColor = if (profit >= 0) SuccessGreen else Color(0xFFEF4444)
    val returnSign = if (returnPercent >= 0) "+" else ""

    val stats = listOf(
        Triple("Modal Investasi",     appState.formatRupiah(totalInvestment),   DarkNavy),
        Triple("Nilai Sekarang",      appState.formatRupiah(currentGoldValue),   DarkNavy),
        Triple("Keuntungan",          "$profitSign${appState.formatRupiah(profit)}", profitColor),
        Triple("Return",              "$returnSign${String.format(Locale.US, "%.2f", returnPercent)}%", profitColor),
        Triple("Harga Emas Hari Ini", "${appState.formatRupiah(appState.goldPriceRate)}/g", GoldAccent)
    )
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Ringkasan Statistik", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
            Spacer(Modifier.height(12.dp))
            stats.forEachIndexed { i, (label, value, color) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(label, fontSize = 13.sp, color = SlateGray)
                    Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
                }
                if (i < stats.lastIndex) HorizontalDivider(color = Color(0xFFF1F5F9))
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkNavy,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)
    )
}


@Composable
private fun ActivityTimelineItem(entry: ActivityEntry, isLast: Boolean) {
    val isPrice = entry.type == "PRICE"
    val isPositive = if (isPrice) entry.priceChange >= 0 else true
    val icon = when (entry.type) {
        "AUTOSPLIT" -> "📲"; "CICILAN" -> "🔄"; "TOPUP" -> "💰"
        "PRICE" -> if (isPositive) "📈" else "📉"; else -> "•"
    }
    val iconBg = when (entry.type) {
        "AUTOSPLIT" -> PrimaryBlue.copy(0.12f); "CICILAN" -> Color(0xFFEDE9FE)
        "TOPUP" -> GoldAccentLight
        "PRICE" -> if (isPositive) Color(0xFFD1FAE5) else Color(0xFFFFE4E6)
        else -> Color(0xFFF1F5F9)
    }
    val title = when (entry.type) {
        "AUTOSPLIT" -> "Autosplit QRIS"; "CICILAN" -> "Cicilan Emas"; "TOPUP" -> "Top Up Manual"
        "PRICE" -> if (isPositive) "Harga emas naik" else "Harga emas turun"; else -> "-"
    }

    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.Top) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 4.dp)) {
            Box(Modifier.size(36.dp).background(iconBg, CircleShape), Alignment.Center) {
                Text(icon, fontSize = 16.sp)
            }
            if (!isLast) Box(Modifier.width(2.dp).height(36.dp).background(Color(0xFFE2E8F0)))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f).padding(bottom = if (isLast) 0.dp else 16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkNavy)
                Text(entry.date, fontSize = 11.sp, color = SlateGray)
            }
            Spacer(Modifier.height(2.dp))
            if (isPrice) {
                Text(
                    "${if (isPositive) "+" else ""}${String.format("%.1f", entry.priceChange)}%",
                    fontSize = 12.sp, fontWeight = FontWeight.Medium,
                    color = if (isPositive) SuccessGreen else Color(0xFFEF4444)
                )
            } else {
                val amtStr = if (entry.amount >= 1_000_000)
                    "Rp${String.format("%.1f", entry.amount / 1_000_000.0)}jt"
                else "Rp${String.format("%,d", entry.amount).replace(",", ".")}"
                Text("+${String.format("%.2f", entry.gram)} gram  •  $amtStr",
                    fontSize = 12.sp, color = SlateGray)
            }
        }
    }
}

// ─── 7. Activity Summary ──────────────────────────────────────────────────────

@Composable
private fun ActivitySummaryCard(appState: AppState) {
    val goldTxs = appState.transactionHistory.filter { it.type == "GOLD_BUY" || it.goldAmount > 0 }
    val totalCount = goldTxs.size
    val qrisCount = goldTxs.count { it.type == "QRIS_IN" || it.type == "AUTOSPLIT_GOLD" }
    val buyCount = goldTxs.count { it.type == "GOLD_BUY" }
    val otherCount = (totalCount - qrisCount - buyCount).coerceAtLeast(0)

    val totalGram = goldTxs.sumOf { it.goldWeightAdded }
    val avgGramPerTx = if (totalCount > 0) totalGram / totalCount else 0.0

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Ringkasan Aktivitas", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "$totalCount" to "Total Tx",
                    "$qrisCount" to "Autosplit",
                    "$buyCount" to "Beli Emas",
                    "$otherCount" to "Lainnya"
                ).forEach { (v, l) ->
                    Column(
                        Modifier.weight(1f)
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(v, fontSize = 16.sp, fontWeight = FontWeight.Black, color = DarkNavy)
                        Text(l, fontSize = 9.sp, color = SlateGray, textAlign = TextAlign.Center)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Rata-rata per transaksi", fontSize = 12.sp, color = SlateGray)
                Text("${String.format(Locale.US, "%.3f", avgGramPerTx)} gram / tx", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
            }
        }
    }
}

@Composable
private fun InsightComingSoonCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .shadow(6.dp, RoundedCornerShape(20.dp),
                ambientColor = PrimaryBlue.copy(0.12f), spotColor = PrimaryBlue.copy(0.12f))
            .background(
                Brush.linearGradient(listOf(Color(0xFF1E3A5F), Color(0xFF0F2640))),
                RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).background(Color.White.copy(0.1f), CircleShape),
                    Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, null,
                        tint = Color(0xFFFBBF24), modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("AI Insight", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Box(
                        Modifier.background(Color(0xFFFBBF24).copy(0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Coming Soon", fontSize = 10.sp,
                            fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24))
                    }
                }
            }
        }
    }
}
