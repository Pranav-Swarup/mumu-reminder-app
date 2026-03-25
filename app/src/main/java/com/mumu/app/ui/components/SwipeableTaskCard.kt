package com.mumu.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.mumu.app.data.model.Task
import com.mumu.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTaskCard(
    task: Task,
    onComplete: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    // Swipe right → complete
                    onComplete(task)
                    true
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    // Swipe left → delete
                    onDelete(task)
                    true
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> Mint.copy(alpha = 0.3f)
                    SwipeToDismissBoxValue.EndToStart -> UrgentRed.copy(alpha = 0.3f)
                    SwipeToDismissBoxValue.Settled -> SoftBlack
                },
                label = "bg"
            )
            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Rounded.Check
                SwipeToDismissBoxValue.EndToStart -> Icons.Rounded.Delete
                else -> Icons.Rounded.Check
            }
            val iconTint = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Mint
                SwipeToDismissBoxValue.EndToStart -> UrgentRed
                else -> MutedGray
            }
            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                else -> Alignment.CenterEnd
            }
            val scale by animateFloatAsState(
                if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) 0.6f else 1f,
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(horizontal = 24.dp),
                contentAlignment = alignment
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.scale(scale)
                )
            }
        },
        content = {
            TaskCard(
                task = task,
                onChecked = { onComplete(task) },
                onClick = { onClick(task) }
            )
        }
    )
}
