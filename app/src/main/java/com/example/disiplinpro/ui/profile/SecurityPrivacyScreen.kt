package com.example.disiplinpro.ui.profile

import android.widget.Toast
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.disiplinpro.ui.components.SaveCredentialsDialog
import com.example.disiplinpro.viewmodel.profile.SecurityPrivacyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityPrivacyScreen(
    navController: NavController,
    viewModel: SecurityPrivacyViewModel = viewModel()
) {
    val context = LocalContext.current
    val showBiometricLogin by viewModel.biometricLoginEnabled.collectAsState()
    val enableTwoFactorAuth by viewModel.twoFactorAuthEnabled.collectAsState()
    val saveLoginInfo by viewModel.saveLoginInfoFlow.collectAsState()
    val shareActivityData by viewModel.shareActivityDataEnabled.collectAsState()
    val allowNotifications by viewModel.allowNotificationsEnabled.collectAsState()
    val hasCredentials by viewModel.hasCredentials.collectAsState()
    val savedCredentials by viewModel.savedCredentials.collectAsState()

    var showPasswordResetDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showCredentialsDialog by remember { mutableStateOf(false) }
    var deleteAccountPassword by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading
    val error by viewModel.error
    val operationSuccess by viewModel.operationSuccess

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF3E0))
    ) {
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

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.Center),
                color = Color(0xFF7DAFCB)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 80.dp, bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Keamanan",
                color = Color(0xFF64B5F6),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SecuritySettingItem(
                title = "Login dengan Sidik Jari",
                description = "Gunakan sidik jari untuk masuk ke aplikasi",
                icon = Icons.Default.Fingerprint,
                iconTint = Color(0xFF64B5F6),
                checked = showBiometricLogin,
                onCheckedChange = {
                    // Sidik jari belum diimplementasikan
                    Toast.makeText(context, "Fitur sidik jari tidak tersedia saat ini", Toast.LENGTH_SHORT).show()
                },
                enabled = false
            )

            SecuritySettingItem(
                title = "Autentikasi Dua Faktor",
                description = "Dapatkan kode verifikasi saat login dari perangkat baru",
                icon = Icons.Default.Shield,
                iconTint = Color(0xFF64B5F6),
                checked = enableTwoFactorAuth,
                onCheckedChange = { viewModel.updateTwoFactorAuth(it) }
            )

            // Save Login Info (dengan dialog kredensial)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        if (saveLoginInfo) {
                            // Jika sudah aktif, tampilkan dialog kredensial
                            showCredentialsDialog = true
                        } else {
                            // Jika belum aktif, aktifkan dulu
                            viewModel.updateSaveLoginInfo(true)
                            // Kemudian tampilkan dialog kredensial
                            showCredentialsDialog = true
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = Color(0x332196F3)
                ),
                shape = RoundedCornerShape(12.dp),
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
                            .background(Color(0xFF64B5F6).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SaveAlt,
                            contentDescription = null,
                            tint = Color(0xFF64B5F6),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp, end = 8.dp)
                    ) {
                        Text(
                            text = "Simpan Informasi Login",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (hasCredentials)
                                "Email/password tersimpan. Klik untuk mengelola."
                            else "Simpan email dan password untuk login lebih cepat",
                            fontSize = 12.sp,
                            color = Color(0xFF666666)
                        )
                    }

                    Switch(
                        checked = saveLoginInfo,
                        onCheckedChange = { enabled ->
                            viewModel.updateSaveLoginInfo(enabled)
                            if (enabled) {
                                showCredentialsDialog = true
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF64B5F6),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFDDDDDD)
                        )
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = 1.dp,
                color = Color(0x4D333333)
            )

            Text(
                text = "Privasi",
                color = Color(0xFFE57373),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SecuritySettingItem(
                title = "Bagikan Data Aktivitas",
                description = "Bagi data aktivitas untuk pengalaman aplikasi yang lebih baik",
                icon = Icons.Default.DataUsage,
                iconTint = Color(0xFFE57373),
                checked = shareActivityData,
                onCheckedChange = { viewModel.updateShareActivityData(it) }
            )

            SecuritySettingItem(
                title = "Izinkan Notifikasi",
                description = "Terima notifikasi pengingat jadwal dan tugas",
                icon = Icons.Default.Notifications,
                iconTint = Color(0xFFE57373),
                checked = allowNotifications,
                onCheckedChange = { viewModel.updateAllowNotifications(it) }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = 1.dp,
                color = Color(0x4D333333)
            )

            ActionItem(
                title = "Hapus Histori dan Cache",
                description = "Bersihkan data sementara dan histori aplikasi",
                icon = Icons.Default.DeleteForever,
                iconTint = Color(0xFF7DAFCB),
                onClick = { viewModel.clearCacheAndHistory() }
            )

            ActionItem(
                title = "Reset Password",
                description = "Atur ulang password akun Anda",
                icon = Icons.Default.Lock,
                iconTint = Color(0xFF7DAFCB),
                onClick = { showPasswordResetDialog = true }
            )

            ActionItem(
                title = "Hapus Akun",
                description = "Hapus permanen akun dan semua data terkait",
                icon = Icons.Default.PersonOff,
                iconTint = Color(0xFFE57373),
                onClick = { showDeleteAccountDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showPasswordResetDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordResetDialog = false },
            title = { Text("Reset Password") },
            text = { Text("Email berisi tautan untuk reset password akan dikirim ke email terdaftar Anda.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetPassword()
                        showPasswordResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7DAFCB))
                ) {
                    Text("Kirim Email Reset")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPasswordResetDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Hapus Akun") },
            text = {
                Column {
                    Text("Penghapusan akun bersifat permanen dan tidak dapat dibatalkan. Semua data Anda akan dihapus.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Masukkan password Anda untuk konfirmasi:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = deleteAccountPassword,
                        onValueChange = { deleteAccountPassword = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (deleteAccountPassword.isNotEmpty()) {
                            viewModel.deleteAccount(
                                deleteAccountPassword,
                                onSuccess = {
                                    Toast.makeText(context, "Akun berhasil dihapus", Toast.LENGTH_LONG).show()
                                    showDeleteAccountDialog = false
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        } else {
                            Toast.makeText(context, "Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))
                ) {
                    Text("Hapus Akun")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showDeleteAccountDialog = false
                    deleteAccountPassword = ""
                }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showCredentialsDialog) {
        SaveCredentialsDialog(
            show = true,
            savedEmail = savedCredentials.first,
            savedPassword = savedCredentials.second,
            onDismiss = { showCredentialsDialog = false },
            onSave = { email, password ->
                viewModel.saveCredentials(email, password)
            },
            onDelete = {
                viewModel.deleteCredentials()
            }
        )
    }
}

@Composable
fun SecuritySettingItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x332196F3)
        ),
        shape = RoundedCornerShape(12.dp),
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
                    color = if (enabled) Color(0xFF333333) else Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = if (enabled) Color(0xFF666666) else Color.Gray
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = iconTint,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFDDDDDD),
                    disabledCheckedThumbColor = Color.LightGray,
                    disabledCheckedTrackColor = iconTint.copy(alpha = 0.3f),
                    disabledUncheckedThumbColor = Color.LightGray,
                    disabledUncheckedTrackColor = Color(0xFFDDDDDD).copy(alpha = 0.3f)
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
            containerColor = Color(0x332196F3)
        ),
        shape = RoundedCornerShape(12.dp),
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