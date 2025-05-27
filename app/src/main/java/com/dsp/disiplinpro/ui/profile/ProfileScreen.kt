package com.dsp.disiplinpro.ui.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.work.WorkManager
import coil.compose.AsyncImage
import com.dsp.disiplinpro.ui.components.DarkModeToggle
import com.dsp.disiplinpro.ui.theme.DarkBackground
import com.dsp.disiplinpro.ui.theme.DarkCardBackground
import com.dsp.disiplinpro.ui.theme.DarkTextLight
import com.dsp.disiplinpro.viewmodel.auth.AuthViewModel
import com.dsp.disiplinpro.viewmodel.home.HomeViewModel
import com.dsp.disiplinpro.viewmodel.theme.ThemeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel(),
    themeViewModel: ThemeViewModel
) {
    val user by homeViewModel.user.collectAsState()
    val currentDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
    val context = LocalContext.current
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkMode) DarkBackground else Color(0xFFFAF3E0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp, start = 20.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Profile",
                color = if (isDarkMode) DarkTextLight else Color(0xFF333333),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            DarkModeToggle(
                isDarkMode = isDarkMode,
                onToggle = { themeViewModel.toggleDarkMode() }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 25.dp)
        ) {
            Spacer(modifier = Modifier.height(70.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isDarkMode) Color(0x332196F3).copy(alpha = 0.2f) else Color(0x332196F3))
                    .padding(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (user?.fotoProfil.isNullOrEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = if (isDarkMode) Color(0x8DFFFFFF) else Color(0x4D333333),
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .border(1.dp, if (isDarkMode) Color(0x8DFFFFFF) else Color(0x4D333333), RoundedCornerShape(100.dp))
                                .background(if (isDarkMode) DarkCardBackground else Color(0xFFFFFFFF))
                        )
                    } else {
                        AsyncImage(
                            model = user?.fotoProfil,
                            contentDescription = "Foto Profil",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(100.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Username dan Tanggal di kanan
                    Column {
                        Text(
                            text = user?.username ?: "Guest",
                            color = if (isDarkMode) DarkTextLight else Color(0xFF333333),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentDate,
                            color = if (isDarkMode) DarkTextLight.copy(alpha = 0.7f) else Color(0xFF333333),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Opsi Menu
            ProfileMenuItem(
                title = "Edit Akun",
                onClick = { navController.navigate("edit_akun") },
                isDarkMode = isDarkMode
            )
            ProfileMenuItem(
                title = "Notifikasi",
                onClick = { navController.navigate("notifikasi") },
                isDarkMode = isDarkMode
            )
            ProfileMenuItem(
                title = "Keamanan dan Privasi",
                onClick = { navController.navigate("keamanan_privasi") },
                isDarkMode = isDarkMode
            )
            ProfileMenuItem(
                title = "FAQ",
                onClick = { navController.navigate("faq") },
                isDarkMode = isDarkMode
            )

            Spacer(modifier = Modifier.weight(1f))

            // Tombol Logout
            Button(
                onClick = {
                    Log.d("ProfileScreen", "Cancelling all notification workers")
                    WorkManager.getInstance(context)
                        .cancelAllWorkByTag("notification_tag")
                    authViewModel.logoutUser(context) {
                        Log.d("ProfileScreen", "Logout successful, navigating to onboarding")
                        navController.navigate("onboarding") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                shape = RoundedCornerShape(10.dp),
                enabled = !authViewModel.isLoading.value
            ) {
                if (authViewModel.isLoading.value) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Logout", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(150.dp))
        }
    }
}

@Composable
fun ProfileMenuItem(
    title: String,
    onClick: () -> Unit,
    isDarkMode: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        val (icon, backgroundColor) = when (title) {
            "Edit Akun" -> Pair(
                Icons.Default.Settings,
                Color(0xFFE57373)
            )
            "Notifikasi" -> Pair(
                Icons.Default.Notifications,
                Color(0xFF81C784)
            )
            "Keamanan dan Privasi" -> Pair(
                Icons.Default.Security,
                Color(0xFF64B5F6)
            )
            "FAQ" -> Pair(
                Icons.Default.Help,
                Color(0xFFFFD54F)
            )
            else -> Pair(
                Icons.Default.Person,
                Color(0xFF7DAFCB)
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(backgroundColor.copy(alpha = if (isDarkMode) 0.5f else 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = title,
            color = if (isDarkMode) DarkTextLight else Color(0xFF333333),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        )

        Card(
            modifier = Modifier
                .size(32.dp)
                .clickable { onClick() },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0x337DAFCB).copy(alpha = 0.2f) else Color(0x337DAFCB)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = "Arrow Right",
                    tint = if (isDarkMode) Color(0xFF5A8CA8) else Color(0xFF7DAFCB),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}