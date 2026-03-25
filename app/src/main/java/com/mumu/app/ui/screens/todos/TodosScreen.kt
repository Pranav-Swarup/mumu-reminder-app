package com.mumu.app.ui.screens.todos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mumu.app.data.model.Task
import com.mumu.app.ui.components.*
import com.mumu.app.ui.theme.*

@Composable
fun TodosScreen(
    todos: List<Task>,
    onSwipeComplete: (Task) -> Unit,
    onSwipeDelete: (Task) -> Unit,
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val active = todos.filter { !it.completed }
    val done = todos.filter { it.completed }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    text = "todos",
                    style = MaterialTheme.typography.displayLarge,
                    color = OffWhite
                )
                if (active.isNotEmpty()) {
                    Text(
                        text = "${active.size} remaining",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Peach,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        if (active.isNotEmpty()) {
            itemsIndexed(active, key = { _, task -> task.id }) { _, task ->
                SwipeableTaskCard(
                    task = task,
                    onComplete = onSwipeComplete,
                    onDelete = onSwipeDelete,
                    onClick = onTaskClick,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        if (done.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "completed",
                    color = Mint.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            itemsIndexed(done, key = { _, task -> task.id }) { _, task ->
                SwipeableTaskCard(
                    task = task,
                    onComplete = onSwipeComplete,
                    onDelete = onSwipeDelete,
                    onClick = onTaskClick,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        if (todos.isEmpty()) {
            item {
                EmptyState(emoji = "✨", message = "Add todos to keep track of things")
            }
        }
    }
}
