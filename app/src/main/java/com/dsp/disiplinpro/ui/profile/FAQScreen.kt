package com.dsp.disiplinpro.ui.profile

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
import androidx.compose.material3.HorizontalDivider
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
import com.dsp.disiplinpro.viewmodel.profile.FAQCategory
import com.dsp.disiplinpro.viewmodel.profile.FAQViewModel
import com.dsp.disiplinpro.viewmodel.theme.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQScreen(
    navController: NavController,
    viewModel: FAQViewModel = viewModel(),
    themeViewModel: ThemeViewModel
) {
    val context = LocalContext.current

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val filteredFaqItems by viewModel.filteredFaqItems.collectAsState()
    val isLoading by viewModel.isLoading

    val categories = remember { listOf(FAQCategory.ALL, FAQCategory.ACCOUNT, FAQCategory.TASK, FAQCategory.OTHER) }

    val isDarkMode by themeViewModel.isDarkMode.collectAsState()
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFFAF3E0)
    val cardBackgroundColor = if (isDarkMode) Color(0xFF2C2C2C) else Color(0x332196F3)
    val primaryTextColor = if (isDarkMode) Color.White else Color(0xFF333333)
    val secondaryTextColor = if (isDarkMode) Color(0xFFBBBBBB) else Color(0xFF666666)
    val accentColor = Color(0xFF7DAFCB)
    val accentColorLight = if (isDarkMode) Color(0x507DAFCB) else Color(0x807DAFCB)
    val dividerColor = if (isDarkMode) Color(0xFF444444) else Color(0xFFEEEEEE)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = primaryTextColor
                )
            }
            Text(
                "Pertanyaan Umum (FAQ)",
                color = primaryTextColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.Center),
                color = accentColor
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 120.dp, bottom = 24.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Cari pertanyaan...", color = secondaryTextColor) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = accentColor
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = accentColorLight,
                    cursorColor = accentColor,
                    focusedTextColor = primaryTextColor,
                    unfocusedTextColor = primaryTextColor
                ),
                singleLine = true
            )

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
                        onClick = { viewModel.updateSelectedCategory(category) },
                        selectedColor = accentColor,
                        unselectedColor = accentColorLight,
                        selectedTextColor = Color.White,
                        unselectedTextColor = primaryTextColor
                    )
                }
            }

            if (filteredFaqItems.isEmpty() && !isLoading) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Tidak ada pertanyaan yang cocok dengan pencarian Anda",
                        color = secondaryTextColor,
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
                    val groupedFaqs = filteredFaqItems.groupBy { it.category }

                    if (groupedFaqs.containsKey(FAQCategory.ACCOUNT)) {
                        Text(
                            text = "Akun & Pengaturan",
                            color = accentColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                        )

                        groupedFaqs[FAQCategory.ACCOUNT]?.forEach { faqItem ->
                            FAQItem(
                                question = faqItem.question,
                                answer = faqItem.answer,
                                cardBackgroundColor = cardBackgroundColor,
                                primaryTextColor = primaryTextColor,
                                secondaryTextColor = secondaryTextColor,
                                accentColor = accentColor,
                                dividerColor = dividerColor
                            )
                        }
                    }

                    if (groupedFaqs.containsKey(FAQCategory.TASK)) {
                        Text(
                            text = "Tugas & Jadwal",
                            color = accentColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp, top = if (groupedFaqs.containsKey(FAQCategory.ACCOUNT)) 16.dp else 8.dp)
                        )

                        groupedFaqs[FAQCategory.TASK]?.forEach { faqItem ->
                            FAQItem(
                                question = faqItem.question,
                                answer = faqItem.answer,
                                cardBackgroundColor = cardBackgroundColor,
                                primaryTextColor = primaryTextColor,
                                secondaryTextColor = secondaryTextColor,
                                accentColor = accentColor,
                                dividerColor = dividerColor
                            )
                        }
                    }

                    if (groupedFaqs.containsKey(FAQCategory.OTHER)) {
                        Text(
                            text = "Lainnya",
                            color = accentColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp, top = if (groupedFaqs.isNotEmpty()) 16.dp else 8.dp)
                        )

                        groupedFaqs[FAQCategory.OTHER]?.forEach { faqItem ->
                            FAQItem(
                                question = faqItem.question,
                                answer = faqItem.answer,
                                cardBackgroundColor = cardBackgroundColor,
                                primaryTextColor = primaryTextColor,
                                secondaryTextColor = secondaryTextColor,
                                accentColor = accentColor,
                                dividerColor = dividerColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Button(
                onClick = { viewModel.contactSupport() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
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
    onClick: () -> Unit,
    selectedColor: Color,
    unselectedColor: Color,
    selectedTextColor: Color,
    unselectedTextColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) selectedColor else unselectedColor
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) selectedTextColor else unselectedTextColor,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun FAQItem(
    question: String,
    answer: String,
    cardBackgroundColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    accentColor: Color,
    dividerColor: Color
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
            containerColor = cardBackgroundColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = question,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = primaryTextColor,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = accentColor,
                        modifier = Modifier.rotate(rotationState)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 1.dp,
                        color = dividerColor
                    )
                    Text(
                        text = answer,
                        fontSize = 14.sp,
                        color = secondaryTextColor,
                        textAlign = TextAlign.Justify,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}