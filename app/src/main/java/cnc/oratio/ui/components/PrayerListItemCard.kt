package cnc.oratio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cnc.oratio.data.local.model.PrayerWithTranslations
import cnc.oratio.ui.theme.GermaniaOneFontFamily

@Composable
fun PrayerListItemCard(
    prayerItem: PrayerWithTranslations,
    preferredLanguageCode: String,
    isPlayingThisPrayer: Boolean,
    isCalendarActiveThisPrayer: Boolean,
    isTodayMarked: Boolean = false,
    onPrayerClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onAudioToggle: () -> Unit,
    onCalendarToggle: () -> Unit
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
        Column(modifier = Modifier.fillMaxWidth()) {
            // Main Content Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
            ) {
                // Title
                Text(
                    text = preferredTranslation?.title ?: prayerItem.prayer.defaultTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 20.sp,
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
                                    else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f)
                                )
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = flag,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                thickness = 1.dp
            )

            // Bottom Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
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

                    IconButton(onClick = onCalendarToggle) {
                        Icon(
                            imageVector = if (isTodayMarked) Icons.Default.EventAvailable else Icons.Default.CalendarMonth,
                            contentDescription = "Prayer Calendar Tracking",
                            tint = when {
                                isTodayMarked -> Color(0xFF388E3C)
                                isCalendarActiveThisPrayer -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
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
