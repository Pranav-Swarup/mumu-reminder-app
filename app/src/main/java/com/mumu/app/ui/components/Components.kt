package com.mumu.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mumu.app.data.model.Task
import com.mumu.app.data.model.TaskType
import com.mumu.app.ui.theme.*

// ─── Task Card ───
@Composable
fun TaskCard(
    task: Task,
    onChecked: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = when (task.type) {
        TaskType.URGENT_PUSH -> UrgentRed
        TaskType.RECURRING_ALARM -> Lavender
        TaskType.SEMI_PASSIVE -> Mint
        TaskType.PASSIVE_TODO -> Peach
    }
    val dimColor = when (task.type) {
        TaskType.URGENT_PUSH -> PinkDim
        TaskType.RECURRING_ALARM -> LavenderDim
        TaskType.SEMI_PASSIVE -> MintDim
        TaskType.PASSIVE_TODO -> PeachDim
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = dimColor.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (task.completed) accentColor.copy(alpha = 0.8f)
                        else Color.Transparent
                    )
                    .then(
                        if (!task.completed) Modifier.background(
                            color = Color.Transparent
                        ) else Modifier
                    )
                    .clickable { onChecked(!task.completed) },
                contentAlignment = Alignment.Center
            ) {
                if (task.completed) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = "Done",
                        tint = SoftBlack,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.Transparent)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.2f))
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (task.completed) MutedGray else OffWhite,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Type indicator dot
            if (task.type == TaskType.URGENT_PUSH) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(UrgentRed)
                )
            }
        }
    }
}

// ─── Note Card ───
@Composable
fun NoteCard(
    title: String,
    preview: String,
    colorIndex: Int,
    isLocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = PastelColors.getOrElse(colorIndex) { Lavender }
    val dimColor = DimColors.getOrElse(colorIndex % DimColors.size) { LavenderDim }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = dimColor.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Color dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = OffWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (isLocked) {
                    Icon(
                        Icons.Rounded.Lock,
                        contentDescription = "Locked",
                        tint = MutedGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            if (preview.isNotBlank() && !isLocked) {
                Text(
                    text = preview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedGray,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else if (isLocked) {
                Text(
                    text = "Locked note",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedGray.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

// ─── Section Header ───
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    color: Color = MutedGray,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = color,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = MaterialTheme.typography.labelLarge.letterSpacing
        )
        Spacer(modifier = Modifier.weight(1f))
        trailingContent?.invoke()
    }
}

// ─── Empty State ───
@Composable
fun EmptyState(
    emoji: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 40.sp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MutedGray
        )
    }
}

// sp is imported via androidx.compose.ui.unit.sp

// ─── Mini Tag Chip ───
@Composable
fun TagChip(
    text: String,
    color: Color = Lavender,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// ─── Soft Text Field ───
@Composable
fun SoftTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        textStyle = textStyle.copy(color = OffWhite),
        cursorBrush = SolidColor(Lavender),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle,
                        color = DimGray
                    )
                }
                innerTextField()
            }
        }
    )
}
