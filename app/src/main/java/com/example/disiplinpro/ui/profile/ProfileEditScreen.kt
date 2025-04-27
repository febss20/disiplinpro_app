package com.example.disiplinpro.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.disiplinpro.ui.theme.*
import com.example.disiplinpro.util.rememberImagePicker
import com.example.disiplinpro.util.RequestStoragePermission
import com.example.disiplinpro.viewmodel.profile.ProfileEditViewModel
import com.example.disiplinpro.viewmodel.theme.ThemeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    navController: NavController,
    profileViewModel: ProfileEditViewModel = viewModel(),
    themeViewModel: ThemeViewModel
) {
    val context = LocalContext.current
    val uiState by profileViewModel.uiState.collectAsState()
    val selectedImageUri by profileViewModel.selectedImageUri.collectAsState()

    val isDarkMode by themeViewModel.isDarkMode.collectAsState()

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val imagePicker = rememberImagePicker(context) { uri ->
        profileViewModel.setSelectedImageUri(uri)
    }

    val backgroundColor = if (isDarkMode) DarkBackground else Color(0xFFFAF3E0)
    val textColor = if (isDarkMode) DarkTextLight else Color(0xFF333333)
    val textSecondaryColor = if (isDarkMode) DarkTextGrey else Color(0xFF666666)
    val primaryBlueColor = if (isDarkMode) DarkPrimaryBlue else Color(0xFF7DAFCB)
    val iconBackgroundColor = if (isDarkMode) DarkCardLight else Color(0xFFE6F1F8)
    val borderColor = if (isDarkMode) DarkPrimaryBlue.copy(alpha = 0.7f) else Color(0xFF7DAFCB)
    val outlinedBorderFocused = if (isDarkMode) DarkPrimaryBlue else Color(0xFF7DAFCB)
    val outlinedBorderUnfocused = if (isDarkMode) DarkPrimaryBlue.copy(alpha = 0.5f) else Color(0x807DAFCB)

    RequestStoragePermission(
        context = context,
        onPermissionGranted = {
        },
        onPermissionDenied = {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = "Izin akses penyimpanan dibutuhkan untuk mengubah foto profil",
                    duration = SnackbarDuration.Short
                )
            }
        }
    )

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            snackbarHostState.showSnackbar(
                message = "Profil berhasil diperbarui!",
                duration = SnackbarDuration.Short
            )
            kotlinx.coroutines.delay(1500)
            navController.popBackStack()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
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
                        tint = textColor
                    )
                }
                Text(
                    "Edit Akun",
                    color = textColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = 80.dp, bottom = 24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (uiState.isLoading && selectedImageUri == null) {
                    CircularProgressIndicator(
                        color = primaryBlueColor,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(iconBackgroundColor)
                            .clickable {
                                if (!uiState.isLoading) {
                                    imagePicker.pickImage()
                                }
                            }
                            .border(2.dp, borderColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            Box {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "Selected Profile Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                if (uiState.isLoading) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }
                                }
                            }
                        } else if (!uiState.profilePhotoUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = uiState.profilePhotoUrl,
                                contentDescription = "Profile Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = primaryBlueColor,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Username Field
                    OutlinedTextField(
                        value = uiState.username,
                        onValueChange = { profileViewModel.setUsername(it) },
                        label = { Text("Username", color = if (isDarkMode) DarkTextLight else Color.Gray) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Username",
                                tint = primaryBlueColor
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = outlinedBorderFocused,
                            unfocusedBorderColor = outlinedBorderUnfocused,
                            cursorColor = primaryBlueColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        ),
                        enabled = !uiState.isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email Field
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { /* Email tidak bisa diubah */ },
                        label = { Text("Email (tidak dapat diubah)", color = if (isDarkMode) DarkTextLight else Color.Gray) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = primaryBlueColor
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = outlinedBorderFocused,
                            unfocusedBorderColor = outlinedBorderUnfocused,
                            cursorColor = primaryBlueColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        ),
                        enabled = false,
                        readOnly = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Current Password Field
                    OutlinedTextField(
                        value = uiState.currentPassword,
                        onValueChange = { profileViewModel.setCurrentPassword(it) },
                        label = { Text("Password Saat Ini", color = if (isDarkMode) DarkTextLight else Color.Gray) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Current Password",
                                tint = primaryBlueColor
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { profileViewModel.toggleCurrentPasswordVisibility() }) {
                                Icon(
                                    imageVector = if (uiState.showCurrentPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (uiState.showCurrentPassword) "Hide Password" else "Show Password",
                                    tint = primaryBlueColor
                                )
                            }
                        },
                        visualTransformation = if (uiState.showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = outlinedBorderFocused,
                            unfocusedBorderColor = outlinedBorderUnfocused,
                            cursorColor = primaryBlueColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        ),
                        enabled = !uiState.isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // New Password Field
                    OutlinedTextField(
                        value = uiState.newPassword,
                        onValueChange = { profileViewModel.setNewPassword(it) },
                        label = { Text("Password Baru", color = if (isDarkMode) DarkTextLight else Color.Gray) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LockReset,
                                contentDescription = "New Password",
                                tint = primaryBlueColor
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { profileViewModel.toggleNewPasswordVisibility() }) {
                                Icon(
                                    imageVector = if (uiState.showNewPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (uiState.showNewPassword) "Hide Password" else "Show Password",
                                    tint = primaryBlueColor
                                )
                            }
                        },
                        visualTransformation = if (uiState.showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = outlinedBorderFocused,
                            unfocusedBorderColor = outlinedBorderUnfocused,
                            cursorColor = primaryBlueColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        ),
                        enabled = !uiState.isLoading
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Save Button
                    Button(
                        onClick = { profileViewModel.updateProfile() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryBlueColor),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text("Simpan Perubahan", color = Color.White, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}