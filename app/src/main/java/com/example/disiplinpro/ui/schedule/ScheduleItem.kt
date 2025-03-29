package com.example.disiplinpro.ui.schedule

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.disiplinpro.R
import com.example.disiplinpro.data.model.Schedule
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ScheduleItem(schedules: List<Schedule>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 31.dp, end = 31.dp, bottom = 20.dp)
            .border(1.dp, Color(0x4D333333), RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x332196F3), // Warna background yang diinginkan
        ),
    ) {
        Column(modifier = Modifier.padding(top = 14.dp, bottom = 22.dp)) {
            Text(
                text = schedules.first().hari, // Hari diambil dari jadwal pertama
                color = Color(0xFF333333),
                fontSize = 20.sp,
                modifier = Modifier.padding(top = 1.dp, bottom = 12.dp, start = 23.dp)
            )
            Spacer(modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .padding(start = 23.dp, end = 23.dp)
                .background(Color(0xFF000000)))
            schedules.forEach { schedule ->
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 12.dp, start = 23.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ellipse706),
                            contentDescription = "ellipse",
                            modifier = Modifier.width(20.dp).padding(end = 8.dp),
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            text = schedule.matkul,
                            color = Color(0xFF333333),
                            fontSize = 19.sp,
                            modifier = Modifier.padding(start = 5.dp),
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 45.dp, top = 9.dp)
                    ) {
                        CoilImage(
                            imageModel = { "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/T7pdvlFwTn/4kklsjaa.png" },
                            modifier = Modifier.width(30.dp).padding(end = 8.dp),
                            imageOptions = ImageOptions(contentScale = ContentScale.Crop)
                        )
                        Text(
                            text = schedule.ruangan,
                            color = Color(0xFF333333),
                            fontSize = 19.sp
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 45.dp, top = 9.dp)
                    ) {
                        CoilImage(
                            imageModel = { "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/T7pdvlFwTn/h4d6k6v9.png" },
                            modifier = Modifier.width(30.dp).padding(end = 8.dp),
                            imageOptions = ImageOptions(contentScale = ContentScale.Crop)
                        )
                        Text(
                            text = "${SimpleDateFormat("HH:mm", Locale.getDefault()).format(schedule.waktuMulai.toDate())} - " +
                                    "${SimpleDateFormat("HH:mm", Locale.getDefault()).format(schedule.waktuSelesai.toDate())}",
                            color = Color(0xFF333333),
                            fontSize = 19.sp
                        )
                    }
                }
            }
        }
    }
}