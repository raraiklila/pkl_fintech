package com.example.pkl_finance.ui.screens

import android.widget.Toast

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.example.pkl_finance.ui.components.ExpressiveLoadingIndicator
import com.example.pkl_finance.data.AppState
import com.example.pkl_finance.data.Screen
import com.example.pkl_finance.data.Transaction
import androidx.compose.ui.res.painterResource
import com.example.pkl_finance.R
import com.example.pkl_finance.ui.components.QrisDownloadTemplate
import com.example.pkl_finance.ui.components.QrisMockCode
import com.example.pkl_finance.ui.theme.*
import com.example.pkl_finance.ui.components.*

@Composable
fun QrisScreen(
    appState: AppState,
    modifier: Modifier = Modifier
) {
    val isVerified = appState.merchantInfo?.isVerified == true
    if (!isVerified) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            androidx.compose.material3.Text(
                "Fitur QRIS Terkunci", 
                fontSize = 20.sp, 
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = Color(0xFF003366)
            )
            Spacer(modifier = Modifier.height(16.dp))
            androidx.compose.material3.Text(
                "Anda harus melakukan verifikasi data merchant (KYC) terlebih dahulu sebelum dapat menggunakan fitur QRIS.",
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(32.dp))
            com.example.pkl_finance.ui.components.AppButton(
                text = "Verifikasi Sekarang",
                onClick = { appState.navigateTo(com.example.pkl_finance.data.Screen.VerifikasiKyc) },
                variant = com.example.pkl_finance.ui.components.AppButtonVariant.Primary
            )
        }
    } else {
        QrisNewLayout(
            appState = appState,
            modifier = modifier
        )
    }
}

@Composable
fun QrisNewLayout(
    appState: AppState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isAddingNominalMode by remember { mutableStateOf(false) }
    var activeNominalAmount by remember { mutableStateOf(0.0) }
    var activeNominalNotes by remember { mutableStateOf("") }
    var isQrisUsed by remember { mutableStateOf(false) }
    var showExpiredWarningDialog by remember { mutableStateOf(false) }

    // ─── State Midtrans QRIS ───────────────────────────────────────
    var midtransOrderId by remember { mutableStateOf("") }
    var midtransQrImageUrl by remember { mutableStateOf("") }
    var isCreatingQr by remember { mutableStateOf(false) }   // loading saat buat QR
    var isPolling by remember { mutableStateOf(false) }      // sedang polling status
    var qrErrorMsg by remember { mutableStateOf("") }        // error dari Midtrans
    // ──────────────────────────────────────────────────────────────

    var showSimulationResult by remember { mutableStateOf(false) }
    var activeTransactionResult by remember { mutableStateOf<Transaction?>(null) }
    var showDownloadPreview by remember { mutableStateOf(false) }

    // Saat nominal dikonfirmasi → langsung buat QR Midtrans
    LaunchedEffect(activeNominalAmount) {
        if (activeNominalAmount > 0.0 && midtransQrImageUrl.isEmpty() && !isCreatingQr) {
            isCreatingQr = true
            qrErrorMsg = ""
            appState.createMidtransQris(
                amount = activeNominalAmount,
                onReady = { orderId, qrUrl ->
                    midtransOrderId = orderId
                    midtransQrImageUrl = qrUrl
                    isCreatingQr = false
                    // Mulai polling otomatis
                    isPolling = true
                    coroutineScope.launch {
                        appState.pollQrisStatus(
                            orderId = orderId,
                            onPaid = { tx ->
                                isPolling = false
                                isQrisUsed = true
                                val resolvedTx = tx ?: Transaction(
                                    id = orderId,
                                    type = "QRIS_IN",
                                    title = "Pembayaran QRIS - ${appState.shopName}",
                                    date = java.text.SimpleDateFormat("dd MMM yyyy HH:mm", java.util.Locale("id", "ID")).format(java.util.Date()) + " WIB",
                                    totalAmount = activeNominalAmount,
                                    mainAmount = activeNominalAmount,
                                    goldAmount = 0.0,
                                    goldWeightAdded = 0.0
                                )
                                activeTransactionResult = resolvedTx
                                showSimulationResult = true
                            },
                            onExpired = {
                                isPolling = false
                                isQrisUsed = true
                                showExpiredWarningDialog = true
                            }
                        )
                    }
                },
                onError = { errMsg ->
                    isCreatingQr = false
                    qrErrorMsg = errMsg
                    Toast.makeText(context, "Gagal buat QR: $errMsg", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    if (isAddingNominalMode) {
        QrisNominalInputScreen(
            onBack = { isAddingNominalMode = false },
            onConfirm = { amt, notes ->
                activeNominalAmount = amt
                activeNominalNotes = notes
                isQrisUsed = false
                midtransOrderId = ""
                midtransQrImageUrl = ""
                isCreatingQr = false
                isPolling = false
                qrErrorMsg = ""
                isAddingNominalMode = false
            }
        )
    } else if (activeNominalAmount > 0.0) {
        // Nominal Generated Layout
        val expiryTime = remember(activeNominalAmount) {
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.MINUTE, 30)
            val sdf = java.text.SimpleDateFormat("d MMM yyyy HH:mm", java.util.Locale("in", "ID"))
            sdf.format(cal.time) + " WIB"
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .statusBarsPadding()
        ) {
            AppTopBar(
                title = "QRIS",
                onBack = {
                    activeNominalAmount = 0.0
                    isQrisUsed = false
                }
            )

            // 2. Scrollable Body Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Merchant Details Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppWhite),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (appState.shopName.isNotEmpty()) appState.shopName else "RARAWR STYLE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DarkNavy,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (appState.shopAddress.isNotEmpty()) appState.shopAddress else "Dk. Tegalrejo Rt/Rw 02/06 Tegalrejo, Kabupaten Sragen, Jawa Tengah",
                            fontSize = 11.sp,
                            color = SlateGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "NMID: ID102030405060",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkNavy,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // QR Details Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppWhite),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Centered QRIS Branding Logo on top
                        Image(
                            painter = painterResource(id = R.drawable.qris_logo),
                            contentDescription = "QRIS Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .height(32.dp)
                                .wrapContentWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Centered Nominal Text
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Rp",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGray,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val amtFormatted = String.format(java.util.Locale("in", "ID"), "%,d", activeNominalAmount.toLong()).replace(",", ".")
                            Text(
                                text = amtFormatted,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = DarkNavy
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ─── QR Area Midtrans ────────────────────────────────
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .background(Color.White, RoundedCornerShape(16.dp))
                                .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(16.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                // 1. Sedang loading / buat QR
                                isCreatingQr -> {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        ExpressiveLoadingIndicator(
                                            size = 54.dp
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Membuat QR Midtrans...",
                                            fontSize = 13.sp,
                                            color = SlateGray,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                // 2. Error dari Midtrans
                                qrErrorMsg.isNotEmpty() -> {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text("❌", fontSize = 32.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = qrErrorMsg,
                                            fontSize = 12.sp,
                                            color = ErrorRed,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Pastikan Server Key Midtrans sudah diisi di .env backend",
                                            fontSize = 11.sp,
                                            color = SlateGray,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                // 3. QR berhasil dibuat dari Midtrans
                                midtransQrImageUrl.isNotEmpty() -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Background Canvas dekorasi pojok merah
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val w = size.width
                                            val h = size.height
                                            val leftPath = Path().apply {
                                                moveTo(0f, h * 0.12f); lineTo(w * 0.05f, h * 0.12f)
                                                lineTo(w * 0.05f, h * 0.65f); lineTo(0f, h * 0.72f); close()
                                            }
                                            drawPath(path = leftPath, color = Color(0xFFEF4444))
                                            val rightPath = Path().apply {
                                                moveTo(w * 0.55f, h); lineTo(w * 0.62f, h * 0.94f)
                                                lineTo(w * 0.94f, h * 0.94f); lineTo(w * 0.94f, h * 0.62f)
                                                lineTo(w, h * 0.55f); lineTo(w, h); close()
                                            }
                                            drawPath(path = rightPath, color = Color(0xFFEF4444))
                                        }

                                        // Gambar QR dari URL Midtrans pakai Coil
                                        SubcomposeAsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(midtransQrImageUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "QR Code Midtrans",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(20.dp),
                                            loading = {
                                                CircularProgressIndicator(
                                                    color = Blue500,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            },
                                            error = {
                                                Text("Gagal load QR", color = ErrorRed, fontSize = 12.sp)
                                            }
                                        )

                                        // Indikator polling (menunggu bayar)
                                        if (isPolling && !isQrisUsed) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .padding(bottom = 8.dp)
                                                    .background(Color(0xCC003366), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    CircularProgressIndicator(
                                                        color = Color.White,
                                                        modifier = Modifier.size(10.dp),
                                                        strokeWidth = 1.5.dp
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Menunggu pembayaran...",
                                                        color = Color.White,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        }

                                        // Overlay sudah digunakan / expired
                                        if (isQrisUsed) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color(0x991E293B), RoundedCornerShape(16.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("✅", fontSize = 32.sp)
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = "PEMBAYARAN DITERIMA",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        letterSpacing = 1.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // 4. Fallback jika belum ada apapun
                                else -> {
                                    CircularProgressIndicator(color = Blue500, modifier = Modifier.size(40.dp))
                                }
                            }
                        }
                        // ─── Akhir QR Area ───────────────────────────────────

                        Spacer(modifier = Modifier.height(20.dp))

                        // Subtext info
                        Text(
                            text = "Berlaku hanya untuk 1x transaksi",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = SlateGray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "hingga $expiryTime",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = SlateGray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Batalkan Button 
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ErrorRedLight),
                            border = BorderStroke(1.dp, ErrorRed),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    activeNominalAmount = 0.0
                                    activeNominalNotes = ""
                                    isQrisUsed = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Hapus",
                                        tint = ErrorRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Batalkan",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ErrorRed
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Tutup",
                                    tint = ErrorRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppButton(
                    text = "Ke Beranda",
                    onClick = { appState.navigateTo(Screen.Home) },
                    variant = AppButtonVariant.Secondary,
                    modifier = Modifier.weight(1f)
                )

                AppButton(
                    text = "Buat QRIS baru",
                    onClick = { isAddingNominalMode = true },
                    variant = AppButtonVariant.Primary,
                    modifier = Modifier.weight(1.2f)
                )
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .statusBarsPadding()
        ) {
            // 1. Top Custom App Bar for Static Default (X + Title + Download) - Centered Title
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // X (Close) button in circular blue shape
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Blue50, CircleShape)
                            .clip(CircleShape)
                            .clickable { appState.navigateTo(Screen.Home) }
                            .align(Alignment.CenterStart),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Blue700,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Centered Title
                    Text(
                        text = "QRIS",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorBlack,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    // Download button (Unduh QRIS) — styled like Laporan Pendapatan (FileDownload icon inside IconButton)
                    IconButton(
                        onClick = { showDownloadPreview = true },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Unduh QRIS",
                            tint = Blue500
                        )
                    }
                }
                HorizontalDivider(thickness = 1.dp, color = ColorDivider)
            }

            // Scrollable body
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                // Card 1: Merchant Details Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppWhite),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (appState.shopName.isNotEmpty()) appState.shopName else "RARAWR STYLE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DarkNavy,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (appState.shopAddress.isNotEmpty()) appState.shopAddress else "Dk. Tegalrejo Rt/Rw 02/06 Tegalrejo, Kabupaten Sragen, Jawa Tengah",
                            fontSize = 11.sp,
                            color = SlateGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "NMID: ID102030405060",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkNavy,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))



                // Card 2: QRIS Card (Logo, QR Code, Button)
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppWhite),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Logo QRIS
                        Image(
                            painter = painterResource(id = R.drawable.qris_logo),
                            contentDescription = "QRIS Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .height(32.dp)
                                .wrapContentWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Direct Square QR box containing QR Code + red corners template background (Tapping triggers simulation)
                        val context2 = androidx.compose.ui.platform.LocalContext.current
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .background(Color.White, RoundedCornerShape(16.dp))
                                .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(16.dp))
                                .clickable {
                                    appState.simulateIncomingQrisPayment(
                                        totalAmount = 150000.0,
                                        onResult = { tx ->
                                            activeTransactionResult = tx
                                            showSimulationResult = true
                                        },
                                        onError = { errMsg ->
                                            Toast.makeText(context2, "Koneksi Gagal: $errMsg", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
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

                            // Real QRIS QR Code generation using ZXing (Static QR - No Amount)
                            val qrisPayload = remember(appState.shopName) {
                                com.example.pkl_finance.data.QrisGenerator.getQrisPayload(
                                    merchantName = appState.shopName,
                                    amount = null
                                )
                            }
                            val qrBitmap = remember(qrisPayload) {
                                try {
                                    com.example.pkl_finance.data.QrisGenerator.generateQrCodeBitmap(qrisPayload, 512)
                                        .asImageBitmap()
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (qrBitmap != null) {
                                    androidx.compose.foundation.Image(
                                        bitmap = qrBitmap,
                                        contentDescription = "QRIS QR Code Static",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text("Gagal memuat QR Code", color = Color.Red, fontSize = 14.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Button Buat Tagihan (Design System Compliant)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F6FE)),
                            border = BorderStroke(1.dp, Blue100),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isAddingNominalMode = true }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Paid,
                                        contentDescription = null,
                                        tint = Blue500,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Buat tagihan QRIS pakai nominal",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Blue800
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Blue500,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Simulation success dialog
    if (showSimulationResult && activeTransactionResult != null) {
        val tx = activeTransactionResult!!
        val splitPercent = appState.activeInstallment?.splitPercentage ?: 0

        Dialog(onDismissRequest = { showSimulationResult = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AppWhite),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(SuccessGreenLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✓", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Pembayaran QRIS Masuk",
                        fontSize = 14.sp,
                        color = SlateGray,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "+" + appState.formatRupiah(tx.totalAmount),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = SuccessGreen
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    AppButton(
                        text = "Tutup",
                        onClick = { showSimulationResult = false },
                        variant = AppButtonVariant.Primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (showExpiredWarningDialog) {
        Dialog(onDismissRequest = { showExpiredWarningDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AppWhite),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFFFEE2E8), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚠️", fontSize = 36.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "QRIS Sudah Digunakan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkNavy
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "QRIS ini sudah digunakan. Merchant harus membuat QRIS baru jika ingin menerima pembayaran berikutnya.",
                        fontSize = 13.sp,
                        color = SlateGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    AppButton(
                        text = "Tutup",
                        onClick = { showExpiredWarningDialog = false },
                        variant = AppButtonVariant.Primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (showDownloadPreview) {
        QrisDownloadTemplate(
            appState = appState,
            onDismiss = { showDownloadPreview = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrisNominalInputScreen(
    onBack: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var amountString by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
    ) {
        AppTopBar(
            title = "QRIS",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = AppWhite),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tambah nominal",
                            fontSize = 12.sp,
                            color = SlateGray,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            val formattedDisplay = if (amountString.isEmpty()) "0" else {
                                val parsed = amountString.toLongOrNull() ?: 0L
                                java.text.NumberFormat.getNumberInstance(java.util.Locale("in", "ID")).format(parsed)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Rp",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SlateGray,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = formattedDisplay,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (amountString.isEmpty()) SlateGray else DarkNavy
                                )
                            }

                            BasicTextField(
                                value = amountString,
                                onValueChange = { newVal ->
                                    val clean = newVal.filter { it.isDigit() }
                                    if (clean.length <= 9) {
                                        amountString = clean
                                    }
                                },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = Color.Transparent,
                                    fontSize = 32.sp,
                                    textAlign = TextAlign.Center
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                                ),
                                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.Transparent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(45.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Catatan / Notes
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppWhite),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💬", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        BasicTextField(
                            value = notesText,
                            onValueChange = { notesText = it },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = Inter,
                                fontSize = 14.sp,
                                color = DarkNavy,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (notesText.isEmpty()) {
                                    Text(
                                        text = "Cth: Orderan Sprei - Sisca",
                                        color = SlateGray,
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F6FE)),
                    border = BorderStroke(1.dp, Color(0xFFD6E4FC)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBack() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Blue500,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Ganti ke QRIS tanpa nominal",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Blue800
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Blue500,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // CTA Bottom Block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppWhite)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                AppButton(
                    text = "Buat tagihan QRIS",
                    onClick = {
                        val amt = amountString.toDoubleOrNull() ?: 0.0
                        if (amt > 0.0) {
                            onConfirm(amt, notesText)
                        }
                    },
                    variant = if (amountString.isNotEmpty() && (amountString.toDoubleOrNull() ?: 0.0) > 0.0) AppButtonVariant.Primary else AppButtonVariant.Disabled,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
