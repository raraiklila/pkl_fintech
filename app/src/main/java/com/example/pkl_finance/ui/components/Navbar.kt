package com.example.pkl_finance.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pkl_finance.data.AppState
import com.example.pkl_finance.data.Screen
import com.example.pkl_finance.ui.theme.DarkNavy
import com.example.pkl_finance.ui.theme.PrimaryBlue
import com.example.pkl_finance.ui.theme.PrimaryBlueLight
import com.example.pkl_finance.ui.theme.SlateGray
import com.example.pkl_finance.ui.theme.AppWhite

@Composable
fun CustomBottomNavigation(
    appState: AppState,
    modifier: Modifier = Modifier
) {
    var showVerificationDialog by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            .background(Color.Transparent)
    ) {
        // Floating Capsule container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    clip = false,
                    ambientColor = Color.Black.copy(alpha = 0.05f),
                    spotColor = Color.Black.copy(alpha = 0.16f)
                )
                .background(AppWhite.copy(alpha = 0.98f), CircleShape)
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Beranda (Home)
            CapsuleNavItem(
                label = "Beranda",
                isSelected = appState.currentScreen == Screen.Home,
                onClick = { appState.navigateTo(Screen.Home) }
            ) { isSelected ->
                Icon(
                    painter = androidx.compose.ui.res.painterResource(
                        id = if (isSelected) com.example.pkl_finance.R.drawable.ic_home_fill else com.example.pkl_finance.R.drawable.ic_home_stroke
                    ),
                    contentDescription = "Beranda",
                    tint = if (isSelected) PrimaryBlue else Color(0xFFB3B3B3),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Tab 2: Transaksi
            CapsuleNavItem(
                label = "Transaksi",
                isSelected = appState.currentScreen == Screen.Transaksi,
                onClick = { appState.navigateTo(Screen.Transaksi) }
            ) { isSelected ->
                Icon(
                    painter = androidx.compose.ui.res.painterResource(
                        id = if (isSelected) com.example.pkl_finance.R.drawable.ic_transaksi_fill else com.example.pkl_finance.R.drawable.ic_transaksi_stroke
                    ),
                    contentDescription = "Transaksi",
                    tint = if (isSelected) Color.Unspecified else Color(0xFFB3B3B3),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Tab 3: QRIS dummy slot with NO label
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            if (!appState.isVerified) {
                                showVerificationDialog = true
                            } else {
                                appState.navigateTo(Screen.Qris)
                            }
                        }
                    ),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(68.dp)) // empty slot placeholder to balance navbar height
            }

            // Tab 4: Emas
            CapsuleNavItem(
                label = "Emas",
                isSelected = appState.currentScreen == Screen.Emas,
                onClick = { appState.navigateTo(Screen.Emas) }
            ) { isSelected ->
                Icon(
                    painter = androidx.compose.ui.res.painterResource(
                        id = if (isSelected) com.example.pkl_finance.R.drawable.ic_emas_fill else com.example.pkl_finance.R.drawable.ic_emas_stroke
                    ),
                    contentDescription = "Emas",
                    tint = if (isSelected) PrimaryBlue else Color(0xFFB3B3B3),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Tab 5: Profil
            CapsuleNavItem(
                label = "Profil",
                isSelected = appState.currentScreen == Screen.Profile,
                onClick = { appState.navigateTo(Screen.Profile) }
            ) { isSelected ->
                Icon(
                    painter = androidx.compose.ui.res.painterResource(
                        id = if (isSelected) com.example.pkl_finance.R.drawable.ic_profil_fill else com.example.pkl_finance.R.drawable.ic_profil_stroke
                    ),
                    contentDescription = "Profil",
                    tint = if (isSelected) PrimaryBlue else Color(0xFFB3B3B3),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

            // Protruding Center QRIS Circular FAB
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-18).dp)
                .shadow(6.dp, CircleShape)
                .size(52.dp)
                .background(PrimaryBlue, CircleShape)
                .clip(CircleShape)
                .clickable {
                    if (!appState.isVerified) {
                        showVerificationDialog = true
                    } else {
                        appState.navigateTo(Screen.Qris)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // QR Code icon inside the blue circle
            Column(
                modifier = Modifier.size(22.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.size(9.dp).background(Color.White, RoundedCornerShape(1.5.dp)), contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.size(3.5.dp).background(PrimaryBlue, RoundedCornerShape(0.5.dp)))
                    }
                    Box(modifier = Modifier.size(9.dp).background(Color.White, RoundedCornerShape(1.5.dp)), contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.size(3.5.dp).background(PrimaryBlue, RoundedCornerShape(0.5.dp)))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.size(9.dp).background(Color.White, RoundedCornerShape(1.5.dp)), contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.size(3.5.dp).background(PrimaryBlue, RoundedCornerShape(0.5.dp)))
                    }
                    Box(modifier = Modifier.size(9.dp), contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.size(5.dp).background(Color.White, CircleShape))
                    }
                }
            }
        }
    }

    if (showVerificationDialog) {
        VerificationRequiredDialog(
            onVerify = {
                showVerificationDialog = false
                appState.navigateTo(Screen.VerifikasiKyc)
            },
            onDismiss = {
                showVerificationDialog = false
            }
        )
    }
}

@Composable
fun RowScope.CapsuleNavItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: @Composable (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "navItemScale"
    )

    // Keep weight constant at 1f so there are no layout width changes or shifting
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Draw a subtle rounded pill background around icon & label only if selected
        Box(
            modifier = Modifier
                .wrapContentSize()
                .scale(scale)
                .then(
                    if (isSelected) {
                        Modifier
                            .background(PrimaryBlueLight, RoundedCornerShape(16.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    } else {
                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                icon(isSelected)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) PrimaryBlue else Color(0xFFB3B3B3)
                )
            }
        }
    }
}
