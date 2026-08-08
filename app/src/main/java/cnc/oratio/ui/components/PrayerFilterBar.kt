package cnc.oratio.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cnc.oratio.data.local.entity.CategoryEntity
import cnc.oratio.ui.util.UiStrings

@Composable
fun PrayerFilterBar(
    categories: List<CategoryEntity>,
    selectedCategoryId: String?,
    showOnlyFavorites: Boolean,
    showOnlyWithAudio: Boolean,
    hasActiveFilters: Boolean,
    userLanguageCode: String,
    onSelectCategory: (String?) -> Unit,
    onToggleFavorites: () -> Unit,
    onToggleAudio: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Clear Filters Action Chip (when any filter is active)
        if (hasActiveFilters) {
            item {
                FilterChip(
                    selected = true,
                    onClick = onClearFilters,
                    label = { Text(UiStrings.clearFilters(userLanguageCode)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )
            }
        }

        // All Prayers Chip
        item {
            FilterChip(
                selected = selectedCategoryId == null && !showOnlyFavorites && !showOnlyWithAudio,
                onClick = { onClearFilters() },
                label = { Text(UiStrings.allPrayers(userLanguageCode)) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }

        // Favorites Chip
        item {
            FilterChip(
                selected = showOnlyFavorites,
                onClick = onToggleFavorites,
                label = { Text(UiStrings.favorites(userLanguageCode)) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }

        // Audio Filter Chip
        item {
            FilterChip(
                selected = showOnlyWithAudio,
                onClick = onToggleAudio,
                label = { Text(UiStrings.audioOnlyFilter(userLanguageCode)) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }

        // Categories Chips
        items(categories) { category ->
            val isSelected = selectedCategoryId == category.id
            FilterChip(
                selected = isSelected,
                onClick = { onSelectCategory(category.id) },
                label = { Text(UiStrings.categoryName(category.id, category.name, userLanguageCode)) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}
