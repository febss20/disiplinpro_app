package com.dsp.disiplinpro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsp.disiplinpro.R
import com.dsp.disiplinpro.data.model.ChatMessage
import com.dsp.disiplinpro.ui.theme.DarkPrimaryBlue
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    isDarkMode: Boolean
) {
    val isUserMessage = message is ChatMessage.UserMessage
    val isSystemMessage = message is ChatMessage.SystemMessage
    val isError = if (isSystemMessage) (message as ChatMessage.SystemMessage).isError else false
    val isLoading = if (message is ChatMessage.AiMessage) message.isLoading else false

    val messageAlignment = if (isUserMessage) Alignment.End else Alignment.Start
    val messageShape = if (isUserMessage) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    val messageColor = when {
        isSystemMessage && isError -> if (isDarkMode) Color(0xFF4F2020) else Color(0xFFFFDEDE)
        isUserMessage -> if (isDarkMode) DarkPrimaryBlue else Color(0xFF7DAFCB)
        else -> if (isDarkMode) Color(0xFF2C2C2C) else Color(0xFFEAEAEA)
    }

    val textColor = when {
        isSystemMessage && isError -> if (isDarkMode) Color(0xFFFF8080) else Color(0xFFB00020)
        isUserMessage -> Color.White
        else -> if (isDarkMode) Color.White else Color(0xFF212121)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = messageAlignment
    ) {
        if (isSystemMessage) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(vertical = 2.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = messageColor
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isError) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_warning),
                            contentDescription = "Error",
                            tint = textColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = message.content,
                        color = textColor,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                if (!isUserMessage) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(DarkPrimaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AI",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Card(
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .fillMaxWidth(0.75f),
                    shape = messageShape,
                    colors = CardDefaults.cardColors(
                        containerColor = messageColor
                    )
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier.padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = textColor,
                                strokeWidth = 2.dp
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = message.content,
                                color = textColor,
                                fontSize = 14.sp
                            )

                            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                            Text(
                                text = timeFormat.format(message.timestamp),
                                color = textColor.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .align(if (isUserMessage) Alignment.Start else Alignment.End)
                                    .padding(top = 4.dp)
                            )
                        }
                    }
                }

                if (isUserMessage) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF5D8AA8)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "U",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}