package com.mumu.app.ui.screens.reminders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mumu.app.data.model.RepeatType
import com.mumu.app.data.model.Task
import com.mumu.app.data.model.TaskType
import com.mumu.app.ui.components.*
import com.mumu.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    reminders: List<Task>,
    onToggleComplete: (Task) -> Unit,
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("recurring", "scheduled")

    val recurring = reminders.filter { it.type == TaskType.RECURRING_ALARM }
    val scheduled = reminders.filter { it.type == TaskType.SEMI_PASSIVE }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    text = "reminders",
                    style = MaterialTheme.typography.displayLarge,
                    color = OffWhite
                )
            }
        }

        // Tab row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, tab ->
                    FilterChip(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = {
                            Text(
                                text = tab,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
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
                            selected = selectedTab == index
                        )
                    )
                }
            }
        }

        // Content based on tab
        val displayList = if (selectedTab == 0) recurring else scheduled

        if (displayList.isNotEmpty()) {
            items(displayList, key = { it.id }) { task ->
                ReminderCard(
                    task = task,
                    onToggle = { onToggleComplete(task) },
                    onClick = { onTaskClick(task) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        } else {
            item {
                EmptyState(
                    emoji = if (selectedTab == 0) "🔔" else "💭",
                    message = if (selectedTab == 0) "No recurring reminders yet" else "No scheduled reminders"
                )
            }
        }
    }
}

@Composable
fun ReminderCard(
    task: Task,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val accentColor = if (task.type == TaskType.RECURRING_ALARM) Lavender else Mint
    val dimColor = if (task.type == TaskType.RECURRING_ALARM) LavenderDim else MintDim

    val repeatLabel = when (task.repeatType) {
        RepeatType.DAILY -> "Every day"
        RepeatType.WEEKLY -> {
            val dayNames = listOf("", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            val days = task.daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
            days.joinToString(", ") { dayNames.getOrElse(it) { "" } }
        }
        RepeatType.MONTHLY -> "Monthly on the ${task.monthDay}${ordinalSuffix(task.monthDay)}"
        RepeatType.NONE -> task.dueTime?.let { timeFormat.format(Date(it)) } ?: ""
    }

    TaskCard(
        task = task.copy(
            description = if (task.dueTime != null) {
                "${timeFormat.format(Date(task.dueTime))} · $repeatLabel"
            } else repeatLabel
        ),
        onChecked = { onToggle() },
        onClick = onClick,
        modifier = modifier
    )
}

private fun ordinalSuffix(day: Int): String {
    return when {
        day in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }
}
