package com.example.pkl_finance.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pkl_finance.data.AppState
import com.example.pkl_finance.data.Screen
import com.example.pkl_finance.ui.components.AppTopBar
import com.example.pkl_finance.ui.components.AppTextField
import com.example.pkl_finance.ui.theme.*

data class BankOption(
    val id: String,
    val name: String,
    val brandColor: Color,
    val logoText: String
)

@Composable
fun CairkanPilihBankScreen(
    appState: AppState,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val banks = remember {
        listOf(
            BankOption("BCA", "BCA (Bank Central Asia)", Color(0xFF00569E), "BCA"),
            BankOption("MANDIRI", "Bank Mandiri", Color(0xFF1E3A8A), "MDR"),
            BankOption("BNI", "BNI (Bank Negara Indonesia)", Color(0xFFE25822), "BNI"),
            BankOption("BRI", "BRI (Bank Rakyat Indonesia)", Color(0xFF00569E), "BRI"),
            BankOption("BSI", "BSI (Bank Syariah Indonesia)", Color(0xFF009B90), "BSI"),
            BankOption("PERMATA", "Bank Permata", Color(0xFF84CC16), "PRM"),
            BankOption("CIMB", "CIMB Niaga", Color(0xFFDC2626), "CMB"),
            BankOption("DANAMON", "Bank Danamon", Color(0xFFEA580C), "DNM")
        )
    }

    val filteredBanks = remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            banks
        } else {
            banks.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        AppTopBar(
            title = "Pilih Bank",
            onBack = { appState.navigateTo(Screen.CairkanMain) }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Pilih bank tujuan pencairan dana Anda",
                fontSize = 13.sp,
                color = SlateGray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Search input field
            AppTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Cari nama bank...",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Daftar Bank",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DarkNavy,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            if (filteredBanks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bank tidak ditemukan",
                        fontSize = 14.sp,
                        color = SlateGray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredBanks) { bank ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AppWhite),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    appState.tempBankName = bank.id
                                    appState.navigateTo(Screen.CairkanInputRekening)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Bank logo/avatar
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(bank.brandColor, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = bank.logoText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Text(
                                    text = bank.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkNavy
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
