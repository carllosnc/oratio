package cnc.oratio.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import cnc.oratio.ui.theme.BokorFontFamily
import kotlinx.coroutines.launch

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
    val userLanguageCode by repository.userLanguageCode.collectAsState()

    var selectedLanguageCode by remember(userLanguageCode) { mutableStateOf(userLanguageCode) }

    val prayer = prayerWithTranslations

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = prayer?.prayer?.defaultTitle ?: "Prayer Details",
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = BokorFontFamily
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    prayer?.let { item ->
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
                                copyToClipboard(context, tr.title, tr.content)
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
            val primaryTranslation = prayer.translations.find { it.languageCode == selectedLanguageCode }
                ?: prayer.translations.find { it.languageCode == "en" }
                ?: prayer.translations.firstOrNull()

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
                                    selectedLanguageCode = lang.code
                                    repository.setUserLanguage(lang.code)
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
                                style = MaterialTheme.typography.headlineSmall,
                                fontFamily = BokorFontFamily,
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
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
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

private fun copyToClipboard(context: Context, title: String, content: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Prayer Text", "$title\n\n$content")
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Prayer text copied to clipboard!", Toast.LENGTH_SHORT).show()
}

private fun getLanguageName(code: String, languages: List<LanguageEntity>): String {
    return languages.find { it.code == code }?.name ?: code
}
