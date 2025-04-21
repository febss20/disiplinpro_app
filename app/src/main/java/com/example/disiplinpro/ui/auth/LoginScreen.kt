package com.example.disiplinpro.ui.auth

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.disiplinpro.viewmodel.auth.AuthState
import com.example.disiplinpro.viewmodel.auth.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.disiplinpro.R
import com.example.disiplinpro.viewmodel.profile.SecurityPrivacyViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginFailed by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val authState by authViewModel.authState.collectAsState()
    val isLoading by remember { authViewModel.isLoading }
    val securityPrivacyViewModel: SecurityPrivacyViewModel = viewModel()
    val hasCredentials by securityPrivacyViewModel.hasCredentials.collectAsState()
    val savedCredentials by securityPrivacyViewModel.savedCredentials.collectAsState()

    LaunchedEffect(Unit) {
        securityPrivacyViewModel.loadSavedCredentials()
    }

    // Isi otomatis email yang tersimpan
    LaunchedEffect(hasCredentials, savedCredentials) {
        if (hasCredentials) {
            if (email.isEmpty() && savedCredentials.first.isNotEmpty()) {
                email = savedCredentials.first
            }
            if (password.isEmpty() && savedCredentials.second.isNotEmpty()) {
                password = savedCredentials.second
            }
        }
    }

    val googleSignInClient = remember {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .requestId()
                .requestServerAuthCode(context.getString(R.string.default_web_client_id))
                .setHostedDomain("*")
                .build()
            GoogleSignIn.getClient(context, gso)
        } catch (e: Exception) {
            Log.e("LoginScreen", "Error setting up Google Sign-In: ${e.message}")
            Toast.makeText(
                context,
                "Google Sign-In setup error. Check Firebase configuration.",
                Toast.LENGTH_SHORT
            ).show()
            val defaultGso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
            GoogleSignIn.getClient(context, defaultGso)
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        authViewModel.handleGoogleSignInResult(result)
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            authViewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Authenticated -> {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                    is AuthState.Error -> {
                        Log.e("LoginScreen", "Auth error: ${state.message}")

                        val userFriendlyMessage = when {
                            state.message.contains("no user record", ignoreCase = true) ->
                                "Email tidak terdaftar. Silakan daftar terlebih dahulu."
                            state.message.contains("password is invalid", ignoreCase = true) ->
                                "Password salah. Silakan coba lagi."
                            state.message.contains("badly formatted", ignoreCase = true) ->
                                "Format email tidak valid. Silakan periksa kembali."
                            state.message.contains("network", ignoreCase = true) ->
                                "Koneksi internet bermasalah. Silakan periksa koneksi Anda."
                            state.message.contains("too many attempts", ignoreCase = true) ->
                                "Terlalu banyak percobaan. Silakan coba lagi nanti."
                            state.message.contains("ID Token", ignoreCase = true) ->
                                "Gagal login dengan Google. Silakan coba lagi."
                            else ->
                                "Login gagal: ${state.message}"
                        }

                        Toast.makeText(context, userFriendlyMessage, Toast.LENGTH_LONG).show()
                        loginFailed = true
                    }
                    else -> { /* No action */ }
                }
            }
        }
    }

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
                    .padding(top = 65.dp, bottom = 30.dp)
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
                modifier = Modifier.padding(start = 31.dp)
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
                    onValueChange = {
                        email = it
                        if (emailError) emailError = false
                    },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (emailError) Color.Red else Color(0xFF7DAFCB),
                        unfocusedBorderColor = if (emailError) Color.Red else Color(0x807DAFCB),
                        errorBorderColor = Color.Red
                    ),
                    isError = emailError
                )
            }

            // Password Field
            Column(
                modifier = Modifier
                    .padding(top = 7.dp, start = 31.dp, end = 31.dp)
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
                    onValueChange = {
                        password = it
                        if (passwordError) passwordError = false
                    },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
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
                        unfocusedBorderColor = if (passwordError) Color.Red else Color(0x807DAFCB),
                        focusedBorderColor = if (passwordError) Color.Red else Color(0xFF7DAFCB),
                        errorBorderColor = Color.Red
                    ),
                    isError = passwordError
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
                    if (emailError) {
                        Text(
                            "Email tidak valid",
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (passwordError) {
                        Text(
                            "Password salah",
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            "Login gagal",
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(0.dp))
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
                    loginFailed = false
                    emailError = false
                    passwordError = false

                    if (email.isEmpty()) {
                        Toast.makeText(context, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
                        emailError = true
                        loginFailed = true
                        return@Button
                    }

                    if (password.isEmpty()) {
                        Toast.makeText(context, "Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                        passwordError = true
                        loginFailed = true
                        return@Button
                    }

                    Log.d("LoginScreen", "Attempting login with email: $email")
                    authViewModel.loginUser(context, email, password) { success ->
                        Log.d("LoginScreen", "Login result: $success")
                        loginFailed = !success
                        if (!success) {
                            passwordError = true
                        }
                    }
                },
                modifier = Modifier
                    .padding(top = 32.dp, start = 31.dp, end = 31.dp)
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7DAFCB)),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        "MASUK",
                        color = Color(0xFFFFFFFF),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // OR Divider
            Row(
                modifier = Modifier
                    .padding(horizontal = 31.dp, vertical = 16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = Color(0xFFEEEEEE)
                )
                Text(
                    text = "ATAU",
                    color = Color(0xFF999999),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = Color(0xFFEEEEEE)
                )
            }

            GoogleSignInButton(
                onClick = {
                    googleSignInClient.signOut().addOnCompleteListener {
                        val signInIntent = googleSignInClient.signInIntent
                        signInIntent.putExtra("prompt", "select_account")
                        googleSignInLauncher.launch(signInIntent)
                    }
                },
                text = "Masuk dengan Google",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
            )

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
                        color = Color(0xFF7DAFCB),
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