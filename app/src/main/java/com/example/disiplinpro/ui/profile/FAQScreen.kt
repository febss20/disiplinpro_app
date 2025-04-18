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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.disiplinpro.viewmodel.profile.FAQCategory
import com.example.disiplinpro.viewmodel.profile.FAQViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQScreen(
    navController: NavController,
    viewModel: FAQViewModel = viewModel()
) {
    val context = LocalContext.current

    // Collect states from viewModel
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val filteredFaqItems by viewModel.filteredFaqItems.collectAsState()
    val isLoading by viewModel.isLoading

    // State untuk kategori-kategori
    val categories = remember { listOf(FAQCategory.ALL, FAQCategory.ACCOUNT, FAQCategory.TASK, FAQCategory.OTHER) }

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

        // Loading indicator
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.Center),
                color = Color(0xFF7DAFCB)
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
                onValueChange = { viewModel.updateSearchQuery(it) },
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
                categories.forEach { category ->
                    CategoryChip(
                        text = category.toDisplayName(),
                        selected = category == selectedCategory,
                        onClick = { viewModel.updateSelectedCategory(category) }
                    )
                }
            }

            // FAQ Items in a ScrollView
            if (filteredFaqItems.isEmpty() && !isLoading) {
                // Tampilkan pesan jika tidak ada FAQ yang cocok dengan pencarian
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Tidak ada pertanyaan yang cocok dengan pencarian Anda",
                        color = Color(0xFF666666),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Group FAQs by category and show section titles
                    val groupedFaqs = filteredFaqItems.groupBy { it.category }

                    // Tampilkan FAQ kategori Akun jika ada
                    if (groupedFaqs.containsKey(FAQCategory.ACCOUNT)) {
                        Text(
                            text = "Akun & Pengaturan",
                            color = Color(0xFF7DAFCB),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                        )

                        groupedFaqs[FAQCategory.ACCOUNT]?.forEach { faqItem ->
                            FAQItem(
                                question = faqItem.question,
                                answer = faqItem.answer
                            )
                        }
                    }

                    // Tampilkan FAQ kategori Tugas jika ada
                    if (groupedFaqs.containsKey(FAQCategory.TASK)) {
                        Text(
                            text = "Tugas & Jadwal",
                            color = Color(0xFF7DAFCB),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp, top = if (groupedFaqs.containsKey(FAQCategory.ACCOUNT)) 16.dp else 8.dp)
                        )

                        groupedFaqs[FAQCategory.TASK]?.forEach { faqItem ->
                            FAQItem(
                                question = faqItem.question,
                                answer = faqItem.answer
                            )
                        }
                    }

                    // Tampilkan FAQ kategori Lainnya jika ada
                    if (groupedFaqs.containsKey(FAQCategory.OTHER)) {
                        Text(
                            text = "Lainnya",
                            color = Color(0xFF7DAFCB),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp, top = if (groupedFaqs.isNotEmpty()) 16.dp else 8.dp)
                        )

                        groupedFaqs[FAQCategory.OTHER]?.forEach { faqItem ->
                            FAQItem(
                                question = faqItem.question,
                                answer = faqItem.answer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Contact Support Button
            Button(
                onClick = { viewModel.contactSupport() },
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
            containerColor = Color(0x332196F3)
        ),
        shape = RoundedCornerShape(12.dp)
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