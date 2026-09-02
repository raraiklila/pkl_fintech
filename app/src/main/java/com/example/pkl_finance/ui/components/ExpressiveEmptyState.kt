package com.example.pkl_finance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pkl_finance.ui.theme.DarkNavy
import com.example.pkl_finance.ui.theme.PrimaryBlue
import com.example.pkl_finance.ui.theme.PrimaryBlueLight
import com.example.pkl_finance.ui.theme.SlateGray

/**
 * Material 3 Expressive Empty State View
 * Displays a glowing expressive badge with an icon, bold title, and subtext.
 */
@Composable
fun ExpressiveEmptyState(
    title: String = "Belum Ada Transaksi",
    subtitle: String = "Transaksi baru Anda akan otomatis muncul di sini setelah Anda melakukan aktivitas.",
    icon: ImageVector = Icons.Default.Inbox,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Expressive Badge Halo
        Box(
            modifier = Modifier
                .size(76.dp)
                .background(PrimaryBlueLight, CircleShape)
                .border(1.5.dp, PrimaryBlue.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = DarkNavy,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = SlateGray,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.fillMaxWidth(0.85f)
        )
    }
}
