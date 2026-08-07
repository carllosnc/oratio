package cnc.oratio.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import cnc.oratio.notification.AlarmScheduler
import cnc.oratio.ui.components.AddReminderBottomSheet
import cnc.oratio.ui.theme.GermaniaOneFontFamily
import cnc.oratio.ui.util.UiStrings
import cnc.oratio.ui.viewmodel.RemindersViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    viewModel: RemindersViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

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
    var selectedHour by remember { mutableIntStateOf(8) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var selectedPrayerId by remember { mutableStateOf<String?>(null) }
    var isDaily by remember { mutableStateOf(true) }
    var selectedDays by remember { mutableStateOf(setOf<Int>()) }
    var editingReminder by remember { mutableStateOf<ReminderEntity?>(null) }

    // TimePicker Dialog state
    var showTimePickerDialog by remember { mutableStateOf(false) }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val openAddForm = {
        editingReminder = null
        selectedHour = 8
        selectedMinute = 0
        selectedPrayerId = null
        isDaily = true
        selectedDays = emptySet()
        showAddBottomSheet = true
    }

    val openEditForm = { reminder: ReminderEntity ->
        editingReminder = reminder
        selectedHour = reminder.hour
        selectedMinute = reminder.minute
        selectedPrayerId = reminder.prayerId
        isDaily = reminder.isDaily
        selectedDays = if (reminder.daysOfWeek.isBlank()) emptySet()
        else reminder.daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        showAddBottomSheet = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = UiStrings.remindersTitle(uiState.userLanguageCode),
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 24.sp,
                        fontFamily = GermaniaOneFontFamily
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { openAddForm() }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Reminder",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Permission Banner if notification permission is missing on Android 13+
                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Enable Notifications",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = UiStrings.enableNotificationsBanner(uiState.userLanguageCode),
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
                                Text(UiStrings.allow(uiState.userLanguageCode))
                            }
                        }
                    }
                }

                // Reminders List / Loading State
                if (uiState.remindersState == null) {
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
                } else if (uiState.remindersState!!.isEmpty()) {
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
                                text = UiStrings.noReminders(uiState.userLanguageCode),
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
                        items(uiState.remindersState!!, key = { it.id }) { reminder ->
                            val prayer = uiState.prayers.find { it.prayer.id == reminder.prayerId }
                            ReminderCard(
                                reminder = reminder,
                                prayer = prayer,
                                userLanguageCode = uiState.userLanguageCode,
                                onEditClick = { openEditForm(reminder) },
                                onToggleEnabled = { isChecked ->
                                    viewModel.setReminderEnabled(reminder.id, isChecked) {
                                        val updated = reminder.copy(isEnabled = isChecked)
                                        if (isChecked) {
                                            AlarmScheduler.schedule(context, updated)
                                        } else {
                                            AlarmScheduler.cancel(context, updated.id)
                                        }
                                    }
                                },
                                onDeleteClick = {
                                    viewModel.deleteReminder(reminder) {
                                        AlarmScheduler.cancel(context, reminder.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Material 3 TimePicker Dialog
            if (showTimePickerDialog) {
                val timePickerState = rememberTimePickerState(
                    initialHour = selectedHour,
                    initialMinute = selectedMinute,
                    is24Hour = true
                )

                AlertDialog(
                    onDismissRequest = { showTimePickerDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            selectedHour = timePickerState.hour
                            selectedMinute = timePickerState.minute
                            showTimePickerDialog = false
                        }) {
                            Text(UiStrings.ok(uiState.userLanguageCode), fontFamily = FontFamily.Default)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTimePickerDialog = false }) {
                            Text(UiStrings.cancel(uiState.userLanguageCode), fontFamily = FontFamily.Default)
                        }
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TimePicker(state = timePickerState)
                        }
                    }
                )
            }

            // Add/Edit Bottom Sheet Modal
            if (showAddBottomSheet) {
                AddReminderBottomSheet(
                    sheetState = bottomSheetState,
                    selectedHour = selectedHour,
                    selectedMinute = selectedMinute,
                    selectedPrayerId = selectedPrayerId,
                    isDaily = isDaily,
                    selectedDays = selectedDays,
                    prayers = uiState.prayers,
                    userLanguageCode = uiState.userLanguageCode,
                    onDismiss = { showAddBottomSheet = false },
                    onOpenTimePicker = { showTimePickerDialog = true },
                    onPrayerSelect = { selectedPrayerId = it },
                    onFrequencyChange = { daily ->
                        isDaily = daily
                        if (daily) selectedDays = emptySet()
                    },
                    onDayToggle = { dayInt ->
                        selectedDays = if (selectedDays.contains(dayInt)) {
                            selectedDays - dayInt
                        } else {
                            selectedDays + dayInt
                        }
                    },
                    onSave = {
                        val daysString = if (isDaily) "" else selectedDays.sorted().joinToString(",")
                        val entity = ReminderEntity(
                            id = editingReminder?.id ?: 0,
                            prayerId = selectedPrayerId,
                            hour = selectedHour,
                            minute = selectedMinute,
                            isDaily = isDaily,
                            daysOfWeek = daysString,
                            isEnabled = true
                        )

                        if (editingReminder == null) {
                            viewModel.insertReminder(entity) { newId ->
                                val savedEntity = entity.copy(id = newId.toInt())
                                AlarmScheduler.schedule(context, savedEntity)
                                showAddBottomSheet = false
                            }
                        } else {
                            viewModel.updateReminder(entity) {
                                AlarmScheduler.schedule(context, entity)
                                showAddBottomSheet = false
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ReminderCard(
    reminder: ReminderEntity,
    prayer: PrayerWithTranslations?,
    userLanguageCode: String,
    onEditClick: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onDeleteClick: () -> Unit
) {
    val titleText = if (reminder.prayerId == null) {
        UiStrings.randomDailyPrayer(userLanguageCode)
    } else {
        prayer?.translations?.find { it.languageCode == userLanguageCode }?.title
            ?: prayer?.prayer?.defaultTitle
            ?: UiStrings.prayerNotFound(userLanguageCode)
    }

    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", reminder.hour, reminder.minute)

    val frequencyText = if (reminder.isDaily) {
        UiStrings.daily(userLanguageCode)
    } else if (reminder.daysOfWeek.isNotBlank()) {
        val daysMap = mapOf(
            "1" to UiStrings.sun(userLanguageCode),
            "2" to UiStrings.mon(userLanguageCode),
            "3" to UiStrings.tue(userLanguageCode),
            "4" to UiStrings.wed(userLanguageCode),
            "5" to UiStrings.thu(userLanguageCode),
            "6" to UiStrings.fri(userLanguageCode),
            "7" to UiStrings.sat(userLanguageCode)
        )
        reminder.daysOfWeek.split(",").mapNotNull { daysMap[it.trim()] }.joinToString(", ")
    } else {
        UiStrings.specificDays(userLanguageCode)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(1.dp))

                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = frequencyText,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Reminder",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
