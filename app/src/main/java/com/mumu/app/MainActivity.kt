package com.mumu.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mumu.app.ui.MainViewModel
import com.mumu.app.ui.components.AddBottomSheet
import com.mumu.app.ui.screens.notes.NotesScreen
import com.mumu.app.ui.screens.reminders.RemindersScreen
import com.mumu.app.ui.screens.today.TodayScreen
import com.mumu.app.ui.screens.todos.TodosScreen
import com.mumu.app.ui.theme.*

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _: Boolean -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            MuMuTheme {
                MuMuApp()
            }
        }
    }
}

@Composable
fun MuMuApp(vm: MainViewModel = viewModel()) {
    var currentTab by remember { mutableIntStateOf(0) }
    var showAddSheet by remember { mutableStateOf(false) }

    val todayTasks by vm.todayTasks.collectAsState()
    val todos by vm.todos.collectAsState()
    val reminders by vm.reminders.collectAsState()
    val notes by vm.notes.collectAsState()
    val searchQuery by vm.searchQuery.collectAsState()

    Scaffold(
        containerColor = SoftBlack,
        bottomBar = {
            MuMuBottomBar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                shape = CircleShape,
                containerColor = when (currentTab) {
                    0 -> UrgentRed
                    1 -> Peach
                    2 -> Lavender
                    3 -> SoftBlue
                    else -> Lavender
                },
                contentColor = SoftBlack,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
        ) {
            when (currentTab) {
                0 -> TodayScreen(
                    tasks = todayTasks,
                    onToggleComplete = { vm.toggleComplete(it) },
                    onTaskClick = { /* TODO: edit sheet */ }
                )
                1 -> TodosScreen(
                    todos = todos,
                    onToggleComplete = { vm.toggleComplete(it) },
                    onTaskClick = { /* TODO: edit sheet */ }
                )
                2 -> RemindersScreen(
                    reminders = reminders,
                    onToggleComplete = { vm.toggleComplete(it) },
                    onTaskClick = { /* TODO: edit sheet */ }
                )
                3 -> NotesScreen(
                    notes = notes,
                    searchQuery = searchQuery,
                    onSearchChange = { vm.searchNotes(it) },
                    onNoteClick = { /* TODO: note editor */ }
                )
            }
        }
    }

    if (showAddSheet) {
        AddBottomSheet(
            currentTab = currentTab,
            onDismiss = { showAddSheet = false },
            onAddTask = { vm.addTask(it) },
            onAddNote = { vm.addNote(it) }
        )
    }
}

@Composable
fun MuMuBottomBar(
    currentTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val items = listOf(
        NavItem("today", Icons.Rounded.WbSunny),
        NavItem("todos", Icons.Rounded.CheckCircleOutline),
        NavItem("reminders", Icons.Rounded.Notifications),
        NavItem("notes", Icons.Rounded.StickyNote2)
    )

    NavigationBar(
        containerColor = SurfaceDark,
        tonalElevation = 0.dp,
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = currentTab == index
            val tint = if (isSelected) {
                when (index) {
                    0 -> Peach
                    1 -> Mint
                    2 -> Lavender
                    3 -> SoftPink
                    else -> Lavender
                }
            } else MutedGray

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = tint
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = tint.copy(alpha = 0.1f)
                )
            )
        }
    }
}

private data class NavItem(val label: String, val icon: ImageVector)
