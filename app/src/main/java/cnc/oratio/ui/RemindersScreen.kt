package cnc.oratio.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.TimePicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.core.content.ContextCompat
import cnc.oratio.data.local.entity.ReminderEntity
import cnc.oratio.data.local.model.PrayerWithTranslations
import cnc.oratio.data.repository.PrayerRepository
import cnc.oratio.notification.AlarmScheduler
import cnc.oratio.ui.theme.GermaniaOneFontFamily
import cnc.oratio.ui.util.UiStrings
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RemindersScreen(
    repository: PrayerRepository,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userLanguageCode by repository.userLanguageCode.collectAsState()
    val reminders by repository.getAllReminders().collectAsState(initial = emptyList())
    val prayers by repository.getAllPrayers().collectAsState(initial = emptyList())

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasNotificationPermission = isGranted }
    )

    var showAddBottomSheet by remember { mutableStateOf(false) }

    // Form state
    var selectedPrayerId by remember { mutableStateOf<String?>(null) } // null = random daily
    var isDaily by remember { mutableStateOf(true) }
    var selectedDays by remember { mutableStateOf(setOf(1, 2, 3, 4, 5, 6, 7)) } // 1..7
    var selectedHour by remember { mutableStateOf(8) }
    var selectedMinute by remember { mutableStateOf(0) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = UiStrings.remindersTitle(userLanguageCode),
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 19.sp),
                        fontFamily = GermaniaOneFontFamily
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
                    IconButton(
                        onClick = {
                            if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            showAddBottomSheet = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = UiStrings.addReminder(userLanguageCode),
                            tint = MaterialTheme.colorScheme.primary
                        )
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
            // Permission Banner (if not granted)
            if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = UiStrings.permissionTitle(userLanguageCode),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = UiStrings.permissionText(userLanguageCode),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(UiStrings.allow(userLanguageCode))
                        }
                    }
                }
            }

            // Reminders List
            if (reminders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "No reminders",
                            modifier = Modifier.padding(bottom = 12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = UiStrings.noReminders(userLanguageCode),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reminders, key = { it.id }) { reminder ->
                        val prayer = prayers.find { it.prayer.id == reminder.prayerId }
                        val translation = prayer?.translations?.find { it.languageCode == userLanguageCode }
                            ?: prayer?.translations?.firstOrNull()

                        val titleText = when {
                            reminder.label.isNotBlank() -> reminder.label
                            translation != null -> translation.title
                            else -> UiStrings.dailyFeaturedPrayer(userLanguageCode)
                        }

                        val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", reminder.hour, reminder.minute)

                        ReminderCard(
                            reminder = reminder,
                            titleText = titleText,
                            formattedTime = formattedTime,
                            userLanguageCode = userLanguageCode,
                            onToggleEnabled = { enabled ->
                                scope.launch {
                                    val updated = reminder.copy(isEnabled = enabled)
                                    repository.updateReminder(updated)
                                    if (enabled) {
                                        AlarmScheduler.schedule(context, updated)
                                    } else {
                                        AlarmScheduler.cancel(context, reminder.id)
                                    }
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    AlarmScheduler.cancel(context, reminder.id)
                                    repository.deleteReminder(reminder)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Time Picker Dialog
        if (showTimePickerDialog) {
            TimePickerDialog(
                initialHour = selectedHour,
                initialMinute = selectedMinute,
                userLanguageCode = userLanguageCode,
                onTimeSelected = { h, m ->
                    selectedHour = h
                    selectedMinute = m
                    showTimePickerDialog = false
                },
                onDismiss = { showTimePickerDialog = false }
            )
        }

        // Add Reminder Bottom Sheet
        if (showAddBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddBottomSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                AddEditReminderBottomSheetContent(
                    prayers = prayers,
                    userLanguageCode = userLanguageCode,
                    selectedPrayerId = selectedPrayerId,
                    isDaily = isDaily,
                    selectedDays = selectedDays,
                    selectedHour = selectedHour,
                    selectedMinute = selectedMinute,
                    onPrayerSelect = { selectedPrayerId = it },
                    onFrequencyChange = { isDaily = it },
                    onDaysChange = { selectedDays = it },
                    onOpenTimePicker = { showTimePickerDialog = true },
                    onSave = {
                        scope.launch {
                            val daysCsv = selectedDays.sorted().joinToString(",")
                            val newReminder = ReminderEntity(
                                prayerId = selectedPrayerId,
                                hour = selectedHour,
                                minute = selectedMinute,
                                isDaily = isDaily,
                                daysOfWeek = daysCsv,
                                isEnabled = true
                            )
                            val insertedId = repository.insertReminder(newReminder)
                            AlarmScheduler.schedule(context, newReminder.copy(id = insertedId.toInt()))
                            showAddBottomSheet = false
                        }
                    },
                    onDismiss = { showAddBottomSheet = false }
                )
            }
        }
    }
}

@Composable
fun ReminderCard(
    reminder: ReminderEntity,
    titleText: String,
    formattedTime: String,
    userLanguageCode: String,
    onToggleEnabled: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val weekDaysMap = when (userLanguageCode) {
        "pt" -> mapOf(1 to "Seg", 2 to "Ter", 3 to "Qua", 4 to "Qui", 5 to "Sex", 6 to "Sáb", 7 to "Dom")
        "es" -> mapOf(1 to "Lun", 2 to "Mar", 3 to "Mié", 4 to "Jue", 5 to "Vie", 6 to "Sáb", 7 to "Dom")
        "la" -> mapOf(1 to "Lun", 2 to "Mar", 3 to "Mer", 4 to "Iov", 5 to "Ven", 6 to "Sat", 7 to "Sol")
        else -> mapOf(1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu", 5 to "Fri", 6 to "Sat", 7 to "Sun")
    }

    val daysText = if (reminder.isDaily) {
        UiStrings.daily(userLanguageCode)
    } else {
        reminder.daysOfWeek.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .mapNotNull { weekDaysMap[it] }
            .joinToString(", ")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Section with padding
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = if (reminder.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(1.dp))

                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            // Full-width Edge-to-Edge Divider
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                thickness = 1.dp
            )

            // Bottom Row with padding
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Text(
                        text = daysText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = UiStrings.delete(userLanguageCode),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditReminderBottomSheetContent(
    prayers: List<PrayerWithTranslations>,
    userLanguageCode: String,
    selectedPrayerId: String?,
    isDaily: Boolean,
    selectedDays: Set<Int>,
    selectedHour: Int,
    selectedMinute: Int,
    onPrayerSelect: (String?) -> Unit,
    onFrequencyChange: (Boolean) -> Unit,
    onDaysChange: (Set<Int>) -> Unit,
    onOpenTimePicker: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    var expandedDropdown by remember { mutableStateOf(false) }

    val daysLabels = mapOf(
        1 to "Seg", 2 to "Ter", 3 to "Qua", 4 to "Qui", 5 to "Sex", 6 to "Sáb", 7 to "Dom"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = UiStrings.addReminder(userLanguageCode),
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
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = FontFamily.Default,
                        fontWeight = if (isDaily) FontWeight.Bold else FontWeight.Normal,
                        color = if (isDaily) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Weekly Button
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
                        text = UiStrings.weekly(userLanguageCode),
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = FontFamily.Default,
                        fontWeight = if (!isDaily) FontWeight.Bold else FontWeight.Normal,
                        color = if (!isDaily) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Days of week selector (if weekly)
        AnimatedVisibility(visible = !isDaily) {
            Column(modifier = Modifier.padding(top = 14.dp)) {
                Text(
                    text = UiStrings.selectDays(userLanguageCode),
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Default,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (1..7).forEach { day ->
                        val isSelected = selectedDays.contains(day)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                                .clickable {
                                    val updated = if (isSelected) selectedDays - day else selectedDays + day
                                    if (updated.isNotEmpty()) {
                                        onDaysChange(updated)
                                    }
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = daysLabels[day] ?: "$day",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Default,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = UiStrings.cancel(userLanguageCode),
                    fontFamily = FontFamily.Default
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = onSave,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = UiStrings.save(userLanguageCode),
                    fontFamily = FontFamily.Default
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    userLanguageCode: String,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(timePickerState.hour, timePickerState.minute)
                }
            ) {
                Text(
                    text = UiStrings.save(userLanguageCode),
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = UiStrings.cancel(userLanguageCode),
                    fontFamily = FontFamily.Default
                )
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(state = timePickerState)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}
