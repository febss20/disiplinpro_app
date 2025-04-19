package com.example.disiplinpro.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.disiplinpro.viewmodel.home.HomeViewModel
import androidx.compose.foundation.shape.CircleShape
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.WatchLater
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.disiplinpro.ui.components.BottomNavigationBar
import com.example.disiplinpro.ui.schedule.ScheduleItem
import com.example.disiplinpro.ui.task.TaskItem
import com.example.disiplinpro.viewmodel.schedule.ScheduleViewModel
import com.example.disiplinpro.viewmodel.task.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel(),
    scheduleViewModel: ScheduleViewModel = viewModel(),
    taskViewModel: TaskViewModel = viewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        taskViewModel.fetchTasks()
        taskViewModel.provideAppContext(context)
    }

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

    val currentDate = remember { SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID")).format(Date()) }
    val currentTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }

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
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Username dan Foto Profil
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Halo,",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontSize = 16.sp,
                                        color = Color(0xFF333333),
                                    )
                                    Text(
                                        text = user?.username ?: "Guest",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF333333),
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.CalendarMonth,
                                            contentDescription = "Date",
                                            tint = Color(0xFF7DAFCB),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = currentDate,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontSize = 12.sp,
                                            color = Color(0xFF666666),
                                        )
                                    }
                                }
                                if (user?.fotoProfil.isNullOrEmpty()) {
                                    Surface(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape)
                                            .clickable { navController.navigate("akun") },
                                        color = Color(0xFFFFFFFF),
                                        shape = CircleShape,
                                        shadowElevation = 4.dp
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Profile",
                                            tint = Color(0x4D333333),
                                            modifier = Modifier
                                                .padding(12.dp)
                                                .size(36.dp)
                                        )
                                    }
                                } else {
                                    Surface(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape),
                                        shape = CircleShape,
                                        shadowElevation = 4.dp
                                    ) {
                                        AsyncImage(
                                            model = user?.fotoProfil,
                                            contentDescription = "Foto Profil",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clickable { navController.navigate("akun") },
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Kategori
                    Text(
                        "Kategori",
                        color = Color(0xFF333333),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Card Tugas
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .animateContentSize(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                                .clickable { navController.navigate("list_tugas") },
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0x332196F3)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape),
                                    color = Color(0x33F39C12),
                                    shape = CircleShape
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Assignment,
                                        contentDescription = "Tugas",
                                        tint = Color(0xFFF39C12),
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tugas",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF333333)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${tasks.size} tugas",
                                    fontSize = 12.sp,
                                    color = Color(0xFF666666)
                                )
                            }
                        }

                        // Card Jadwal
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .animateContentSize(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                                .clickable { navController.navigate("list_jadwal") },
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0x332196F3)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape),
                                    color = Color(0x332196F3),
                                    shape = CircleShape
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarMonth,
                                        contentDescription = "Jadwal",
                                        tint = Color(0xFF2196F3),
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Jadwal",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF333333)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${schedules.size} jadwal",
                                    fontSize = 12.sp,
                                    color = Color(0xFF666666)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Jadwal Hari Ini
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WatchLater,
                            contentDescription = "Jadwal Hari Ini",
                            tint = Color(0xFF7DAFCB),
                            modifier = Modifier
                                .size(28.dp)
                                .padding(end = 8.dp)
                        )
                        Text(
                            "Jadwal Hari Ini",
                            color = Color(0xFF333333),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { navController.navigate("add_jadwal") },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF7DAFCB))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Tambah Jadwal",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Panggil ScheduleItem
            item {
                if (todaySchedules.isNotEmpty()) {
                    ScheduleItem(todaySchedules)
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tidak ada jadwal hari ini",
                            color = Color(0xFF7DAFCB),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Tugas Hari Ini",
                        tint = Color(0xFFF39C12),
                        modifier = Modifier
                            .size(28.dp)
                            .padding(end = 8.dp)
                    )
                    Text(
                        "Tugas Hari Ini",
                        color = Color(0xFF333333),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { navController.navigate("add_tugas") },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF39C12))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah Tugas",
                            tint = Color.White
                        )
                    }
                }
            }

            // Panggil TaskItem
            item {
                if (todayTasks.isNotEmpty()) {
                    TaskItem(todayTasks, taskViewModel)
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tidak ada tugas hari ini",
                            color = Color(0xFF7DAFCB),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
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
            BottomNavigationBar(
                navController = navController,
                currentRoute = currentRoute,
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }
    }
}