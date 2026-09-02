package com.example.pkl_finance.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pkl_finance.data.AppState
import com.example.pkl_finance.data.Screen
import com.example.pkl_finance.ui.components.AppButton
import com.example.pkl_finance.ui.components.AppButtonVariant
import com.example.pkl_finance.ui.theme.*
import java.util.Locale

@Composable
fun ReceiptScreen(
    appState: AppState,
    modifier: Modifier = Modifier
) {
    val trans = appState.selectedTransactionReceipt ?: return
    val context = androidx.compose.ui.platform.LocalContext.current

    // Determine header theme based on transaction type
    val isGoldRelated = trans.type == "GOLD_BUY" || trans.type == "AUTOSPLIT_GOLD" || trans.type == "GOLD_SELL"
    val isPositive = trans.totalAmount >= 0.0

    val headerGradient = if (isGoldRelated) {
        Brush.verticalGradient(colors = listOf(Color(0xFFF5D978), GoldAccent))
    } else {
        Brush.verticalGradient(colors = listOf(Blue500, Blue800))
    }

    val headerTitleColor = if (isGoldRelated) DarkNavy else Color.White
    val closeIconTint = if (isGoldRelated) DarkNavy else Color.White

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Header background gradient
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerGradient)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(vertical = 14.dp, horizontal = 8.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
            }
            Spacer(modifier = Modifier.height(70.dp))
        }

        // konten utama di atas background
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // header + tombol tutup
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(vertical = 14.dp, horizontal = 8.dp)
            ) {
                IconButton(
                    onClick = {
                        appState.selectedTransactionReceipt = null
                        if (appState.previousScreen == Screen.BeliEmas || appState.previousScreen == Screen.JualEmas || appState.previousScreen == Screen.TarikSaldo) {
                            appState.navigateTo(Screen.Emas)
                        } else {
                            appState.navigateTo(appState.previousScreen)
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = closeIconTint
                    )
                }

                Text(
                    text = "Status Transaksi",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = headerTitleColor,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // isi nota
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // kartu nota utama
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppWhite),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // ikon bulat kiri
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (isGoldRelated) GoldAccent else if (isPositive) SuccessGreen else PrimaryBlue,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isGoldRelated) "🪙" else if (isPositive) "⚡" else "🏦",
                                    fontSize = 16.sp
                                )
                            }

                            // id transaksi di kanan
                            Text(
                                text = "ID Transaksi #${trans.id}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkNavy
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(20.dp))

                        // animasi centang berhasil
                        AnimatedCheckmark(size = 70.dp)

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (isPositive) "Pembayaran Diterima" else "Transaksi Berhasil",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive && !isGoldRelated) SuccessGreen else DarkNavy
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = trans.date,
                            fontSize = 12.sp,
                            color = SlateGray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val formattedAmt = if (trans.totalAmount < 0.0) {
                            "-Rp " + appState.formatRupiah(kotlin.math.abs(trans.totalAmount)).replace("Rp", "").trim()
                        } else {
                            "+Rp " + appState.formatRupiah(trans.totalAmount).replace("Rp", "").trim()
                        }
                        Text(
                            text = formattedAmt,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isPositive && !isGoldRelated) SuccessGreen else DarkNavy
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // garis putus-putus pemisah
                        DashedDivider(
                            color = Color(0xFFE2E8F0),
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // detail sesuai jenis transaksi
                        when (trans.type) {
                            "QRIS_IN" -> {
                                val shop = trans.title.substringAfter("Pembayaran QRIS - ").ifEmpty { "Pelanggan" }
                                DetailReceiptRow(label = "Metode pembayaran", value = "QRIS Masuk")
                                Spacer(modifier = Modifier.height(12.dp))
                                DetailReceiptRow(label = "Pengirim", value = shop)
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                DetailReceiptRow(label = "MDR", value = "- " + appState.formatRupiah(trans.mdrFee))
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                val netReceived = trans.totalAmount - trans.mdrFee
                                DetailReceiptRow(label = "Pendapatan Bersih", value = appState.formatRupiah(netReceived))
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                if (trans.mainAmount < netReceived) {
                                    val goldCut = netReceived - trans.mainAmount
                                    val goldWeight = goldCut / 1100000.0
                                    DetailReceiptRow(label = "Masuk Saldo", value = appState.formatRupiah(trans.mainAmount))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    DetailReceiptRow(
                                        label = "Autosplit Emas", 
                                        value = "${appState.formatRupiah(goldCut)} (${String.format(Locale.US, "%.4f", goldWeight)} g)"
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                                
                                DetailReceiptRow(label = "Toko", value = appState.shopName)
                            }
                            "WITHDRAW" -> {
                                DetailReceiptRow(label = "Akun pencairan", value = getFullBankName(appState.disbursementBankName))
                                Spacer(modifier = Modifier.height(12.dp))
                                DetailReceiptRow(label = "No. akun", value = maskAccountNumber(appState.disbursementAccountNumber))
                                Spacer(modifier = Modifier.height(12.dp))
                                DetailReceiptRow(label = "Nama pemilik", value = maskAccountName(appState.disbursementAccountName))
                                Spacer(modifier = Modifier.height(12.dp))
                                DetailReceiptRow(label = "Toko", value = appState.shopName)
                            }
                            "GOLD_BUY" -> {
                                DetailReceiptRow(label = "Metode pembayaran", value = "Saldo Merchant")
                                Spacer(modifier = Modifier.height(12.dp))
                                DetailReceiptRow(label = "Nominal pembelian", value = appState.formatRupiah(kotlin.math.abs(trans.totalAmount)))
                                Spacer(modifier = Modifier.height(12.dp))
                                DetailReceiptRow(
                                    label = "Emas didapat",
                                    value = String.format(Locale.US, "%.4f Gram", trans.goldWeightAdded)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                DetailReceiptRow(label = "Harga beli emas", value = "${appState.formatRupiah(appState.goldBuyPrice)} / Gram")
                                Spacer(modifier = Modifier.height(12.dp))
                                DetailReceiptRow(label = "Toko", value = appState.shopName)
                            }
                            "GOLD_SELL" -> {
                                DetailReceiptRow(label = "Tujuan dana", value = "Saldo Merchant")
                                Spacer(modifier = Modifier.height(12.dp))
                                DetailReceiptRow(label = "Total dana diterima", value = "+ " + appState.formatRupiah(trans.totalAmount))
                                Spacer(modifier = Modifier.height(12.dp))
                                DetailReceiptRow(
                                    label = "Emas yang dijual",
                                    value = String.format(Locale.US, "%.4f Gram", kotlin.math.abs(trans.goldWeightAdded))
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                DetailReceiptRow(label = "Harga jual (buyback)", value = "${appState.formatRupiah(appState.goldSellPrice)} / Gram")
                                Spacer(modifier = Modifier.height(12.dp))
                                DetailReceiptRow(label = "Toko", value = appState.shopName)
                            }
                            "AUTOSPLIT_GOLD" -> {
                                DetailReceiptRow(label = "Alokasi", value = "Autosplit Emas")
                                Spacer(modifier = Modifier.height(12.dp))
                                DetailReceiptRow(label = "Tujuan", value = "Tabungan Emas")
                                Spacer(modifier = Modifier.height(12.dp))
                                DetailReceiptRow(
                                    label = "Emas didapat",
                                    value = String.format(Locale.US, "%.4f Gram", trans.goldWeightAdded)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                DetailReceiptRow(label = "Toko", value = appState.shopName)
                            }
                            else -> {
                                DetailReceiptRow(label = "Kategori", value = trans.title)
                                Spacer(modifier = Modifier.height(12.dp))
                                DetailReceiptRow(label = "Toko", value = appState.shopName)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // tombol bawah
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
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // unduh & bagikan
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AppButton(
                                text = "Unduh",
                                onClick = {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Nota berhasil diunduh ke Galeri!",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                },
                                variant = AppButtonVariant.Secondary,
                                modifier = Modifier.weight(1f)
                            )
                            AppButton(
                                text = "Bagikan",
                                onClick = {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Link nota berhasil disalin ke clipboard!",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                },
                                variant = AppButtonVariant.Secondary,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // balik ke beranda
                        AppButton(
                            text = "Kembali ke Beranda",
                            onClick = {
                                appState.selectedTransactionReceipt = null
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
