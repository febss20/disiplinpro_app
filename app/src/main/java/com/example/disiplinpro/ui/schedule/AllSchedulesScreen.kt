package com.example.disiplinpro.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.disiplinpro.data.model.Schedule
import com.example.disiplinpro.ui.components.BottomNavigationBar
import com.example.disiplinpro.viewmodel.schedule.ScheduleViewModel
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import com.example.disiplinpro.R
import com.example.disiplinpro.ui.theme.DisiplinproTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllSchedulesScreen(navController: NavController, viewModel: ScheduleViewModel = viewModel()) {
    val schedules by viewModel.schedules.collectAsState(initial = emptyList())
    var selectedSchedule by remember { mutableStateOf<Schedule?>(null) } // State untuk jadwal yang dipilih
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF3E0)) // Background utama
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 17.dp)
        ) {
            // TopAppBar dengan tombol edit dan hapus
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp)
                    ) {
                        Text(
                            "Semua Jadwal",
                            color = Color(0xFF333333),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))

                        // Tombol Edit
                        IconButton(
                            onClick = {
                                selectedSchedule?.let { schedule ->
                                    navController.navigate("edit_jadwal/${schedule.id}")
                                }
                            },
                            enabled = selectedSchedule != null,
                            modifier = Modifier.size(40.dp)
                        ) {
                            CoilImage(
                                imageModel = { R.drawable.edit },
                                modifier = Modifier.size(24.dp),
                                imageOptions = ImageOptions(contentScale = ContentScale.Fit)
                            )
                        }
                        // Tombol Hapus
                        IconButton(
                            onClick = {
                                selectedSchedule?.let { schedule ->
                                    schedule.id?.let { id ->
                                        viewModel.deleteSchedule(id)
                                        selectedSchedule = null // Reset pilihan setelah hapus
                                    }
                                }
                            },
                            enabled = selectedSchedule != null,
                            modifier = Modifier.size(40.dp)
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

            // Daftar Jadwal
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                items(schedules) { schedule ->
                    ScheduleItem(
                        schedules = listOf(schedule),
                        modifier = Modifier
                            .clickable {
                                selectedSchedule = if (selectedSchedule == schedule) null else schedule
                            }
                            .background(
                                if (selectedSchedule == schedule) Color(0x4D2196F3) else Color.Transparent
                            )
                    )
                }
            }

            // Tombol Tambah Jadwal di atas NavigationBar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = { navController.navigate("add_jadwal") },
                    modifier = Modifier.size(90.dp)
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

            // BottomNavigationBar
            BottomNavigationBar(navController = navController, currentRoute = currentRoute)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAllSchedulesScreen() {
    DisiplinproTheme {
        AllSchedulesScreen(rememberNavController())
    }
}