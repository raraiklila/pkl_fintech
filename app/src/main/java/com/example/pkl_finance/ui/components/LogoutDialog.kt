package com.example.pkl_finance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.pkl_finance.ui.theme.*

@Composable
fun LogoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
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
                // ── Mascot placeholder ────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Blue50, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👶", fontSize = 52.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Keluar dari Aplikasi?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorBlack,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Kamu yakin ingin keluar?\nSemua data tetap aman dan bisa diakses kembali saat login.",
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
                    // "Batal" – Primary filled
                    AppButton(
                        text = "Batal",
                        onClick = onDismiss,
                        variant = AppButtonVariant.Primary,
                        modifier = Modifier.weight(1f)
                    )

                    // "Keluar" – Secondary outlined
                    AppButton(
                        text = "Keluar",
                        onClick = onConfirm,
                        variant = AppButtonVariant.Secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
