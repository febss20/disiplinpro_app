package com.dsp.disiplinpro.ui.auth

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.dsp.disiplinpro.data.preferences.ThemePreferences
import com.dsp.disiplinpro.ui.theme.*
import com.dsp.disiplinpro.viewmodel.auth.AuthState
import com.dsp.disiplinpro.viewmodel.auth.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.skydoves.landscapist.coil.CoilImage
import com.skydoves.landscapist.ImageOptions
import com.dsp.disiplinpro.R

@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var registerFailed by remember { mutableStateOf(false) }
    var usernameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val authState by authViewModel.authState.collectAsState()
    val isLoading by remember { authViewModel.isLoading }

    val themePreferences = ThemePreferences(context)
    val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = false)

    val backgroundColor = if (isDarkMode) DarkBackground else Color(0xFFFAF3E0)
    val textColor = if (isDarkMode) DarkTextLight else Color(0xFF333333)
    val secondaryTextColor = if (isDarkMode) DarkTextGrey else Color(0xFF757575)
    val primaryColor = if (isDarkMode) DarkPrimaryBlue else Color(0xFF7DAFCB)
    val dividerColor = if (isDarkMode) Color(0xFF444444) else Color(0xFFEEEEEE)

    val googleSignInClient = remember {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .requestId()
                .build()

            val client = GoogleSignIn.getClient(context, gso)

            client.signOut().addOnCompleteListener {
                Log.d("RegisterScreen", "Signed out from previous Google session to force account picker")
            }

            client

        } catch (e: Exception) {
            Log.e("RegisterScreen", "Error setting up Google Sign-In: ${e.message}")
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
                            popUpTo("register") { inclusive = true }
                        }
                    }
                    is AuthState.Error -> {
                        Log.e("RegisterScreen", "Auth error: ${state.message}")

                        val userFriendlyMessage = when {
                            state.message.contains("email address is already in use", ignoreCase = true) ->
                                "Email sudah terdaftar. Silakan login atau gunakan email lain."
                            state.message.contains("password is invalid", ignoreCase = true) ||
                                    state.message.contains("password is too weak", ignoreCase = true) ->
                                "Password terlalu lemah. Gunakan minimal 6 karakter dengan kombinasi huruf dan angka."
                            state.message.contains("badly formatted", ignoreCase = true) ->
                                "Format email tidak valid. Silakan periksa kembali."
                            state.message.contains("network", ignoreCase = true) ->
                                "Koneksi internet bermasalah. Silakan periksa koneksi Anda."
                            state.message.contains("ID Token", ignoreCase = true) ->
                                "Gagal mendaftar dengan Google. Silakan coba lagi."
                            else ->
                                "Registrasi gagal: ${state.message}"
                        }

                        Toast.makeText(context, userFriendlyMessage, Toast.LENGTH_LONG).show()
                        registerFailed = true
                    }
                    else -> { /* No action */ }
                }
            }
        }
    }

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
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(top = 65.dp, bottom = 30.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    "Daftar Akun",
                    color = textColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                "Daftar",
                color = primaryColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 31.dp)
            )

            // Username Field
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
                        imageModel = { "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/T7pdvlFwTn/a637tpg7_expires_30_days.png" },
                        imageOptions = ImageOptions(contentScale = ContentScale.Crop),
                        modifier = Modifier
                            .width(20.dp)
                            .height(20.dp)
                    )
                    Text(
                        "Username",
                        color = textColor,
                        fontSize = 15.sp
                    )
                }
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        if (usernameError) usernameError = false
                    },
                    label = { Text("Username", color = if (isDarkMode) DarkTextGrey else Color(0xFF757575)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (usernameError) Color.Red else primaryColor,
                        unfocusedBorderColor = if (usernameError) Color.Red else primaryColor.copy(alpha = 0.5f),
                        errorBorderColor = Color.Red,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = primaryColor
                    ),
                    isError = usernameError
                )
            }

            // Email Field
            Column(
                modifier = Modifier
                    .padding(top = 7.dp, start = 31.dp, end = 31.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 7.dp, start = 1.dp)
                ) {
                    CoilImage(
                        imageModel = { "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/T7pdvlFwTn/kz2ez595_expires_30_days.png" },
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
                        if (emailError) emailError = false
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
            }

            // Password Field
            Column(
                modifier = Modifier
                    .padding(top = 7.dp, start = 31.dp, end = 31.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 7.dp, start = 1.dp)
                ) {
                    CoilImage(
                        imageModel = { "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/T7pdvlFwTn/kx84zlxo_expires_30_days.png" },
                        imageOptions = ImageOptions(contentScale = ContentScale.Crop),
                        modifier = Modifier
                            .width(20.dp)
                            .height(20.dp)
                    )
                    Text(
                        "Password",
                        color = textColor,
                        fontSize = 15.sp
                    )
                }
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (passwordError) passwordError = false
                    },
                    label = { Text("Password", color = if (isDarkMode) DarkTextGrey else Color(0xFF757575)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = if (isDarkMode) DarkTextGrey else Color(0xFF757575)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (passwordError) Color.Red else primaryColor,
                        unfocusedBorderColor = if (passwordError) Color.Red else primaryColor.copy(alpha = 0.5f),
                        errorBorderColor = Color.Red,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = primaryColor
                    ),
                    isError = passwordError
                )
            }

            // Register Button
            Button(
                onClick = {
                    registerFailed = false
                    usernameError = false
                    emailError = false
                    passwordError = false

                    if (username.isEmpty()) {
                        Toast.makeText(context, "Username tidak boleh kosong", Toast.LENGTH_SHORT).show()
                        usernameError = true
                        registerFailed = true
                        return@Button
                    }

                    if (email.isEmpty()) {
                        Toast.makeText(context, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
                        emailError = true
                        registerFailed = true
                        return@Button
                    }

                    if (password.isEmpty()) {
                        Toast.makeText(context, "Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                        passwordError = true
                        registerFailed = true
                        return@Button
                    }

                    if (password.length < 6) {
                        Toast.makeText(context, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
                        passwordError = true
                        registerFailed = true
                        return@Button
                    }

                    Log.d("RegisterScreen", "Attempting registration with username: $username, email: $email")
                    authViewModel.registerUser(username, email, password) { success ->
                        Log.d("RegisterScreen", "Registration result: $success")
                        registerFailed = !success
                        if (!success) {
                            Log.e("RegisterScreen", "Registration failed in callback")
                        }
                    }
                },
                modifier = Modifier
                    .padding(top = 30.dp, start = 32.dp, end = 32.dp)
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        "DAFTAR",
                        color = Color(0xFFFFFFFF),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // Login Link
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
                        "Sudah Memiliki akun? ",
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Masuk",
                        color = primaryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            navController.navigate("login")
                        }
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
                    color = dividerColor
                )
                Text(
                    text = "ATAU",
                    color = if (isDarkMode) DarkTextGrey else Color(0xFF999999),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = dividerColor
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0xFFDDDDDD), CircleShape)
                        .background(Color.White)
                        .clickable(enabled = !isLoading) {
                            val signInIntent = googleSignInClient.signInIntent
                            signInIntent.putExtra("prompt", "select_account")
                            signInIntent.putExtra("account_chooser_enabled", true)
                            signInIntent.putExtra("always_show_account_picker", true)
                            googleSignInLauncher.launch(signInIntent)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google Sign In",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}