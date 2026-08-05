package cnc.oratio.ui

import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cnc.oratio.data.local.model.PrayerWithTranslations
import cnc.oratio.data.repository.PrayerRepository
import cnc.oratio.ui.theme.GermaniaOneFontFamily
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: PrayerRepository,
    onPrayerClick: (prayerId: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        repository.initializeDatabaseIfNeeded()
    }

    val prayers by repository.getAllPrayers().collectAsState(initial = emptyList())
    val categories by repository.getAllCategories().collectAsState(initial = emptyList())
    val languages by repository.getAllLanguages().collectAsState(initial = emptyList())
    val userLanguageCode by repository.userLanguageCode.collectAsState()

    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var showOnlyFavorites by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }

    // Audio Narration State for Home Screen Cards
    var playingPrayerId by remember { mutableStateOf<String?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

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

    val toggleAudioForPrayer = { prayerItem: PrayerWithTranslations ->
        if (playingPrayerId == prayerItem.prayer.id) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            tts?.stop()
            playingPrayerId = null
        } else {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            tts?.stop()

            val resName = "${prayerItem.prayer.id}_${userLanguageCode}".lowercase()
            val resId = context.resources.getIdentifier(resName, "raw", context.packageName)

            if (resId != 0) {
                try {
                    val player = MediaPlayer.create(context, resId)
                    mediaPlayer = player
                    player?.setOnCompletionListener {
                        playingPrayerId = null
                        player.release()
                        mediaPlayer = null
                    }
                    player?.start()
                    playingPrayerId = prayerItem.prayer.id
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                val translation = prayerItem.translations.find { it.languageCode == userLanguageCode }
                    ?: prayerItem.translations.firstOrNull()

                translation?.content?.let { textToRead ->
                    val locale = when (userLanguageCode) {
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
                            playingPrayerId = prayerItem.prayer.id
                        }
                        override fun onDone(utteranceId: String?) {
                            playingPrayerId = null
                        }
                        override fun onError(utteranceId: String?) {
                            playingPrayerId = null
                        }
                    })

                    val params = Bundle().apply {
                        putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "HOME_AUDIO_${prayerItem.prayer.id}")
                    }
                    tts?.speak(textToRead, TextToSpeech.QUEUE_FLUSH, params, "HOME_AUDIO_${prayerItem.prayer.id}")
                    playingPrayerId = prayerItem.prayer.id
                }
            }
        }
    }

    val filteredPrayers = remember(prayers, selectedCategoryId, showOnlyFavorites) {
        prayers.filter { item ->
            val matchesCategory = selectedCategoryId == null || item.prayer.categoryId == selectedCategoryId
            val matchesFavorite = !showOnlyFavorites || item.prayer.isFavorite

            matchesCategory && matchesFavorite
        }
    }

    val activePlayingPrayer = prayers.find { it.prayer.id == playingPrayerId }
    val activePlayingTranslation = activePlayingPrayer?.translations?.find { it.languageCode == userLanguageCode }
        ?: activePlayingPrayer?.translations?.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Oratio 🕊️",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = GermaniaOneFontFamily
                    )
                },
                actions = {
                    Box {
                        IconButton(onClick = { showLanguageMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Change Language",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        DropdownMenu(
                            expanded = showLanguageMenu,
                            onDismissRequest = { showLanguageMenu = false }
                        ) {
                            languages.forEach { lang ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${lang.flagIcon} ${lang.name}",
                                            fontWeight = if (lang.code == userLanguageCode) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        if (playingPrayerId != null) {
                                            mediaPlayer?.stop()
                                            mediaPlayer?.release()
                                            mediaPlayer = null
                                            tts?.stop()
                                            playingPrayerId = null
                                        }
                                        repository.setUserLanguage(lang.code)
                                        showLanguageMenu = false
                                    }
                                )
                            }
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
                visible = playingPrayerId != null,
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
                                    text = activePlayingTranslation?.title ?: activePlayingPrayer?.prayer?.defaultTitle ?: "Playing Prayer",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Audio Narration • Playing",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        IconButton(onClick = {
                            mediaPlayer?.stop()
                            mediaPlayer?.release()
                            mediaPlayer = null
                            tts?.stop()
                            playingPrayerId = null
                        }) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Category Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryId == null && !showOnlyFavorites,
                        onClick = {
                            selectedCategoryId = null
                            showOnlyFavorites = false
                        },
                        label = { Text("All Prayers") },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                item {
                    FilterChip(
                        selected = showOnlyFavorites,
                        onClick = { showOnlyFavorites = !showOnlyFavorites },
                        label = { Text("Favorites ⭐") },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategoryId == category.id && !showOnlyFavorites,
                        onClick = {
                            selectedCategoryId = if (selectedCategoryId == category.id) null else category.id
                            showOnlyFavorites = false
                        },
                        label = { Text(category.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )

            // Prayer List
            if (filteredPrayers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No prayers found.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredPrayers, key = { it.prayer.id }) { prayerItem ->
                        PrayerListItemCard(
                            prayerItem = prayerItem,
                            preferredLanguageCode = userLanguageCode,
                            isPlayingThisPrayer = playingPrayerId == prayerItem.prayer.id,
                            onPrayerClick = {
                                if (playingPrayerId != null) {
                                    mediaPlayer?.stop()
                                    mediaPlayer?.release()
                                    mediaPlayer = null
                                    tts?.stop()
                                    playingPrayerId = null
                                }
                                onPrayerClick(prayerItem.prayer.id)
                            },
                            onFavoriteToggle = {
                                scope.launch {
                                    repository.toggleFavorite(prayerItem.prayer.id, !prayerItem.prayer.isFavorite)
                                }
                            },
                            onAudioToggle = {
                                toggleAudioForPrayer(prayerItem)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrayerListItemCard(
    prayerItem: PrayerWithTranslations,
    preferredLanguageCode: String,
    isPlayingThisPrayer: Boolean,
    onPrayerClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onAudioToggle: () -> Unit
) {
    val preferredTranslation = prayerItem.translations.find { it.languageCode == preferredLanguageCode }
        ?: prayerItem.translations.find { it.languageCode == "en" }
        ?: prayerItem.translations.firstOrNull()

    val latinTranslation = prayerItem.translations.find { it.languageCode == "la" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPrayerClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = preferredTranslation?.title ?: prayerItem.prayer.defaultTitle,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = GermaniaOneFontFamily,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle / Original Latin Name
            val subtitleText = preferredTranslation?.subtitle ?: latinTranslation?.title
            subtitleText?.let { sub ->
                Text(
                    text = sub,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Preview snippet
            preferredTranslation?.content?.let { content ->
                val cleanSnippet = content.replace("\n", " ").replace(Regex("\\s+"), " ")
                Text(
                    text = cleanSnippet.take(90) + if (cleanSnippet.length > 90) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Available Language Badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                prayerItem.translations.forEach { tr ->
                    val flag = when (tr.languageCode) {
                        "la" -> "🌐 Latin"
                        "pt" -> "🇧🇷 PT"
                        "en" -> "🇺🇸 EN"
                        "es" -> "🇪🇸 ES"
                        else -> tr.languageCode.uppercase()
                    }
                    val isSelected = tr.languageCode == preferredLanguageCode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = flag,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Bottom action bar with Favorite and Audio at left, Arrow at right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onFavoriteToggle) {
                        Icon(
                            imageVector = if (prayerItem.prayer.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Bookmark Favorite",
                            tint = if (prayerItem.prayer.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onAudioToggle) {
                        Icon(
                            imageVector = if (isPlayingThisPrayer) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Audio Playback",
                            tint = if (isPlayingThisPrayer) Color.Red else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                IconButton(onClick = onPrayerClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "View Prayer",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
