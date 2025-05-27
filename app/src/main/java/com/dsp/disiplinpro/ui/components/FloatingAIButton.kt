package com.dsp.disiplinpro.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.dsp.disiplinpro.R
import kotlin.math.roundToInt

@Composable
fun FloatingAIButton(
    navController: NavController,
    isUserLoggedIn: Boolean,
    isDarkMode: Boolean,
    currentRoute: String?
) {
    if (!isUserLoggedIn || currentRoute == "chatbot") {
        return
    }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val density = LocalDensity.current
    val screenWidthPx = with(density) { screenWidth.toPx() }
    val screenHeightPx = with(density) { screenHeight.toPx() }

    val buttonSize = 60.dp
    val buttonSizePx = with(density) { buttonSize.toPx() }
    val edgePadding = 16.dp
    val edgePaddingPx = with(density) { edgePadding.toPx() }

    var offsetX by remember { mutableStateOf(screenWidthPx - buttonSizePx - edgePaddingPx) }
    var offsetY by remember { mutableStateOf(screenHeightPx - buttonSizePx - edgePaddingPx * 5) }

    var isDragging by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.2f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isDragging) 0.7f else 1.0f,
        label = "alpha"
    )

    var rotation by remember { mutableStateOf(0f) }
    val rotationAnim by animateFloatAsState(
        targetValue = rotation,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "rotation"
    )

    var isHovered by remember { mutableStateOf(false) }

    LaunchedEffect(isDragging) {
        if (!isDragging) {
            if (offsetX < screenWidthPx / 2) {
                offsetX = edgePaddingPx
            } else {
                offsetX = screenWidthPx - buttonSizePx - edgePaddingPx
            }

            rotation += 360f
        }
    }

    val gradientColors = if (isDarkMode) {
        listOf(
            Color(0xFF1A73E8),
            Color(0xFF4285F4),
            Color(0xFF2176FF)
        )
    } else {
        listOf(
            Color(0xFF4285F4),
            Color(0xFF65A7FF),
            Color(0xFF1A73E8)
        )
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .shadow(elevation = 8.dp, shape = CircleShape, spotColor = Color(0xFF2196F3))
            .size(buttonSize * scale)
            .alpha(alpha)
            .clip(CircleShape)
            .background(Brush.radialGradient(gradientColors))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = {
                        isDragging = false
                        isHovered = true
                    },
                    onDragCancel = { isDragging = false },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y

                        offsetX = offsetX.coerceIn(edgePaddingPx, screenWidthPx - buttonSizePx - edgePaddingPx)
                        offsetY = offsetY.coerceIn(edgePaddingPx, screenHeightPx - buttonSizePx - edgePaddingPx)
                    }
                )
            }
            .zIndex(10f)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isHovered) {
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(buttonSize * 1.2f * scale)
                        .align(Alignment.Center)
                ) {}
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_ai_assistant),
                contentDescription = "AI Asisten",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .rotate(rotationAnim)
                    .scale(if (isHovered) 1.1f else 1f)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        navController.navigate("chatbot")
                        isHovered = false
                        rotation += 360f
                    }
            )

            LaunchedEffect(isHovered) {
                if (isHovered) {
                    kotlinx.coroutines.delay(2000)
                    isHovered = false
                }
            }
        }
    }
}