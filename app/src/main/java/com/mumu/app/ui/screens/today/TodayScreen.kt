package com.mumu.app.ui.screens.today

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mumu.app.data.model.Task
import com.mumu.app.data.model.TaskType
import com.mumu.app.ui.components.*
import com.mumu.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TodayScreen(
    tasks: List<Task>,
    onToggleComplete: (Task) -> Unit,
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("EEEE, MMM d", Locale.getDefault()) }
    val today = remember { dateFormat.format(Date()) }

    val mustDo = tasks.filter { it.type == TaskType.URGENT_PUSH && !it.completed }
    val laterToday = tasks.filter { it.type != TaskType.URGENT_PUSH && !it.completed }
    val completed = tasks.filter { it.completed }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    text = "today",
                    style = MaterialTheme.typography.displayLarge,
                    color = OffWhite
                )
                Text(
                    text = today,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedGray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Must Do section
        if (mustDo.isNotEmpty()) {
            item {
                SectionHeader(title = "must do", color = UrgentRed)
            }
            items(mustDo, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onChecked = { onToggleComplete(task) },
                    onClick = { onTaskClick(task) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        // Later Today section
        if (laterToday.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "later today",
                    color = Lavender,
                    modifier = Modifier.padding(top = if (mustDo.isNotEmpty()) 16.dp else 0.dp)
                )
            }
            items(laterToday, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onChecked = { onToggleComplete(task) },
                    onClick = { onTaskClick(task) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        // Completed
        if (completed.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "done",
                    color = Mint.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            items(completed, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onChecked = { onToggleComplete(task) },
                    onClick = { onTaskClick(task) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        // Empty state
        if (tasks.isEmpty()) {
            item {
                EmptyState(
                    emoji = "🌸",
                    message = "Nothing for today — enjoy the calm"
                )
            }
        }
    }
}
