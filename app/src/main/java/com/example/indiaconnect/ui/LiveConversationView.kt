package com.example.indiaconnect.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.indiaconnect.ui.theme.CardSurface
import com.example.indiaconnect.ui.theme.DarkBackground
import com.example.indiaconnect.ui.theme.MintGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.util.*

enum class Speaker { PERSON_A, PERSON_B }

data class ChatMessage(
    val originalText: String,
    val translatedText: String,
    val speaker: Speaker
)

class LiveViewModel : ViewModel() {
    val conversationHistory = mutableStateListOf<ChatMessage>()
    var isListening by mutableStateOf(false)
    var activeSpeaker by mutableStateOf<Speaker?>(null)
    var errorMessage by mutableStateOf<String?>(null)
    
    // UI Notification event
    var uiEvent = mutableStateOf<String?>(null)

    // Translation parameters
    var sourceLanguage by mutableStateOf(languageList[0]) // Person A
    var targetLanguage by mutableStateOf(languageList[1]) // Person B
    
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
                    if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                        speechRecognizer?.cancel()
                        uiEvent.value = "Microphone busy, try again."
                    } else {
                        errorMessage = "Speech Error: $error"
                    }
                }
                override fun onResults(results: Bundle?) {
                    isListening = false
                    val data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = data?.get(0) ?: ""
                    if (text.isNotBlank()) {
                        processTranscription(text)
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {}
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

    fun startListening(speaker: Speaker) {
        // Fix 1: Aggressive Cancellation
        speechRecognizer?.cancel()
        
        activeSpeaker = speaker
        isListening = true // Set immediately for UI feedback
        
        val languageCode = if (speaker == Speaker.PERSON_A) sourceLanguage.code else targetLanguage.code
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
        }
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
    }

    private fun processTranscription(text: String) {
        val speaker = activeSpeaker ?: return
        viewModelScope.launch {
            try {
                val sourceCode = if (speaker == Speaker.PERSON_A) sourceLanguage.code else targetLanguage.code
                val targetCode = if (speaker == Speaker.PERSON_A) targetLanguage.code else sourceLanguage.code
                
                val rawResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.translate(
                        sourceLanguage = sourceCode,
                        targetLanguage = targetCode,
                        query = text
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
                val translated = result.toString()
                
                // Add to history
                conversationHistory.add(ChatMessage(text, translated, speaker))
                
                // Speak out loud in the OPPOSITE person's language
                speak(translated, targetCode)
                
            } catch (e: Exception) {
                errorMessage = "Translation Error: ${e.localizedMessage}"
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
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
    }
}

@Composable
fun LiveConversationView(
    appLanguageCode: String = "English",
    viewModel: LiveViewModel = viewModel()
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    
    LaunchedEffect(Unit) {
        viewModel.initializeServices(context)
    }

    LaunchedEffect(appLanguageCode) {
        viewModel.appLanguage = languageList.find { it.englishName == appLanguageCode } ?: languageList[0]
    }

    // Fix 3: Handle Toast events
    LaunchedEffect(viewModel.uiEvent.value) {
        viewModel.uiEvent.value?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.uiEvent.value = null
        }
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(viewModel.conversationHistory.size) {
        if (viewModel.conversationHistory.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.conversationHistory.size - 1)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.activeSpeaker?.let { viewModel.startListening(it) }
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
                    modifier = Modifier.weight(1f),
                    enabled = !viewModel.isListening
                )

                IconButton(
                    onClick = {
                        val temp = viewModel.sourceLanguage
                        viewModel.sourceLanguage = viewModel.targetLanguage
                        viewModel.targetLanguage = temp
                    }, 
                    modifier = Modifier.padding(horizontal = 8.dp),
                    enabled = !viewModel.isListening
                ) {
                    Icon(Icons.Default.Sync, contentDescription = "Swap", tint = if (viewModel.isListening) Color.Gray else MintGreen)
                }

                LanguageSelector(
                    languageName = viewModel.targetLanguage.englishName,
                    onClick = {
                        viewModel.pickerType = "TARGET"
                        viewModel.isPickerOpen = true
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !viewModel.isListening
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Chat Area
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(
                    items = viewModel.conversationHistory,
                    key = { it.originalText + it.speaker.name + System.identityHashCode(it) }
                ) { message ->
                    ChatBubble(
                        message = message,
                        onListen = {
                            val langCode = if (message.speaker == Speaker.PERSON_A)
                                viewModel.targetLanguage.code else viewModel.sourceLanguage.code
                            viewModel.speak(message.translatedText, langCode)
                        }
                    )
                }
            }

            if (viewModel.errorMessage != null) {
                Text(
                    text = viewModel.errorMessage!!,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp).align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fix 2: Dual Mic Bottom Panel with Disable logic
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MicButton(
                    label = viewModel.sourceLanguage.englishName,
                    isListening = viewModel.isListening && viewModel.activeSpeaker == Speaker.PERSON_A,
                    isDisabled = viewModel.isListening && viewModel.activeSpeaker == Speaker.PERSON_B,
                    onClick = {
                        if (viewModel.isListening && viewModel.activeSpeaker == Speaker.PERSON_A) {
                            viewModel.stopListening()
                        } else {
                            viewModel.activeSpeaker = Speaker.PERSON_A
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                MicButton(
                    label = viewModel.targetLanguage.englishName,
                    isListening = viewModel.isListening && viewModel.activeSpeaker == Speaker.PERSON_B,
                    isDisabled = viewModel.isListening && viewModel.activeSpeaker == Speaker.PERSON_A,
                    onClick = {
                        if (viewModel.isListening && viewModel.activeSpeaker == Speaker.PERSON_B) {
                            viewModel.stopListening()
                        } else {
                            viewModel.activeSpeaker = Speaker.PERSON_B
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
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

@Composable
fun ChatBubble(message: ChatMessage, onListen: () -> Unit) {
    val isA = message.speaker == Speaker.PERSON_A
    val alignment = if (isA) Alignment.Start else Alignment.End
    val color = if (isA) Color(0xFF2C2C2C) else Color(0xFF1B3D2F)
    val shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isA) 0.dp else 16.dp,
        bottomEnd = if (isA) 16.dp else 0.dp
    )

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(color)
                .padding(12.dp)
        ) {
            Column {
                Text(text = message.originalText, color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = message.translatedText,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onListen,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Listen",
                            tint = MintGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MicButton(
    label: String,
    isListening: Boolean,
    isDisabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Button(
        onClick = onClick,
        enabled = !isDisabled,
        modifier = modifier
            .height(64.dp)
            .then(
                if (isListening) Modifier.border(
                    width = 2.dp,
                    color = Color.Red.copy(alpha = borderAlpha),
                    shape = RoundedCornerShape(16.dp)
                ) else Modifier
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isListening) Color.Red else CardSurface,
            disabledContainerColor = CardSurface.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (!isListening && !isDisabled) androidx.compose.foundation.BorderStroke(1.dp, MintGreen) else null
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = null,
                tint = if (isListening) Color.White else if (isDisabled) Color.Gray else MintGreen
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = if (isListening) Color.White else if (isDisabled) Color.Gray else MintGreen,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}
