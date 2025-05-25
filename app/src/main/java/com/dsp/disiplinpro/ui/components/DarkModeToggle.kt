package com.dsp.disiplinpro.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DarkModeToggle(
    isDarkMode: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val thumbPosition = animateDpAsState(
        targetValue = if (isDarkMode) 22.dp else 2.dp,
        animationSpec = tween(durationMillis = 300),
        label = "thumbPosition"
    )

    val backgroundTint = if (isDarkMode) {
        Color(0xFF2C2C2C)
    } else {
        Color(0xFFE0E0E0)
    }

    val thumbTint = if (isDarkMode) {
        Color(0xFF5A8CA8) // Dark mode primary
    } else {
        Color(0xFF7DAFCB) // Light mode primary
    }

    Box(
        modifier = modifier
            .width(50.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(backgroundTint)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbPosition.value)
                .size(26.dp)
                .clip(CircleShape)
                .background(thumbTint),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = if (isDarkMode) "Dark Mode" else "Light Mode",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Preview
@Composable
fun DarkModeTogglePreview() {
    Row {
        DarkModeToggle(
            isDarkMode = false,
            onToggle = { }
        )

        Spacer(modifier = Modifier.width(16.dp))

        DarkModeToggle(
            isDarkMode = true,
            onToggle = { }
        )
    }
}