package com.example.indiaconnect.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.indiaconnect.R
import com.example.indiaconnect.ui.theme.CardSurface
import com.example.indiaconnect.ui.theme.DarkBackground
import com.example.indiaconnect.ui.theme.MintGreen
import com.example.indiaconnect.ui.theme.TextPrimary
import com.example.indiaconnect.ui.theme.TextSecondary
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

import com.example.indiaconnect.BuildConfig

// --- Data Model ---
data class LanguageItem(
    val code: String,
    val englishName: String,
    val nativeName: String,
    val inputPlaceholder: String,
    val outputPlaceholder: String,
    val translateBtnText: String
)

val languageList = listOf(
    LanguageItem("en", "English", "English", "Enter text here...", "Translation will appear here", "Translate"),
    LanguageItem("hi", "Hindi", "हिन्दी", "यहाँ पाठ दर्ज करें...", "अनुवाद यहाँ दिखाई देगा", "अनुवाद करें"),
    LanguageItem("ml", "Malayalam", "മലയാളം", "വാചകം നൽകുക...", "വിവർത്തനം ഇവിടെ ദൃശ്യമാകും", "വിവർത്തനം ചെയ്യുക"),
    LanguageItem("bn", "Bengali", "বাংলা", "এখানে পাঠ্য লিখুন...", "অনুবাদ এখানে প্রদর্শিত হবে", "অনুবাদ করুন"),
    LanguageItem("ta", "Tamil", "தமிழ்", "இங்கே உரையை உள்ளிடவும்...", "மொழிபெயர்ப்பு இங்கே தோன்றும்", "மொழிபெயர்க்க"),
    LanguageItem("te", "Telugu", "తెలుగు", "ఇక్కడ వచనాన్ని నమోదు చేయండి...", "అనువాదం ఇక్కడ కనిపిస్తుంది", "అనువదించు"),
    LanguageItem("kn", "Kannada", "ಕನ್ನಡ", "ಇಲ್ಲಿ ಪಠ್ಯವನ್ನು ನਮੂದಿಸಿ...", "అనువాదం ಇಲ್ಲಿದೆ", "అనువాదಿಸಿ"),
    LanguageItem("gu", "Gujarati", "ગુજરાતી", "અહીં લખાણ દાખલ કરો...", "અનુવાદ અહીં દેખાશે", "અનુવાદ કરો"),
    LanguageItem("mr", "Marathi", "मराठी", "येथे मजকूर प्रविष्ट करा...", "अनुवाद येथे दिसेल", "अनुवाद करा"),
    LanguageItem("ur", "Urdu", "اردو", "یہاں عبارت درج کریں...", "ترجمہ یہاں ظاہر ہوگا", "ترجمہ کریں"),
    LanguageItem("pa", "Punjabi", "ਪੰਜਾਬੀ", "ਇੱਥੇ ਲਿਖਤ ਦਰਜ ਕਰੋ...", "ਅਨੁਵਾਦ ਇੱਥੇ ਦਿਖਾਈ ਦੇਵੇਗਾ", "ਅਨੁਵਾਦ ਕਰੋ"),
    LanguageItem("as", "Assamese", "অসমীয়া", "ইয়াত পাঠ লিখক...", "অনুবাদ ইয়াত দেখা যাব", "অনুবাদ কৰক"),
    LanguageItem("or", "Odia", "ଓଡ଼িଆ", "ଏଠାରେ পাঠ্য ଲେଖନ୍ତু...", "ଅନୁବାଦ ଏଠାରେ ଦେଖାଯିବ", "ଅନୁବାଦ କରନ୍ତୁ"),
    LanguageItem("ne", "Nepali", "नेपाली", "यहाँ পাঠ প্রविष्ट गर्नुहोस्...", "অনুবাদ यहाँ देखिनेछ", "अनुवाद गर्नुहोस्"),
    LanguageItem("kok", "Konkani", "कोंकणी", "हांगा बरप बरयात...", "भाशांतर हांगा दिसतलें", "भाशांतर करात"),
    LanguageItem("mai", "Maithili", "मैथिली", "এতয় পাঠ লিখু...", "অনুবাদ এতয় দেখায়ব", "অনুবাদ কৰূ"),
    LanguageItem("mni", "Manipuri", "মৈতেইলোন", "মসিদা ইবা হাপচিনবিউ...", "হন্দোকপা মসিদা উবা ফংগনি", "হন্দোকপা"),
    LanguageItem("sa", "Sanskrit", "संस्कृतम्", "अत्र लेखनं कुरुत...", "अनुवादः अत्र दृश्यते", "अनुवादं कुरुत"),
    LanguageItem("ks", "Kashmiri", "کٲشُر", "यतिन इبارत दर्ज करिव...", "तरजुमा यतिन हावन", "तरजुमा करिव"),
    LanguageItem("sd", "Sindhi", "سنڌي", "هتي لکت داخل ڪريو...", "ترجمو هتي ظاهر ٿيندو", "ترجمو ڪريو"),
    LanguageItem("sat", "Santali", "ᱥᱟᱱᱛᱟᱲᱤ", "ᱱᱚᱸᱰᱮ ᱚᱞ ᱢᱮ...", "ᱛᱚᱨᱡᱚᱢᱟ ᱱᱚᱸᱰᱮ ᱧᱮᱞᱚᱜᱼᱟ", "ᱛᱚᱨᱡᱚᱢᱟ ᱢᱮ"),
    LanguageItem("brx", "Bodo", "बर'", "बेयाव লিরনায় ফজ'...", "राव সোলায়নায় বেयाव नুজাগোন", "राव सोलाय"),
    LanguageItem("doi", "Dogri", "डोगरी", "एत्थें इबारत लिखो...", "तरजुमा एत्थें दक्खे", "तरजुमा करो")
)

// --- Networking ---
interface GtxApiService {
    @GET("translate_a/single")
    suspend fun translate(
        @Query("client") client: String = "gtx",
        @Query("dt") dt: String = "t",
        @Query("sl") sourceLanguage: String,
        @Query("tl") targetLanguage: String,
        @Query("q") query: String
    ): String
}

object RetrofitClient {
    private val BASE_URL = BuildConfig.TRANSLATION_BASE_URL
    
    val apiService: GtxApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(GtxApiService::class.java)
    }
}

// --- Shared UI Composables ---

@Composable
fun LanguageSelector(languageName: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Card(
        modifier = modifier.clickable(enabled = enabled) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) CardSurface else CardSurface.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = languageName, 
                color = if (enabled) Color.White else Color.Gray, 
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.ArrowDropDown, 
                contentDescription = null, 
                tint = if (enabled) Color.White else Color.Gray
            )
        }
    }
}

@Composable
fun TranslationBox(
    text: String,
    onValueChange: (String) -> Unit = {},
    placeholder: String,
    charLimit: Int = 0,
    onCopy: () -> Unit,
    onListen: () -> Unit,
    onClear: (() -> Unit)? = null,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier,
    isReadOnly: Boolean = false
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MintGreen)
                    } else if (errorMessage != null) {
                        Text(text = errorMessage, color = Color.Red, modifier = Modifier.fillMaxWidth())
                    } else if (isReadOnly) {
                        if (text.isEmpty()) {
                            Text(text = placeholder, color = TextSecondary)
                        } else {
                            Text(text = text, color = Color.White, fontSize = 18.sp)
                        }
                    } else {
                        TextField(
                            value = text,
                            onValueChange = onValueChange,
                            placeholder = { Text(text = placeholder, color = TextSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        IconButton(onClick = onCopy) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White)
                        }
                        IconButton(onClick = onListen) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Listen", tint = Color.White)
                        }
                        if (onClear != null) {
                            IconButton(onClick = onClear) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White)
                            }
                        }
                    }
                    if (charLimit > 0) {
                        Text(text = "${text.length}/$charLimit", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun LanguagePickerOverlay(onLanguageSelected: (LanguageItem) -> Unit, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(DarkBackground).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(text = "Choose language", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "India Map Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(languageList, key = { it.code }) { lang ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLanguageSelected(lang) }
                        .padding(vertical = 12.dp)
                ) {
                    Text(text = lang.englishName, color = Color.White, fontSize = 16.sp)
                    Text(text = lang.nativeName, color = Color.Gray, fontSize = 14.sp)
                }
                Divider(color = Color(0xFF333333))
            }
        }
    }
}

fun copyToClipboard(context: Context, text: String) {
    if (text.isBlank()) return
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("IndiaConnect Translation", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
}
