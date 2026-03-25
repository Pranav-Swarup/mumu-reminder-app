package com.mumu.app.ui.screens.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mumu.app.data.model.Note
import com.mumu.app.ui.components.*
import com.mumu.app.ui.theme.*

@Composable
fun NotesScreen(
    notes: List<Note>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onNoteClick: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    var isGrid by remember { mutableStateOf(true) }

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "notes",
                style = MaterialTheme.typography.displayLarge,
                color = OffWhite,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { isGrid = !isGrid }) {
                Icon(
                    if (isGrid) Icons.Rounded.ViewList else Icons.Rounded.GridView,
                    contentDescription = "Toggle view",
                    tint = MutedGray
                )
            }
        }

        // Search
        SoftTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = "Search notes...",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (notes.isEmpty()) {
            EmptyState(
                emoji = "📝",
                message = "Capture your thoughts here"
            )
        } else if (isGrid) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalItemSpacing = 8.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                items(notes, key = { it.id }) { note ->
                    NoteCard(
                        title = note.title,
                        preview = note.content,
                        colorIndex = note.color,
                        isLocked = note.isLocked,
                        onClick = { onNoteClick(note) }
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(notes, key = { it.id }) { note ->
                    NoteCard(
                        title = note.title,
                        preview = note.content,
                        colorIndex = note.color,
                        isLocked = note.isLocked,
                        onClick = { onNoteClick(note) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}
