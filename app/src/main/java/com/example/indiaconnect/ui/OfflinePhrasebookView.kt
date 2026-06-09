package com.example.indiaconnect.ui

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.indiaconnect.ui.theme.CardSurface
import com.example.indiaconnect.ui.theme.DarkBackground
import com.example.indiaconnect.ui.theme.MintGreen
import com.example.indiaconnect.ui.theme.TextPrimary
import com.example.indiaconnect.ui.theme.TextSecondary
import java.util.*

@Composable
fun OfflinePhrasebookView(appLanguageCode: String) {
    var selectedLanguage by remember { mutableStateOf<LanguageItem?>(null) }

    if (selectedLanguage == null) {
        OfflineLanguageListView(onLanguageSelected = { selectedLanguage = it })
    } else {
        PhrasebookDetailView(
            appLanguageName = appLanguageCode,
            selectedLanguage = selectedLanguage!!,
            onBack = { selectedLanguage = null }
        )
    }
}

@Composable
fun OfflineLanguageListView(onLanguageSelected: (LanguageItem) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "Offline Phrasebook",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(languageList, key = { it.code }) { lang ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clickable { onLanguageSelected(lang) },
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = lang.englishName, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text(text = lang.nativeName, color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PhrasebookDetailView(appLanguageName: String, selectedLanguage: LanguageItem, onBack: () -> Unit) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    
    // Dynamically fetch phrases based on app language and selected target language
    val phrases = remember(appLanguageName, selectedLanguage) { 
        PhrasebookRepository.getPhrasesForLanguage(appLanguageName, selectedLanguage.englishName) 
    }

    DisposableEffect(Unit) {
        val ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Initialized successfully
            }
        }
        tts = ttsInstance
        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            // Dynamic title: [Selected Language Name] [Localized "Phrases"]
            Text(
                text = "${selectedLanguage.englishName} ${strings.phrasesLabel}",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(phrases, key = { it.source + it.translated }) { phrase ->
                PhraseItem(
                    phrase = phrase,
                    onListen = {
                        tts?.let {
                            it.language = Locale(selectedLanguage.code)
                            it.speak(phrase.translated, TextToSpeech.QUEUE_FLUSH, null, null)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PhraseItem(phrase: Phrase, onListen: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Source text is now dynamic (based on app language)
                Text(text = phrase.source, color = TextSecondary, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = phrase.translated, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
            IconButton(onClick = onListen) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Listen", tint = MintGreen)
            }
        }
    }
}
