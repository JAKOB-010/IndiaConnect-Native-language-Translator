package com.example.indiaconnect.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.indiaconnect.ui.theme.DarkBackground
import com.example.indiaconnect.ui.theme.MintGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.util.*

// --- ViewModel ---
class TranslationViewModel : ViewModel() {
    var inputText by mutableStateOf("")
    var translatedText by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    
    // Translation parameters
    var sourceLanguage by mutableStateOf(languageList[0])
    var targetLanguage by mutableStateOf(languageList[1])
    
    // UI Shell language
    var appLanguage by mutableStateOf(languageList[0])
    
    var isPickerOpen by mutableStateOf(false)
    var pickerType by mutableStateOf("SOURCE")

    private var tts: TextToSpeech? = null

    fun initializeTTS(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context) { status ->
                if (status != TextToSpeech.SUCCESS) {
                    errorMessage = "TTS Initialization failed"
                }
            }
        }
    }

    fun speak(text: String, languageCode: String) {
        if (text.isBlank()) return
        tts?.let { engine ->
            engine.language = Locale(languageCode)
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun translateText() {
        if (inputText.isBlank()) return
        
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val rawResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.translate(
                        sourceLanguage = sourceLanguage.code,
                        targetLanguage = targetLanguage.code,
                        query = inputText
                    )
                }
                
                val jsonArray = JSONArray(rawResponse)
                val translationParts = jsonArray.getJSONArray(0)
                val result = StringBuilder()
                for (i in 0 until translationParts.length()) {
                    val part = translationParts.getJSONArray(i)
                    if (!part.isNull(0)) {
                        result.append(part.getString(0))
                    }
                }
                translatedText = result.toString()
                
            } catch (e: Exception) {
                errorMessage = "Translation Error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }
}

// --- UI ---
@Composable
fun TextTranslationView(
    appLanguageCode: String = "English",
    viewModel: TranslationViewModel = viewModel()
) {
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        viewModel.initializeTTS(context)
    }

    LaunchedEffect(appLanguageCode) {
        viewModel.appLanguage = languageList.find { it.englishName == appLanguageCode } ?: languageList[0]
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LanguageSelector(
                    languageName = viewModel.sourceLanguage.englishName,
                    onClick = {
                        viewModel.pickerType = "SOURCE"
                        viewModel.isPickerOpen = true
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !viewModel.isLoading
                )

                IconButton(
                    onClick = {
                        val temp = viewModel.sourceLanguage
                        viewModel.sourceLanguage = viewModel.targetLanguage
                        viewModel.targetLanguage = temp
                    }, 
                    modifier = Modifier.padding(horizontal = 8.dp),
                    enabled = !viewModel.isLoading
                ) {
                    Icon(Icons.Default.Sync, contentDescription = "Swap", tint = if (viewModel.isLoading) Color.Gray else MintGreen)
                }

                LanguageSelector(
                    languageName = viewModel.targetLanguage.englishName,
                    onClick = {
                        viewModel.pickerType = "TARGET"
                        viewModel.isPickerOpen = true
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !viewModel.isLoading
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input Box
            TranslationBox(
                text = viewModel.inputText,
                onValueChange = { if (it.length <= 2000) viewModel.inputText = it },
                placeholder = viewModel.appLanguage.inputPlaceholder,
                charLimit = 2000,
                onCopy = { copyToClipboard(context, viewModel.inputText) },
                onListen = { viewModel.speak(viewModel.inputText, viewModel.sourceLanguage.code) },
                onClear = { viewModel.inputText = "" },
                modifier = Modifier.fillMaxWidth().height(200.dp),
                isReadOnly = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Translate Button
            Button(
                onClick = { viewModel.translateText() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = viewModel.appLanguage.translateBtnText, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Output Box
            TranslationBox(
                text = viewModel.translatedText,
                placeholder = viewModel.appLanguage.outputPlaceholder,
                onCopy = { copyToClipboard(context, viewModel.translatedText) },
                onListen = { viewModel.speak(viewModel.translatedText, viewModel.targetLanguage.code) },
                onClear = { viewModel.translatedText = "" },
                isLoading = viewModel.isLoading,
                errorMessage = viewModel.errorMessage,
                modifier = Modifier.fillMaxWidth().weight(1f),
                isReadOnly = true
            )
        }

        if (viewModel.isPickerOpen) {
            LanguagePickerOverlay(
                onLanguageSelected = { lang ->
                    if (viewModel.pickerType == "SOURCE") {
                        viewModel.sourceLanguage = lang
                    } else {
                        viewModel.targetLanguage = lang
                    }
                    viewModel.isPickerOpen = false
                },
                onDismiss = { viewModel.isPickerOpen = false }
            )
        }
    }
}
