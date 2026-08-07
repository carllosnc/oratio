package cnc.oratio.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cnc.oratio.data.local.entity.LanguageEntity
import cnc.oratio.data.repository.PrayerRepository
import cnc.oratio.ui.theme.GermaniaOneFontFamily
import cnc.oratio.ui.util.UiStrings
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerDetailScreen(
    prayerId: String,
    repository: PrayerRepository,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prayerWithTranslations by repository.getPrayerById(prayerId).collectAsState(initial = null)
    val languages by repository.getAllLanguages().collectAsState(initial = emptyList())
    val userLanguageCode by repository.userLanguageCode.collectAsState(initial = "en")

    var selectedLanguageCode by remember(userLanguageCode) { mutableStateOf(userLanguageCode) }

    val prayer = prayerWithTranslations
    val primaryTranslation = prayer?.translations?.find { it.languageCode == selectedLanguageCode }
        ?: prayer?.translations?.find { it.languageCode == "en" }
        ?: prayer?.translations?.firstOrNull()

    // Audio Narration setup with MediaPlayer & TextToSpeech fallback
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isSpeaking by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        var textToSpeech: TextToSpeech? = null
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts = textToSpeech
            }
        }
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        }
    }

    val toggleAudioPlayback = {
        if (isSpeaking) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            tts?.stop()
            isSpeaking = false
        } else {
            val resName = "${prayerId}_${selectedLanguageCode}".lowercase()
            val resId = context.resources.getIdentifier(resName, "raw", context.packageName)

            if (resId != 0) {
                try {
                    mediaPlayer?.release()
                    val player = MediaPlayer.create(context, resId)
                    mediaPlayer = player
                    player?.setOnCompletionListener {
                        isSpeaking = false
                        player.release()
                        mediaPlayer = null
                    }
                    player?.start()
                    isSpeaking = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                primaryTranslation?.content?.let { textToRead ->
                    val locale = when (selectedLanguageCode) {
                        "la" -> Locale.forLanguageTag("it-IT")
                        "pt" -> Locale.forLanguageTag("pt-BR")
                        "en" -> Locale.forLanguageTag("en-US")
                        "es" -> Locale.forLanguageTag("es-ES")
                        else -> Locale.getDefault()
                    }

                    tts?.language = locale
                    tts?.setPitch(0.65f)
                    tts?.setSpeechRate(0.82f)

                    tts?.voices?.find { voice ->
                        voice.locale.language == locale.language &&
                        voice.name.contains("male", ignoreCase = true)
                    }?.let { maleVoice ->
                        tts?.voice = maleVoice
                    }

                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            isSpeaking = true
                        }
                        override fun onDone(utteranceId: String?) {
                            isSpeaking = false
                        }
                        override fun onError(utteranceId: String?) {
                            isSpeaking = false
                        }
                    })

                    val params = Bundle().apply {
                        putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "PRAYER_AUDIO_ID")
                    }
                    tts?.speak(textToRead, TextToSpeech.QUEUE_FLUSH, params, "PRAYER_AUDIO_ID")
                    isSpeaking = true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = primaryTranslation?.title ?: prayer?.prayer?.defaultTitle ?: UiStrings.prayerDetailsTitle(userLanguageCode),
                        maxLines = 1,
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 18.sp),
                        fontFamily = GermaniaOneFontFamily
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        mediaPlayer?.stop()
                        mediaPlayer?.release()
                        mediaPlayer = null
                        tts?.stop()
                        onBackClick()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    prayer?.let { item ->
                        IconButton(onClick = { toggleAudioPlayback() }) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Audio Playback",
                                tint = if (isSpeaking) Color.Red else MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(onClick = {
                            scope.launch {
                                repository.toggleFavorite(item.prayer.id, !item.prayer.isFavorite)
                            }
                        }) {
                            Icon(
                                imageVector = if (item.prayer.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (item.prayer.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = {
                            val activeTranslation = item.translations.find { it.languageCode == selectedLanguageCode }
                                ?: item.translations.firstOrNull()
                            activeTranslation?.let { tr ->
                                copyToClipboard(context, tr.title, tr.content, userLanguageCode)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Text"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = isSpeaking,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 6.dp,
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Playing Audio",
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = primaryTranslation?.title ?: "Playing Prayer",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Text(
                                    text = UiStrings.audioNarrationPlaying(userLanguageCode),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        IconButton(onClick = { toggleAudioPlayback() }) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop Playback",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (prayer == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Language Selection Tabs
                if (languages.isNotEmpty()) {
                    SecondaryScrollableTabRow(
                        selectedTabIndex = languages.indexOfFirst { it.code == selectedLanguageCode }.coerceAtLeast(0),
                        edgePadding = 16.dp
                    ) {
                        languages.forEach { lang ->
                            Tab(
                                selected = selectedLanguageCode == lang.code,
                                onClick = {
                                    if (isSpeaking) {
                                        mediaPlayer?.stop()
                                        mediaPlayer?.release()
                                        mediaPlayer = null
                                        tts?.stop()
                                        isSpeaking = false
                                    }
                                    selectedLanguageCode = lang.code
                                    scope.launch {
                                        repository.setUserLanguage(lang.code)
                                    }
                                },
                                text = {
                                    Text(
                                        text = "${lang.flagIcon} ${lang.name}",
                                        fontFamily = FontFamily.Default
                                    )
                                }
                            )
                        }
                    }
                }

                // Scrollable Prayer Content Body
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Main Title Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = primaryTranslation?.title ?: prayer.prayer.defaultTitle,
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                                fontFamily = GermaniaOneFontFamily,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            primaryTranslation?.subtitle?.let { sub ->
                                Text(
                                    text = sub,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Prayer Content View with Paragraph Dividers
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp)
                        ) {
                            val paragraphs = (primaryTranslation?.content ?: "")
                                .split("\n")
                                .filter { it.isNotBlank() }

                            paragraphs.forEachIndexed { index, paragraph ->
                                Text(
                                    text = paragraph.trim(),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 18.sp,
                                        lineHeight = 28.sp,
                                        letterSpacing = 0.3.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (index < paragraphs.lastIndex) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f),
                                        thickness = 1.dp
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                }
                            }
                        }
                    }

                    // Historical / Liturgical Notes Section
                    primaryTranslation?.notes?.let { note ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Notes",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = note,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, title: String, content: String, userLanguageCode: String = "en") {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Prayer Text", "$title\n\n$content")
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, UiStrings.copiedToClipboard(userLanguageCode), Toast.LENGTH_SHORT).show()
}

private fun getLanguageName(code: String, languages: List<LanguageEntity>): String {
    return languages.find { it.code == code }?.name ?: code
}
