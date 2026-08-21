package com.example.pkl_finance.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.pkl_finance.data.AppState
import com.example.pkl_finance.ui.components.AppButton
import com.example.pkl_finance.ui.components.AppButtonVariant
import com.example.pkl_finance.ui.components.AppTopBar
import com.example.pkl_finance.ui.components.CustomProgressBar
import com.example.pkl_finance.ui.theme.*

@Composable
fun ProfileScreen(
    appState: AppState,
    modifier: Modifier = Modifier
) {
    var activeSubScreen by remember { mutableStateOf("MAIN") }
    var editFieldKey by remember { mutableStateOf("") }
    var editFieldTitle by remember { mutableStateOf("") }
    var editFieldValue by remember { mutableStateOf("") }
    var editFieldKeyboardType by remember { mutableStateOf(KeyboardType.Text) }
    var editFieldBackRoute by remember { mutableStateOf("MAIN") }

    LaunchedEffect(activeSubScreen) {
        appState.isBottomBarVisible = (activeSubScreen == "MAIN")
    }

    DisposableEffect(Unit) {
        onDispose {
            appState.isBottomBarVisible = true
        }
    }

    when (activeSubScreen) {
        "MAIN" -> {
            MainProfileView(
                appState = appState,
                onNavigate = { activeSubScreen = it },
                modifier = modifier
            )
        }
        "KEAMANAN" -> {
            KeamananAkunView(
                onBack = { activeSubScreen = "MAIN" }
            )
        }
        "EDIT_PROFILE" -> {
            EditProfileView(
                appState = appState,
                onBack = { activeSubScreen = "MAIN" },
                onNavigateToEditField = { key, title, valStr, kbType, backRoute ->
                    editFieldKey = key
                    editFieldTitle = title
                    editFieldValue = valStr
                    editFieldKeyboardType = kbType
                    editFieldBackRoute = backRoute
                    activeSubScreen = "SINGLE_FIELD_EDIT"
                }
            )
        }
        "SINGLE_FIELD_EDIT" -> {
            SingleFieldEditView(
                title = editFieldTitle,
                initialValue = editFieldValue,
                keyboardType = editFieldKeyboardType,
                onSave = { newValue ->
                    when (editFieldKey) {
                        "OWNER_NAME" -> appState.ownerName = newValue
                        "PHONE" -> appState.phoneNumber = newValue
                        "EMAIL" -> appState.email = newValue
                        "SHOP_NAME" -> appState.shopName = newValue
                        "BUSINESS_TYPE" -> appState.businessType = newValue
                        "SHOP_ADDRESS" -> appState.shopAddress = newValue
                    }
                    activeSubScreen = editFieldBackRoute
                },
                onCancel = {
                    activeSubScreen = editFieldBackRoute
                }
            )
        }
        "FAQ" -> {
            FaqView(
                onBack = { activeSubScreen = "MAIN" }
            )
        }
        "TOKO_SAYA" -> {
            TokoSayaView(
                appState = appState,
                onBack = { activeSubScreen = "MAIN" },
                onNavigateToEditShopInfo = { activeSubScreen = "EDIT_SHOP_INFO" }
            )
        }
        "EDIT_SHOP_INFO" -> {
            EditShopInfoView(
                appState = appState,
                onBack = { activeSubScreen = "TOKO_SAYA" }
            )
        }
        "QRIS_INFO" -> {
            QrisInfoView(
                appState = appState,
                onBack = { activeSubScreen = "MAIN" }
            )
        }
        "REKENING_INFO" -> {
            RekeningInfoView(
                appState = appState,
                onBack = { activeSubScreen = "MAIN" }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainProfileView(
    appState: AppState,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
    ) {
        // Top Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp)
                .background(AppWhite)
                .padding(vertical = 16.dp, horizontal = 24.dp)
        ) {
            Text(
                text = "Profil",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkNavy,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Scrollable content list
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Profile Header Section (Avatar & Name/Shop Details)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Circular Avatar with border
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Blue50, CircleShape)
                        .border(2.dp, PrimaryBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = appState.ownerName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkNavy
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = appState.shopName,
                    fontSize = 14.sp,
                    color = SlateGray,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (appState.isVerified) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFD1FAE5), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Merchant Terverifikasi",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Card 1: Pengaturan Akun
            ProfileSectionCard(title = "Pengaturan Akun") {
                ProfileSettingRow(
                    title = "Toko Saya",
                    sub = "Lihat rincian identitas merchant",
                    icon = Icons.Default.Store,
                    onClick = { onNavigate("TOKO_SAYA") }
                )
                ProfileSettingRow(
                    title = "Informasi QRIS",
                    sub = "Lihat status QRIS, tarif MDR, & volume limit",
                    icon = Icons.Default.QrCode,
                    onClick = { onNavigate("QRIS_INFO") }
                )
                ProfileSettingRow(
                    title = "Edit Profil",
                    sub = "Atur informasi pribadi",
                    icon = Icons.Default.Edit,
                    onClick = { onNavigate("EDIT_PROFILE") }
                )
                ProfileSettingRow(
                    title = "Keamanan Akun",
                    sub = "Kata sandi, PIN",
                    icon = Icons.Default.Lock,
                    onClick = { onNavigate("KEAMANAN") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 2: Pengaturan Umum (FAQ, Bahasa, Tema, Tentang Kami, QRIS info & Rekening)
            ProfileSectionCard(title = "Pengaturan Umum") {
                ProfileSettingRow(
                    title = "FAQ",
                    sub = "Cari pertanyaan anda",
                    icon = Icons.Default.QuestionAnswer,
                    onClick = { onNavigate("FAQ") }
                )
                ProfileSettingRow(
                    title = "Bahasa",
                    sub = "Pilih bahasa sesuai preferensi",
                    icon = Icons.Default.Language,
                    onClick = { Toast.makeText(context, "Aplikasi diset default Bahasa Indonesia", Toast.LENGTH_SHORT).show() }
                )
                ProfileSettingRow(
                    title = "Tema",
                    sub = "Atur tampilan sesuai kenyamanan",
                    icon = Icons.Default.Contrast,
                    onClick = { Toast.makeText(context, "Mode Gelap akan hadir segera!", Toast.LENGTH_SHORT).show() }
                )
                ProfileSettingRow(
                    title = "Tentang Kami",
                    sub = "Informasi mengenai aplikasi",
                    icon = Icons.Default.Info,
                    onClick = { showAboutDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout Button (Design System Style - Danger Red Variant)
            AppButton(
                text = "Keluar Akun",
                onClick = { showLogoutDialog = true },
                variant = AppButtonVariant.Danger,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 100.dp)
            )

            if (showLogoutDialog) {
                com.example.pkl_finance.ui.components.LogoutDialog(
                    onConfirm = {
                        showLogoutDialog = false
                        Toast.makeText(context, "Berhasil Keluar Akun", Toast.LENGTH_SHORT).show()
                        appState.logout()
                    },
                    onDismiss = {
                        showLogoutDialog = false
                    }
                )
            }
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Tentang QiGold", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("QiGold Merchant Application", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Version 1.0.0 (Release Build)", fontSize = 12.sp, color = SlateGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Platform pencatatan tabungan emas digital terintegrasi untuk merchant PKL secara instan.", fontSize = 13.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Tutup", color = Color(0xFF7C3AED))
                }
            }
        )
    }
}

@Composable
fun EditProfileView(
    appState: AppState,
    onBack: () -> Unit,
    onNavigateToEditField: (key: String, title: String, currentVal: String, kbType: KeyboardType, backRoute: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
    ) {
        AppTopBar(
            title = "Edit Profil",
            onBack = onBack,
            showDivider = false
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Profile Picture
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(Blue50, CircleShape)
                    .border(2.dp, PrimaryBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Ubah Foto",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue,
                modifier = Modifier.clickable {
                    // Stub for profile picture change
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = AppWhite),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    EditableSettingRow(
                        label = "Nama Lengkap",
                        value = appState.ownerName,
                        onClick = {
                            onNavigateToEditField("OWNER_NAME", "Nama Lengkap", appState.ownerName, KeyboardType.Text, "EDIT_PROFILE")
                        }
                    )
                    HorizontalDivider(color = Color(0xFFF8FAFC), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    EditableSettingRow(
                        label = "Nomor Telepon",
                        value = appState.phoneNumber,
                        onClick = {
                            onNavigateToEditField("PHONE", "Nomor Telepon", appState.phoneNumber, KeyboardType.Phone, "EDIT_PROFILE")
                        }
                    )
                    HorizontalDivider(color = Color(0xFFF8FAFC), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    EditableSettingRow(
                        label = "Email",
                        value = appState.email,
                        onClick = {
                            onNavigateToEditField("EMAIL", "Email", appState.email, KeyboardType.Email, "EDIT_PROFILE")
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun TokoSayaView(
    appState: AppState,
    onBack: () -> Unit,
    onNavigateToEditShopInfo: () -> Unit
) {
    val context = LocalContext.current
    var showWarningDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
    ) {
        AppTopBar(
            title = "Toko Saya",
            onBack = onBack,
            showDivider = false
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Shop Store Avatar (Branding Blue Theme)
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(Blue50, CircleShape)
                    .border(2.dp, PrimaryBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = "Store Avatar",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = AppWhite),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    // ID Toko / NMID at the very top (Copyable)
                    CopyableSettingRow(
                        label = "ID Toko / NMID",
                        value = "ID1029384756123",
                        onCopy = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("NMID", "ID1029384756123")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "NMID berhasil disalin!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    HorizontalDivider(color = Color(0xFFF8FAFC), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    
                    // Static Detail Rows (No Chevron)
                    DetailSettingRow(label = "Nama Toko", value = appState.shopName)
                    HorizontalDivider(color = Color(0xFFF8FAFC), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    DetailSettingRow(label = "Jenis Usaha", value = appState.businessType)
                    HorizontalDivider(color = Color(0xFFF8FAFC), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    DetailSettingRow(label = "Alamat Toko", value = appState.shopAddress)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Card containing Single Edit Row
            Card(
                colors = CardDefaults.cardColors(containerColor = AppWhite),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                modifier = Modifier.fillMaxWidth()
            ) {
                EditableSettingRow(
                    label = "Ubah Informasi Toko",
                    value = "",
                    onClick = {
                        showWarningDialog = true
                    }
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showWarningDialog) {
        QrisChangeWarningDialog(
            onConfirm = {
                showWarningDialog = false
                onNavigateToEditShopInfo()
            },
            onDismiss = {
                showWarningDialog = false
            }
        )
    }
}

@Composable
fun KeamananAkunView(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var transactionPin by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
    ) {
        AppTopBar(
            title = "Keamanan Akun",
            onBack = onBack,
            showDivider = false
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            ProfileEditField(label = "Kata Sandi Saat Ini", value = currentPassword, onValueChange = { currentPassword = it })
            Spacer(modifier = Modifier.height(16.dp))
            ProfileEditField(label = "Kata Sandi Baru", value = newPassword, onValueChange = { newPassword = it })
            Spacer(modifier = Modifier.height(16.dp))
            ProfileEditField(label = "PIN Transaksi Baru (6 Digit)", value = transactionPin, onValueChange = { transactionPin = it }, keyboardType = KeyboardType.Number)

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Bottom CTA Button with white background and top border/shadow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppWhite)
                .padding(24.dp)
        ) {
            AppButton(
                text = "Simpan Sandi & PIN",
                onClick = {
                    Toast.makeText(context, "Kata sandi & PIN berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                variant = AppButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SingleFieldEditView(
    title: String,
    initialValue: String,
    keyboardType: KeyboardType,
    onSave: (String) -> Unit,
    onCancel: () -> Unit
) {
    var value by remember { mutableStateOf(initialValue) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
    ) {
        AppTopBar(
            title = "Ubah $title",
            onBack = onCancel,
            navigationIcon = Icons.Default.Close,
            showDivider = false
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DarkNavy
            )
            Spacer(modifier = Modifier.height(8.dp))

            // White capsule input matching AppButton height and shape
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(AppWhite, CircleShape)
                    .border(BorderStroke(1.5.dp, Blue50), CircleShape)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = { value = it },
                    textStyle = TextStyle(
                        color = DarkNavy,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pastikan data yang dimasukkan benar dan sesuai identitas resmi.",
                fontSize = 11.sp,
                color = SlateGray
            )

        }

        // Bottom CTA Button with white background and top border/shadow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppWhite)
                .padding(24.dp)
        ) {
            AppButton(
                text = "Simpan Perubahan",
                onClick = {
                    if (value.isNotBlank()) {
                        onSave(value)
                    }
                },
                variant = AppButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun EditableSettingRow(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = SlateGray,
            fontWeight = FontWeight.Medium
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.weight(1f).padding(start = 16.dp)
        ) {
            Text(
                text = value,
                fontSize = 14.sp,
                color = DarkNavy,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f).padding(end = 6.dp)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = SlateGray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun CopyableSettingRow(
    label: String,
    value: String,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCopy() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = SlateGray,
            fontWeight = FontWeight.Medium
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.weight(1f).padding(start = 16.dp)
        ) {
            Text(
                text = value,
                fontSize = 14.sp,
                color = DarkNavy,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f).padding(end = 6.dp)
            )
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy ID",
                tint = SlateGray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun QrisInfoView(
    appState: AppState,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
    ) {
        AppTopBar(
            title = "Informasi QRIS & MDR",
            onBack = onBack,
            showDivider = false
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Card 1: Merchant QRIS Info
            Card(
                colors = CardDefaults.cardColors(containerColor = AppWhite),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    CopyableSettingRow(
                        label = "NMID / ID Toko",
                        value = "ID1029384756123",
                        onCopy = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("NMID", "ID1029384756123")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "NMID berhasil disalin!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    HorizontalDivider(color = Color(0xFFF8FAFC), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    DetailSettingRow(
                        label = "Status QRIS",
                        value = "",
                        badgeText = "Aktif",
                        badgeColor = Color(0xFFD1FAE5),
                        badgeTextColor = Color(0xFF059669)
                    )
                    HorizontalDivider(color = Color(0xFFF8FAFC), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    DetailSettingRow(
                        label = "Kategori Merchant",
                        value = if (appState.isUMI) "UMI" else "Reguler"
                    )
                    HorizontalDivider(color = Color(0xFFF8FAFC), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    DetailSettingRow(
                        label = "Bergabung Sejak",
                        value = "15 Juli 2026"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 2: MDR Rates
            Card(
                colors = CardDefaults.cardColors(containerColor = AppWhite),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    DetailSettingRow(
                        label = "Kategori MDR",
                        value = "",
                        badgeText = if (appState.isUMI) "Subsidi UMI" else "Reguler",
                        badgeColor = Color(0xFFFEF3C7),
                        badgeTextColor = Color(0xFFB45309)
                    )
                    HorizontalDivider(color = Color(0xFFF8FAFC), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    DetailSettingRow(
                        label = "Tarif MDR",
                        value = if (appState.isUMI) "0% (s.d Rp500k), 0.3% (> Rp500k)" else "0.7% Flat"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 3: Annual Volume Limit Progress Bar
            Card(
                colors = CardDefaults.cardColors(containerColor = AppWhite),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Volume Transaksi Tahunan",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkNavy
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${appState.formatRupiah(appState.annualQrisVolume)} / Rp300jt",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    val progress = (appState.annualQrisVolume / 300000000.0).toFloat().coerceIn(0f, 1f)
                    CustomProgressBar(
                        progress = progress,
                        color = PrimaryBlue,
                        trackColor = Color(0xFFE2E8F0),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Batas maksimum volume transaksi QRIS UMI adalah Rp300.000.000 per tahun.",
                        fontSize = 11.sp,
                        color = SlateGray
                    )
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun RekeningInfoView(
    appState: AppState,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp)
                .background(AppWhite)
                .padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Kembali",
                    tint = DarkNavy
                )
            }
            Text(
                text = "Rekening Pencairan",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkNavy,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = AppWhite),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = Color(0xFF7C3AED),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Rincian Rekening Pencairan Kas",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkNavy
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileDetailRow(label = "Nama Bank", value = appState.disbursementBankName)
                    ProfileDetailRow(label = "Nomor Rekening", value = appState.disbursementAccountNumber)
                    ProfileDetailRow(label = "Nama Pemilik Rekening", value = appState.disbursementAccountName)
                }
            }
        }
    }
}

@Composable
fun FaqView(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp)
                .background(AppWhite)
                .padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Kembali",
                    tint = DarkNavy
                )
            }
            Text(
                text = "Frequently Asked Questions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkNavy,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Custom Vector Cat Mascot Illustration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier.size(140.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    // 1. Draw Legs
                    drawRoundRect(
                        color = Color(0xFF7C3AED),
                        topLeft = Offset(w * 0.35f, h * 0.8f),
                        size = Size(w * 0.08f, h * 0.12f),
                        cornerRadius = CornerRadius(10f, 10f)
                    )
                    drawRoundRect(
                        color = Color(0xFF7C3AED),
                        topLeft = Offset(w * 0.57f, h * 0.8f),
                        size = Size(w * 0.08f, h * 0.12f),
                        cornerRadius = CornerRadius(10f, 10f)
                    )

                    // Shoes
                    drawOval(
                        color = Color(0xFF6D28D9),
                        topLeft = Offset(w * 0.31f, h * 0.87f),
                        size = Size(w * 0.13f, h * 0.07f)
                    )
                    drawOval(
                        color = Color(0xFF6D28D9),
                        topLeft = Offset(w * 0.55f, h * 0.87f),
                        size = Size(w * 0.13f, h * 0.07f)
                    )

                    // 2. Draw Ears
                    // Left Ear
                    val leftEarPath = Path().apply {
                        moveTo(w * 0.25f, h * 0.35f)
                        lineTo(w * 0.20f, h * 0.15f)
                        lineTo(w * 0.40f, h * 0.30f)
                        close()
                    }
                    drawPath(path = leftEarPath, color = Color(0xFFFBBF24))
                    val leftEarInnerPath = Path().apply {
                        moveTo(w * 0.27f, h * 0.32f)
                        lineTo(w * 0.23f, h * 0.20f)
                        lineTo(w * 0.36f, h * 0.29f)
                        close()
                    }
                    drawPath(path = leftEarInnerPath, color = Color(0xFF7C3AED))

                    // Right Ear
                    val rightEarPath = Path().apply {
                        moveTo(w * 0.75f, h * 0.35f)
                        lineTo(w * 0.80f, h * 0.15f)
                        lineTo(w * 0.60f, h * 0.30f)
                        close()
                    }
                    drawPath(path = rightEarPath, color = Color(0xFFFBBF24))
                    val rightEarInnerPath = Path().apply {
                        moveTo(w * 0.73f, h * 0.32f)
                        lineTo(w * 0.77f, h * 0.20f)
                        lineTo(w * 0.64f, h * 0.29f)
                        close()
                    }
                    drawPath(path = rightEarInnerPath, color = Color(0xFF7C3AED))

                    // 3. Draw Body/Face (Yellow Oval)
                    drawOval(
                        color = Color(0xFFFBBF24),
                        topLeft = Offset(w * 0.2f, h * 0.25f),
                        size = Size(w * 0.6f, h * 0.58f)
                    )

                    // White face patch
                    drawOval(
                        color = Color.White,
                        topLeft = Offset(w * 0.3f, h * 0.45f),
                        size = Size(w * 0.4f, h * 0.33f)
                    )

                    // 4. Arms
                    // Left Arm
                    drawOval(
                        color = Color(0xFFF87171),
                        topLeft = Offset(w * 0.1f, h * 0.45f),
                        size = Size(w * 0.13f, h * 0.13f)
                    )
                    drawOval(
                        color = Color.White,
                        topLeft = Offset(w * 0.08f, h * 0.48f),
                        size = Size(w * 0.08f, h * 0.08f)
                    )
                    // Right Arm
                    drawOval(
                        color = Color(0xFFF87171),
                        topLeft = Offset(w * 0.77f, h * 0.45f),
                        size = Size(w * 0.13f, h * 0.13f)
                    )
                    drawOval(
                        color = Color.White,
                        topLeft = Offset(w * 0.84f, h * 0.48f),
                        size = Size(w * 0.08f, h * 0.08f)
                    )

                    // 5. Glasses & Eyes
                    drawCircle(
                        color = Color(0xFF7C3AED),
                        radius = w * 0.11f,
                        center = Offset(w * 0.38f, h * 0.45f),
                        style = Stroke(width = 6f)
                    )
                    drawCircle(
                        color = Color(0xFF7C3AED),
                        radius = w * 0.11f,
                        center = Offset(w * 0.62f, h * 0.45f),
                        style = Stroke(width = 6f)
                    )
                    drawLine(
                        color = Color(0xFF7C3AED),
                        start = Offset(w * 0.49f, h * 0.45f),
                        end = Offset(w * 0.51f, h * 0.45f),
                        strokeWidth = 6f
                    )

                    // Pupils
                    drawCircle(
                        color = Color.Black,
                        radius = w * 0.04f,
                        center = Offset(w * 0.38f, h * 0.45f)
                    )
                    drawCircle(
                        color = Color.Black,
                        radius = w * 0.04f,
                        center = Offset(w * 0.62f, h * 0.45f)
                    )
                    // White eye reflections
                    drawCircle(
                        color = Color.White,
                        radius = w * 0.015f,
                        center = Offset(w * 0.36f, h * 0.43f)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = w * 0.015f,
                        center = Offset(w * 0.60f, h * 0.43f)
                    )

                    // 6. Pink Cheeks
                    drawCircle(
                        color = Color(0xFFF472B6).copy(alpha = 0.8f),
                        radius = w * 0.04f,
                        center = Offset(w * 0.28f, h * 0.56f)
                    )
                    drawCircle(
                        color = Color(0xFFF472B6).copy(alpha = 0.8f),
                        radius = w * 0.04f,
                        center = Offset(w * 0.72f, h * 0.56f)
                    )

                    // 7. Mouth (Smiley)
                    val mouthPath = Path().apply {
                        moveTo(w * 0.46f, h * 0.53f)
                        quadraticTo(w * 0.50f, h * 0.59f, w * 0.54f, h * 0.53f)
                    }
                    drawPath(
                        path = mouthPath,
                        color = Color(0xFFE11D48),
                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // FAQ Accordion Card
            Card(
                colors = CardDefaults.cardColors(containerColor = AppWhite),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FaqAccordionItem(
                        question = "Apa itu QiGold?",
                        answer = "QiGold adalah platform tabungan emas digital terintegrasi untuk merchant PKL Finance. Anda bisa menabung emas otomatis dari setiap transaksi penjualan."
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    FaqAccordionItem(
                        question = "Bagaimana cara kerja Autosplit?",
                        answer = "Setiap pembayaran QRIS masuk akan dipotong persentasenya sesuai pengaturan cicilan emas Anda secara otomatis. Sisa pembayaran masuk ke saldo kas utama."
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    FaqAccordionItem(
                        question = "Berapa lama proses pencairan saldo?",
                        answer = "Pencairan saldo rekening kas diproses secara instan (real-time) melalui jaringan BI-FAST ke bank tujuan pencairan Anda."
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    FaqAccordionItem(
                        question = "Bagaimana jika server backend offline?",
                        answer = "Aplikasi memiliki sistem ketahanan data lokal (fallback) sehingga transaksi dan saldo tetap disimulasikan secara aman sampai koneksi pulih."
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun FaqAccordionItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = question,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DarkNavy,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color(0xFF7C3AED)
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = answer,
                    fontSize = 13.sp,
                    color = SlateGray,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun ProfileSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppWhite),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = DarkNavy
            )
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = SlateGray,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = DarkNavy,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ProfileSettingRow(
    title: String,
    sub: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DarkNavy
            )
            Text(
                text = sub,
                fontSize = 11.sp,
                color = SlateGray
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = SlateGray
        )
    }
}

@Composable
fun ProfileEditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = DarkNavy
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(if (readOnly) Color(0xFFF1F5F9) else AppWhite, CircleShape)
                .border(BorderStroke(1.5.dp, if (readOnly) Color(0xFFE2E8F0) else Blue50), CircleShape)
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = { if (!readOnly) onValueChange(it) },
                readOnly = readOnly,
                textStyle = TextStyle(
                    color = if (readOnly) SlateGray else DarkNavy,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                ),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun DetailSettingRow(
    label: String,
    value: String,
    badgeColor: Color? = null,
    badgeText: String? = null,
    badgeTextColor: Color? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = SlateGray,
            fontWeight = FontWeight.Medium
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.weight(1f).padding(start = 16.dp)
        ) {
            Text(
                text = value,
                fontSize = 14.sp,
                color = DarkNavy,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f).padding(end = 6.dp)
            )
            if (badgeText != null && badgeColor != null) {
                Box(
                    modifier = Modifier
                        .background(badgeColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeTextColor ?: Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun QrisChangeWarningDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
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
                // Warning icon circle matching LogoutDialog baby icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFFFEF3C7), CircleShape), // Light amber
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚠️", fontSize = 40.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Ubah Informasi Toko?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorBlack,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Perubahan informasi toko memerlukan penerbitan QRIS baru. Selama proses tersebut, QRIS lama akan dinonaktifkan dan QRIS baru akan tersedia setelah proses verifikasi selesai (maksimal 7 hari kerja).\n\nApakah Anda yakin ingin melanjutkan perubahan informasi toko?",
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
                    // Cancel button - Secondary (white background)
                    AppButton(
                        text = "Batal",
                        onClick = onDismiss,
                        variant = AppButtonVariant.Secondary,
                        modifier = Modifier.weight(1f)
                    )

                    // Confirm button - Primary (blue brand color)
                    AppButton(
                        text = "Ya, Lanjut",
                        onClick = onConfirm,
                        variant = AppButtonVariant.Primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun EditShopInfoView(
    appState: AppState,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var shopName by remember { mutableStateOf(appState.shopName) }
    var businessType by remember { mutableStateOf(appState.businessType) }
    var shopAddress by remember { mutableStateOf(appState.shopAddress) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
    ) {
        AppTopBar(
            title = "Ubah Info Toko",
            onBack = onBack,
            showDivider = false
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            ProfileEditField(label = "Nama Toko", value = shopName, onValueChange = { shopName = it })
            Spacer(modifier = Modifier.height(16.dp))
            ProfileEditField(label = "Jenis Usaha", value = businessType, onValueChange = { businessType = it })
            Spacer(modifier = Modifier.height(16.dp))
            ProfileEditField(label = "Alamat Toko", value = shopAddress, onValueChange = { shopAddress = it })

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Bottom CTA Button with white background and top border/shadow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppWhite)
                .padding(24.dp)
        ) {
            AppButton(
                text = "Simpan Perubahan Toko",
                onClick = {
                    if (shopName.isNotBlank() && businessType.isNotBlank() && shopAddress.isNotBlank()) {
                        appState.shopName = shopName
                        appState.businessType = businessType
                        appState.shopAddress = shopAddress
                        Toast.makeText(context, "Informasi toko berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                        onBack()
                    } else {
                        Toast.makeText(context, "Harap lengkapi semua data toko!", Toast.LENGTH_SHORT).show()
                    }
                },
                variant = AppButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
