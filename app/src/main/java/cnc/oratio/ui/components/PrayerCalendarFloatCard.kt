package cnc.oratio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cnc.oratio.ui.theme.GermaniaOneFontFamily
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PrayerCalendarFloatCard(
    prayerTitle: String,
    markedDates: List<String>,
    languageCode: String = "en",
    onToggleDate: (dateString: String, currentlyMarked: Boolean) -> Unit,
    onClose: () -> Unit
) {
    val today = remember { LocalDate.now() }
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }

    val daysInMonth = currentYearMonth.lengthOfMonth()
    val firstDayOfWeek = currentYearMonth.atDay(1).dayOfWeek.value % 7 // 0 = Sunday, 1 = Mon ...

    val locale = remember(languageCode) {
        when (languageCode) {
            "pt" -> Locale.forLanguageTag("pt-BR")
            "es" -> Locale.forLanguageTag("es-ES")
            "la" -> Locale.forLanguageTag("it-IT")
            else -> Locale.forLanguageTag("en-US")
        }
    }

    val monthFormatter = remember(locale) { DateTimeFormatter.ofPattern("MMMM yyyy", locale) }
    val todayString = remember { today.format(DateTimeFormatter.ISO_LOCAL_DATE) }

    val subtitleText = when (languageCode) {
        "pt" -> "Acompanhamento Diário de Oração"
        "es" -> "Seguimiento Diario de Oración"
        "la" -> "Calendarium Precationis Diurnum"
        else -> "Daily Prayer Calendar Tracking"
    }

    val weekDays = when (languageCode) {
        "pt" -> listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")
        "es" -> listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
        "la" -> listOf("Sol", "Lun", "Mar", "Mer", "Iov", "Ven", "Sat")
        else -> listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    }

    val todayMarkedText = when (languageCode) {
        "pt" -> "Oração Concluída Hoje ✓"
        "es" -> "Oración Completada Hoy ✓"
        "la" -> "Precatio Hodie Completa ✓"
        else -> "Prayer Completed Today ✓"
    }

    val markTodayText = when (languageCode) {
        "pt" -> "Marcar Hoje como Concluída"
        "es" -> "Marcar Hoy como Completada"
        "la" -> "Marca Hodie ut Completa"
        else -> "Mark Today as Completed"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = prayerTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = GermaniaOneFontFamily,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Calendar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Month Navigation Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentYearMonth = currentYearMonth.minusMonths(1) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Month"
                    )
                }

                Text(
                    text = currentYearMonth.format(monthFormatter).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = { currentYearMonth = currentYearMonth.plusMonths(1) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Month"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Day of Week Header
            Row(modifier = Modifier.fillMaxWidth()) {
                weekDays.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Days Grid (7 columns)
            val totalCells = firstDayOfWeek + daysInMonth
            val rows = (totalCells + 6) / 7

            Column(modifier = Modifier.fillMaxWidth()) {
                for (row in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - firstDayOfWeek + 1

                            if (cellIndex < firstDayOfWeek || dayNumber > daysInMonth) {
                                Spacer(modifier = Modifier.weight(1f))
                            } else {
                                val date = currentYearMonth.atDay(dayNumber)
                                val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                                val isMarked = markedDates.contains(dateString)
                                val isToday = dateString == todayString

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isMarked) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceContainerLow
                                        )
                                        .then(
                                            if (isToday && !isMarked) Modifier.border(
                                                1.dp,
                                                MaterialTheme.colorScheme.primary,
                                                CircleShape
                                            ) else Modifier
                                        )
                                        .clickable {
                                            onToggleDate(dateString, isMarked)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isMarked) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Marked",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(4.dp)
                                        )
                                    } else {
                                        Text(
                                            text = dayNumber.toString(),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isToday) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick "Mark Today" Button
            val isTodayMarked = markedDates.contains(todayString)
            Button(
                onClick = { onToggleDate(todayString, isTodayMarked) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTodayMarked) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Mark Today",
                    tint = if (isTodayMarked) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isTodayMarked) todayMarkedText else markTodayText,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isTodayMarked) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
