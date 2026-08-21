package com.example.pkl_finance.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.pkl_finance.data.AppState
import com.example.pkl_finance.data.Screen
import com.example.pkl_finance.ui.components.AppButton
import com.example.pkl_finance.ui.components.AppButtonVariant
import com.example.pkl_finance.ui.components.AppTopBar
import com.example.pkl_finance.ui.theme.*
import java.util.Locale

private data class DailyBreakdown(
    val date: String,
    val gross: Double,
    val mdr: Double,
    val net: Double
)

private data class ReportData(
    val periodLabel: String,
    val grossAmount: Double,
    val mdrAmount: Double,
    val netAmount: Double,
    val txCount: Int,
    val dailyList: List<DailyBreakdown>,
    val chartPoints: List<Float>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanScreen(
    appState: AppState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPeriod by remember { mutableStateOf("Hari ini") }
    var selectedGraphTab by remember { mutableStateOf("Hari") }
    
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showComingSoonDialog by remember { mutableStateOf(false) }
    
    var customStartDate by remember { mutableStateOf("10 Jul 2026") }
    var customEndDate by remember { mutableStateOf("15 Jul 2026") }

    val activeReport = remember(selectedPeriod, appState.transactionHistory) {
        val txList = appState.transactionHistory
        val gross = txList.sumOf { it.totalAmount }
        val mdr = txList.sumOf { it.mdrFee }
        val net = txList.sumOf { it.mainAmount }
        val count = txList.size

        val dailyMap = txList.groupBy { it.date.ifEmpty { "Hari ini" } }
        val dailyBreakdowns = dailyMap.map { (date, items) ->
            DailyBreakdown(
                date = date,
                gross = items.sumOf { it.totalAmount },
                mdr = items.sumOf { it.mdrFee },
                net = items.sumOf { it.mainAmount }
            )
        }

        val maxAmt = txList.maxOfOrNull { it.totalAmount }?.coerceAtLeast(1.0) ?: 1.0
        val rawPoints = txList.take(6).map { (it.totalAmount / maxAmt).toFloat().coerceIn(0.1f, 1f) }
        val points = if (rawPoints.size < 2) listOf(0.1f, 0.2f, 0.5f, 0.8f) else rawPoints

        ReportData(
            periodLabel = selectedPeriod,
            grossAmount = gross,
            mdrAmount = mdr,
            netAmount = net,
            txCount = count,
            dailyList = dailyBreakdowns,
            chartPoints = points
        )
    }

    val periodDetailText = remember(selectedPeriod, customStartDate, customEndDate) {
        if (selectedPeriod == "pilih tanggal") {
            "$customStartDate - $customEndDate"
        } else {
            selectedPeriod.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
    }

    fun formatCompactRupiah(amount: Double): String {
        return if (amount >= 1000000.0) {
            val millions = amount / 1000000.0
            val formatted = String.format(Locale("in", "ID"), "%.1f", millions).replace(",0", "")
            "Rp" + formatted + "jt"
        } else if (amount >= 1000.0) {
            val thousands = amount / 1000.0
            val formatted = String.format(Locale("in", "ID"), "%.1f", thousands).replace(",0", "")
            "Rp" + formatted + "rb"
        } else {
            "Rp" + String.format(Locale("in", "ID"), "%.0f", amount)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        AppTopBar(
            title = "Laporan Pendapatan",
            onBack = { appState.navigateTo(Screen.Home) },
            actionIcon = Icons.Default.FileDownload,
            onAction = { showComingSoonDialog = true }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Period Selector Filters (Styled exactly like Transaction History page filters, transparent container)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val periodsList = listOf("Hari ini", "Kemarin", "7 hari terakhir", "1 bulan", "6 bulan", "pilih tanggal")
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(periodsList.size) { index ->
                            val period = periodsList[index]
                            val isSelected = period == selectedPeriod
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isSelected) PrimaryBlue else Color.White,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        selectedPeriod = period
                                        if (period == "pilih tanggal") {
                                            showDatePickerDialog = true
                                        }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (period == "pilih tanggal") {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else SlateGray,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = if (period == "pilih tanggal" && selectedPeriod == "pilih tanggal") "Pilih Tgl" else period,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else SlateGray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(6.dp))

                // 2. Card Pertama: 1 Card Pendapatan Bersih
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppWhite),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(PrimaryBlueLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Paid,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pendapatan Bersih",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGray
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = appState.formatRupiah(activeReport.netAmount),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryBlue
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AppWhite),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(PrimaryBlueLight, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Kotor", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateGray)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = formatCompactRupiah(activeReport.grossAmount),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = DarkNavy
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = AppWhite),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(PrimaryBlueLight, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("MDR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateGray)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "-" + formatCompactRupiah(activeReport.mdrAmount),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = ErrorRed
                            )
                        }
                    }

                    // Card 3: Transaksi
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AppWhite),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(Color(0xFFE0F2FE), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Transaksi", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateGray)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "${activeReport.txCount} Tx",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = ColorBlack
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                val points = activeReport.chartPoints
                var selectedChartPointIndex by remember(selectedPeriod) { 
                    mutableStateOf(if (points.isNotEmpty()) points.lastIndex else null) 
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = AppWhite),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tren Penjualan",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkNavy
                            )

                            Row(
                                modifier = Modifier
                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                                    .padding(2.dp)
                            ) {
                                listOf("Hari", "Minggu", "Bulan").forEach { tab ->
                                    val isTabSelected = tab == selectedGraphTab
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isTabSelected) AppWhite else Color.Transparent,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedGraphTab = tab }
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = tab,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isTabSelected) DarkNavy else SlateGray
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        selectedChartPointIndex?.let { idx ->
                            if (activeReport.dailyList.isNotEmpty()) {
                                val listIdx = (activeReport.dailyList.size - 1 - idx).coerceIn(0, activeReport.dailyList.size - 1)
                                val dailyData = activeReport.dailyList[listIdx]
                            Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                        .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = dailyData.date,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkNavy
                                    )
                                    Text(
                                        text = "Bersih: " + appState.formatRupiah(dailyData.net),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SuccessGreen
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Line Chart
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(points) {
                                        detectTapGestures { offset ->
                                            if (points.isNotEmpty()) {
                                                val step = size.width / (points.size - 1).coerceAtLeast(1)
                                                points.forEachIndexed { idx, pct ->
                                                    val cx = idx * step
                                                    val py = size.height - (pct * (size.height - 20.dp.toPx()) + 10.dp.toPx())
                                                    val dist = kotlin.math.abs(offset.x - cx) + kotlin.math.abs(offset.y - py)
                                                    if (dist < 40.dp.toPx()) {
                                                        selectedChartPointIndex = idx
                                                    }
                                                }
                                            }
                                        }
                                    }
                            ) {
                                val w = size.width
                                val h = size.height
                                
                                repeat(4) { i ->
                                    val y = i * (h / 3)
                                    drawLine(
                                        color = Color(0xFFF1F5F9),
                                        start = Offset(0f, y),
                                        end = Offset(w, y),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }

                                if (points.isNotEmpty()) {
                                    val segmentWidth = w / (points.size - 1).coerceAtLeast(1)
                                    val coordinates = points.mapIndexed { idx, pct ->
                                        val py = h - (pct * (h - 20.dp.toPx()) + 10.dp.toPx())
                                        Offset(idx * segmentWidth, py)
                                    }

                                    val areaPath = Path().apply {
                                        moveTo(0f, h)
                                        lineTo(coordinates.first().x, coordinates.first().y)
                                        for (i in 0 until coordinates.size - 1) {
                                            val cpx = (coordinates[i].x + coordinates[i+1].x) / 2f
                                            cubicTo(cpx, coordinates[i].y, cpx, coordinates[i+1].y, coordinates[i+1].x, coordinates[i+1].y)
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

                                    val linePath = Path().apply {
                                        moveTo(coordinates.first().x, coordinates.first().y)
                                        for (i in 0 until coordinates.size - 1) {
                                            val cpx = (coordinates[i].x + coordinates[i+1].x) / 2f
                                            cubicTo(cpx, coordinates[i].y, cpx, coordinates[i+1].y, coordinates[i+1].x, coordinates[i+1].y)
                                        }
                                    }
                                    drawPath(
                                        path = linePath,
                                        color = PrimaryBlue,
                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                    )

                                    coordinates.forEachIndexed { index, point ->
                                        val isTapped = selectedChartPointIndex == index
                                        drawCircle(
                                            color = if (isTapped) PrimaryBlue else PrimaryBlue.copy(alpha = 0.5f),
                                            radius = if (isTapped) 6.dp.toPx() else 4.dp.toPx(),
                                            center = point
                                        )
                                        drawCircle(
                                            color = AppWhite,
                                            radius = 2.dp.toPx(),
                                            center = point
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val labels = when (selectedGraphTab) {
                                "Hari" -> listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
                                "Minggu" -> listOf("M-1", "M-2", "M-3", "M-4", "M-5", "M-6")
                                else -> listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun")
                            }
                            labels.forEach { label ->
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateGray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pendapatan Pertanggal 
                val dailyList = activeReport.dailyList
                if (dailyList.isNotEmpty()) {
                    var activeDailyIndex by remember(selectedPeriod) { mutableStateOf(0) }
                    val currentDay = dailyList[activeDailyIndex.coerceIn(0, dailyList.lastIndex)]

                    Card(
                        colors = CardDefaults.cardColors(containerColor = AppWhite),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Rincian Pendapatan Harian",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkNavy
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Navigation Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            if (activeDailyIndex < dailyList.lastIndex) PrimaryBlue else Color(0xFFF8FAFC),
                                            CircleShape
                                        )
                                        .clip(CircleShape)
                                        .clickable(enabled = activeDailyIndex < dailyList.lastIndex) {
                                            activeDailyIndex++
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Kiri",
                                        modifier = Modifier.size(18.dp),
                                        tint = if (activeDailyIndex < dailyList.lastIndex) Color.White else Color(0xFFCBD5E1)
                                    )
                                }

                                Text(
                                    text = currentDay.date,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DarkNavy
                                )

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            if (activeDailyIndex > 0) PrimaryBlue else Color(0xFFF8FAFC),
                                            CircleShape
                                        )
                                        .clip(CircleShape)
                                        .clickable(enabled = activeDailyIndex > 0) {
                                            activeDailyIndex--
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Kanan",
                                        modifier = Modifier.size(18.dp),
                                        tint = if (activeDailyIndex > 0) Color.White else Color(0xFFCBD5E1)
                                    )   
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(20.dp))

                            // Breakdown details of the selected date
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Pendapatan Kotor",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SlateGray
                                )
                                Text(
                                    text = appState.formatRupiah(currentDay.gross),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkNavy
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Potongan MDR",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SlateGray
                                )
                                Text(
                                    text = "- " + appState.formatRupiah(currentDay.mdr),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ErrorRed
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Pendapatan Bersih",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateGray
                                )
                                Text(
                                    text = appState.formatRupiah(currentDay.net),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SuccessGreen
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    if (showComingSoonDialog) {
        Dialog(onDismissRequest = { showComingSoonDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AppWhite),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Unduh Laporan ⚡",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = DarkNavy
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Fitur ekspor laporan (Excel / PDF) segera hadir di PKL Finance! Tetap nantikan ya! 😊",
                        fontSize = 12.sp,
                        color = SlateGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    AppButton(
                        text = "Oke, Siap!",
                        onClick = { showComingSoonDialog = false },
                        variant = AppButtonVariant.Primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    // Custom Date Range Picker Simulator Dialog
    if (showDatePickerDialog) {
        Dialog(onDismissRequest = { showDatePickerDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AppWhite),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Pilih Periode Tanggal",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkNavy
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Silakan tentukan rentang tanggal laporan",
                        fontSize = 12.sp,
                        color = SlateGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = customStartDate,
                        onValueChange = { customStartDate = it },
                        label = { Text("Tanggal Mulai", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customEndDate,
                        onValueChange = { customEndDate = it },
                        label = { Text("Tanggal Selesai", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    AppButton(
                        text = "Terapkan Tanggal",
                        onClick = {
                            selectedPeriod = "pilih tanggal"
                            showDatePickerDialog = false
                        },
                        variant = AppButtonVariant.Primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
