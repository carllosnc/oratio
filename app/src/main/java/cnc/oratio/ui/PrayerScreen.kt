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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cnc.oratio.data.local.entity.LanguageEntity
import cnc.oratio.data.local.model.PrayerWithTranslations
import cnc.oratio.data.repository.PrayerRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPrayerScreen(
    repository: PrayerRepository
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }

    val languages by repository.getAllLanguages().collectAsState(initial = emptyList())
    val prayers by repository.getAllPrayers().collectAsState(initial = emptyList())

    var selectedLanguageCode by remember { mutableStateOf("pt") }
    var secondaryLanguageCode by remember { mutableStateOf<String?>("la") } // Para modo paralelo (Latim por padrão)
    var searchQuery by remember { mutableStateOf("") }
    var isBilingualMode by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        repository.initializeDatabaseIfNeeded()
        isLoading = false
    }

    val filteredPrayers = remember(prayers, searchQuery) {
        if (searchQuery.isBlank()) {
            prayers
        } else {
            prayers.filter { item ->
                item.prayer.defaultTitle.contains(searchQuery, ignoreCase = true) ||
                        item.translations.any { tr ->
                            tr.title.contains(searchQuery, ignoreCase = true) ||
                                    tr.content.contains(searchQuery, ignoreCase = true)
                        }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Oratio",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Orações e Rezas Multilíngues",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { innerPadding ->
        if (isLoading) {
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
            ) {
                // Barra de busca
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Buscar oração ou trecho...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                // Seleção de Idioma Principal
                if (languages.isNotEmpty()) {
                    Text(
                        text = "Idioma Principal:",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    SecondaryScrollableTabRow(
                        selectedTabIndex = languages.indexOfFirst { it.code == selectedLanguageCode }.coerceAtLeast(0),
                        edgePadding = 16.dp
                    ) {
                        languages.forEach { lang ->
                            Tab(
                                selected = selectedLanguageCode == lang.code,
                                onClick = { selectedLanguageCode = lang.code },
                                text = { Text("${lang.flagIcon} ${lang.name}") }
                            )
                        }
                    }
                }

                // Chaveador do Modo Bilíngue
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBilingualMode) "Modo Bilíngue: Latim + ${getLanguageName(selectedLanguageCode, languages)}"
                        else "Modo Idioma Único",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isBilingualMode) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                            .clickable { isBilingualMode = !isBilingualMode }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isBilingualMode) "Bilíngue (Ativo)" else "Único",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isBilingualMode) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Lista de Orações
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredPrayers, key = { it.prayer.id }) { prayerItem ->
                        PrayerCard(
                            prayerItem = prayerItem,
                            primaryLangCode = selectedLanguageCode,
                            secondaryLangCode = if (isBilingualMode) secondaryLanguageCode else null,
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
fun PrayerCard(
    prayerItem: PrayerWithTranslations,
    primaryLangCode: String,
    secondaryLangCode: String?,
    onFavoriteToggle: () -> Unit
) {
    val primaryTranslation = prayerItem.translations.find { it.languageCode == primaryLangCode }
        ?: prayerItem.translations.firstOrNull()

    val secondaryTranslation = secondaryLangCode?.let { langCode ->
        if (langCode != primaryLangCode) {
            prayerItem.translations.find { it.languageCode == langCode }
        } else null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header do Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = primaryTranslation?.title ?: prayerItem.prayer.defaultTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    primaryTranslation?.subtitle?.let { sub ->
                        Text(
                            text = sub,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (prayerItem.prayer.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (prayerItem.prayer.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Conteúdo da Oração (Lado a Lado se Bilíngue, ou Único)
            if (secondaryTranslation != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Idioma Secundário (ex: Latim)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = secondaryTranslation.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = secondaryTranslation.content,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp
                        )
                    }

                    // Idioma Principal (ex: Português)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = primaryTranslation?.title ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = primaryTranslation?.content ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                // Modo Único
                Text(
                    text = primaryTranslation?.content ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp)
                )
            }
        }
    }
}

private fun getLanguageName(code: String, languages: List<LanguageEntity>): String {
    return languages.find { it.code == code }?.name ?: code
}
