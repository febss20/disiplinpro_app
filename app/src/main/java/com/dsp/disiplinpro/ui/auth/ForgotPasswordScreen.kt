package com.dsp.disiplinpro.ui.auth

import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.dsp.disiplinpro.viewmodel.auth.AuthViewModel
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import androidx.compose.ui.layout.ContentScale
import com.dsp.disiplinpro.data.preferences.ThemePreferences
import com.dsp.disiplinpro.ui.theme.*

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val isLoading by remember { authViewModel.isLoading }
    val context = LocalContext.current

    val themePreferences = ThemePreferences(context)
    val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = false)

    val backgroundColor = if (isDarkMode) DarkBackground else Color(0xFFFAF3E0)
    val textColor = if (isDarkMode) DarkTextLight else Color(0xFF333333)
    val primaryColor = if (isDarkMode) DarkPrimaryBlue else Color(0xFF7DAFCB)
    val cancelColor = if (isDarkMode) Color(0xFFFF6E40) else Color(0xFFFF5722)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(color = if (isDarkMode) Color(0xFF121212) else Color(0xFFFFFFFF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(color = backgroundColor)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(top = 65.dp, bottom = 25.dp, start = 30.dp)
            ) {
                Text(
                    "Lupa Password",
                    color = textColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(end = 150.dp)
                )
                Text(
                    "Batal",
                    color = cancelColor,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable { navController.popBackStack() }
                )
            }

            Text(
                "Mohon masukkan email anda untuk reset password",
                color = textColor,
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 30.dp, bottom = 25.dp)
            )

            // Email Field
            Column(
                modifier = Modifier
                    .padding(start = 29.dp, end = 29.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 7.dp)
                ) {
                    CoilImage(
                        imageModel = { "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/T7pdvlFwTn/s1dpsztl.png" },
                        imageOptions = ImageOptions(contentScale = ContentScale.Crop),
                        modifier = Modifier
                            .width(20.dp)
                            .height(20.dp)
                    )
                    Text(
                        "Email Anda",
                        color = textColor,
                        fontSize = 15.sp
                    )
                }
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (emailError) {
                            emailError = false
                            errorMessage = ""
                        }
                    },
                    label = { Text("Email", color = if (isDarkMode) DarkTextGrey else Color(0xFF757575)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (emailError) Color.Red else primaryColor,
                        unfocusedBorderColor = if (emailError) Color.Red else primaryColor.copy(alpha = 0.5f),
                        errorBorderColor = Color.Red,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = primaryColor
                    ),
                    isError = emailError
                )

                if (emailError) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
            }

            // Reset Password Button
            Button(
                onClick = {
                    emailError = false
                    errorMessage = ""

                    if (email.isEmpty()) {
                        Log.e("ForgotPasswordScreen", "Email kosong")
                        errorMessage = "Email tidak boleh kosong"
                        emailError = true
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Log.e("ForgotPasswordScreen", "Format email tidak valid: $email")
                        errorMessage = "Format email tidak valid"
                        emailError = true
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    Log.d("ForgotPasswordScreen", "Attempting to send password reset email to: $email")
                    authViewModel.sendPasswordResetEmail(email) { success ->
                        if (success) {
                            Log.d("ForgotPasswordScreen", "Password reset email sent successfully to: $email")
                            navController.navigate("email_verification/$email")
                        } else {
                            Log.e("ForgotPasswordScreen", "Failed to send password reset email to: $email")
                            errorMessage = "Email tidak terdaftar atau gagal mengirim email reset"
                            emailError = true
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier
                    .padding(top = 50.dp, start = 32.dp, end = 32.dp)
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(50.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                contentPadding = PaddingValues(vertical = 13.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        "Reset Password",
                        color = Color(0xFFFFFFFF),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}