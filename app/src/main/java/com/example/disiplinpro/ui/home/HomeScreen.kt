package com.example.disiplinpro.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.disiplinpro.viewmodel.home.HomeViewModel
import com.example.disiplinpro.ui.theme.DisiplinproTheme
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import androidx.compose.foundation.shape.CircleShape
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.disiplinpro.ui.components.BottomNavigationBar
import com.example.disiplinpro.ui.schedule.ScheduleItem
import com.example.disiplinpro.ui.task.TaskItem
import com.example.disiplinpro.viewmodel.schedule.ScheduleViewModel
import com.example.disiplinpro.viewmodel.task.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel(),
    scheduleViewModel: ScheduleViewModel = viewModel(),
    taskViewModel: TaskViewModel = viewModel()
) {
    val tasks by taskViewModel.tasks.collectAsState()
    val schedules by scheduleViewModel.schedules.collectAsState()
    val user by viewModel.user.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Filter jadwal dan tugas untuk hari ini
    val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    val days = listOf("Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
    val todayDay = days[today - 1]
    val todaySchedules = schedules.filter { it.hari == todayDay }
    val todayTasks = tasks.filter {
        val taskDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.tanggal.toDate())
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        taskDate == currentDate
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF3E0))
    ) {
        // Konten utama yang bisa di-scroll
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 8.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAF3E0))
                ) {
                    // Username dan Foto Profil
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 55.dp, bottom = 8.dp, start = 31.dp, end = 31.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = user?.username ?: "Guest",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                        )
                        if (user?.fotoProfil.isNullOrEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color(0x4D333333),
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color(0x4D333333), RoundedCornerShape(100.dp))
                                    .background(Color(0xFFFFFFFF))
                            )
                        } else {
                            AsyncImage(
                                model = user?.fotoProfil,
                                contentDescription = "Foto Profil",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Kategori
                    Text(
                        "Kategori",
                        color = Color(0xFF333333),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 15.dp, start = 31.dp)
                    )
                    Row(
                        modifier = Modifier.padding(top = 25.dp, start = 30.dp)
                    ) {
                        Button(
                            onClick = { navController.navigate("list_tugas") },
                            modifier = Modifier
                                .padding(end = 22.dp)
                                .border(1.dp, Color(0xFF7DAFCB), RoundedCornerShape(10.dp))
                                .padding(top = 30.dp, bottom = 14.dp, start = 35.dp, end = 35.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CoilImage(
                                    imageModel = { "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/T7pdvlFwTn/gs17algr.png" },
                                    modifier = Modifier.size(29.dp),
                                    imageOptions = ImageOptions(contentScale = ContentScale.Crop)
                                )
                                Text("Tugas", color = Color(0xFF7DAFCB), fontSize = 12.sp)
                            }
                        }
                        Button(
                            onClick = { navController.navigate("list_jadwal") },
                            modifier = Modifier
                                .border(1.dp, Color(0xFF7DAFCB), RoundedCornerShape(10.dp))
                                .padding(top = 28.dp, bottom = 15.dp, start = 32.dp, end = 32.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CoilImage(
                                    imageModel = { "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/T7pdvlFwTn/rujrrssz.png" },
                                    modifier = Modifier.size(29.dp),
                                    imageOptions = ImageOptions(contentScale = ContentScale.Crop)
                                )
                                Text("Jadwal", color = Color(0xFF7DAFCB), fontSize = 12.sp)
                            }
                        }
                    }

                    // Jadwal Hari Ini
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp, start = 31.dp, end = 7.dp)
                    ) {
                        Text(
                            "Jadwal Hari Ini",
                            color = Color(0xFF333333),
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { navController.navigate("add_jadwal") },
                            modifier = Modifier.size(85.dp)
                        ) {
                            CoilImage(
                                imageModel = { "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/T7pdvlFwTn/0lx4ib3k.png" },
                                modifier = Modifier.fillMaxSize(),
                                imageOptions = ImageOptions(
                                    contentScale = ContentScale.Fit,
                                    alignment = Alignment.Center
                                )
                            )
                        }
                    }
                }
            }

            // Panggil ScheduleItem
            item {
                if (todaySchedules.isNotEmpty()) {
                    ScheduleItem(todaySchedules)
                }
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 31.dp, end = 7.dp)
                ) {
                    Text(
                        "Tugas Hari Ini",
                        color = Color(0xFF333333),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { navController.navigate("add_tugas") },
                        modifier = Modifier.size(85.dp)
                    ) {
                        CoilImage(
                            imageModel = { "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/T7pdvlFwTn/0lx4ib3k.png" },
                            modifier = Modifier
                                .fillMaxSize(),
                            imageOptions = ImageOptions(
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.Center
                            )
                        )
                    }
                }
            }

            // Panggil TaskItem
            item {
                if (todayTasks.isNotEmpty()) {
                    TaskItem(todayTasks, taskViewModel)
                }
            }

            item {
                Spacer(modifier = Modifier.height(120.dp))
            }
        }

        // Box untuk navigasi di bagian bawah
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


@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    DisiplinproTheme {
        HomeScreen(rememberNavController())
    }
}