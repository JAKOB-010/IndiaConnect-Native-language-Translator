package com.example.indiaconnect.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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

class VoiceViewModel : ViewModel() {
    var transcriptText by mutableStateOf("")
    var translatedText by mutableStateOf("")
    var isListening by mutableStateOf(false)
    var isTranslating by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // Translation parameters
    var sourceLanguage by mutableStateOf(languageList[0])
    var targetLanguage by mutableStateOf(languageList[1])
    
    // UI Shell language
    var appLanguage by mutableStateOf(languageList[0])
    
    var isPickerOpen by mutableStateOf(false)
    var pickerType by mutableStateOf("SOURCE")

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null

    fun initializeServices(context: Context) {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    errorMessage = null
                    isListening = true
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    isListening = false
                }
                override fun onError(error: Int) {
                    isListening = false
                    errorMessage = "Speech Error: $error"
                }
                override fun onResults(results: Bundle?) {
                    val data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    transcriptText = data?.get(0) ?: ""
                    translateText()
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val data = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    transcriptText = data?.get(0) ?: ""
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
        if (tts == null) {
            tts = TextToSpeech(context) { status ->
                if (status != TextToSpeech.SUCCESS) {
                    errorMessage = "TTS Initialization failed"
                }
            }
        }
    }

    fun startListening(context: Context) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, sourceLanguage.code)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
    }

    fun translateText() {
        if (transcriptText.isBlank()) return
        
        viewModelScope.launch {
            isTranslating = true
            errorMessage = null
            try {
                val rawResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.translate(
                        sourceLanguage = sourceLanguage.code,
                        targetLanguage = targetLanguage.code,
                        query = transcriptText
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
                isTranslating = false
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

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
    }
}

@Composable
fun VoiceTranslationView(
    appLanguageCode: String = "English",
    viewModel: VoiceViewModel = viewModel()
) {
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        viewModel.initializeServices(context)
    }

    LaunchedEffect(appLanguageCode) {
        viewModel.appLanguage = languageList.find { it.englishName == appLanguageCode } ?: languageList[0]
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.startListening(context)
            } else {
                viewModel.errorMessage = "Audio Permission Denied"
            }
        }
    )

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
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = {
                    val temp = viewModel.sourceLanguage
                    viewModel.sourceLanguage = viewModel.targetLanguage
                    viewModel.targetLanguage = temp
                }, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Icon(Icons.Default.Sync, contentDescription = "Swap", tint = MintGreen)
                }

                LanguageSelector(
                    languageName = viewModel.targetLanguage.englishName,
                    onClick = {
                        viewModel.pickerType = "TARGET"
                        viewModel.isPickerOpen = true
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transcript Box (Box 1)
            TranslationBox(
                text = viewModel.transcriptText,
                placeholder = TranslationMap.getStrings(viewModel.appLanguage.englishName).voiceInstruction,
                onCopy = { copyToClipboard(context, viewModel.transcriptText) },
                onListen = { viewModel.speak(viewModel.transcriptText, viewModel.sourceLanguage.code) },
                onClear = { viewModel.transcriptText = "" },
                isLoading = viewModel.isListening,
                errorMessage = viewModel.errorMessage,
                modifier = Modifier.fillMaxWidth().weight(1f),
                isReadOnly = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Translation Box (Box 2)
            TranslationBox(
                text = viewModel.translatedText,
                placeholder = viewModel.appLanguage.outputPlaceholder,
                onCopy = { copyToClipboard(context, viewModel.translatedText) },
                onListen = { viewModel.speak(viewModel.translatedText, viewModel.targetLanguage.code) },
                onClear = { viewModel.translatedText = "" },
                isLoading = viewModel.isTranslating,
                modifier = Modifier.fillMaxWidth().weight(1f),
                isReadOnly = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = {
                        if (viewModel.isListening) {
                            viewModel.stopListening()
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    containerColor = if (viewModel.isListening) Color.Red else MintGreen,
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = if (viewModel.isListening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Mic",
                        tint = if (viewModel.isListening) Color.White else Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
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
