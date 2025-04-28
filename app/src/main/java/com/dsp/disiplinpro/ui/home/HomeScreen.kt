package com.dsp.disiplinpro.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.WatchLater
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.dsp.disiplinpro.ui.components.BottomNavigationBar
import com.dsp.disiplinpro.ui.schedule.ScheduleItem
import com.dsp.disiplinpro.ui.task.TaskItem
import com.dsp.disiplinpro.viewmodel.home.HomeViewModel
import com.dsp.disiplinpro.viewmodel.schedule.ScheduleViewModel
import com.dsp.disiplinpro.viewmodel.task.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import com.dsp.disiplinpro.data.preferences.ThemePreferences
import com.dsp.disiplinpro.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel(),
    scheduleViewModel: ScheduleViewModel = viewModel(),
    taskViewModel: TaskViewModel = viewModel()
) {
    val context = LocalContext.current
    val user by viewModel.user.collectAsState()
    val tasks by taskViewModel.tasks.collectAsState()
    val schedules by scheduleViewModel.schedules.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val themePreferences = ThemePreferences(context)
    val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = false)

    LaunchedEffect(Unit) {
        taskViewModel.getTodayTasks()

        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val days = listOf("Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
        val todayDay = days[today - 1]

        scheduleViewModel.getSchedulesByDay(todayDay)

        taskViewModel.provideAppContext(context)
    }

    val todaySchedules = schedules
    val todayTasks = tasks

    val currentDate = remember { SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID")).format(Date()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkMode) DarkBackground else Color(0xFFFAF3E0))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 8.dp)
        ) {
            item { UserProfileSection(user?.username, user?.fotoProfil, currentDate, navController, isDarkMode) }
            item { CategorySection(tasks.size, schedules.size, navController, isDarkMode) }
            item { SectionHeader("Jadwal Hari Ini", Icons.Outlined.WatchLater, Color(0xFF7DAFCB), isDarkMode) { navController.navigate("add_jadwal") } }
            item {
                if (todaySchedules.isNotEmpty()) ScheduleItem(todaySchedules)
                else EmptyItemMessage("Tidak ada jadwal hari ini", isDarkMode)
            }
            item { SectionHeader("Tugas Hari Ini", Icons.Outlined.CheckCircle, Color(0xFFF39C12), isDarkMode) { navController.navigate("add_tugas") } }
            item {
                if (todayTasks.isNotEmpty()) TaskItem(todayTasks, taskViewModel)
                else EmptyItemMessage("Tidak ada tugas hari ini", isDarkMode)
            }
            item { Spacer(modifier = Modifier.height(120.dp)) }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(if (isDarkMode) DarkBackground else Color(0xFFFAF3E0))
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

@Composable
private fun UserProfileSection(
    username: String?,
    profilePic: String?,
    currentDate: String,
    navController: NavController,
    isDarkMode: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Halo,",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 16.sp,
                        color = if (isDarkMode) DarkTextLight else Color(0xFF333333)
                    )
                    Text(
                        text = username ?: "Guest",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) DarkTextLight else Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                            color = if (isDarkMode) DarkTextGrey else Color(0xFF757575)
                        )
                    }
                }
                ProfilePicture(profilePic, navController)
            }
        }
    }
}

@Composable
private fun ProfilePicture(profilePic: String?, navController: NavController) {
    if (profilePic.isNullOrEmpty()) {
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
                model = profilePic,
                contentDescription = "Foto Profil",
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { navController.navigate("akun") },
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun CategorySection(
    taskCount: Int,
    scheduleCount: Int,
    navController: NavController,
    isDarkMode: Boolean
) {
    Text(
        "Kategori",
        color = if (isDarkMode) DarkTextLight else Color(0xFF333333),
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
        Box(modifier = Modifier.weight(1f)) {
            CategoryCard(
                title = "Tugas",
                count = taskCount,
                icon = Icons.Outlined.Assignment,
                iconBgColor = Color(0x33F39C12),
                iconColor = Color(0xFFF39C12),
                isDarkMode = isDarkMode,
                onClick = { navController.navigate("list_tugas") }
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            CategoryCard(
                title = "Jadwal",
                count = scheduleCount,
                icon = Icons.Outlined.CalendarMonth,
                iconBgColor = Color(0x332196F3),
                iconColor = Color(0xFF2196F3),
                isDarkMode = isDarkMode,
                onClick = { navController.navigate("list_jadwal") }
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
private fun CategoryCard(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBgColor: Color,
    iconColor: Color,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) DarkCardLight else Color(0x332196F3)
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
                color = if (isDarkMode) iconBgColor.copy(alpha = 0.3f) else iconBgColor,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (isDarkMode) DarkTextLight else Color(0xFF333333)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$count $title".lowercase(),
                fontSize = 12.sp,
                color = if (isDarkMode) DarkTextGrey else Color(0xFF666666)
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    isDarkMode: Boolean,
    onAddClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = tint,
            modifier = Modifier
                .size(28.dp)
                .padding(end = 8.dp)
        )
        Text(
            title,
            color = if (isDarkMode) DarkTextLight else Color(0xFF333333),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onAddClick,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(tint)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Tambah $title",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun EmptyItemMessage(message: String, isDarkMode: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = if (isDarkMode) DarkPrimaryBlue else Color(0xFF7DAFCB),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}