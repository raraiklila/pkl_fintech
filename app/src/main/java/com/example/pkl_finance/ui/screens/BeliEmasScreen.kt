package com.example.pkl_finance.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
import com.example.pkl_finance.data.Transaction
import com.example.pkl_finance.ui.components.AppButton
import com.example.pkl_finance.ui.components.AppButtonVariant
import com.example.pkl_finance.ui.components.AppTopBar
import com.example.pkl_finance.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeliEmasScreen(
    appState: AppState,
    modifier: Modifier = Modifier
) {
    var amountString by remember { mutableStateOf("") }
    var isBuying by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current

    val goldRate = appState.goldPriceRate
    val rawAmt = amountString.toDoubleOrNull() ?: 0.0
    val weightAdded = rawAmt / goldRate
    val isExcessive = rawAmt > appState.mainBalance
    val isValid = rawAmt > 0.0 && !isExcessive

    val presets = remember(appState.goldPriceRate) {
        listOf(
            50000L to "Rp50.000",
            100000L to "Rp100.000",
            250000L to "Rp250.000",
            500000L to "Rp500.000",
            1000000L to "Rp1.000.000",
            appState.goldPriceRate.toLong() to "1 Gram"
        )
    }

    // Main Form Column
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        AppTopBar(
            title = "Beli Emas Digital",
            onBack = { appState.navigateTo(appState.previousScreen) },
            showDivider = false
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Merchant balance banner
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
                        text = "Saldo merchant",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Blue500
                    )
                    Text(
                        text = appState.formatRupiah(appState.mainBalance),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Blue800
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                    // Centered Input Box overlay
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        val formattedDisplay = if (amountString.isEmpty()) "0" else {
                            val parsed = amountString.toLongOrNull() ?: 0L
                            NumberFormat.getNumberInstance(Locale("in", "ID")).format(parsed)
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
                                color = DarkNavy
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
                            textStyle = TextStyle(
                                color = Color.Transparent,
                                fontSize = 32.sp,
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
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

                    // Preset buttons grid
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            presets.take(3).forEach { (amt, label) ->
                                val isSelected = amountString == amt.toString()
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
                                        .clickable { amountString = amt.toString() }
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
                            presets.drop(3).forEach { (amt, label) ->
                                val isSelected = amountString == amt.toString()
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
                                        .clickable { amountString = amt.toString() }
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

                    // Estimasi Emas Didapat Banner
                    if (rawAmt > 0.0) {
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
                                    text = "Estimasi Emas Didapat:",
                                    modifier = Modifier.weight(1f),
                                    fontSize = 12.sp,
                                    color = GoldDark,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = String.format(Locale.US, "%.4f Gram", weightAdded),
                                    fontSize = 13.sp,
                                    color = GoldDark,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Balance Error Notification
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
                            text = "Saldo merchant tidak mencukupi untuk pembelian ini",
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
                    text = "Beli Emas Digital",
                    onClick = {
                        if (isValid && !isBuying) {
                            isBuying = true
                            appState.buyGold(
                                amount = rawAmt,
                                onError = { err -> 
                                    isBuying = false
                                    android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_LONG).show()
                                },
                                onCompleted = { trans ->
                                    amountString = ""
                                    isBuying = false
                                    appState.selectedTransactionReceipt = trans
                                    appState.navigateTo(Screen.ReceiptDetail)
                                }
                            )
                        }
                    },
                    variant = if (isValid && !isBuying) AppButtonVariant.Primary else AppButtonVariant.Disabled,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
