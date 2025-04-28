package com.dsp.disiplinpro.ui.task

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.dsp.disiplinpro.data.model.Task
import com.dsp.disiplinpro.ui.components.BottomNavigationBar
import com.dsp.disiplinpro.viewmodel.task.TaskViewModel
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.ui.text.style.TextAlign
import com.dsp.disiplinpro.data.preferences.ThemePreferences
import com.dsp.disiplinpro.ui.theme.DarkBackground
import com.dsp.disiplinpro.ui.theme.DarkPrimaryBlue
import com.dsp.disiplinpro.ui.theme.DarkTextGrey
import com.dsp.disiplinpro.ui.theme.DarkTextLight
import com.dsp.disiplinpro.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTasksScreen(
    navController: NavController,
    viewModel: TaskViewModel = viewModel()
) {
    val context = LocalContext.current

    val themePreferences = ThemePreferences(context)
    val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = false)

    val backgroundColor = if (isDarkMode) DarkBackground else Color(0xFFFAF3E0)
    val textColor = if (isDarkMode) DarkTextLight else Color(0xFF333333)
    val secondaryTextColor = if (isDarkMode) DarkTextGrey else Color(0xFF757575)
    val accentColor = if (isDarkMode) DarkPrimaryBlue else Color(0xFF7DAFCB)
    val accentColorDisabled = if (isDarkMode) DarkPrimaryBlue.copy(alpha = 0.5f) else Color(0xFF7DAFCB).copy(alpha = 0.5f)
    val deleteColor = if (isDarkMode) Color(0xFFFF5252) else Color(0xFFD86F6F)
    val deleteColorDisabled = if (isDarkMode) Color(0xFFFF5252).copy(alpha = 0.5f) else Color(0xFFD86F6F).copy(alpha = 0.5f)

    LaunchedEffect(key1 = Unit) {
        viewModel.fetchTasks()
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.provideAppContext(context)
    }

    val tasks by viewModel.tasks.collectAsState(initial = emptyList())
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 20.dp)
        ) {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 30.dp)
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = textColor
                            )
                        }
                        Text(
                            "Semua Tugas",
                            color = textColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))

                        IconButton(
                            onClick = {
                                selectedTask?.let { task ->
                                    navController.navigate("edit_tugas/${task.id}")
                                }
                            },
                            enabled = selectedTask != null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selectedTask != null) accentColor else accentColorDisabled
                                )
                        ) {
                            CoilImage(
                                imageModel = { R.drawable.edit },
                                modifier = Modifier.size(24.dp),
                                imageOptions = ImageOptions(contentScale = ContentScale.Fit)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))

                        IconButton(
                            onClick = {
                                selectedTask?.let { task ->
                                    task.id.let { id ->
                                        viewModel.deleteTask(id)
                                        selectedTask = null
                                    }
                                }
                            },
                            enabled = selectedTask != null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selectedTask != null) deleteColor else deleteColorDisabled
                                )
                        ) {
                            CoilImage(
                                imageModel = { R.drawable.delete },
                                modifier = Modifier.size(24.dp),
                                imageOptions = ImageOptions(contentScale = ContentScale.Fit)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (tasks.isEmpty()) {
                    // Empty state message
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Assignment,
                            contentDescription = "Empty Tasks",
                            modifier = Modifier.size(120.dp),
                            tint = accentColor
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Belum ada tugas",
                            color = accentColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tambahkan tugas baru dengan tombol di bawah",
                            color = secondaryTextColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 32.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                    ) {
                        tasks.forEach { task ->
                            TaskItem(
                                tasks = listOf(task),
                                viewModel = viewModel,
                                isSelected = selectedTask == task,
                                modifier = Modifier
                                    .clickable {
                                        selectedTask = if (selectedTask == task) null else task
                                    }
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        }

                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }

                // Tombol Tambah Tugas
                FloatingActionButton(
                    onClick = { navController.navigate("add_tugas") },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 25.dp, end = 30.dp),
                    containerColor = Color(0xFFF39C12),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah Tugas",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            BottomNavigationBar(navController = navController, currentRoute = currentRoute)
        }
    }
}