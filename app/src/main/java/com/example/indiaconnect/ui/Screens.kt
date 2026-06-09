package com.example.indiaconnect.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.example.indiaconnect.R
import com.example.indiaconnect.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onAnimationFinish: () -> Unit) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.splash_anim))
    val progress by animateLottieCompositionAsState(composition)

    LaunchedEffect(Unit) {
        delay(3000)
        onAnimationFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(250.dp)
        )
    }
}

@Composable
fun ChooseLanguageScreen(onLanguageSelected: (String) -> Unit) {
    val strings = LocalAppStrings.current
    val languages = listOf(
        "English" to "English", "Assamese" to "অসমীয়া", "Bengali" to "বাংলা",
        "Bodo" to "बड़ो", "Dogri" to "डोगरी", "Gujarati" to "ગુજરાતી",
        "Hindi" to "हिन्दी", "Kannada" to "ಕನ್ನಡ", "Kashmiri" to "كٲशুর / कॉशুর",
        "Konkani" to "कोंকणी", "Maithili" to "मैथिली", "Malayalam" to "മലയാളം",
        "Manipuri" to "মেითეილი / ಮಣಿಪುರಿ", "Marathi" to "मराठी", "Nepali" to "नेपाली",
        "Odia" to "ଓଡ଼ିଆ", "Punjabi" to "ਪੰਜਾਬੀ", "Sanskrit" to "संस्कृतम्",
        "Santali" to "ᱚᱞ ᱪᱤᱠᱤ", "Sindhi" to "سنڌي / सिन्धी", "Tamil" to "தமிழ்",
        "Telugu" to "తెలుగు", "Urdu" to "اُردُو"
    )

    var selectedLanguage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Section: Logo and Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardSurface),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = strings.chooseLanguage,
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Background Image Section
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Background",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Language List Section
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(languages, key = { it.first }) { (english, native) ->
                LanguageItem(
                    englishName = english,
                    nativeName = native,
                    isSelected = selectedLanguage == english,
                    onClick = { selectedLanguage = english }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Select Button
        Button(
            onClick = { selectedLanguage?.let { onLanguageSelected(it) } },
            colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(8.dp),
            enabled = selectedLanguage != null
        ) {
            Text(text = strings.select, color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LanguageItem(
    englishName: String,
    nativeName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CardSurface else Color.Transparent)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = englishName, color = TextPrimary, fontSize = 16.sp)
            Text(text = nativeName, color = TextSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
fun MainDashboardScreen(language: String) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val strings = LocalAppStrings.current
    
    val tabs = listOf(
        TabItem(strings.textTab, Icons.Default.TextFields),
        TabItem(strings.voiceTab, Icons.Default.Mic),
        TabItem(strings.liveTab, Icons.Default.Forum),
        TabItem(strings.scanTab, Icons.Default.CameraAlt),
        TabItem(strings.offlineTab, Icons.Default.MenuBook)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = CardSurface,
                contentColor = TextSecondary
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.2f else 1.0f,
                        animationSpec = tween(durationMillis = 300),
                        label = "IconScale"
                    )

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.scale(scale),
                                tint = if (isSelected) MintGreen else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                color = if (isSelected) MintGreen else TextSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> TextTranslationView(appLanguageCode = language)
                1 -> VoiceTranslationView(appLanguageCode = language)
                2 -> LiveConversationView(appLanguageCode = language)
                3 -> ScanTranslationView(appLanguageCode = language)
                4 -> OfflinePhrasebookView(appLanguageCode = language)
            }
        }
    }
}

data class TabItem(val title: String, val icon: ImageVector)

@Preview
@Composable
fun ChooseLanguageScreenPreview() {
    IndiaConnectTheme {
        CompositionLocalProvider(LocalAppStrings provides TranslationMap.getStrings("English")) {
            ChooseLanguageScreen(onLanguageSelected = {})
        }
    }
}
