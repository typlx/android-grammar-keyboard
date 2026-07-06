package com.typlx.keyboard

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.typlx.keyboard.ui.theme.TyplxKeyboardTheme

class WordListActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPrefs = getSharedPreferences(GrammarKeyboardService.WORD_LIST_PREFS, Context.MODE_PRIVATE)
        val wordList = PersonalWordList()
        sharedPrefs.getString(GrammarKeyboardService.WORD_LIST_KEY, null)?.let { wordList.loadFromJson(it) }

        fun persist() {
            sharedPrefs.edit().putString(GrammarKeyboardService.WORD_LIST_KEY, wordList.toJson()).apply()
        }

        setContent {
            TyplxKeyboardTheme {
                WordListScreen(
                    wordList = wordList,
                    onPersist = ::persist,
                    onBack = { finish() },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WordListScreen(
    wordList: PersonalWordList,
    onPersist: () -> Unit,
    onBack: () -> Unit,
) {
    var words by remember { mutableStateOf(wordList.getAll()) }
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.writer().use { it.write(wordList.toExportText()) }
            }
            Toast.makeText(context, "Exported ${words.size} word(s)", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val added = context.contentResolver.openInputStream(uri)?.use { stream ->
                wordList.importFromText(stream.bufferedReader().readText())
            } ?: 0
            words = wordList.getAll()
            onPersist()
            Toast.makeText(context, "Added $added word(s)", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(context, "Import failed", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal word list") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { importLauncher.launch(arrayOf("text/plain")) }) {
                        Icon(Icons.Default.FileUpload, contentDescription = "Import words")
                    }
                    IconButton(onClick = { exportLauncher.launch("typlx_words.txt") }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export words")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add word")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (words.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No words yet.\nTap + to add words the grammar engine should ignore.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(32.dp),
                    )
                }
            } else {
                Text(
                    text = "${words.size} word${if (words.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                LazyColumn {
                    items(words, key = { it }) { word ->
                        ListItem(
                            headlineContent = { Text(word) },
                            trailingContent = {
                                IconButton(onClick = {
                                    wordList.remove(word)
                                    words = wordList.getAll()
                                    onPersist()
                                }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Remove $word",
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
        AddWordDialog(
            onConfirm = { input ->
                wordList.add(input)
                words = wordList.getAll()
                onPersist()
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false },
        )
    }
}

@Composable
private fun AddWordDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add word") },
        text = {
            Column {
                Text(
                    text = "Enter a word or phrase that grammar correction should never change.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        error = null
                    },
                    label = { Text("Word or phrase") },
                    isError = error != null,
                    supportingText = error?.let { e -> { Text(e) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val trimmed = text.trim()
                when {
                    trimmed.isEmpty() -> error = "Word cannot be empty"
                    else -> onConfirm(trimmed)
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
