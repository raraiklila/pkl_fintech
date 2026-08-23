package com.example.pkl_finance.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pkl_finance.R
import com.example.pkl_finance.data.AppState
import com.example.pkl_finance.data.Screen
import com.example.pkl_finance.data.Transaction
import com.example.pkl_finance.ui.components.AppButton
import com.example.pkl_finance.ui.components.AppButtonVariant
import com.example.pkl_finance.ui.components.CustomProgressBar
import com.example.pkl_finance.ui.components.VerificationRequiredDialog
import com.example.pkl_finance.ui.theme.*

@Composable
fun HomeScreen(
    appState: AppState,
    modifier: Modifier = Modifier
) {
    var isBalanceVisible by remember { mutableStateOf(true) }
    var showVerificationDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(appState.refreshError) {
        appState.refreshError?.let { err ->
            android.widget.Toast.makeText(context, "Sinkronisasi Gagal: $err", android.widget.Toast.LENGTH_LONG).show()
            appState.refreshError = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White, CircleShape)
                        .border(BorderStroke(1.5.dp, PrimaryBlue), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_profil_fill),
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Selamat Datang, ${appState.ownerName.ifEmpty { "Rara" }}!",
                        fontSize = 12.sp,
                        color = SlateGray,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = appState.shopName.ifEmpty { "RARAWR STYLE" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkNavy
                    )
                }
            }
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Verification Alert Banner
            if (!appState.isVerified) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable { appState.navigateTo(Screen.VerifikasiKyc) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Akun Belum Diverifikasi",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Ketuk untuk verifikasi & aktifkan QRIS & Fitur Emas.",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                lineHeight = 16.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Main Balance Card
            Card(
                colors = CardDefaults.cardColors(containerColor = AppWhite),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color.Black.copy(alpha = 0.4f),
                        spotColor = Color.Black.copy(alpha = 0.5f)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Blue500, Blue600)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_atm),
                                contentDescription = null,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 0.dp, y = 25.dp)
                                    .size(92.dp, 60.dp)
                            )

                            Column(
                                modifier = Modifier.padding(horizontal = 20.dp)
                            ) {
                                Spacer(modifier = Modifier.height(25.dp))

                                Text(
                                    text = "Saldo Merchant Anda",
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isBalanceVisible) appState.formatRupiah(appState.mainBalance) else "Rp ••••••",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    IconButton(
                                        onClick = { isBalanceVisible = !isBalanceVisible },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle Saldo",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(28.dp))

                                HorizontalDivider(color = Color.White.copy(alpha = 0.25f), thickness = 1.dp)

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (!appState.isVerified) {
                                                showVerificationDialog = true
                                            } else {
                                                appState.navigateTo(Screen.Emas)
                                            }
                                        },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Image(
                                            painter = painterResource(id = R.drawable.gold_home),
                                            contentDescription = null,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${String.format(java.util.Locale.US, "%.4f", appState.goldBalance)} gram",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Detail Emas",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(15.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Action Menu
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        QuickActionItem(
                            label = "Cairkan",
                            iconResId = R.drawable.ic_menu_cairkan,
                            onClick = { appState.navigateTo(Screen.CairkanMain) }
                        )
                        QuickActionItem(
                            label = "Cicil Emas",
                            iconResId = R.drawable.ic_menu_cicil,
                            onClick = {
                                if (!appState.isVerified) {
                                    showVerificationDialog = true
                                } else {
                                    appState.previousScreen = Screen.Home
                                    appState.navigateTo(Screen.ConfigCicilEmas)
                                }
                            }
                        )
                        QuickActionItem(
                            label = "Beli Emas",
                            iconResId = R.drawable.ic_menu_beli,
                            onClick = {
                                if (!appState.isVerified) {
                                    showVerificationDialog = true
                                } else {
                                    appState.previousScreen = Screen.Home
                                    appState.navigateTo(Screen.BeliEmas)
                                }
                            }
                        )
                        QuickActionItem(
                            label = "Laporan",
                            iconResId = R.drawable.ic_menu_laporan,
                            onClick = {
                                if (!appState.isVerified) {
                                    showVerificationDialog = true
                                } else {
                                    appState.previousScreen = Screen.Home
                                    appState.navigateTo(Screen.Laporan)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Active Installment Section
            Text(
                text = "Cicilan Emas Aktif",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DarkNavy
            )
            Spacer(modifier = Modifier.height(12.dp))

            val active = appState.activeInstallment
            if (active == null) {
                val dashColor = Color(0xFFCBD5E1)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            val strokeWidth = 1.5.dp.toPx()
                            val dashPathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(12f, 8f), 0f
                            )
                            drawRoundRect(
                                color = PrimaryBlue,
                                size = size,
                                cornerRadius = CornerRadius(16.dp.toPx()),
                                style = Stroke(
                                    width = strokeWidth,
                                    pathEffect = dashPathEffect
                                )
                            )
                        }
                        .background(AppWhite, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            if (!appState.isVerified) {
                                showVerificationDialog = true
                            } else {
                                appState.navigateTo(Screen.ConfigCicilEmas)
                            }
                        }
                        .padding(vertical = 20.dp, horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(Yellow100, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Light,
                                color = Color(0xFFD97706)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum Ada Cicilan Aktif",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkNavy
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Mulai cicil emas mulai 10% dari transaksimu",
                            fontSize = 12.sp,
                            color = SlateGray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppWhite),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Yellow100, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Au",
                                        color = GoldAccent,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Cicilan Emas ${active.targetWeight}g",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkNavy
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Autosplit ${active.splitPercentage}%",
                                        fontSize = 11.sp,
                                        color = SlateGray
                                    )
                                }
                            }
                            Text(
                                text = "${String.format(java.util.Locale.US, "%.4f", active.accumulatedGoldWeight)} / ${active.targetWeight}g",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        CustomProgressBar(
                            progress = active.progress,
                            color = GoldAccent,
                            trackColor = Color(0xFFE2E8F0),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Terkumpul",
                                    fontSize = 11.sp,
                                    color = SlateGray
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = appState.formatRupiah(active.accumulatedAmount),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkNavy
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Sisa Tagihan",
                                    fontSize = 11.sp,
                                    color = SlateGray
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = appState.formatRupiah(active.remainingAmount),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Recent Transactions Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Riwayat Transaksi",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkNavy
                )
                Text(
                    text = "Lihat Semua",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    modifier = Modifier.clickable { appState.navigateTo(Screen.Transaksi) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val recents = appState.transactionHistory.take(6)
            if (recents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada riwayat transaksi",
                        fontSize = 12.sp,
                        color = SlateGray
                    )
                }
            } else {
                recents.forEach { trans ->
                    TransactionItem(trans = trans, appState = appState)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            Spacer(modifier = Modifier.height(100.dp)) // Bottom navbar spacing
        }
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
fun QuickActionItem(
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


