package com.example.disiplinpro.ui.notification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.disiplinpro.data.model.NotificationHistory
import com.example.disiplinpro.data.model.NotificationType
import com.example.disiplinpro.viewmodel.notification.NotificationHistoryViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationHistoryScreen(
    navController: NavController,
    viewModel: NotificationHistoryViewModel = viewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val isLoading by remember { viewModel.isLoading }
    val error by remember { viewModel.error }

    var deletingNotifications by remember { mutableStateOf<Set<String>>(emptySet()) }

    val backgroundColor = Color(0xFFFAF3E0)
    val headerColor = Color(0xFF1E88E5)

    val filteredNotifications = if (currentFilter == null) {
        notifications.filter { it.id !in deletingNotifications }
    } else {
        notifications.filter { it.type == currentFilter && it.id !in deletingNotifications }
    }

    val scope = rememberCoroutineScope()
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            TopAppBar(
                title = {
                    Text(
                        "Riwayat Notifikasi",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF333333)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Hapus Notifikasi Dibaca",
                            tint = Color(0xFF333333)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = Color(0xFF333333)
                )
            )

            NotificationFilterChips(
                currentFilter = currentFilter,
                onFilterSelected = { viewModel.setFilter(it) }
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = headerColor)
                }
            } else if (filteredNotifications.isEmpty()) {
                EmptyNotificationView()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = filteredNotifications,
                        key = { it.id }
                    ) { notification ->
                        NotificationItem(
                            notification = notification,
                            onMarkAsRead = { viewModel.markAsRead(notification.id) },
                            onDelete = {
                                scope.launch {
                                    deletingNotifications = deletingNotifications + notification.id
                                    delay(300)
                                    viewModel.deleteNotification(notification.id)
                                    delay(500)
                                    deletingNotifications = deletingNotifications - notification.id
                                }
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        error?.let {
            Snackbar(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                containerColor = Color(0xFFFF5252),
                contentColor = Color.White,
                dismissAction = {
                    IconButton(onClick = { viewModel.error.value = null }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color.White
                        )
                    }
                }
            ) {
                Text(it)
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Konfirmasi") },
            text = { Text("Hapus semua notifikasi yang sudah dibaca?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.clearReadNotifications()
                            showClearDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E88E5)
                    )
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClearDialog = false }
                ) {
                    Text("Batal")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationFilterChips(
    currentFilter: NotificationType?,
    onFilterSelected: (NotificationType?) -> Unit
) {
    val filters = listOf(
        null to "Semua",
        NotificationType.TASK to "Tugas",
        NotificationType.SCHEDULE to "Jadwal",
        NotificationType.SYSTEM to "Sistem"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { (type, label) ->
            FilterChip(
                selected = currentFilter == type,
                onClick = { onFilterSelected(type) },
                label = { Text(label) },
                leadingIcon = if (currentFilter == type) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White,
                    labelColor = Color(0xFF333333),
                    selectedContainerColor = Color(0x331E88E5),
                    selectedLabelColor = Color(0xFF1E88E5)
                )
            )
        }
    }
}

@Composable
fun NotificationItem(
    notification: NotificationHistory,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit,
    viewModel: NotificationHistoryViewModel = viewModel()
) {
    var expanded by remember { mutableStateOf(false) }
    var isReadLocally by remember { mutableStateOf(notification.isRead) }

    LaunchedEffect(notification.id, notification.isRead) {
        isReadLocally = notification.isRead
    }

    val iconTint = when(notification.type) {
        NotificationType.TASK -> Color(0xFFFFA000)
        NotificationType.SCHEDULE -> Color(0xFF1E88E5)
        NotificationType.SYSTEM -> Color(0xFF4CAF50)
    }

    val icon = when(notification.type) {
        NotificationType.TASK -> Icons.Default.Task
        NotificationType.SCHEDULE -> Icons.Default.Schedule
        NotificationType.SYSTEM -> Icons.Default.Info
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                expanded = !expanded
                if (!isReadLocally) {
                    isReadLocally = true
                    onMarkAsRead()
                }
            }
            .alpha(if (isReadLocally) 0.7f else 1f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isReadLocally)
                Color(0x1F1E88E5) else Color(0x331E88E5)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = notification.title,
                        fontWeight = if (isReadLocally) FontWeight.Normal else FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF333333)
                    )

                    Text(
                        text = viewModel.formatTimestamp(notification.timestamp),
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus notifikasi",
                            tint = Color(0xFFE57373),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (!isReadLocally) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E88E5))
                        )
                    }
                }
            }

            // Expandable message
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(200)) + slideInVertically(tween(200)),
                exit = fadeOut(tween(200)) + slideOutVertically(tween(200))
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text(
                        text = notification.message,
                        fontSize = 14.sp,
                        color = Color(0xFF333333),
                        letterSpacing = 0.25.sp,
                        lineHeight = 20.sp
                    )

                    if (notification.relatedItemTitle.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when(notification.type) {
                                    NotificationType.TASK -> Icons.Default.Assignment
                                    NotificationType.SCHEDULE -> Icons.Default.Event
                                    else -> Icons.Default.Info
                                },
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = notification.relatedItemTitle,
                                fontSize = 13.sp,
                                color = iconTint,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyNotificationView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = null,
            tint = Color(0xFFBDBDBD),
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tidak Ada Notifikasi",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF757575)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Notifikasi akan muncul di sini ketika ada pengingat tugas atau jadwal",
            fontSize = 14.sp,
            color = Color(0xFF9E9E9E),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 32.dp),
            lineHeight = 20.sp
        )
    }
}