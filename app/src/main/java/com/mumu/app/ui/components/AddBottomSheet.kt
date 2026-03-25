package com.mumu.app.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mumu.app.data.model.*
import com.mumu.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class AddSheetMode {
    TYPE_SELECT, TASK_INPUT, NOTE_INPUT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBottomSheet(
    currentTab: Int,
    onDismiss: () -> Unit,
    onAddTask: (Task) -> Unit,
    onAddNote: (Note) -> Unit
) {
    var mode by remember {
        mutableStateOf(
            when (currentTab) {
                3 -> AddSheetMode.NOTE_INPUT
                else -> AddSheetMode.TYPE_SELECT
            }
        )
    }
    var selectedType by remember {
        mutableStateOf(
            when (currentTab) {
                0 -> TaskType.URGENT_PUSH
                1 -> TaskType.PASSIVE_TODO
                2 -> TaskType.RECURRING_ALARM
                else -> TaskType.PASSIVE_TODO
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SoftBlack,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DimGray)
            )
        }
    ) {
        when (mode) {
            AddSheetMode.TYPE_SELECT -> TypeSelectContent(
                onSelectType = { type ->
                    selectedType = type
                    mode = AddSheetMode.TASK_INPUT
                },
                onSelectNote = { mode = AddSheetMode.NOTE_INPUT }
            )
            AddSheetMode.TASK_INPUT -> TaskInputContent(
                type = selectedType,
                onBack = { mode = AddSheetMode.TYPE_SELECT },
                onSave = { task ->
                    onAddTask(task)
                    onDismiss()
                }
            )
            AddSheetMode.NOTE_INPUT -> NoteInputContent(
                onSave = { note ->
                    onAddNote(note)
                    onDismiss()
                }
            )
        }
    }
}

// ─── Type Selector ───

@Composable
private fun TypeSelectContent(
    onSelectType: (TaskType) -> Unit,
    onSelectNote: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .navigationBarsPadding()
    ) {
        Text(
            text = "what kind?",
            style = MaterialTheme.typography.headlineMedium,
            color = OffWhite,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        TypeOption("🔴", "Urgent task", "Must do today, loud notification", UrgentRed) {
            onSelectType(TaskType.URGENT_PUSH)
        }
        TypeOption("🔔", "Recurring alarm", "Daily, weekly, or monthly habit", Lavender) {
            onSelectType(TaskType.RECURRING_ALARM)
        }
        TypeOption("💭", "Gentle reminder", "Future date, choose alert or passive", Mint) {
            onSelectType(TaskType.SEMI_PASSIVE)
        }
        TypeOption("📋", "Todo", "Simple checklist item", Peach) {
            onSelectType(TaskType.PASSIVE_TODO)
        }
        TypeOption("📝", "Note", "Capture a thought", SoftBlue) {
            onSelectNote()
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun TypeOption(
    emoji: String,
    label: String,
    subtitle: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.titleMedium, color = OffWhite)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MutedGray)
        }
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.6f))
        )
    }
}

// ─── Task Input ───

@Composable
private fun TaskInputContent(
    type: TaskType,
    onBack: () -> Unit,
    onSave: (Task) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Date + time state
    var selectedDate by remember { mutableStateOf<Calendar?>(null) }
    var selectedHour by remember { mutableIntStateOf(-1) }
    var selectedMinute by remember { mutableIntStateOf(-1) }
    val hasTime = selectedHour >= 0

    // Compute dueTime from date + time
    val dueTime: Long? = remember(selectedDate, selectedHour, selectedMinute) {
        val date = selectedDate ?: return@remember null
        if (!hasTime) return@remember null
        Calendar.getInstance().apply {
            set(Calendar.YEAR, date.get(Calendar.YEAR))
            set(Calendar.MONTH, date.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, selectedHour)
            set(Calendar.MINUTE, selectedMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    // Recurring options
    var repeatType by remember { mutableStateOf(RepeatType.NONE) }
    var selectedDays by remember { mutableStateOf(setOf<Int>()) }
    var monthDay by remember { mutableIntStateOf(1) }

    // Notification controls
    var notifMode by remember { mutableStateOf(NotificationMode.ALERT) }
    var isAnonymous by remember { mutableStateOf(false) }
    var isPersistent by remember { mutableStateOf(false) }
    var showOnUnlock by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("EEE, MMM d", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    val typeName = when (type) {
        TaskType.URGENT_PUSH -> "urgent task"
        TaskType.RECURRING_ALARM -> "recurring alarm"
        TaskType.SEMI_PASSIVE -> "gentle reminder"
        TaskType.PASSIVE_TODO -> "todo"
    }
    val typeColor = when (type) {
        TaskType.URGENT_PUSH -> UrgentRed
        TaskType.RECURRING_ALARM -> Lavender
        TaskType.SEMI_PASSIVE -> Mint
        TaskType.PASSIVE_TODO -> Peach
    }

    // Whether this type supports scheduling
    val showScheduling = type != TaskType.PASSIVE_TODO

    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Back + type label
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = MutedGray)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = typeName, style = MaterialTheme.typography.labelLarge, color = typeColor)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        SoftTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = "What needs doing?",
            textStyle = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Description
        SoftTextField(
            value = description,
            onValueChange = { description = it },
            placeholder = "Details (optional)",
            singleLine = false
        )

        // ─── Date & Time Pickers ───
        if (showScheduling) {
            Spacer(modifier = Modifier.height(16.dp))

            // Date picker row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardDark)
                    .clickable {
                        val now = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                selectedDate = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DAY_OF_MONTH, day)
                                }
                            },
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH)
                        ).apply {
                            datePicker.minDate = System.currentTimeMillis() - 1000
                        }.show()
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = typeColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = selectedDate?.let { dateFormat.format(it.time) } ?: "Pick a date",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selectedDate != null) OffWhite else DimGray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Time picker row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardDark)
                    .clickable {
                        val now = Calendar.getInstance()
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                selectedHour = hour
                                selectedMinute = minute
                                // Auto-set date to today if not picked yet
                                if (selectedDate == null) {
                                    selectedDate = Calendar.getInstance()
                                }
                            },
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            false
                        ).show()
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Schedule, contentDescription = null, tint = typeColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (hasTime) {
                        val cal = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, selectedHour)
                            set(Calendar.MINUTE, selectedMinute)
                        }
                        timeFormat.format(cal.time)
                    } else "Set time",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (hasTime) OffWhite else DimGray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── Notification Mode (for all scheduled types) ───
            Text(
                text = "NOTIFICATION STYLE",
                style = MaterialTheme.typography.labelLarge,
                color = MutedGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NotifModeChip(
                    label = "Alert",
                    emoji = "🔔",
                    selected = notifMode == NotificationMode.ALERT,
                    color = typeColor
                ) { notifMode = NotificationMode.ALERT }

                NotifModeChip(
                    label = "Passive",
                    emoji = "💤",
                    selected = notifMode == NotificationMode.PASSIVE,
                    color = typeColor
                ) { notifMode = NotificationMode.PASSIVE }
            }

            // Passive explanation
            if (notifMode == NotificationMode.PASSIVE) {
                Text(
                    text = "Shows silently when you open your phone around the set time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedGray,
                    modifier = Modifier.padding(top = 6.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ─── Anonymous toggle ───
            ToggleRow(
                label = "Anonymous notification",
                subtitle = "Hides content — says \"something pending\"",
                checked = isAnonymous,
                onCheckedChange = { isAnonymous = it },
                color = typeColor
            )
        }

        // ─── Type-specific extras ───

        // Recurring alarm: repeat options
        if (type == TaskType.RECURRING_ALARM) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "REPEAT",
                style = MaterialTheme.typography.labelLarge,
                color = MutedGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Daily" to RepeatType.DAILY, "Weekly" to RepeatType.WEEKLY, "Monthly" to RepeatType.MONTHLY).forEach { (label, rt) ->
                    FilterChip(
                        selected = repeatType == rt,
                        onClick = { repeatType = rt },
                        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Lavender.copy(alpha = 0.2f),
                            selectedLabelColor = Lavender,
                            containerColor = CardDark,
                            labelColor = MutedGray
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = DimGray.copy(alpha = 0.3f),
                            selectedBorderColor = Lavender.copy(alpha = 0.3f),
                            enabled = true,
                            selected = repeatType == rt
                        )
                    )
                }
            }

            if (repeatType == RepeatType.WEEKLY) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("S" to 1, "M" to 2, "T" to 3, "W" to 4, "T" to 5, "F" to 6, "S" to 7).forEach { (label, day) ->
                        val isSelected = day in selectedDays
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Lavender.copy(alpha = 0.3f) else CardDark)
                                .clickable {
                                    selectedDays = if (isSelected) selectedDays - day else selectedDays + day
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) Lavender else MutedGray
                            )
                        }
                    }
                }
            }
        }

        // Urgent: persistent toggle
        if (type == TaskType.URGENT_PUSH) {
            Spacer(modifier = Modifier.height(8.dp))
            ToggleRow(
                label = "Persistent notification",
                subtitle = "Can't swipe away until marked done",
                checked = isPersistent,
                onCheckedChange = { isPersistent = it },
                color = UrgentRed
            )
        }

        // Semi-passive: on-unlock toggle
        if (type == TaskType.SEMI_PASSIVE && notifMode == NotificationMode.PASSIVE) {
            Spacer(modifier = Modifier.height(4.dp))
            ToggleRow(
                label = "Show on unlock only",
                subtitle = "Appears when you open your phone",
                checked = showOnUnlock,
                onCheckedChange = { showOnUnlock = it },
                color = Mint
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ─── Save Button ───
        Button(
            onClick = {
                if (title.isNotBlank()) {
                    onSave(
                        Task(
                            title = title.trim(),
                            description = description.trim(),
                            type = type,
                            dueTime = dueTime,
                            priority = if (type == TaskType.URGENT_PUSH) 2 else 0,
                            repeatType = if (type == TaskType.RECURRING_ALARM) repeatType else RepeatType.NONE,
                            daysOfWeek = selectedDays.sorted().joinToString(","),
                            monthDay = monthDay,
                            isPersistentNotification = isPersistent,
                            isSilent = notifMode == NotificationMode.PASSIVE,
                            showOnUnlockOnly = showOnUnlock,
                            notificationMode = notifMode,
                            isAnonymous = isAnonymous
                        )
                    )
                }
            },
            enabled = title.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = typeColor,
                contentColor = SoftBlack,
                disabledContainerColor = typeColor.copy(alpha = 0.2f),
                disabledContentColor = MutedGray
            )
        ) {
            Text("Save", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ─── Notification Mode Chip ───

@Composable
private fun NotifModeChip(
    label: String,
    emoji: String,
    selected: Boolean,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (selected) color.copy(alpha = 0.2f) else CardDark
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) color else MutedGray
            )
        }
    }
}

// ─── Toggle Row (reusable) ───

@Composable
private fun ToggleRow(
    label: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = OffWhite)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedGray,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = color,
                checkedTrackColor = color.copy(alpha = 0.3f),
                uncheckedThumbColor = MutedGray,
                uncheckedTrackColor = CardDark
            )
        )
    }
}

// ─── Note Input ───

@Composable
private fun NoteInputContent(onSave: (Note) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedColor by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .navigationBarsPadding()
    ) {
        Text(
            text = "new note",
            style = MaterialTheme.typography.headlineMedium,
            color = OffWhite,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        SoftTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = "Title",
            textStyle = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        SoftTextField(
            value = content,
            onValueChange = { content = it },
            placeholder = "Write something...",
            singleLine = false
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "COLOR",
            style = MaterialTheme.typography.labelLarge,
            color = MutedGray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PastelColors.forEachIndexed { index, color ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = if (selectedColor == index) 1f else 0.4f))
                        .clickable { selectedColor = index },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedColor == index) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(SoftBlack)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (title.isNotBlank()) {
                    onSave(Note(title = title.trim(), content = content.trim(), color = selectedColor))
                }
            },
            enabled = title.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PastelColors[selectedColor],
                contentColor = SoftBlack,
                disabledContainerColor = PastelColors[selectedColor].copy(alpha = 0.2f),
                disabledContentColor = MutedGray
            )
        ) {
            Text("Save", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
