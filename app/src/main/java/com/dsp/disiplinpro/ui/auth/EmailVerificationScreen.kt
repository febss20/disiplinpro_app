package com.dsp.disiplinpro.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.dsp.disiplinpro.viewmodel.auth.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import com.dsp.disiplinpro.data.preferences.ThemePreferences
import com.dsp.disiplinpro.ui.theme.*

@Composable
fun EmailVerificationScreen(
    navController: NavController,
    email: String,
    authViewModel: AuthViewModel = viewModel()
) {
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
                    .padding(top = 65.dp, bottom = 19.dp, start = 30.dp)
            ) {
                Text(
                    "Cek Email Anda",
                    color = textColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 140.dp)
                )
                Text(
                    "Batal",
                    color = cancelColor,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { navController.popBackStack() }
                )
            }

            Text(
                buildAnnotatedString {
                    append("Kami mengirim tautan reset ke ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(email)
                    }
                },
                color = textColor,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 10.dp, start = 30.dp)
            )

            // Login Button
            Button(
                onClick = { navController.navigate("login") },
                modifier = Modifier
                    .padding(top = 32.dp, start = 32.dp, end = 32.dp)
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(50.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                contentPadding = PaddingValues(vertical = 13.dp)
            ) {
                Text(
                    "Login",
                    color = Color(0xFFFFFFFF),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            // Resend Email Link
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(top = 32.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 33.dp)
                ) {
                    Text(
                        "Belum mendapatkan email? ",
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Kirim ulang email",
                        color = primaryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            authViewModel.sendPasswordResetEmail(email) { success ->
                                if (success) {
                                    Toast.makeText(
                                        context,
                                        "Email reset telah dikirim ulang",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Gagal mengirim ulang email",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}