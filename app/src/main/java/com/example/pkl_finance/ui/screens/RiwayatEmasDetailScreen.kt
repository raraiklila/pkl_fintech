package com.example.pkl_finance.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Paid
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
import com.example.pkl_finance.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatEmasDetailScreen(
    appState: AppState,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("Semua") }
    var searchQuery by remember { mutableStateOf("") }

    val goldTransactions = remember(appState.transactionHistory) {
        appState.transactionHistory.filter { it.type == "GOLD_BUY" || it.goldAmount > 0.0 }
    }

    val filteredTransactions = remember(selectedFilter, searchQuery, goldTransactions) {
        val base = when (selectedFilter) {
            "Beli Emas" -> goldTransactions.filter { it.type == "GOLD_BUY" }
            "Autosplit QRIS" -> goldTransactions.filter { it.type == "QRIS_IN" }
            else -> goldTransactions
        }
        if (searchQuery.isEmpty()) {
            base
        } else {
            base.filter { trans ->
                val titleText = if (trans.type == "GOLD_BUY") "Beli Emas Fisik" else "Autosplit Emas"
                val subText = if (trans.type == "GOLD_BUY") "Dari Saldo Merchant" else "Ke Cicilan Emas"
                titleText.contains(searchQuery, ignoreCase = true) ||
                subText.contains(searchQuery, ignoreCase = true) ||
                trans.title.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        AppTopBar(
            title = "Riwayat Emas",
            onBack = { appState.navigateTo(Screen.Emas) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari riwayat emas...", fontSize = 13.sp, color = SlateGray) },
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

        // Filter chip row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("Semua", "Beli Emas", "Autosplit QRIS")
            filters.forEach { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isSelected) PrimaryBlue else AppWhite,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .border(
                            BorderStroke(1.dp, if (isSelected) Color.Transparent else Color(0xFFE2E8F0)),
                            RoundedCornerShape(20.dp)
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
                    text = "Belum ada riwayat emas",
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTransactions) { trans ->
                    RiwayatEmasRow(
                        trans = trans,
                        appState = appState,
                        onClick = {
                            appState.selectedTransactionReceipt = trans
                            appState.navigateTo(Screen.ReceiptDetail)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RiwayatEmasRow(
    trans: Transaction,
    appState: AppState,
    onClick: () -> Unit = {}
) {
    val isBuy = trans.type == "GOLD_BUY"
    val titleText = if (isBuy) "Beli Emas Fisik" else "Autosplit Emas"
    val subText = if (isBuy) "Dari Saldo Merchant" else "Ke Cicilan Emas"
    val weightText = "+" + String.format("%.4f", trans.goldWeightAdded) + " Gram"
    val rupiahText = appState.formatRupiah(if (isBuy) -trans.totalAmount else trans.goldAmount)

    Card(
        colors = CardDefaults.cardColors(containerColor = AppWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Yellow100, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Paid,
                    contentDescription = "Coin Icon",
                    tint = GoldAccent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
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

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = weightText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = rupiahText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkNavy
                )
            }
        }
    }
}
