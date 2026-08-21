package com.example.pkl_finance.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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


// Sensor nama rekening: hanya tampilkan huruf pertama tiap kata, sisanya '*'
fun maskAccountName(name: String): String {
    if (name.isEmpty()) return ""
    return name.split(" ").joinToString(" ") { word ->
        if (word.isEmpty()) ""
        else word[0] + "*".repeat(word.length - 1)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CairkanMainScreen(
    appState: AppState,
    modifier: Modifier = Modifier
) {
    var comingSoonFeature by remember { mutableStateOf<String?>(null) }
    val isBalanceSufficient = appState.mainBalance >= 10000.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        AppTopBar(
            title = "Pencairan dana",
            onBack = { appState.navigateTo(Screen.Home) }
        )

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(20.dp)
        ) {
            // Section 1: Saldo Merchant
            Text(
                text = "Saldo merchant",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = DarkNavy
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isBalanceSufficient) Color(0xFFEDFBF0) else Color(0xFFE2E8F0)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.dp,
                    if (isBalanceSufficient) Color(0xFFBFF0C9) else Color(0xFFCBD5E1)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Saldo QRIS",
                            fontSize = 12.sp,
                            color = SlateGray,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = appState.formatRupiah(appState.mainBalance),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isBalanceSufficient) DarkNavy else SlateGray
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isBalanceSufficient) {
                                "Min. saldo pencairan Rp10.000"
                            } else {
                                "Jumlah dana yang bisa dicairkan kurang dari Rp 10.000"
                            },
                            fontSize = 11.sp,
                            color = if (isBalanceSufficient) SlateGray else Color(0xFFEF4444),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Radio Check
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(
                                color = if (isBalanceSufficient) SuccessGreen else Color.Transparent,
                                shape = CircleShape
                            )
                            .border(
                                BorderStroke(
                                    2.dp,
                                    if (isBalanceSufficient) SuccessGreen else Color(0xFFCBD5E1)
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isBalanceSufficient) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.White, CircleShape)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Terakhir diperbarui: 11 Jul 2026, 21:31",
                fontSize = 11.sp,
                color = SlateGray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: Akun Pencairan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Akun Pencairan",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkNavy
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Blue500, Blue600)
                            )
                        )
                        .clickable {
                            appState.tempBankName = appState.disbursementBankName
                            appState.tempAccountNumber = appState.disbursementAccountNumber
                            appState.tempAccountName = appState.disbursementAccountName
                            appState.navigateTo(Screen.CairkanPilihBank)
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Edit",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            val bankBrandColor = when (appState.disbursementBankName) {
                "BCA", "BRI" -> Color(0xFF00569E)
                "MANDIRI" -> Color(0xFF1E3A8A)
                "BNI" -> Color(0xFFE25822)
                "BSI" -> Color(0xFF009B90)
                "PERMATA" -> Color(0xFF84CC16)
                "CIMB" -> Color(0xFFDC2626)
                "DANAMON" -> Color(0xFFEA580C)
                else -> PrimaryBlue
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = AppWhite),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Dynamic logo square
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(bankBrandColor, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = appState.disbursementBankName.take(3),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = appState.disbursementBankName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkNavy
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Nomor rekening", fontSize = 12.sp, color = SlateGray)
                        val num = appState.disbursementAccountNumber
                        val maskedNum = if (num.length > 4) {
                            "******" + num.takeLast(4)
                        } else {
                            num
                        }
                        Text(text = maskedNum, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Nama pemilik rekening", fontSize = 12.sp, color = SlateGray)
                        Text(text = appState.disbursementAccountName.let { maskAccountName(it) }, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 3: Metode Pencairan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Metode pencairan",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkNavy
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Blue500, Blue600)
                            )
                        )
                        .clickable { comingSoonFeature = "Ubah Metode Pencairan" }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Edit",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = AppWhite),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Pencairan kapan saja",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkNavy
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Cairkan dana kapan saja atau sesuai jadwal harian Anda. Pencairan akan langsung diproses di hari yang sama, termasuk akhir pekan dan hari libur nasional.",
                        fontSize = 12.sp,
                        color = SlateGray,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Waktu pencairan", fontSize = 12.sp, color = DarkNavy, fontWeight = FontWeight.Bold)
                        Text(text = "06:00 WIB", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SlateGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            // Warning Notice - amber style card
            Card(
                colors = CardDefaults.cardColors(containerColor = Yellow50),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Yellow300),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Peringatan",
                        tint = Yellow700,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Perhatian: Anda dapat mengganti waktu pencairan maks. satu kali per hari.",
                        fontSize = 12.sp,
                        color = Yellow800,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            AppButton(
                text = "Lanjut",
                onClick = {
                    if (isBalanceSufficient) {
                        appState.navigateTo(Screen.TarikSaldo)
                    }
                },
                variant = if (isBalanceSufficient) AppButtonVariant.Primary else AppButtonVariant.Disabled,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (comingSoonFeature != null) {
        ComingSoonDialog(
            featureName = comingSoonFeature,
            onDismiss = { comingSoonFeature = null }
        )
    }
}
