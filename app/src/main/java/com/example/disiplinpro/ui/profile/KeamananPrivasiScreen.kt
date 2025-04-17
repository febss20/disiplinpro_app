package com.example.disiplinpro.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeamananPrivasiScreen(
    navController: NavController
) {
    var showBiometricLogin by remember { mutableStateOf(false) }
    var enableTwoFactorAuth by remember { mutableStateOf(false) }
    var shareActivityData by remember { mutableStateOf(true) }
    var allowNotifications by remember { mutableStateOf(true) }
    var saveLoginInfo by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF3E0))
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF333333)
                )
            }
            Text(
                "Keamanan dan Privasi",
                color = Color(0xFF333333),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 80.dp, bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Section Title: Security
            Text(
                text = "Keamanan",
                color = Color(0xFF64B5F6),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Biometric Login
            SecuritySettingItem(
                title = "Login dengan Sidik Jari",
                description = "Gunakan sidik jari untuk masuk ke aplikasi",
                icon = Icons.Default.Fingerprint,
                iconTint = Color(0xFF64B5F6),
                checked = showBiometricLogin,
                onCheckedChange = { showBiometricLogin = it }
            )

            // Two-Factor Authentication
            SecuritySettingItem(
                title = "Autentikasi Dua Faktor",
                description = "Dapatkan kode verifikasi saat login dari perangkat baru",
                icon = Icons.Default.Shield,
                iconTint = Color(0xFF64B5F6),
                checked = enableTwoFactorAuth,
                onCheckedChange = { enableTwoFactorAuth = it }
            )

            // Save Login Info
            SecuritySettingItem(
                title = "Simpan Informasi Login",
                description = "Simpan email dan nama pengguna untuk login lebih cepat",
                icon = Icons.Default.SaveAlt,
                iconTint = Color(0xFF64B5F6),
                checked = saveLoginInfo,
                onCheckedChange = { saveLoginInfo = it }
            )

            Divider(
                color = Color(0x4D333333),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Section Title: Privacy
            Text(
                text = "Privasi",
                color = Color(0xFFE57373),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Activity Data
            SecuritySettingItem(
                title = "Bagikan Data Aktivitas",
                description = "Bagi data aktivitas untuk pengalaman aplikasi yang lebih baik",
                icon = Icons.Default.DataUsage,
                iconTint = Color(0xFFE57373),
                checked = shareActivityData,
                onCheckedChange = { shareActivityData = it }
            )

            // Notifications
            SecuritySettingItem(
                title = "Izinkan Notifikasi",
                description = "Terima notifikasi pengingat jadwal dan tugas",
                icon = Icons.Default.Notifications,
                iconTint = Color(0xFFE57373),
                checked = allowNotifications,
                onCheckedChange = { allowNotifications = it }
            )

            Divider(
                color = Color(0x4D333333),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Other Actions
            ActionItem(
                title = "Hapus Histori dan Cache",
                description = "Bersihkan data sementara dan histori aplikasi",
                icon = Icons.Default.DeleteForever,
                iconTint = Color(0xFF7DAFCB),
                onClick = { /* TODO: Implement cache clearing */ }
            )

            ActionItem(
                title = "Reset Password",
                description = "Atur ulang password akun Anda",
                icon = Icons.Default.Lock,
                iconTint = Color(0xFF7DAFCB),
                onClick = { /* TODO: Implement password reset */ }
            )

            ActionItem(
                title = "Hapus Akun",
                description = "Hapus permanen akun dan semua data terkait",
                icon = Icons.Default.PersonOff,
                iconTint = Color(0xFFE57373),
                onClick = { /* TODO: Implement account deletion */ }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SecuritySettingItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconTint.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, end = 8.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = iconTint,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFDDDDDD)
                )
            )
        }
    }
}

@Composable
fun ActionItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconTint.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = Color(0xFF7DAFCB),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}