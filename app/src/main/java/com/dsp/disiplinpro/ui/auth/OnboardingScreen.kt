package com.dsp.disiplinpro.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.navigation.NavController
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import androidx.compose.ui.layout.ContentScale
import com.dsp.disiplinpro.data.preferences.ThemePreferences
import com.dsp.disiplinpro.ui.theme.DarkBackground
import com.dsp.disiplinpro.ui.theme.DarkPrimaryBlue
import com.dsp.disiplinpro.ui.theme.DarkTextLight
import com.dsp.disiplinpro.R

@Composable
fun OnboardingScreen(navController: NavController) {
    val context = LocalContext.current
    val themePreferences = ThemePreferences(context)
    val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = false)

    val backgroundColor = if (isDarkMode) DarkBackground else Color(0xFFFAF3E0)
    val textColor = if (isDarkMode) DarkTextLight else Color(0xFF333333)
    val primaryColor = if (isDarkMode) DarkPrimaryBlue else Color(0xFF7DAFCB)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = if (isDarkMode) Color(0xFF121212) else Color(0xFFFFFFFF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(color = backgroundColor)
                .padding(horizontal = 26.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "DisiplinPro",
                color = primaryColor,
                fontSize = 33.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 50.dp, bottom = 12.dp, start = 2.dp, end = 2.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(top = 53.dp)
                    .shadow(elevation = 4.dp, spotColor = Color(0x40000000))
                    .padding(top = 10.dp, bottom = 14.dp, start = 2.dp, end = 2.dp)
            ) {
                Box(
                    modifier = Modifier.padding(1.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CoilImage(
                            imageModel = { R.drawable.onboarding_image },
                            imageOptions = ImageOptions(contentScale = ContentScale.Fit),
                            modifier = Modifier
                                .width(253.dp)
                                .height(260.dp),
                        )
                    }
                }
            }

            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = textColor,
                            fontSize = 33.sp,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Sederhanakan, Atur, dan")
                    }
                    append("\n")
                    withStyle(
                        style = SpanStyle(
                            color = textColor,
                            fontSize = 33.sp,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Taklukan ")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = primaryColor,
                            fontSize = 33.sp,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Hari Anda")
                    }
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 43.dp),
                lineHeight = 45.sp
            )

            Text(
                "Kendalikan tugas Anda dan capai tujuan Anda.",
                color = textColor,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 43.dp)
                    .width(281.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { navController.navigate("login") },
                    modifier = Modifier
                        .padding(top = 60.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    ),
                    contentPadding = PaddingValues(vertical = 15.dp)
                ) {
                    Text(
                        "Ayo Mulai",
                        color = Color(0xFFFFFFFF),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}