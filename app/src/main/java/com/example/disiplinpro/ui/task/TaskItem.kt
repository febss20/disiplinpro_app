package com.example.disiplinpro.ui.task

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.disiplinpro.data.model.Task
import com.example.disiplinpro.viewmodel.task.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TaskItem(
    tasks: List<Task>,
    viewModel: TaskViewModel,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val cardColor = if (isSelected) Color(0xFF7DAFCB) else Color(0x332196F3)
    val cardTextColor = if (isSelected) Color(0xFF333333) else Color(0xFF333333)
    val checkboxColor = if (isSelected) Color.White else Color(0xFF7DAFCB)
    val iconTint = if (isSelected) Color(0xFF333333) else Color(0xFF333333)

    val completedStatusColor = Color(0xFF4CAF50)
    val pendingStatusColor = Color(0xFFFF9800)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 30.dp, end = 30.dp, bottom = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(top = 5.dp, bottom = 20.dp)) {
            tasks.forEach { task ->
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 12.dp, start = 15.dp, end = 15.dp)
                    ) {
                        val taskCompleted = task.isCompleted || (task.completed ?: false)
                        var isChecked by remember { mutableStateOf(taskCompleted) }

                        LaunchedEffect(key1 = task.id, key2 = task.isCompleted, key3 = task.completed) {
                            val newStatus = task.isCompleted || (task.completed ?: false)
                            if (isChecked != newStatus) {
                                isChecked = newStatus
                            }
                        }

                        Box(
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    isChecked = checked
                                    viewModel.updateTaskCompletion(task.id, checked)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = if (isSelected) Color.White else Color(0xFF7DAFCB),
                                    uncheckedColor = if (isSelected) Color.White.copy(alpha = 0.7f) else Color(0xFF7DAFCB).copy(alpha = 0.7f),
                                    checkmarkColor = if (isSelected) cardColor else Color.White
                                )
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = task.judulTugas,
                                color = cardTextColor,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
                            )

                            // Status indicator with pill shape
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isChecked) completedStatusColor else pendingStatusColor)
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Text(
                                    text = if (isChecked) "Selesai" else "Belum selesai",
                                    color = if (isChecked) completedStatusColor else pendingStatusColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp, start = 60.dp, end = 15.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Subject,
                            contentDescription = "Mata Kuliah",
                            modifier = Modifier.size(24.dp),
                            tint = iconTint
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = task.matkul,
                            color = cardTextColor,
                            fontSize = 19.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp, bottom = 15.dp, start = 60.dp, end = 15.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = "Tanggal",
                            modifier = Modifier.size(24.dp),
                            tint = iconTint
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(task.tanggal.toDate()),
                            color = cardTextColor,
                            fontSize = 19.sp
                        )
                    }

                    Spacer(modifier = Modifier
                        .height(1.dp)
                        .fillMaxWidth()
                        .padding(start = 23.dp, end = 23.dp)
                        .background(Color(0xFF000000))
                    )
                }
            }
        }
    }
}