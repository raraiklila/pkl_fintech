package com.example.pkl_finance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pkl_finance.data.AppState
import com.example.pkl_finance.data.Screen
import com.example.pkl_finance.data.Transaction
import com.example.pkl_finance.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransaksiScreen(
    appState: AppState,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("Semua") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredTransactions = remember(selectedFilter, searchQuery, appState.transactionHistory) {
        val base = when (selectedFilter) {
            "QRIS Masuk" -> appState.transactionHistory.filter { it.type == "QRIS_IN" }
            "Tabungan Emas" -> appState.transactionHistory.filter { it.type == "GOLD_BUY" || it.goldAmount > 0.0 }
            else -> appState.transactionHistory
        }
        if (searchQuery.isEmpty()) {
            base
        } else {
            base.filter { trans ->
                trans.title.contains(searchQuery, ignoreCase = true) ||
                trans.type.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Top Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp)
                .background(AppWhite)
                .statusBarsPadding()
                .padding(vertical = 16.dp, horizontal = 20.dp)
        ) {
            Text(
                text = "Riwayat Transaksi",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkNavy,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari riwayat transaksi...", fontSize = 13.sp, color = SlateGray) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = SlateGray,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = SlateGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedContainerColor = AppWhite,
                unfocusedContainerColor = AppWhite
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("Semua", "QRIS Masuk", "Tabungan Emas")
            filters.forEach { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isSelected) PrimaryBlue else Color.White,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) Color.White else SlateGray
                    )
                }
            }
        }

        // Transactions list
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada transaksi",
                    fontSize = 14.sp,
                    color = SlateGray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredTransactions) { trans ->
                    TransactionItem(trans, appState)
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    trans: Transaction,
    appState: AppState
) {
    val isPositive = trans.totalAmount >= 0.0

    val titleText: String
    val subText: String

    when (trans.type) {
        "QRIS_IN" -> {
            titleText = "Pembayaran QRIS"
            val shop = trans.title.substringAfter("Pembayaran QRIS - ").ifEmpty { "Toko Anda" }
            subText = "Dari $shop"
        }
        "AUTOSPLIT_GOLD" -> {
            titleText = "Autosplit Emas"
            subText = "Ke Cicilan Emas"
        }
        "WITHDRAW" -> {
            titleText = "Pencairan Saldo"
            subText = "Ke Rekening BCA - ******1711"
        }
        "GOLD_BUY" -> {
            titleText = "Beli Emas Fisik"
            subText = "Dari Saldo Merchant"
        }
        else -> {
            if (trans.title.contains(" - ")) {
                titleText = trans.title.substringBefore(" - ")
                subText = trans.title.substringAfter(" - ")
            } else {
                titleText = trans.title
                subText = "Transaksi Dompet"
            }
        }
    }

    val formattedAmt = if (trans.totalAmount < 0.0) {
        "-Rp" + appState.formatRupiah(kotlin.math.abs(trans.totalAmount)).replace("Rp", "").trim()
    } else {
        "+Rp" + appState.formatRupiah(trans.totalAmount).replace("Rp", "").trim()
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = AppWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                appState.selectedTransactionReceipt = trans
                appState.navigateTo(Screen.ReceiptDetail)
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Transaction Icon Container
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (isPositive) SuccessGreenLight else ErrorRedLight,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isPositive) "↗" else "↙",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPositive) SuccessGreen else ErrorRed
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details Column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = titleText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkNavy
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subText,
                    fontSize = 12.sp,
                    color = SlateGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = trans.date,
                    fontSize = 11.sp,
                    color = SlateGray
                )
            }

            // Amount aligned to the top-right
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formattedAmt,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPositive) SuccessGreen else ErrorRed
                )
                if (trans.goldWeightAdded > 0.0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "+${String.format("%.4f", trans.goldWeightAdded)} Gram Emas",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GoldAccent
                    )
                }
            }
        }
    }
}
