package com.dsp.disiplinpro.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsp.disiplinpro.data.model.ChatHistory
import com.dsp.disiplinpro.ui.theme.DarkCardBackground
import com.dsp.disiplinpro.ui.theme.DarkPrimaryBlue
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ChatHistoryDrawer(
    isVisible: Boolean,
    histories: List<ChatHistory>,
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onSelectHistory: (ChatHistory) -> Unit,
    onDeleteHistory: (String) -> Unit
) {
    val backgroundColor = if (isDarkMode) Color.Black.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.3f)
    val drawerColor = if (isDarkMode) DarkCardBackground else Color(0xFFFFF8E1)

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .clickable { onDismiss() },
            contentAlignment = Alignment.CenterEnd
        ) {
            Card(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(300.dp)
                    .clickable(enabled = false) { /* Prevent click through */ },
                shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                colors = CardDefaults.cardColors(containerColor = drawerColor)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .padding(top = 22.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onDismiss() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Close",
                                tint = if (isDarkMode) Color.White else Color.Black
                            )
                        }

                        Text(
                            text = "Riwayat Percakapan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (isDarkMode) Color.White else Color.Black
                        )
                    }

                    Divider(
                        color = if (isDarkMode) Color(0xFF444444) else Color(0xFFDDDDDD),
                        thickness = 1.dp
                    )

                    if (histories.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Belum ada riwayat percakapan",
                                color = if (isDarkMode) Color(0xFFAAAAAA) else Color(0xFF666666)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(histories) { history ->
                                ChatHistoryItem(
                                    history = history,
                                    isDarkMode = isDarkMode,
                                    onSelect = { onSelectHistory(history) },
                                    onDelete = { onDeleteHistory(history.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatHistoryItem(
    history: ChatHistory,
    isDarkMode: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(history.updatedAt.toDate())

    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF2A2A2A) else Color(0xFFF5F5F5)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDarkMode) DarkPrimaryBlue else MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubble,
                    contentDescription = "Chat",
                    tint = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = history.title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = if (isDarkMode) Color.White else Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = history.lastMessage,
                    fontSize = 12.sp,
                    color = if (isDarkMode) Color(0xFFAAAAAA) else Color(0xFF666666),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = dateString,
                    fontSize = 10.sp,
                    color = if (isDarkMode) Color(0xFF888888) else Color(0xFF999999)
                )
            }

            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = if (isDarkMode) Color(0xFFE57373) else Color(0xFFE53935)
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Hapus Riwayat Chat",
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color.Black
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus riwayat percakapan ini?",
                    color = if (isDarkMode) Color.White.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.7f)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkMode) Color(0xFFE57373) else Color(0xFFE53935),
                        contentColor = Color.White
                    )
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text(
                        "Batal",
                        color = if (isDarkMode) Color.White.copy(alpha = 0.8f) else Color(0xFF666666)
                    )
                }
            },
            containerColor = if (isDarkMode) Color(0xFF2A2A2A) else Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}