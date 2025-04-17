package com.example.disiplinpro.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQScreen(
    navController: NavController
) {
    var searchQuery by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF3E0))
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF333333)
                )
            }
            Text(
                "Pertanyaan Umum (FAQ)",
                color = Color(0xFF333333),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 80.dp, bottom = 24.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari pertanyaan...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF7DAFCB)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF7DAFCB),
                    unfocusedBorderColor = Color(0x807DAFCB),
                    cursorColor = Color(0xFF7DAFCB)
                ),
                singleLine = true
            )

            // FAQ Categories
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CategoryChip(
                    text = "Semua",
                    selected = true,
                    onClick = { /* Filter by All */ }
                )
                CategoryChip(
                    text = "Akun",
                    selected = false,
                    onClick = { /* Filter by Account */ }
                )
                CategoryChip(
                    text = "Tugas",
                    selected = false,
                    onClick = { /* Filter by Tasks */ }
                )
                CategoryChip(
                    text = "Lainnya",
                    selected = false,
                    onClick = { /* Filter by Others */ }
                )
            }

            // FAQ Items in a ScrollView
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Account Section
                Text(
                    text = "Akun & Pengaturan",
                    color = Color(0xFF7DAFCB),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                )

                FAQItem(
                    question = "Bagaimana cara mengubah password akun saya?",
                    answer = "Untuk mengubah password, ikuti langkah berikut:\n\n1. Buka halaman Profil\n2. Pilih 'Edit Akun'\n3. Masukkan password lama Anda\n4. Masukkan password baru yang diinginkan\n5. Tekan 'Simpan Perubahan'"
                )

                FAQItem(
                    question = "Apakah saya bisa mengubah nama pengguna?",
                    answer = "Ya, Anda dapat mengubah nama pengguna kapan saja melalui halaman 'Edit Akun' di profil Anda. Perubahan akan langsung terlihat setelah disimpan."
                )

                FAQItem(
                    question = "Bagaimana cara logout dari aplikasi?",
                    answer = "Untuk logout, buka halaman Profil dan gulir ke bawah. Anda akan menemukan tombol 'Logout' di bagian bawah halaman."
                )

                // Task Section
                Text(
                    text = "Tugas & Jadwal",
                    color = Color(0xFF7DAFCB),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                )

                FAQItem(
                    question = "Bagaimana cara menambahkan tugas baru?",
                    answer = "Untuk menambahkan tugas baru:\n\n1. Klik tombol '+' pada halaman utama\n2. Isi informasi tugas seperti judul, mata kuliah, tanggal, dll\n3. Tekan 'Simpan' untuk menyimpan tugas baru"
                )

                FAQItem(
                    question = "Bagaimana cara menandai tugas sudah selesai?",
                    answer = "Cukup klik kotak centang di samping tugas untuk menandainya sebagai selesai. Anda juga dapat membatalkan tanda ini dengan mengklik kembali kotak tersebut."
                )

                FAQItem(
                    question = "Apakah saya bisa menghapus tugas yang sudah dibuat?",
                    answer = "Ya, untuk menghapus tugas, cukup tekan lama pada tugas yang ingin dihapus, kemudian pilih opsi 'Hapus' yang muncul."
                )

                // Other Section
                Text(
                    text = "Lainnya",
                    color = Color(0xFF7DAFCB),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                )

                FAQItem(
                    question = "Apakah notifikasi dapat disesuaikan?",
                    answer = "Ya, Anda dapat menyesuaikan notifikasi di halaman 'Keamanan dan Privasi'. Anda dapat mengaktifkan atau menonaktifkan notifikasi dan mengatur jenis notifikasi yang ingin diterima."
                )

                FAQItem(
                    question = "Bagaimana cara melaporkan bug atau masalah aplikasi?",
                    answer = "Untuk melaporkan bug atau masalah, silakan kirim email ke support@disiplinpro.id dengan detail masalah yang Anda alami. Tim kami akan merespons secepat mungkin."
                )

                FAQItem(
                    question = "Di mana saya bisa mendapatkan bantuan lebih lanjut?",
                    answer = "Anda dapat menghubungi tim dukungan kami melalui email support@disiplinpro.id atau kunjungi situs web kami di www.disiplinpro.id untuk informasi bantuan lebih lanjut."
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Contact Support Button
            Button(
                onClick = { /* TODO: Implement contact support functionality */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7DAFCB)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Hubungi Dukungan", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun CategoryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) Color(0xFF7DAFCB) else Color(0x337DAFCB)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color(0xFF333333),
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun FAQItem(
    question: String,
    answer: String
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Question Row with arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = question,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF333333),
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = Color(0xFF7DAFCB),
                        modifier = Modifier.rotate(rotationState)
                    )
                }
            }

            // Answer
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Divider(
                        color = Color(0xFFEEEEEE),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = answer,
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Justify,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}