package com.example.disiplinpro.ui.auth

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.disiplinpro.viewmodel.auth.AuthViewModel
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginFailed by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFFFFFFF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(color = Color(0xFFFAF3E0))
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(top = 65.dp, bottom = 45.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    "Selamat Datang",
                    color = Color(0xFF333333),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                "Masuk",
                color = Color(0xFF7DAFCB),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 26.dp, start = 31.dp)
            )

            // Email Field
            Column(
                modifier = Modifier
                    .padding(top = 26.dp, start = 31.dp, end = 31.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 7.dp, start = 1.dp)
                ) {
                    CoilImage(
                        imageModel = { "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/T7pdvlFwTn/fvx7ynkw.png" },
                        imageOptions = ImageOptions(contentScale = ContentScale.Crop),
                        modifier = Modifier
                            .width(20.dp)
                            .height(20.dp)
                    )
                    Text(
                        "Email Anda",
                        color = Color(0xFF333333),
                        fontSize = 15.sp
                    )
                }
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7DAFCB),
                    )
                )
            }

            // Password Field
            Column(
                modifier = Modifier
                    .padding(top = 26.dp, start = 31.dp, end = 31.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp, start = 1.dp)
                ) {
                    CoilImage(
                        imageModel = { "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/T7pdvlFwTn/0635yr0i.png" },
                        imageOptions = ImageOptions(contentScale = ContentScale.Crop),
                        modifier = Modifier
                            .width(20.dp)
                            .height(20.dp)
                    )
                    Text(
                        "Password",
                        color = Color(0xFF333333),
                        fontSize = 15.sp
                    )
                }
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Gray,
                        focusedBorderColor = if (loginFailed) Color.Red else Color(0xFF7DAFCB),
                        errorBorderColor = Color.Red
                    ),
                    isError = loginFailed
                )
            }

            // Forgot Password dan Pesan Error
            Row(
                modifier = Modifier
                    .padding(top = 8.dp, start = 35.dp, end = 26.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (loginFailed) {
                    Text(
                        "Password atau email salah",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Spacer(modifier = Modifier.width(0.dp)) // Placeholder kosong agar layout tetap konsisten
                }
                TextButton(onClick = { navController.navigate("forgot_password") }) {
                    Text(
                        "Lupa Password?",
                        color = Color(0xFF7DAFCB),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Login Button
            Button(
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(context, "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    } else {
                        authViewModel.loginUser(email, password) { success ->
                            if (success) {
                                navController.navigate("home")
                            } else {
                                loginFailed = true
                                Toast.makeText(context, "Login gagal, periksa email dan password", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .padding(top = 10.dp, start = 32.dp, end = 32.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7DAFCB)),
                contentPadding = PaddingValues(vertical = 13.dp)
            ) {
                Text(
                    "Masuk",
                    color = Color(0xFFFFFFFF),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Register Link
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .padding(top = 32.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 33.dp)
                ) {
                    Text(
                        "Tidak Memiliki akun? ",
                        color = Color(0xFF333333),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Daftar",
                        color = Color(0xFF7DAFCB), // Changed color to match your theme
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            navController.navigate("register")
                        }
                    )
                }
            }
        }
    }
}