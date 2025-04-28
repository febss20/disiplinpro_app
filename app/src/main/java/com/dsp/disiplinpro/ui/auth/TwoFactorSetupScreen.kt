package com.dsp.disiplinpro.ui.auth

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.dsp.disiplinpro.data.preferences.ThemePreferences
import com.dsp.disiplinpro.viewmodel.auth.TwoFactorAuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoFactorSetupScreen(
    navController: NavController,
    viewModel: TwoFactorAuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val qrCodeBitmap by viewModel.qrCodeBitmap.collectAsState()
    val secret by viewModel.secret.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()
    val otpCode = remember { mutableStateOf("") }
    val isVerifying by viewModel.isVerifying.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val setupSuccess by viewModel.setupSuccess.collectAsState()

    val themePreferences = ThemePreferences(context)
    val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = false)

    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFFAF3E0)
    val cardColor = if (isDarkMode) Color(0xFF2C2C2C) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color.White else Color(0xFF333333)
    val accentColor = Color(0xFF64B5F6)

    LaunchedEffect(Unit) {
        viewModel.generateQRCode()
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotEmpty()) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(setupSuccess) {
        if (setupSuccess) {
            Toast.makeText(context, "Autentikasi dua faktor berhasil diaktifkan!", Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = textColor
                    )
                }
                Text(
                    text = "Aktifkan Autentikasi Dua Faktor",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(3) { step ->
                    val isActive = step <= currentStep
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (isActive) accentColor else Color.Gray.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${step + 1}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (step) {
                                0 -> "Persiapan"
                                1 -> "Pindai"
                                2 -> "Verifikasi"
                                else -> ""
                            },
                            fontSize = 12.sp,
                            color = if (isActive) textColor else Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Content berdasarkan step
            when (currentStep) {
                0 -> {
                    SetupStep1(
                        textColor = textColor,
                        cardColor = cardColor,
                        accentColor = accentColor,
                        onContinue = { viewModel.nextStep() }
                    )
                }
                1 -> {
                    SetupStep2(
                        qrCodeBitmap = qrCodeBitmap,
                        secret = secret,
                        textColor = textColor,
                        cardColor = cardColor,
                        accentColor = accentColor,
                        onContinue = { viewModel.nextStep() }
                    )
                }
                2 -> {
                    SetupStep3(
                        otpCode = otpCode.value,
                        onOtpChanged = { otpCode.value = it },
                        isVerifying = isVerifying,
                        textColor = textColor,
                        cardColor = cardColor,
                        accentColor = accentColor,
                        onVerify = { viewModel.verifyOTP(otpCode.value) },
                        onCancel = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
fun SetupStep1(
    textColor: Color,
    cardColor: Color,
    accentColor: Color,
    onContinue: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tingkatkan Keamanan Akun Anda",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = textColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Autentikasi dua faktor menambahkan lapisan keamanan ekstra pada akun Anda. " +
                        "Setiap kali masuk, Anda akan diminta memasukkan kode unik dari aplikasi autentikator.",
                color = textColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Untuk mengaktifkan fitur ini, Anda perlu mengunduh dan menginstal aplikasi autentikator seperti:",
                color = textColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "• Google Authenticator\n• Authy\n• Microsoft Authenticator",
                color = textColor,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("Lanjutkan", color = Color.White)
            }
        }
    }
}

@Composable
fun SetupStep2(
    qrCodeBitmap: Bitmap?,
    secret: String,
    textColor: Color,
    cardColor: Color,
    accentColor: Color,
    onContinue: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pindai Kode QR",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = textColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Buka aplikasi autentikator Anda dan pindai kode QR berikut:",
                color = textColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // QR Code
            qrCodeBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } ?: run {
                CircularProgressIndicator(
                    modifier = Modifier.size(100.dp),
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Atau masukkan kode berikut secara manual:",
                color = textColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (secret.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.1f))
                ) {
                    Text(
                        text = secret,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("Lanjutkan", color = Color.White)
            }
        }
    }
}

@Composable
fun SetupStep3(
    otpCode: String,
    onOtpChanged: (String) -> Unit,
    isVerifying: Boolean,
    textColor: Color,
    cardColor: Color,
    accentColor: Color,
    onVerify: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Verifikasi Kode",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = textColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Masukkan kode 6 digit dari aplikasi autentikator Anda:",
                color = textColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // OTP Input
            OutlinedTextField(
                value = otpCode,
                onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) onOtpChanged(it) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    cursorColor = accentColor,
                    unfocusedTextColor = textColor,
                    focusedTextColor = textColor
                ),
                placeholder = { Text("Kode 6 digit") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor)
                ) {
                    Text("Batal")
                }

                Button(
                    onClick = onVerify,
                    enabled = otpCode.length == 6 && !isVerifying,
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
    }
}