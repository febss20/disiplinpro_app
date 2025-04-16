package com.example.disiplinpro.ui.task

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.disiplinpro.data.model.Task
import com.example.disiplinpro.viewmodel.task.TaskViewModel
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TaskItem(
    tasks: List<Task>,
    viewModel: TaskViewModel,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 30.dp, end = 30.dp, bottom = 20.dp)
            .border(1.dp, Color(0x4D333333), RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF7DAFCB) else Color(0x332196F3)
        )
    ) {
        Column(modifier = Modifier.padding(top = 5.dp, bottom = 28.dp)) {
            tasks.forEach { task ->
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 12.dp, start = 15.dp)
                    ) {
                        val taskCompleted = task.isCompleted || (task.completed ?: false)
                        var isChecked by remember { mutableStateOf(taskCompleted) }

                        LaunchedEffect(key1 = task.id, key2 = task.isCompleted, key3 = task.completed) {
                            isChecked = task.isCompleted || (task.completed ?: false)
                        }

                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                isChecked = checked
                                viewModel.updateTaskCompletion(task.id, checked)
                            }
                        )
                        Text(
                            text = task.judulTugas,
                            color = Color(0xFF333333),
                            fontSize = 19.sp
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 5.dp, start = 60.dp)
                    ) {
                        CoilImage(
                            imageModel = { "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/T7pdvlFwTn/xhhozfad.png" },
                            modifier = Modifier.width(33.dp).padding(end = 8.dp),
                            imageOptions = ImageOptions(contentScale = ContentScale.Crop)
                        )
                        Text(
                            text = task.matkul,
                            color = Color(0xFF333333),
                            fontSize = 19.sp
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 12.dp, bottom = 15.dp, start = 61.dp)
                    ) {
                        CoilImage(
                            imageModel = { "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/T7pdvlFwTn/egtmj8ve.png" },
                            modifier = Modifier.width(30.dp).padding(end = 8.dp),
                            imageOptions = ImageOptions(contentScale = ContentScale.Crop)
                        )
                        Text(
                            text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(task.tanggal.toDate()),
                            color = Color(0xFF333333),
                            fontSize = 19.sp
                        )
                    }
                    Spacer(modifier = Modifier
                        .height(2.dp)
                        .fillMaxWidth()
                        .padding(start = 23.dp, end = 23.dp)
                        .background(Color(0xFF000000))
                    )
                }
            }
        }
    }
}