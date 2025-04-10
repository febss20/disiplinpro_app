package com.example.disiplinpro.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.disiplinpro.R

@Composable
fun BottomNavigationBar(
    navController: NavController,
    currentRoute: String?
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 17.dp)
            .height(80.dp),
        shape = RoundedCornerShape(50.dp),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        color = Color(0xFFFFF8E1)
    ) {
        NavigationBar(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent
        ) {
            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ){
                NavigationBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.vector),
                            contentDescription = "Home",
                            modifier = Modifier.size(22.dp).scale(1.2f)
                        )
                    },
                    label = { Text("Home") },
                    selected = currentRoute == "home",
                    onClick = { navController.navigate("home") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = Color(0xFF2196F3),
                        unselectedIconColor = Color(0xFF333333),
                        selectedTextColor = Color(0xFF7DAFCB)
                    )
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.kalender),
                            contentDescription = "Kalender",
                            modifier = Modifier.size(22.dp).scale(1f)
                        )
                    },
                    label = { Text("Kalender") },
                    selected = currentRoute == "kalender",
                    onClick = { navController.navigate("kalender") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = Color(0xFF2196F3),
                        unselectedIconColor = Color(0xFF333333),
                        selectedTextColor = Color(0xFF7DAFCB)
                    )
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.notif),
                            contentDescription = "Notifikasi",
                            modifier = Modifier.size(22.dp).scale(1.4f)
                        )
                    },
                    label = { Text("Notifikasi") },
                    selected = currentRoute == "notifikasi",
                    onClick = { navController.navigate("notifikasi") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = Color(0xFF2196F3),
                        unselectedIconColor = Color(0xFF333333),
                        selectedTextColor = Color(0xFF7DAFCB)
                    )
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.akun),
                            contentDescription = "Akun",
                            modifier = Modifier.size(22.dp).scale(1f)
                        )
                    },
                    label = { Text("Akun") },
                    selected = currentRoute == "akun",
                    onClick = { navController.navigate("akun") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = Color(0xFF2196F3),
                        unselectedIconColor = Color(0xFF333333),
                        selectedTextColor = Color(0xFF7DAFCB)
                    )
                )
            }
        }
    }
}