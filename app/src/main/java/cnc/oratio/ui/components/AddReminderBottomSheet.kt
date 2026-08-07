package cnc.oratio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cnc.oratio.data.local.model.PrayerWithTranslations
import cnc.oratio.ui.util.UiStrings
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddReminderBottomSheet(
    sheetState: SheetState,
    selectedHour: Int,
    selectedMinute: Int,
    selectedPrayerId: String?,
    isDaily: Boolean,
    selectedDays: Set<Int>,
    prayers: List<PrayerWithTranslations>,
    userLanguageCode: String,
    onDismiss: () -> Unit,
    onOpenTimePicker: () -> Unit,
    onPrayerSelect: (String?) -> Unit,
    onFrequencyChange: (Boolean) -> Unit,
    onDayToggle: (Int) -> Unit,
    onSave: () -> Unit
) {
    var expandedDropdown by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header
            Text(
                text = UiStrings.newReminder(userLanguageCode),
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Time Selection Button
            Text(
                text = UiStrings.timeLabel(userLanguageCode),
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Default,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenTimePicker() },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute),
                        style = MaterialTheme.typography.headlineLarge,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = "Set Time",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Prayer Selection Dropdown
            Text(
                text = UiStrings.prayerSelection(userLanguageCode),
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Default,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box {
                val selectedPrayer = prayers.find { it.prayer.id == selectedPrayerId }
                val selectedPrayerTitle = selectedPrayer?.translations?.find { it.languageCode == userLanguageCode }?.title
                    ?: selectedPrayer?.prayer?.defaultTitle
                    ?: UiStrings.dailyFeaturedPrayer(userLanguageCode)

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedDropdown = true },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        text = selectedPrayerTitle,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Default,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                DropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(UiStrings.dailyFeaturedPrayer(userLanguageCode), fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold) },
                        onClick = {
                            onPrayerSelect(null)
                            expandedDropdown = false
                        }
                    )

                    prayers.forEach { item ->
                        val title = item.translations.find { it.languageCode == userLanguageCode }?.title
                            ?: item.prayer.defaultTitle
                        DropdownMenuItem(
                            text = { Text(title, fontFamily = FontFamily.Default) },
                            onClick = {
                                onPrayerSelect(item.prayer.id)
                                expandedDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Frequency Selection Segmented Row
            Text(
                text = UiStrings.frequency(userLanguageCode),
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Default,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    // Daily Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isDaily) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            )
                            .clickable { onFrequencyChange(true) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = UiStrings.daily(userLanguageCode),
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Default,
                            fontWeight = if (isDaily) FontWeight.Bold else FontWeight.Normal,
                            color = if (isDaily) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Select Days Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (!isDaily) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            )
                            .clickable { onFrequencyChange(false) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = UiStrings.specificDays(userLanguageCode),
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Default,
                            fontWeight = if (!isDaily) FontWeight.Bold else FontWeight.Normal,
                            color = if (!isDaily) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Days Selection Chips
            if (!isDaily) {
                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val daysMap = mapOf(
                        1 to UiStrings.sun(userLanguageCode),
                        2 to UiStrings.mon(userLanguageCode),
                        3 to UiStrings.tue(userLanguageCode),
                        4 to UiStrings.wed(userLanguageCode),
                        5 to UiStrings.thu(userLanguageCode),
                        6 to UiStrings.fri(userLanguageCode),
                        7 to UiStrings.sat(userLanguageCode)
                    )

                    daysMap.forEach { (dayInt, dayLabel) ->
                        val isSelected = selectedDays.contains(dayInt)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onDayToggle(dayInt) },
                            label = { Text(dayLabel, fontFamily = FontFamily.Default) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onDismiss) {
                    Text(UiStrings.cancel(userLanguageCode), fontFamily = FontFamily.Default)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = onSave,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(UiStrings.save(userLanguageCode), fontFamily = FontFamily.Default)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
