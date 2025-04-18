package com.example.disiplinpro.ui.task

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
import com.example.disiplinpro.data.model.Task
import com.example.disiplinpro.ui.components.BottomNavigationBar
import com.example.disiplinpro.viewmodel.task.TaskViewModel
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import com.example.disiplinpro.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTasksScreen(
    navController: NavController,
    viewModel: TaskViewModel = viewModel()
) {
    LaunchedEffect(key1 = Unit) {
        viewModel.fetchTasks()
    }

    val context = LocalContext.current
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
            .background(Color(0xFFFAF3E0))
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
                            .padding(start = 20.dp, end = 30.dp)
                    ) {
                        Text(
                            "Semua Tugas",
                            color = Color(0xFF333333),
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
                                    if (selectedTask != null) Color(0xFF7DAFCB)
                                    else Color(0xFF7DAFCB).copy(alpha = 0.5f)
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
                                    if (selectedTask != null) Color(0xFFD86F6F)
                                    else Color(0xFFD86F6F).copy(alpha = 0.5f)
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
                    containerColor = Color(0xFFFAF3E0)
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
                            tint = Color(0xFF7DAFCB)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Belum ada tugas",
                            color = Color(0xFF7DAFCB),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tambahkan tugas baru dengan tombol di bawah",
                            color = Color(0xFF757575),
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
                IconButton(
                    onClick = { navController.navigate("add_tugas") },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 10.dp, end = 10.dp)
                        .size(90.dp)
                ) {
                    CoilImage(
                        imageModel = { "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/T7pdvlFwTn/78p77w0y.png" },
                        modifier = Modifier.fillMaxSize(),
                        imageOptions = ImageOptions(
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.Center
                        )
                    )
                }
            }
            BottomNavigationBar(navController = navController, currentRoute = currentRoute)
        }
    }
}