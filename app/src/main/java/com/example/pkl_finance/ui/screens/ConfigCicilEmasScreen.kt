package com.example.pkl_finance.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.pkl_finance.data.AppState
import com.example.pkl_finance.data.Screen
import com.example.pkl_finance.ui.theme.*
import com.example.pkl_finance.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigCicilEmasScreen(
    appState: AppState,
    modifier: Modifier = Modifier
) {
    var selectedWeight by remember { mutableStateOf(0.5) }
    var selectedPercentage by remember { mutableStateOf(10) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Dynamic price calculation from backend rate
    val goldPrice = selectedWeight * appState.goldPriceRate
    val serviceFee = 50000.0
    val totalDebt = goldPrice + serviceFee

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            AppTopBar(
                title = "Konfigurasi Cicil Emas",
                onBack = { appState.navigateTo(appState.previousScreen) }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Info Alert
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryBlueLight, RoundedCornerShape(16.dp))
                        .border(BorderStroke(1.dp, Blue300), RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Cicilan akan otomatis dipotong setiap kali ada transaksi QRIS masuk ke toko Anda.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryBlueDark,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Step 1: Pilih Berat Emas
                StepHeader(number = 1, title = "Pilih Berat Emas")
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Option 0.5g
                    WeightOptionCard(
                        weightText = "0.5g",
                        stockText = "Sisa: 3",
                        isSelected = selectedWeight == 0.5,
                        onClick = { selectedWeight = 0.5 },
                        modifier = Modifier.weight(1f)
                    )

                    // Option 1.0g
                    WeightOptionCard(
                        weightText = "1g",
                        stockText = "Sisa: 2",
                        isSelected = selectedWeight == 1.0,
                        onClick = { selectedWeight = 1.0 },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Step 2: Pilih Target Tabungan
                StepHeader(number = 2, title = "Pilih Target Tabungan dari Tiap Transaksi")
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val percentages = listOf(10, 15, 20, 30, 35)
                    percentages.forEach { pct ->
                        PercentageChip(
                            percentage = pct,
                            isSelected = selectedPercentage == pct,
                            onClick = { selectedPercentage = pct }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Summary: Target Cicil Emas
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppWhite),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            RoundedCornerShape(20.dp)
                        )
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
                                text = "Target Cicil Emas",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkNavy
                            )
                            Box(
                                modifier = Modifier
                                    .background(GoldAccentLight, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Target: ${selectedWeight}g",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GoldAccent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        SummaryRow(label = "Total Harga Emas", value = appState.formatRupiah(goldPrice))
                        Spacer(modifier = Modifier.height(10.dp))
                        SummaryRow(label = "Biaya Layanan", value = appState.formatRupiah(serviceFee))

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sisa yang harus dicicil",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkNavy
                            )
                            Text(
                                text = appState.formatRupiah(totalDebt),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Bottom CTA Button
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(24.dp)
                ) {
                    AppButton(
                        text = "Aktifkan Cicilan Emas",
                        onClick = { showSuccessDialog = true },
                        variant = AppButtonVariant.Primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Success Dialog modal
        if (showSuccessDialog) {
            Dialog(onDismissRequest = { }) {
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
                        // Green Success Icon
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(SuccessGreenLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            // Draw checkmark
                            Text(
                                text = "✓",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Berhasil Diaktifkan!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkNavy,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Cicilan emas ${selectedWeight}g Anda telah aktif. Saldo akan otomatis terpotong ${selectedPercentage}% dari setiap transaksi QRIS.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = SlateGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        AppButton(
                            text = "Oke, Mengerti",
                            onClick = {
                                appState.activateInstallment(selectedWeight, selectedPercentage)
                                showSuccessDialog = false
                                appState.navigateTo(Screen.Home)
                            },
                            variant = AppButtonVariant.Primary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StepHeader(number: Int, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(DarkNavy, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = DarkNavy
        )
    }
}

@Composable
fun WeightOptionCard(
    weightText: String,
    stockText: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryBlue else AppWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            1.dp,
            if (isSelected) Color.Transparent else Color(0xFFE2E8F0)
        ),
        modifier = modifier
            .height(130.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            val iconColor = if (isSelected) Color.White else GoldAccent
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.15f) else GoldAccentLight,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Au",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = iconColor
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = weightText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else DarkNavy
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) Color.White.copy(alpha = 0.2f) else Color(0xFFF1F5F9),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = stockText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) Color.White else SlateGray
                    )
                }
            }
        }
    }
}

@Composable
fun PercentageChip(
    percentage: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) GoldAccent else Color.White
    val borderStroke = if (isSelected) BorderStroke(0.dp, Color.Transparent) else BorderStroke(1.dp, Color(0xFFE2E8F0))
    val textColor = if (isSelected) Color.White else SlateGray

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(16.dp))
            .border(borderStroke, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$percentage%",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun SummaryRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = SlateGray
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = DarkNavy
        )
    }
}
