package com.example.pkl_finance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color             
import androidx.compose.ui.text.font.FontWeight       
import androidx.compose.ui.text.style.TextAlign       
import androidx.compose.ui.text.style.TextDecoration   
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pkl_finance.data.*
import com.example.pkl_finance.ui.components.*
import com.example.pkl_finance.ui.theme.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(appState: AppState) {
    var ownerName by remember { mutableStateOf("") }
    var shopName by remember { mutableStateOf("") }
    var businessType by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Qigold", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF003366),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Daftarkan Merchant Anda!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DarkNavy,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Lengkapi data di bawah ini",
                fontSize = 14.sp,
                color = SlateGray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            AppTextField(
                value = ownerName,
                onValueChange = { ownerName = it },
                label = "Nama Pemilik",
                placeholder = "Contoh: Budi Santoso",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            AppTextField(
                value = shopName,
                onValueChange = { shopName = it },
                label = "Nama Toko / Usaha",
                placeholder = "Contoh: Warung Berkah",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            AppTextField(
                value = businessType,
                onValueChange = { businessType = it },
                label = "Jenis Usaha",
                placeholder = "Contoh: Kuliner, Sembako, Pakaian, Jasa",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            AppTextField(
                value = username,
                onValueChange = { username = it },
                label = "Username",
                placeholder = "Masukkan username",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            AppTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                placeholder = "Masukkan password",
                isPassword = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            AppTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Konfirmasi Password",
                placeholder = "Ulangi password",
                isPassword = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(text = errorMessage, color = ErrorRed, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(32.dp))

            AppButton(
                text = "Daftar Akun",
                onClick = {
                    if (ownerName.isEmpty() || shopName.isEmpty() || businessType.isEmpty() || username.isEmpty() || password.isEmpty()) {
                        errorMessage = "Semua kolom wajib diisi"
                    } else if (password != confirmPassword) {
                        errorMessage = "Password tidak cocok"
                    } else {
                        isLoading = true
                        errorMessage = ""
                        appState.register(
                            request = RegisterRequest(username, password, ownerName, shopName, businessType),
                            onSuccess = {
                                // Auto login after register
                                appState.login(
                                    request = LoginRequest(username, password),
                                    onSuccess = {
                                        isLoading = false
                                        appState.navigateTo(Screen.Home)
                                    },
                                    onError = {
                                        isLoading = false
                                        appState.navigateTo(Screen.Login)
                                    }
                                )
                            },
                            onError = { err ->
                                isLoading = false
                                errorMessage = err
                            }
                        )
                    }
                },
                variant = AppButtonVariant.Primary,
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.clickable { appState.navigateTo(Screen.Login) },
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Sudah punya akun?", color = Blue600, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "Login",
                    color = Yellow600,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}
