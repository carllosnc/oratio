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
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cnc.oratio.data.local.model.PrayerWithTranslations
import cnc.oratio.ui.components.PrayerAudioPlayerBar
import cnc.oratio.ui.components.PrayerCalendarFloatCard
import cnc.oratio.ui.components.PrayerFilterBar
import cnc.oratio.ui.components.PrayerListItemCard
import cnc.oratio.ui.components.PrayerSearchBar
import cnc.oratio.ui.theme.GermaniaOneFontFamily
import cnc.oratio.ui.util.UiStrings
import cnc.oratio.ui.viewmodel.HomeViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onPrayerClick: (prayerId: String) -> Unit,
    onRemindersClick: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var showLanguageMenu by remember { mutableStateOf(false) }

    // Audio Engine State
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    val todayString = remember { java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE) }

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
        if (uiState.playingPrayerId == prayerItem.prayer.id) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            tts?.stop()
            viewModel.setPlayingPrayerId(null)
        } else {
            viewModel.closeCalendar()
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            tts?.stop()

            val resName = "${prayerItem.prayer.id}_${uiState.userLanguageCode}".lowercase()
            val resId = context.resources.getIdentifier(resName, "raw", context.packageName)

            if (resId != 0) {
                try {
                    val player = MediaPlayer.create(context, resId)
                    mediaPlayer = player
                    player?.setOnCompletionListener {
                        viewModel.setPlayingPrayerId(null)
                        player.release()
                        mediaPlayer = null
                    }
                    player?.start()
                    viewModel.setPlayingPrayerId(prayerItem.prayer.id)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                val translation = prayerItem.translations.find { it.languageCode == uiState.userLanguageCode }
                    ?: prayerItem.translations.firstOrNull()

                translation?.content?.let { textToRead ->
                    val locale = when (uiState.userLanguageCode) {
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
                            viewModel.setPlayingPrayerId(prayerItem.prayer.id)
                        }
                        override fun onDone(utteranceId: String?) {
                            viewModel.setPlayingPrayerId(null)
                        }
                        override fun onError(utteranceId: String?) {
                            viewModel.setPlayingPrayerId(null)
                        }
                    })

                    val params = Bundle().apply {
                        putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "HOME_AUDIO_${prayerItem.prayer.id}")
                    }
                    tts?.speak(textToRead, TextToSpeech.QUEUE_FLUSH, params, "HOME_AUDIO_${prayerItem.prayer.id}")
                    viewModel.setPlayingPrayerId(prayerItem.prayer.id)
                }
            }
        }
    }

    val activePlayingPrayer = uiState.prayersState?.find { it.prayer.id == uiState.playingPrayerId }
    val activeCalendarPrayer = uiState.prayersState?.find { it.prayer.id == uiState.activeCalendarPrayerId }
    val activeCalendarPrayerTranslation = activeCalendarPrayer?.translations?.find { it.languageCode == uiState.userLanguageCode }
        ?: activeCalendarPrayer?.translations?.firstOrNull()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Oratio",
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 26.sp,
                            fontFamily = GermaniaOneFontFamily
                        )
                    },
                    actions = {
                        IconButton(onClick = onRemindersClick) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Prayer Reminders",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

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
                                uiState.languages.forEach { lang ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "${lang.flagIcon} ${lang.name}",
                                                fontWeight = if (lang.code == uiState.userLanguageCode) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            if (uiState.playingPrayerId != null) {
                                                mediaPlayer?.stop()
                                                mediaPlayer?.release()
                                                mediaPlayer = null
                                                tts?.stop()
                                            }
                                            viewModel.setUserLanguage(lang.code)
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
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Search Bar
                    PrayerSearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = { viewModel.setSearchQuery(it) },
                        userLanguageCode = uiState.userLanguageCode
                    )

                    // Multi-Filter Chips Bar
                    PrayerFilterBar(
                        categories = uiState.categories,
                        selectedCategoryId = uiState.selectedCategoryId,
                        showOnlyFavorites = uiState.showOnlyFavorites,
                        showOnlyWithAudio = uiState.showOnlyWithAudio,
                        hasActiveFilters = uiState.hasActiveFilters,
                        userLanguageCode = uiState.userLanguageCode,
                        onSelectCategory = { viewModel.selectCategory(it) },
                        onToggleFavorites = { viewModel.toggleShowOnlyFavorites() },
                        onToggleAudio = { viewModel.toggleShowOnlyWithAudio() },
                        onClearFilters = { viewModel.clearAllFilters() }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Results Counter Banner
                    if (uiState.prayersState != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = UiStrings.prayersFoundCount(uiState.filteredPrayers.size, uiState.userLanguageCode),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                        thickness = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Prayer List / Loading / Empty Filter State
                    if (uiState.prayersState == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp
                            )
                        }
                    } else if (uiState.filteredPrayers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.FilterListOff,
                                    contentDescription = "No prayers found",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                Text(
                                    text = UiStrings.noPrayersFound(uiState.userLanguageCode),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (uiState.hasActiveFilters) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(onClick = { viewModel.clearAllFilters() }) {
                                        Text(text = UiStrings.clearFilters(uiState.userLanguageCode))
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.filteredPrayers, key = { it.prayer.id }) { prayerItem ->
                                val isTodayMarked = uiState.allPrayerLogs.any { it.prayerId == prayerItem.prayer.id && it.dateString == todayString }
                                PrayerListItemCard(
                                    prayerItem = prayerItem,
                                    preferredLanguageCode = uiState.userLanguageCode,
                                    isPlayingThisPrayer = uiState.playingPrayerId == prayerItem.prayer.id,
                                    isCalendarActiveThisPrayer = uiState.activeCalendarPrayerId == prayerItem.prayer.id,
                                    isTodayMarked = isTodayMarked,
                                    onPrayerClick = {
                                        if (uiState.playingPrayerId != null) {
                                            mediaPlayer?.stop()
                                            mediaPlayer?.release()
                                            mediaPlayer = null
                                            tts?.stop()
                                            viewModel.setPlayingPrayerId(null)
                                        }
                                        onPrayerClick(prayerItem.prayer.id)
                                    },
                                    onFavoriteToggle = {
                                        viewModel.toggleFavorite(prayerItem.prayer.id, !prayerItem.prayer.isFavorite)
                                    },
                                    onAudioToggle = {
                                        toggleAudioForPrayer(prayerItem)
                                    },
                                    onCalendarToggle = {
                                        viewModel.setActiveCalendarPrayerId(prayerItem.prayer.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Audio Mini Player
        PrayerAudioPlayerBar(
            visible = uiState.playingPrayerId != null && uiState.activeCalendarPrayerId == null,
            playingPrayer = activePlayingPrayer,
            userLanguageCode = uiState.userLanguageCode,
            onStopClick = {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
                tts?.stop()
                viewModel.setPlayingPrayerId(null)
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Backdrop Scrim overlay
        AnimatedVisibility(
            visible = uiState.activeCalendarPrayerId != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        viewModel.closeCalendar()
                    }
            )
        }

        // Floating Prayer Calendar Card
        AnimatedVisibility(
            visible = uiState.activeCalendarPrayerId != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                PrayerCalendarFloatCard(
                    prayerTitle = activeCalendarPrayerTranslation?.title ?: activeCalendarPrayer?.prayer?.defaultTitle ?: "Prayer Calendar",
                    markedDates = uiState.activeCalendarPrayerLogs,
                    languageCode = uiState.userLanguageCode,
                    onToggleDate = { dateStr, isMarked ->
                        uiState.activeCalendarPrayerId?.let { pId ->
                            viewModel.togglePrayerDate(pId, dateStr, isMarked)
                        }
                    },
                    onClose = { viewModel.closeCalendar() }
                )
            }
        }
    }
}
