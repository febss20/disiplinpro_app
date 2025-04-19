package com.example.disiplinpro.ui.profile

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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.work.WorkManager
import coil.compose.AsyncImage
import com.example.disiplinpro.ui.components.BottomNavigationBar
import com.example.disiplinpro.viewmodel.auth.AuthViewModel
import com.example.disiplinpro.viewmodel.home.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel()
) {
    val user by homeViewModel.user.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF3E0))
    ) {
        Text(
            "Profile",
            color = Color(0xFF333333),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 20.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(70.dp))

            // Card untuk Foto Profil dan Username
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x332196F3))
                    .padding(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Foto Profil di kiri
                    if (user?.fotoProfil.isNullOrEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color(0x4D333333),
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .border(1.dp, Color(0x4D333333), RoundedCornerShape(100.dp))
                                .background(Color(0xFFFFFFFF))
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
                            color = Color(0xFF333333),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentDate,
                            color = Color(0xFF333333),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Opsi Menu
            ProfileMenuItem(
                title = "Edit Akun",
                onClick = { navController.navigate("edit_akun") }
            )
            ProfileMenuItem(
                title = "Notifikasi",
                onClick = { navController.navigate("notifikasi") }
            )
            ProfileMenuItem(
                title = "Keamanan dan Privasi",
                onClick = { navController.navigate("keamanan_privasi") }
            )
            ProfileMenuItem(
                title = "FAQ",
                onClick = { navController.navigate("faq") }
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
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Logout", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(96.dp))
        }

        // Bottom Navigation Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color(0xFFFAF3E0))
                .align(Alignment.BottomCenter)
        ) {
            BottomNavigationBar(navController = navController, currentRoute = currentRoute)
        }
    }
}

@Composable
fun ProfileMenuItem(title: String, onClick: () -> Unit) {
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
                .background(backgroundColor.copy(alpha = 0.7f)),
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
            color = Color(0xFF333333),
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
                containerColor = Color(0x337DAFCB)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = "Arrow Right",
                    tint = Color(0xFF7DAFCB),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}