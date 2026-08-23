package com.example.pkl_finance.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pkl_finance.data.AppState
import com.example.pkl_finance.data.Screen
import com.example.pkl_finance.ui.components.AppButton
import com.example.pkl_finance.ui.components.AppButtonVariant
import com.example.pkl_finance.ui.components.AppTopBar
import com.example.pkl_finance.ui.theme.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JualEmasScreen(
    appState: AppState,
    modifier: Modifier = Modifier
) {
    var gramString by remember { mutableStateOf("") }
    var isSelling by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current

    val goldRate = appState.goldSellPrice
    val rawGram = gramString.toDoubleOrNull() ?: 0.0
    val estimatedRupiah = rawGram * goldRate
    val isExcessive = rawGram > appState.goldBalance
    val isValid = rawGram > 0.0 && !isExcessive

    // Preset gram options
    val presets = listOf(
        0.1 to "0,1 Gram",
        0.25 to "0,25 Gram",
        0.5 to "0,5 Gram",
        1.0 to "1 Gram",
        2.0 to "2 Gram",
        5.0 to "5 Gram"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        AppTopBar(
            title = "Jual Emas Digital",
            onBack = { appState.navigateTo(appState.previousScreen) },
            showDivider = false
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Gold balance banner
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEC)),
                border = BorderStroke(1.dp, Color(0xFFEDD06B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Saldo Emas",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = GoldDark
                    )
                    Text(
                        text = String.format(Locale.US, "%.4f Gram", appState.goldBalance),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldDark
                    )
                }
            }

            // Harga jual emas banner
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F6FE)),
                border = BorderStroke(1.dp, Color(0xFFD6E4FC)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Harga Jual (Buyback)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Blue500
                    )
                    Text(
                        text = "${appState.formatRupiah(appState.goldSellPrice)} / gram",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Blue800
                    )
                }
            }

            // Input Card
            Card(
                colors = CardDefaults.cardColors(containerColor = AppWhite),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Input overlay
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        val formattedDisplay = if (gramString.isEmpty()) "0" else gramString

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = formattedDisplay,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkNavy
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "gram",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Medium,
                                color = SlateGray
                            )
                        }

                        BasicTextField(
                            value = gramString,
                            onValueChange = { newVal ->
                                val clean = newVal.filter { it.isDigit() || it == '.' }
                                val dotIndex = clean.indexOf('.')
                                val finalVal = if (dotIndex >= 0) {
                                    val intPart = clean.substring(0, dotIndex).take(4)
                                    val decPart = clean.substring(dotIndex + 1).take(4)
                                    "$intPart.$decPart"
                                } else {
                                    clean.take(4)
                                }
                                if (finalVal.count { it == '.' } <= 1) {
                                    gramString = finalVal
                                }
                            },
                            textStyle = TextStyle(
                                color = Color.Transparent,
                                fontSize = 32.sp,
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            cursorBrush = SolidColor(Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(45.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(20.dp))

                    // Preset gram buttons grid
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            presets.take(3).forEach { (gram, label) ->
                                val gramKey = if (gram == gram.toLong().toDouble()) gram.toLong().toString() else gram.toString()
                                val isSelected = gramString == gramKey
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isSelected) GoldAccent else AppWhite,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            BorderStroke(1.dp, if (isSelected) Color.Transparent else ColorBorder),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { gramString = gramKey }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else SlateGray
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            presets.drop(3).forEach { (gram, label) ->
                                val gramKey = if (gram == gram.toLong().toDouble()) gram.toLong().toString() else gram.toString()
                                val isSelected = gramString == gramKey
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isSelected) GoldAccent else AppWhite,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            BorderStroke(1.dp, if (isSelected) Color.Transparent else ColorBorder),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { gramString = gramKey }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else SlateGray
                                    )
                                }
                            }
                        }
                    }

                    // Estimasi Rupiah Didapat Banner
                    if (rawGram > 0.0) {
                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(20.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = GoldAccentLight),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Estimasi Rupiah Didapat:",
                                    modifier = Modifier.weight(1f),
                                    fontSize = 12.sp,
                                    color = GoldDark,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = appState.formatRupiah(estimatedRupiah),
                                    fontSize = 13.sp,
                                    color = GoldDark,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Insufficient gold notification
            if (isExcessive) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = ErrorRedLight),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = ErrorRed,
                            modifier = Modifier.size(16.dp).padding(top = 1.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Saldo emas tidak mencukupi untuk penjualan ini",
                            fontSize = 12.sp,
                            color = ErrorRed,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Pinned Bottom CTA Button
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
                    text = if (isSelling) "Memproses..." else "Jual Emas Digital",
                    onClick = {
                        if (isValid && !isSelling) {
                            showConfirmDialog = true
                        }
                    },
                    variant = if (isValid && !isSelling) AppButtonVariant.Primary else AppButtonVariant.Disabled,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Confirmation Modal Dialog
    if (showConfirmDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { if (!isSelling) showConfirmDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AppWhite),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(GoldAccentLight, androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🪙", fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Konfirmasi Jual Emas",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkNavy
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Pastikan rincian penjualan emas Anda sudah sesuai",
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
                        Text("Jumlah Emas", fontSize = 13.sp, color = SlateGray)
                        Text(
                            String.format(Locale.US, "%.4f Gram", rawGram),
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
                        Text("Harga Jual", fontSize = 13.sp, color = SlateGray)
                        Text(
                            "${appState.formatRupiah(goldRate)}/g",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = DarkNavy
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Dana Diterima", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                        Text(
                            appState.formatRupiah(estimatedRupiah),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = SuccessGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Yellow warning banner
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Dana akan langsung masuk ke Saldo Merchant Anda setelah konfirmasi.",
                            fontSize = 11.sp,
                            color = Color(0xFFB45309),
                            modifier = Modifier.padding(12.dp),
                            lineHeight = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action buttons (1 single row, perfectly centered text)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppButton(
                            text = "Batal",
                            onClick = { showConfirmDialog = false },
                            variant = AppButtonVariant.Secondary,
                            horizontalPadding = 8.dp,
                            modifier = Modifier.weight(1f)
                        )

                        AppButton(
                            text = if (isSelling) "Memproses..." else "Konfirmasi Jual",
                            onClick = {
                                if (!isSelling) {
                                    isSelling = true
                                    appState.sellGold(
                                        goldWeight = rawGram,
                                        onError = { err ->
                                            isSelling = false
                                            showConfirmDialog = false
                                            android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_LONG).show()
                                        },
                                        onCompleted = { trans ->
                                            gramString = ""
                                            isSelling = false
                                            showConfirmDialog = false
                                            appState.selectedTransactionReceipt = trans
                                            appState.navigateTo(Screen.ReceiptDetail)
                                        }
                                    )
                                }
                            },
                            variant = if (isSelling) AppButtonVariant.Disabled else AppButtonVariant.Primary,
                            horizontalPadding = 8.dp,
                            modifier = Modifier.weight(1.3f)
                        )
                    }
                }
            }
        }
    }
}
