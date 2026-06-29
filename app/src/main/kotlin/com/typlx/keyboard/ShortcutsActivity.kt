package com.typlx.keyboard

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.typlx.keyboard.ui.theme.TyplxKeyboardTheme

class ShortcutsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPrefs = getSharedPreferences(GrammarKeyboardService.SHORTCUTS_PREFS, Context.MODE_PRIVATE)
        val manager = TextShortcutsManager()
        val stored = sharedPrefs.getString(GrammarKeyboardService.SHORTCUTS_KEY, null)
        if (stored != null) {
            manager.loadFromJson(stored)
        } else {
            TextShortcutsManager.defaults().forEach { manager.add(it.shortcut, it.expansion) }
        }

        fun persist() {
            sharedPrefs.edit().putString(GrammarKeyboardService.SHORTCUTS_KEY, manager.toJson()).apply()
        }

        setContent {
            TyplxKeyboardTheme {
                ShortcutsScreen(
                    manager = manager,
                    onPersist = ::persist,
                    onBack = { finish() },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShortcutsScreen(
    manager: TextShortcutsManager,
    onPersist: () -> Unit,
    onBack: () -> Unit,
) {
    var shortcuts by remember { mutableStateOf(manager.getAll()) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Text shortcuts") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add shortcut")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (shortcuts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No shortcuts yet.\nTap + to add one — type the shortcut and press space to expand it.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(32.dp),
                    )
                }
            } else {
                Text(
                    text = "${shortcuts.size} shortcut${if (shortcuts.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                LazyColumn {
                    items(shortcuts, key = { it.shortcut }) { sc ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = sc.shortcut,
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = sc.expansion,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            trailingContent = {
                                IconButton(onClick = {
                                    manager.remove(sc.shortcut)
                                    shortcuts = manager.getAll()
                                    onPersist()
                                }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Remove ${sc.shortcut}",
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                }
                            },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddShortcutDialog(
            onConfirm = { shortcut, expansion ->
                manager.add(shortcut, expansion)
                shortcuts = manager.getAll()
                onPersist()
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false },
        )
    }
}

@Composable
private fun AddShortcutDialog(
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var shortcut by remember { mutableStateOf("") }
    var expansion by remember { mutableStateOf("") }
    var shortcutError by remember { mutableStateOf<String?>(null) }
    var expansionError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add shortcut") },
        text = {
            Column {
                Text(
                    text = "Type the shortcut, press space while typing to expand it automatically.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = shortcut,
                    onValueChange = {
                        shortcut = it.trim().lowercase()
                        shortcutError = null
                    },
                    label = { Text("Shortcut (e.g. omw)") },
                    isError = shortcutError != null,
                    supportingText = shortcutError?.let { e -> { Text(e) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = expansion,
                    onValueChange = {
                        expansion = it
                        expansionError = null
                    },
                    label = { Text("Expansion (e.g. On my way!)") },
                    isError = expansionError != null,
                    supportingText = expansionError?.let { e -> { Text(e) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val s = shortcut.trim()
                val e = expansion.trim()
                var ok = true
                if (s.isEmpty()) { shortcutError = "Shortcut cannot be empty"; ok = false }
                if (e.isEmpty()) { expansionError = "Expansion cannot be empty"; ok = false }
                if (ok) onConfirm(s, e)
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
