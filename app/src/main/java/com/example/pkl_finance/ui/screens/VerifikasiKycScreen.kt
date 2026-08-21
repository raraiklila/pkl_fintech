package com.example.pkl_finance.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pkl_finance.data.AppState
import com.example.pkl_finance.data.VerifyRequest
import com.example.pkl_finance.data.Screen
import com.example.pkl_finance.ui.components.AppButton
import com.example.pkl_finance.ui.components.AppButtonVariant
import com.example.pkl_finance.ui.components.AppTextField
import com.example.pkl_finance.ui.components.AppTopBar
import com.example.pkl_finance.ui.components.IncompleteDataDialog
import com.example.pkl_finance.ui.theme.*

@Composable
fun VerifikasiKycScreen(appState: AppState) {
    val context = LocalContext.current
    var fullName by remember { mutableStateOf(appState.ownerName) }
    var nik by remember { mutableStateOf("") }
    var shopAddress by remember { mutableStateOf("") }
    
    var isKtpUploaded by remember { mutableStateOf(false) }
    var isSelfieUploaded by remember { mutableStateOf(false) }
    var isShopUploaded by remember { mutableStateOf(false) }
    
    var isLoading by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }

    var incompleteDialogMessage by remember { mutableStateOf<String?>(null) }

    val isNameValid = fullName.trim().length >= 3
    val isNikValid = nik.length == 16
    val isShopAddressValid = shopAddress.trim().length >= 5
    val isPhotosValid = isKtpUploaded && isSelfieUploaded && isShopUploaded

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Verifikasi Data (KYC)",
                onBack = { appState.navigateTo(Screen.Home) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(paddingValues)
        ) {
            // Scrollable content (grows)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp, bottom = 16.dp)
            ) {
                Text(
                    text = "Lengkapi Dokumen Anda",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkNavy
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Verifikasi data diperlukan agar Anda bisa menerima pembayaran melalui QRIS dan mengakses fitur emas.",
                    fontSize = 14.sp,
                    color = SlateGray,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(24.dp))

                AppTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = "Nama Lengkap (Sesuai KTP)",
                    placeholder = "Masukkan nama sesuai KTP",
                    errorText = if (showErrors && !isNameValid) "Nama lengkap wajib diisi (minimal 3 karakter)" else null,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                AppTextField(
                    value = shopAddress,
                    onValueChange = { shopAddress = it },
                    label = "Alamat Toko",
                    placeholder = "Contoh: Jl. Soekarno-Hatta No.12, Malang",
                    errorText = if (showErrors && !isShopAddressValid) "Alamat toko wajib diisi (minimal 5 karakter)" else null,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                AppTextField(
                    value = nik,
                    onValueChange = { newVal ->
                        val clean = newVal.filter { it.isDigit() }
                        if (clean.length <= 16) {
                            nik = clean
                        }
                    },
                    label = "Nomor Induk Kependudukan (NIK)",
                    placeholder = "Contoh: 3314012345678901",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    errorText = if (showErrors && !isNikValid) "NIK harus 16 digit angka (saat ini ${nik.length}/16)" else null,
                    helperText = "Wajib 16 digit angka sesuai KTP Anda",
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("Dokumen Pendukung", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkNavy)
                Spacer(modifier = Modifier.height(14.dp))
                
                PhotoUploadBox("Foto KTP Asli", isKtpUploaded) { isKtpUploaded = true }
                Spacer(modifier = Modifier.height(14.dp))
                PhotoUploadBox("Foto Selfie dengan KTP", isSelfieUploaded) { isSelfieUploaded = true }
                Spacer(modifier = Modifier.height(14.dp))
                PhotoUploadBox("Foto Tampak Depan Toko", isShopUploaded) { isShopUploaded = true }
            }

            // Fixed bottom button
            Surface(
                shadowElevation = 8.dp,
                color = AppWhite
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    AppButton(
                        text = "Kirim Data Verifikasi",
                        onClick = {
                            showErrors = true
                            val missingParts = mutableListOf<String>()
                            if (!isNameValid) missingParts.add("• Nama lengkap minimal 3 karakter")
                            if (!isNikValid) missingParts.add("• NIK harus persis 16 digit angka (saat ini ${nik.length}/16)")
                            if (!isShopAddressValid) missingParts.add("• Alamat toko wajib diisi (minimal 5 karakter)")
                            if (!isKtpUploaded) missingParts.add("• Foto KTP Asli belum diupload")
                            if (!isSelfieUploaded) missingParts.add("• Foto Selfie dengan KTP belum diupload")
                            if (!isShopUploaded) missingParts.add("• Foto Tampak Depan Toko belum diupload")

                            if (missingParts.isNotEmpty()) {
                                incompleteDialogMessage = "Harap lengkapi/perbaiki data berikut:\n\n" + missingParts.joinToString("\n")
                            } else {
                                isLoading = true
                                appState.verifyKyc(
                                    request = VerifyRequest(
                                        merchantId = appState.merchantId,
                                        fullName = fullName,
                                        nik = nik,
                                        shopAddress = shopAddress
                                    ),
                                    onSuccess = {
                                        isLoading = false
                                        appState.isVerified = true
                                        Toast.makeText(context, "Verifikasi Berhasil! Layanan QRIS & Emas Aktif.", Toast.LENGTH_LONG).show()
                                        appState.navigateTo(Screen.Home)
                                    },
                                    onError = { err ->
                                        isLoading = false
                                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        },
                        variant = AppButtonVariant.Primary,
                        isLoading = isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    incompleteDialogMessage?.let { msg ->
        IncompleteDataDialog(
            message = msg,
            onDismiss = { incompleteDialogMessage = null }
        )
    }
}

@Composable
fun PhotoUploadBox(label: String, isUploaded: Boolean, onClick: () -> Unit) {
    val bgColor = if (isUploaded) Color(0xFFECFDF5) else AppWhite
    val iconBgColor = if (isUploaded) Color(0xFFD1FAE5) else Blue50
    val iconTint = if (isUploaded) SuccessGreen else Blue600

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
            .background(bgColor, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(vertical = 24.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(iconBgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isUploaded) Icons.Default.CheckCircle else Icons.Default.CameraAlt,
                    contentDescription = "Upload",
                    tint = iconTint,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                color = DarkNavy,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isUploaded) "Dokumen berhasil diunggah ✓" else "Ketuk untuk mengambil/mengunggah foto",
                color = if (isUploaded) SuccessGreen else SlateGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
