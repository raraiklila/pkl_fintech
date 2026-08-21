package com.example.pkl_finance.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pkl_finance.data.AppState
import com.example.pkl_finance.data.Screen
import com.example.pkl_finance.ui.components.AppButton
import com.example.pkl_finance.ui.components.AppButtonVariant
import com.example.pkl_finance.ui.components.AppTextField
import com.example.pkl_finance.ui.components.AppTopBar
import com.example.pkl_finance.ui.theme.*

@Composable
fun CairkanInputRekeningScreen(
    appState: AppState,
    modifier: Modifier = Modifier
) {
    var accountNumber by remember { mutableStateOf(appState.tempAccountNumber) }
    var accountName by remember { mutableStateOf(appState.tempAccountName) }

    val isFormValid = accountNumber.isNotEmpty() && accountName.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        AppTopBar(
            title = "Detail Rekening",
            onBack = { appState.navigateTo(Screen.CairkanPilihBank) }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Selected Bank Display
            Card(
                colors = CardDefaults.cardColors(containerColor = AppWhite),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Blue500, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = appState.tempBankName.take(3),
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)  
                        ) {
                            Text(
                                "Bank Tujuan",
                                fontSize = 11.sp,
                                color = SlateGray
                            )

                            Text(
                                appState.tempBankName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = {
                                appState.navigateTo(Screen.CairkanPilihBank)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Blue500
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Yellow50),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Yellow300),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Peringatan",
                        tint = Yellow700,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Nama pemilik rekening harus sama persis dengan nama di KTP pemilik usaha/toko.",
                            fontSize = 11.sp,
                            color = Yellow800,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Pencairan dana tidak dapat dikirim ke Virtual Account (VA). Pastikan menggunakan rekening bank reguler.",
                            fontSize = 11.sp,
                            color = Yellow800,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Informasi Rekening",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DarkNavy,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Input fields
            AppTextField(
                value = accountNumber,
                onValueChange = { accountNumber = it },
                label = "Nomor Rekening",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = accountName,
                onValueChange = { accountName = it },
                label = "Nama Pemilik Rekening",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            AppButton(
                text = "Cek Data",
                onClick = {
                    if (isFormValid) {
                        appState.tempAccountNumber = accountNumber
                        appState.tempAccountName = accountName
                        appState.navigateTo(Screen.CairkanKonfirmasi)
                    }
                },
                variant = if (isFormValid) AppButtonVariant.Primary else AppButtonVariant.Disabled,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
