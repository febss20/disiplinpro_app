package com.dsp.disiplinpro.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dsp.disiplinpro.data.preferences.ThemePreferences
import com.dsp.disiplinpro.ui.components.ChatMessageItem
import com.dsp.disiplinpro.ui.theme.DarkBackground
import com.dsp.disiplinpro.ui.theme.DarkPrimaryBlue
import com.dsp.disiplinpro.viewmodel.chat.ChatbotViewModel
import com.dsp.disiplinpro.util.NavigationBarUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen(
    navController: NavController,
    viewModel: ChatbotViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val context = LocalContext.current
    val themePreferences = ThemePreferences(context)
    val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = false)

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val isGestureNavigation = NavigationBarUtils.isGestureNavigation(context)
    val bottomPadding = if (isGestureNavigation) 15.dp else 55.dp

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Chatbot Asisten",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.clearChat() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Hapus Chat"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDarkMode) Color(0xFF202020) else MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .padding(bottom = bottomPadding),
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(8.dp),
                color = if (isDarkMode) Color(0xFF202020) else Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { viewModel.updateInputText(it) },
                        placeholder = {
                            Text(
                                "Ketik pesan...",
                                color = if (isDarkMode) Color(0xFF8E8E8E) else Color(0xFF757575)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDarkMode) DarkPrimaryBlue else MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (isDarkMode) Color(0xFF404040) else Color(0xFFDDDDDD),
                            focusedTextColor = if (isDarkMode) Color.White else Color(0xFF212121),
                            unfocusedTextColor = if (isDarkMode) Color.White else Color(0xFF212121),
                            cursorColor = if (isDarkMode) DarkPrimaryBlue else MaterialTheme.colorScheme.primary
                        ),
                        maxLines = 4
                    )

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (inputText.isNotEmpty() && !isLoading) {
                                    if (isDarkMode) DarkPrimaryBlue else MaterialTheme.colorScheme.primary
                                } else {
                                    if (isDarkMode) Color(0xFF404040) else Color(0xFFDDDDDD)
                                }
                            )
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                if (inputText.isNotEmpty() && !isLoading) {
                                    viewModel.sendMessage()
                                }
                            },
                            enabled = inputText.isNotEmpty() && !isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Kirim",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = if (isDarkMode) DarkBackground else Color(0xFFF5F5F5)
        ) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Selamat datang di chatbot DisiplinPro!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else Color(0xFF212121)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Mulai bertanya tentang tugas, jadwal, atau fitur aplikasi.",
                        fontSize = 14.sp,
                        color = if (isDarkMode) Color(0xFFBBBBBB) else Color(0xFF757575),
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp),
                    state = listState,
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(messages) { message ->
                        ChatMessageItem(
                            message = message,
                            isDarkMode = isDarkMode
                        )
                    }
                }
            }
        }
    }
}