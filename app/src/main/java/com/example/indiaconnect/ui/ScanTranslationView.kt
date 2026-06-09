package com.example.indiaconnect.ui

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.indiaconnect.ui.theme.CardSurface
import com.example.indiaconnect.ui.theme.DarkBackground
import com.example.indiaconnect.ui.theme.MintGreen
import com.example.indiaconnect.ui.theme.TextSecondary
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.util.*

class ScanViewModel : ViewModel() {
    var extractedText by mutableStateOf("")
    var translatedText by mutableStateOf("")
    var isProcessing by mutableStateOf(false)
    var imagePreview by mutableStateOf<Any?>(null) // Can be Bitmap or Uri
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

    fun processImage(context: Context, image: Any) {
        imagePreview = image
        isProcessing = true
        errorMessage = null
        
        val inputImage = when (image) {
            is Bitmap -> InputImage.fromBitmap(image, 0)
            is Uri -> InputImage.fromFilePath(context, image)
            else -> null
        }

        if (inputImage == null) {
            isProcessing = false
            errorMessage = "Invalid Image Source"
            return
        }

        val recognizer = TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
        
        viewModelScope.launch {
            try {
                val result = recognizer.process(inputImage).await()
                extractedText = result.text
                if (extractedText.isNotBlank()) {
                    translateText()
                } else {
                    errorMessage = "No text found in image"
                }
            } catch (e: Exception) {
                errorMessage = "OCR Error: ${e.localizedMessage}"
            } finally {
                isProcessing = false
            }
        }
    }

    fun translateText() {
        if (extractedText.isBlank()) return
        
        viewModelScope.launch {
            isProcessing = true
            errorMessage = null
            try {
                val rawResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.translate(
                        sourceLanguage = sourceLanguage.code,
                        targetLanguage = targetLanguage.code,
                        query = extractedText
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
                isProcessing = false
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
        tts?.stop()
        tts?.shutdown()
    }
}

@Composable
fun ScanTranslationView(
    appLanguageCode: String = "English",
    viewModel: ScanViewModel = viewModel()
) {
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        viewModel.initializeTTS(context)
    }

    LaunchedEffect(appLanguageCode) {
        viewModel.appLanguage = languageList.find { it.englishName == appLanguageCode } ?: languageList[0]
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap ->
            if (bitmap != null) {
                viewModel.processImage(context, bitmap)
            }
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.processImage(context, uri)
            }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                cameraLauncher.launch()
            } else {
                viewModel.errorMessage = "Camera Permission Denied"
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
                    enabled = !viewModel.isProcessing
                )

                IconButton(
                    onClick = {
                        val temp = viewModel.sourceLanguage
                        viewModel.sourceLanguage = viewModel.targetLanguage
                        viewModel.targetLanguage = temp
                    }, 
                    modifier = Modifier.padding(horizontal = 8.dp),
                    enabled = !viewModel.isProcessing
                ) {
                    Icon(Icons.Default.Sync, contentDescription = "Swap", tint = if (viewModel.isProcessing) Color.Gray else MintGreen)
                }

                LanguageSelector(
                    languageName = viewModel.targetLanguage.englishName,
                    onClick = {
                        viewModel.pickerType = "TARGET"
                        viewModel.isPickerOpen = true
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !viewModel.isProcessing
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Image Preview (Small clean banner)
            if (viewModel.imagePreview != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = viewModel.imagePreview,
                        contentDescription = "Scan Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Raw Text Box (Box 1)
            TranslationBox(
                text = viewModel.extractedText,
                placeholder = TranslationMap.getStrings(viewModel.appLanguage.englishName).scanInstruction,
                onCopy = { copyToClipboard(context, viewModel.extractedText) },
                onListen = { viewModel.speak(viewModel.extractedText, viewModel.sourceLanguage.code) },
                onClear = { viewModel.extractedText = "" },
                isLoading = viewModel.isProcessing && viewModel.extractedText.isEmpty(),
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
                isLoading = viewModel.isProcessing && viewModel.extractedText.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().weight(1f),
                isReadOnly = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    enabled = !viewModel.isProcessing,
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Camera", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    enabled = !viewModel.isProcessing,
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CardSurface,
                        disabledContainerColor = CardSurface.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = if (!viewModel.isProcessing) androidx.compose.foundation.BorderStroke(1.dp, MintGreen) else null
                ) {
                    Icon(Icons.Default.Collections, contentDescription = null, tint = if (viewModel.isProcessing) Color.Gray else MintGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Gallery", color = if (viewModel.isProcessing) Color.Gray else MintGreen, fontWeight = FontWeight.Bold)
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
