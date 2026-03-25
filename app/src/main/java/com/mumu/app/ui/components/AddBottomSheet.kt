package com.mumu.app.ui.components

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
    currentTab: Int, // 0=today, 1=todos, 2=reminders, 3=notes
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
                defaultType = selectedType,
                onSelectType = { type ->
                    selectedType = type
                    mode = AddSheetMode.TASK_INPUT
                },
                onSelectNote = {
                    mode = AddSheetMode.NOTE_INPUT
                }
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

@Composable
private fun TypeSelectContent(
    defaultType: TaskType,
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

        TypeOption(
            emoji = "🔴",
            label = "Urgent task",
            subtitle = "Must do today, loud notification",
            color = UrgentRed,
            onClick = { onSelectType(TaskType.URGENT_PUSH) }
        )
        TypeOption(
            emoji = "🔔",
            label = "Recurring alarm",
            subtitle = "Daily, weekly, or monthly habit",
            color = Lavender,
            onClick = { onSelectType(TaskType.RECURRING_ALARM) }
        )
        TypeOption(
            emoji = "💭",
            label = "Gentle reminder",
            subtitle = "Soft nudge, silent or on-unlock",
            color = Mint,
            onClick = { onSelectType(TaskType.SEMI_PASSIVE) }
        )
        TypeOption(
            emoji = "📋",
            label = "Todo",
            subtitle = "Simple checklist item",
            color = Peach,
            onClick = { onSelectType(TaskType.PASSIVE_TODO) }
        )
        TypeOption(
            emoji = "📝",
            label = "Note",
            subtitle = "Capture a thought",
            color = SoftBlue,
            onClick = onSelectNote
        )

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
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = OffWhite
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MutedGray
            )
        }
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.6f))
        )
    }
}

@Composable
private fun TaskInputContent(
    type: TaskType,
    onBack: () -> Unit,
    onSave: (Task) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueTime by remember { mutableStateOf<Long?>(null) }
    var repeatType by remember { mutableStateOf(RepeatType.NONE) }
    var selectedDays by remember { mutableStateOf(setOf<Int>()) }
    var monthDay by remember { mutableStateOf(1) }
    var isPersistent by remember { mutableStateOf(false) }
    var isSilent by remember { mutableStateOf(false) }
    var showOnUnlock by remember { mutableStateOf(false) }

    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val dateTimeFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

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
            Text(
                text = typeName,
                style = MaterialTheme.typography.labelLarge,
                color = typeColor
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title input
        SoftTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = "What needs doing?",
            textStyle = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Description (optional)
        SoftTextField(
            value = description,
            onValueChange = { description = it },
            placeholder = "Details (optional)",
            singleLine = false
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Time picker (for all types except passive todo)
        if (type != TaskType.PASSIVE_TODO) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardDark)
                    .clickable {
                        val cal = Calendar.getInstance()
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                val timeCal = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, hour)
                                    set(Calendar.MINUTE, minute)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                // If time has passed today, set for tomorrow (for non-recurring)
                                if (timeCal.timeInMillis <= System.currentTimeMillis() && type == TaskType.URGENT_PUSH) {
                                    timeCal.add(Calendar.DAY_OF_YEAR, 1)
                                }
                                dueTime = timeCal.timeInMillis
                            },
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE),
                            false
                        ).show()
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Schedule, contentDescription = null, tint = typeColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = dueTime?.let { timeFormat.format(Date(it)) } ?: "Set time",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (dueTime != null) OffWhite else DimGray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Repeat options (for recurring alarms)
        if (type == TaskType.RECURRING_ALARM) {
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

            // Weekly day selector
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

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Type B extras
        if (type == TaskType.URGENT_PUSH) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Persistent notification",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OffWhite,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = isPersistent,
                    onCheckedChange = { isPersistent = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = UrgentRed,
                        checkedTrackColor = UrgentRed.copy(alpha = 0.3f),
                        uncheckedThumbColor = MutedGray,
                        uncheckedTrackColor = CardDark
                    )
                )
            }
        }

        // Type D extras
        if (type == TaskType.SEMI_PASSIVE) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Silent notification", style = MaterialTheme.typography.bodyLarge, color = OffWhite, modifier = Modifier.weight(1f))
                Switch(
                    checked = isSilent,
                    onCheckedChange = { isSilent = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Mint,
                        checkedTrackColor = Mint.copy(alpha = 0.3f),
                        uncheckedThumbColor = MutedGray,
                        uncheckedTrackColor = CardDark
                    )
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Show on unlock only", style = MaterialTheme.typography.bodyLarge, color = OffWhite, modifier = Modifier.weight(1f))
                Switch(
                    checked = showOnUnlock,
                    onCheckedChange = { showOnUnlock = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Mint,
                        checkedTrackColor = Mint.copy(alpha = 0.3f),
                        uncheckedThumbColor = MutedGray,
                        uncheckedTrackColor = CardDark
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save button
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
                            isSilent = isSilent,
                            showOnUnlockOnly = showOnUnlock
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
            Text(
                text = "Save",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

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

        // Color picker
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
            modifier = Modifier.fillMaxWidth().height(52.dp),
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
