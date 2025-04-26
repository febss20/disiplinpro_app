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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.disiplinpro.data.model.Notification
import com.example.disiplinpro.data.model.NotificationType
import com.example.disiplinpro.data.preferences.ThemePreferences
import com.example.disiplinpro.ui.theme.*
import com.example.disiplinpro.viewmodel.notification.NotificationViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationListScreen(
    navController: NavController,
    viewModel: NotificationViewModel = viewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val isLoading by remember { viewModel.isLoading }
    val error by remember { viewModel.error }
    val unreadCount by viewModel.unreadCount.collectAsState()

    val context = LocalContext.current
    val themePreferences = ThemePreferences(context)
    val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = false)

    var deletingNotifications by remember { mutableStateOf<Set<String>>(emptySet()) }

    val backgroundColor = if (isDarkMode) DarkBackground else Color(0xFFFAF3E0)
    val headerColor = if (isDarkMode) DarkPrimaryBlue else Color(0xFF1E88E5)
    val textColor = if (isDarkMode) DarkTextLight else Color(0xFF333333)
    val textSecondaryColor = if (isDarkMode) DarkTextGrey else Color(0xFF757575)

    val filteredNotifications = if (currentFilter == null) {
        notifications.filter { it.id !in deletingNotifications }
    } else {
        notifications.filter { it.type == currentFilter && it.id !in deletingNotifications }
    }

    val scope = rememberCoroutineScope()
    var showClearDialog by remember { mutableStateOf(false) }
    var showMarkAllReadDialog by remember { mutableStateOf(false) }

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Riwayat Notifikasi",
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )

                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(24.dp)
                                    .background(headerColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = textColor
                        )
                    }
                },
                actions = {
                    if (unreadCount > 0) {
                        IconButton(
                            onClick = { showMarkAllReadDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Tandai Semua Dibaca",
                                tint = headerColor
                            )
                        }
                    }

                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Hapus Notifikasi Dibaca",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = textColor
                )
            )

            NotificationFilterChips(
                currentFilter = currentFilter,
                onFilterSelected = { viewModel.setFilter(it) },
                isDarkMode = isDarkMode
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = headerColor)
                }
            } else if (filteredNotifications.isEmpty()) {
                EmptyNotificationView(isDarkMode = isDarkMode)
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
                            },
                            viewModel = viewModel,
                            isDarkMode = isDarkMode
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

        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = { Text("Konfirmasi", color = if (isDarkMode) DarkTextLight else Color.Black) },
                text = { Text("Hapus semua notifikasi yang sudah dibaca?", color = if (isDarkMode) DarkTextLight else Color.Black) },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.clearReadNotifications()
                                showClearDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkMode) DarkPrimaryBlue else Color(0xFF1E88E5)
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
                },
                containerColor = if (isDarkMode) DarkCardBackground else Color.White
            )
        }

        if (showMarkAllReadDialog) {
            AlertDialog(
                onDismissRequest = { showMarkAllReadDialog = false },
                title = { Text("Konfirmasi", color = if (isDarkMode) DarkTextLight else Color.Black) },
                text = { Text("Tandai semua notifikasi sebagai sudah dibaca?", color = if (isDarkMode) DarkTextLight else Color.Black) },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                markAllNotificationsAsRead(viewModel, notifications)
                                showMarkAllReadDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkMode) DarkPrimaryBlue else Color(0xFF1E88E5)
                        )
                    ) {
                        Text("Tandai Semua")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showMarkAllReadDialog = false }
                    ) {
                        Text("Batal")
                    }
                },
                containerColor = if (isDarkMode) DarkCardBackground else Color.White
            )
        }
    }
}

private suspend fun markAllNotificationsAsRead(viewModel: NotificationViewModel, notifications: List<Notification>) {
    viewModel.markAllAsRead()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationFilterChips(
    currentFilter: NotificationType?,
    onFilterSelected: (NotificationType?) -> Unit,
    isDarkMode: Boolean
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
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                            tint = Color.White
                        )
                    }
                } else null,
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = if (isDarkMode) DarkCardLight.copy(alpha = 0.3f) else Color(0x337DAFCB),
                    labelColor = if (isDarkMode) DarkTextLight else Color(0xFF333333),
                    selectedContainerColor = if (isDarkMode) DarkPrimaryBlue else Color(0xFF7DAFCB),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit,
    viewModel: NotificationViewModel = viewModel(),
    isDarkMode: Boolean
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
            containerColor = if (isDarkMode)
                (if (isReadLocally) DarkCardBackground.copy(alpha = 0.3f) else DarkCardBackground.copy(alpha = 0.7f))
            else (if (isReadLocally) Color(0x1F1E88E5) else Color(0x331E88E5))
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
                        color = if (isDarkMode) DarkTextLight else Color(0xFF333333)
                    )

                    Text(
                        text = viewModel.formatTimestamp(notification.timestamp),
                        fontSize = 12.sp,
                        color = if (isDarkMode) DarkTextGrey else Color(0xFF666666)
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
                            tint = if (isDarkMode) Color(0xFFE57373).copy(alpha = 0.8f) else Color(0xFFE57373),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (!isReadLocally) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isDarkMode) DarkPrimaryBlue else Color(0xFF1E88E5))
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
                        color = if (isDarkMode) DarkTextLight else Color(0xFF333333),
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
fun EmptyNotificationView(isDarkMode: Boolean) {
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
            tint = if (isDarkMode) DarkIconInactive else Color(0xFFBDBDBD),
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tidak Ada Notifikasi",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDarkMode) DarkTextLight else Color(0xFF757575)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Notifikasi akan muncul saat ada pengingat tugas atau jadwal",
            fontSize = 14.sp,
            color = if (isDarkMode) DarkTextGrey else Color(0xFF9E9E9E),
            overflow = TextOverflow.Ellipsis
        )
    }
}