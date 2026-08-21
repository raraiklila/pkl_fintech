package com.example.pkl_finance.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.pkl_finance.data.AppState
import com.example.pkl_finance.data.Screen
import com.example.pkl_finance.ui.components.AppButton
import com.example.pkl_finance.ui.components.AppButtonVariant
import com.example.pkl_finance.ui.components.AppTopBar
import com.example.pkl_finance.ui.theme.*

@Composable
fun CairkanKonfirmasiScreen(
    appState: AppState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isConsented by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        AppTopBar(
            title = "Konfirmasi Rekening",
            onBack = { appState.navigateTo(Screen.CairkanInputRekening) }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Periksa kembali data rekening pencairan Anda sebelum disimpan.",
                fontSize = 13.sp,
                color = SlateGray,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Review Details Card
            Card(
                colors = CardDefaults.cardColors(containerColor = AppWhite),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Rincian Rekening",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkNavy
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Bank
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Bank Tujuan", fontSize = 12.sp, color = SlateGray)
                        Text(
                            text = appState.tempBankName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkNavy
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Account Number
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Nomor Rekening", fontSize = 12.sp, color = SlateGray)
                        Text(
                            text = appState.tempAccountNumber,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkNavy
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Account Name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Nama Pemilik", fontSize = 12.sp, color = SlateGray)
                        Text(
                            text = appState.tempAccountName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkNavy
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isConsented = !isConsented }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = isConsented,
                    onCheckedChange = { isConsented = it },
                    colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Saya memahami dan menyetujui rekening bank tersebut digunakan untuk seluruh proses pencairan dana usaha/toko saya.",
                    fontSize = 12.sp,
                    color = DarkNavy,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            AppButton(
                text = "Ya, Simpan",
                onClick = {
                    if (isConsented) {
                        showConfirmDialog = true
                    }
                },
                variant = if (isConsented) AppButtonVariant.Primary else AppButtonVariant.Disabled,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showConfirmDialog) {
        Dialog(
            onDismissRequest = { showConfirmDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(ColorSurface)
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Blue50, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏦", fontSize = 52.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Simpan Rekening?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorBlack,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Pastikan data rekening sudah benar untuk kelancaran pencairan dana usaha Anda.",
                        fontSize = 13.sp,
                        color = SlateGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AppButton(
                            text = "Batal",
                            onClick = { showConfirmDialog = false },
                            variant = AppButtonVariant.Primary,
                            modifier = Modifier.weight(1f)
                        )

                        AppButton(
                            text = if (isSaving) "Menyimpan..." else "Simpan",
                            onClick = {
                                if (!isSaving) {
                                    isSaving = true
                                    appState.saveBankAccount(
                                        bankName = appState.tempBankName,
                                        accountNumber = appState.tempAccountNumber,
                                        accountName = appState.tempAccountName,
                                        onSuccess = {
                                            isSaving = false
                                            showConfirmDialog = false
                                            appState.navigateTo(Screen.CairkanMain)
                                        },
                                        onError = { err ->
                                            isSaving = false
                                            showConfirmDialog = false
                                            Toast.makeText(context, "Gagal menyimpan: $err", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            },
                            variant = AppButtonVariant.Secondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
