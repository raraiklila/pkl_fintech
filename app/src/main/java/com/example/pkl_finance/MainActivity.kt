package com.example.pkl_finance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.window.Dialog
import com.example.pkl_finance.data.AppState
import com.example.pkl_finance.data.Screen
import com.example.pkl_finance.ui.components.CustomBottomNavigation
import com.example.pkl_finance.ui.components.AppButton
import com.example.pkl_finance.ui.components.AppButtonVariant
import com.example.pkl_finance.ui.screens.*
import com.example.pkl_finance.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Pkl_financeTheme {
                val appState = remember { AppState() }

                androidx.compose.runtime.LaunchedEffect(Unit) {
                    appState.checkInitialAuth()
                    if (!appState.isLoggedIn) {
                        appState.navigateTo(Screen.Login)
                    } else {
                        appState.refreshData()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // Hide bottom nav bar on Config screen, verified QRIS page, Riwayat Emas Detail, and Cairkan screens
                        val showBottomBar = appState.currentScreen != Screen.ConfigCicilEmas &&
                                appState.currentScreen != Screen.RiwayatEmasDetail &&
                                appState.currentScreen != Screen.CairkanMain &&
                                appState.currentScreen != Screen.TarikSaldo &&
                                appState.currentScreen != Screen.LaporanEmas &&
                                appState.currentScreen != Screen.BeliEmas &&
                                appState.currentScreen != Screen.CairkanPilihBank &&
                                appState.currentScreen != Screen.CairkanInputRekening &&
                                appState.currentScreen != Screen.CairkanKonfirmasi &&
                                appState.currentScreen != Screen.ReceiptDetail &&
                                appState.currentScreen != Screen.Laporan &&
                                appState.currentScreen != Screen.Login &&
                                appState.currentScreen != Screen.Register &&
                                appState.currentScreen != Screen.VerifikasiKyc &&
                                !(appState.currentScreen == Screen.Qris && (appState.merchantInfo?.isVerified == true)) &&
                                appState.isBottomBarVisible
                        if (showBottomBar) {
                            CustomBottomNavigation(appState = appState)
                        }
                    }
                ) { innerPadding ->
                    // Switch screen layout based on active state with crossfade transition
                    val hidePadding = appState.currentScreen == Screen.ConfigCicilEmas ||
                            appState.currentScreen == Screen.RiwayatEmasDetail ||
                            appState.currentScreen == Screen.CairkanMain ||
                            appState.currentScreen == Screen.TarikSaldo ||
                            appState.currentScreen == Screen.LaporanEmas ||
                            appState.currentScreen == Screen.BeliEmas ||
                            appState.currentScreen == Screen.CairkanPilihBank ||
                            appState.currentScreen == Screen.CairkanInputRekening ||
                            appState.currentScreen == Screen.CairkanKonfirmasi ||
                            appState.currentScreen == Screen.ReceiptDetail ||
                            appState.currentScreen == Screen.Laporan ||
                            appState.currentScreen == Screen.Login ||
                            appState.currentScreen == Screen.Register ||
                            appState.currentScreen == Screen.VerifikasiKyc ||
                            (appState.currentScreen == Screen.Qris && appState.merchantInfo?.isVerified == true)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                bottom = if (hidePadding) 0.dp else innerPadding.calculateBottomPadding()
                            )
                    ) {
                        Crossfade(targetState = appState.currentScreen, label = "ScreenTransition") { screen ->
                            when (screen) {
                                Screen.Home -> HomeScreen(appState = appState)
                                Screen.Transaksi -> TransaksiScreen(appState = appState)
                                Screen.Qris -> QrisScreen(appState = appState)
                                Screen.Emas -> EmasScreen(appState = appState)
                                Screen.RiwayatEmasDetail -> RiwayatEmasDetailScreen(appState = appState)
                                Screen.LaporanEmas -> LaporanEmasScreen(appState = appState)
                                Screen.CairkanMain -> CairkanMainScreen(appState = appState)
                                Screen.TarikSaldo -> TarikSaldoScreen(appState = appState)
                                Screen.BeliEmas -> BeliEmasScreen(appState = appState)
                                Screen.CairkanPilihBank -> CairkanPilihBankScreen(appState = appState)
                                Screen.CairkanInputRekening -> CairkanInputRekeningScreen(appState = appState)
                                Screen.CairkanKonfirmasi -> CairkanKonfirmasiScreen(appState = appState)
                                Screen.Profile -> ProfileScreen(appState = appState)
                                Screen.ConfigCicilEmas -> ConfigCicilEmasScreen(appState = appState)
                                Screen.ReceiptDetail -> ReceiptScreen(appState = appState)
                                Screen.Laporan -> LaporanScreen(appState = appState)
                                Screen.Login -> LoginScreen(appState = appState)
                                Screen.Register -> RegisterScreen(appState = appState)
                                Screen.VerifikasiKyc -> VerifikasiKycScreen(appState = appState)
                                else -> HomeScreen(appState = appState)
                            }
                        }

                        // Congratulatory completion dialog overlay
                        if (appState.showCompletionDialog) {
                            Dialog(onDismissRequest = { appState.showCompletionDialog = false }) {
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
                                        // Golden Badge
                                        Box(
                                            modifier = Modifier
                                                .size(80.dp)
                                                .background(GoldAccentLight, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "🎉",
                                                fontSize = 40.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))

                                        Text(
                                            text = "Cicilan Emas Lunas!",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkNavy,
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = "Selamat! Cicilan emas sebesar ${appState.completedInstallmentWeight}g Anda telah lunas terbayar otomatis lewat autosplit QRIS. Emas batangan fisik Anda telah ditransfer ke Tabungan Emas Utama.",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = SlateGray,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 20.sp
                                        )

                                        Spacer(modifier = Modifier.height(28.dp))

                                        Button(
                                            onClick = { appState.showCompletionDialog = false },
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                            shape = CircleShape,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                        ) {
                                            Text(
                                                text = "Hebat! Terima Kasih",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }


                    }
                }
            }
        }
    }
}