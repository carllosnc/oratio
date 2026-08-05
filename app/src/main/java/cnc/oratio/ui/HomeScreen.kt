package cnc.oratio.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cnc.oratio.data.local.model.PrayerWithTranslations
import cnc.oratio.data.repository.PrayerRepository
import kotlinx.coroutines.launch

import cnc.oratio.ui.theme.BokorFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: PrayerRepository,
    onPrayerClick: (prayerId: String) -> Unit
) {
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

    val filteredPrayers = remember(prayers, selectedCategoryId, showOnlyFavorites) {
        prayers.filter { item ->
            val matchesCategory = selectedCategoryId == null || item.prayer.categoryId == selectedCategoryId
            val matchesFavorite = !showOnlyFavorites || item.prayer.isFavorite

            matchesCategory && matchesFavorite
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Oratio 🕊️",
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = BokorFontFamily
                        )
                        Text(
                            text = "Prayers & Devotions Collection",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                        label = { Text("All Prayers") }
                    )
                }

                item {
                    FilterChip(
                        selected = showOnlyFavorites,
                        onClick = { showOnlyFavorites = !showOnlyFavorites },
                        label = { Text("Favorites ⭐") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
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
                        label = { Text(category.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredPrayers, key = { it.prayer.id }) { prayerItem ->
                        PrayerListItemCard(
                            prayerItem = prayerItem,
                            preferredLanguageCode = userLanguageCode,
                            onPrayerClick = { onPrayerClick(prayerItem.prayer.id) },
                            onFavoriteToggle = {
                                scope.launch {
                                    repository.toggleFavorite(prayerItem.prayer.id, !prayerItem.prayer.isFavorite)
                                }
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
    onPrayerClick: () -> Unit,
    onFavoriteToggle: () -> Unit
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Title
                Text(
                    text = preferredTranslation?.title ?: prayerItem.prayer.defaultTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = BokorFontFamily,
                    color = MaterialTheme.colorScheme.onSurface
                )

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
                    Text(
                        text = content.take(90) + if (content.length > 90) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

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
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (prayerItem.prayer.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Bookmark Favorite",
                        tint = if (prayerItem.prayer.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "View Prayer",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
