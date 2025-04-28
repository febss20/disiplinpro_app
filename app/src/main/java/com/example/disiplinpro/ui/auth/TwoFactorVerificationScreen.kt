package com.example.disiplinpro.ui.auth

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.disiplinpro.data.preferences.ThemePreferences
import com.example.disiplinpro.viewmodel.auth.AuthViewModel
import com.example.disiplinpro.viewmodel.auth.TwoFactorAuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoFactorVerificationScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    email: String,
    twoFactorViewModel: TwoFactorAuthViewModel = viewModel()
) {
    val context = LocalContext.current

    val isVerifying by twoFactorViewModel.isVerifying.collectAsState()
    val verificationSuccess by twoFactorViewModel.verificationSuccess.collectAsState()
    val errorMessageFromVM by twoFactorViewModel.errorMessage.collectAsState()

    var otpCode by remember { mutableStateOf("") }

    val themePreferences = ThemePreferences(context)
    val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = false)
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFFAF3E0)
    val cardColor = if (isDarkMode) Color(0xFF2C2C2C) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color.White else Color(0xFF333333)
    val accentColor = Color(0xFF64B5F6)

    LaunchedEffect(errorMessageFromVM) {
        if (errorMessageFromVM.isNotEmpty()) {
            Toast.makeText(context, errorMessageFromVM, Toast.LENGTH_LONG).show()
            twoFactorViewModel.clearError()
        }
    }

    LaunchedEffect(verificationSuccess) {
        if (verificationSuccess) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    LaunchedEffect(Unit) {
        Log.d("TwoFactorVerify", "Email received: $email")

        twoFactorViewModel.resetVerificationStatus()

        // Periksa apakah konfigurasi 2FA valid
        if (!twoFactorViewModel.check2FAConfiguration()) {
            Log.w("TwoFactorVerify", "2FA not properly configured, bypassing verification")
            Toast.makeText(
                context,
                "Konfigurasi 2FA tidak lengkap. Silahkan atur ulang di pengaturan keamanan.",
                Toast.LENGTH_LONG
            ).show()

            delay(1500)
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Verifikasi Keamanan",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Masukkan kode verifikasi dari aplikasi autentikator Anda untuk melanjutkan",
                color = textColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Kode Autentikator",
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // OTP Input Box
                    TextField(
                        value = otpCode,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) otpCode = it },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        placeholder = { Text("Kode 6 digit") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = cardColor,
                            unfocusedContainerColor = cardColor,
                            cursorColor = accentColor,
                            focusedIndicatorColor = accentColor,
                            unfocusedIndicatorColor = Color.Gray,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            twoFactorViewModel.verifyLoginOTP(otpCode)
                        },
                        enabled = !isVerifying,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Verifikasi", color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    Log.d("TwoFactorVerify", "Cancel button clicked - returning to login")
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            ) {
                Text(
                    text = "Batal & Kembali ke Login",
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = {
                    Toast.makeText(context, "Fitur bantuan sedang dalam pengembangan.", Toast.LENGTH_LONG).show()
                }
            ) {
                Text(
                    text = "Tidak Dapat Mengakses Autentikator?",
                    color = textColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}