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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Search
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

        val isPremium = FeatureGate.isEnabled(FeatureGate.Feature.CUSTOM_DICTIONARY)
        val maxWords = if (isPremium) Int.MAX_VALUE else PersonalWordList.FREE_WORD_LIMIT
        val sharedPrefs = getSharedPreferences(GrammarKeyboardService.WORD_LIST_PREFS, Context.MODE_PRIVATE)
        val wordList = PersonalWordList(maxSize = maxWords)
        sharedPrefs.getString(GrammarKeyboardService.WORD_LIST_KEY, null)?.let { wordList.loadFromJson(it) }

        fun persist() {
            sharedPrefs.edit().putString(GrammarKeyboardService.WORD_LIST_KEY, wordList.toJson()).apply()
        }

        setContent {
            TyplxKeyboardTheme {
                WordListScreen(
                    wordList = wordList,
                    isPremium = isPremium,
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
    isPremium: Boolean,
    onPersist: () -> Unit,
    onBack: () -> Unit,
) {
    var words by remember { mutableStateOf(wordList.getAll()) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val displayedWords = if (searchQuery.isBlank()) words
    else words.filter { it.contains(searchQuery.trim(), ignoreCase = true) }

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
            // Word count row with free-tier limit indicator
            val countLabel = if (isPremium) {
                "${words.size} word${if (words.size == 1) "" else "s"}"
            } else {
                "${words.size}/${PersonalWordList.FREE_WORD_LIMIT} words · Free"
            }
            Text(
                text = countLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            // Search bar (visible only when there are words to search)
            if (words.isNotEmpty()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search words") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            if (displayedWords.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    val emptyText = when {
                        searchQuery.isNotBlank() -> "No words match \"$searchQuery\"."
                        else -> "No words yet.\nTap + to add words the grammar engine should ignore."
                    }
                    Text(
                        text = emptyText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(32.dp),
                    )
                }
            } else {
                LazyColumn {
                    items(displayedWords, key = { it }) { word ->
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
            onTryAdd = { input ->
                when {
                    wordList.contains(input) ->
                        "\"$input\" is already in your dictionary"
                    !isPremium && wordList.size >= PersonalWordList.FREE_WORD_LIMIT ->
                        "Free tier allows up to ${PersonalWordList.FREE_WORD_LIMIT} custom words. Upgrade to Pro for unlimited."
                    else -> {
                        wordList.add(input)
                        words = wordList.getAll()
                        onPersist()
                        null // success — dialog closes
                    }
                }
            },
            onDismiss = { showAddDialog = false },
        )
    }
}

@Composable
private fun AddWordDialog(
    onTryAdd: (String) -> String?, // null = success; non-null = error message to display
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
                if (trimmed.isEmpty()) {
                    error = "Word cannot be empty"
                } else {
                    val result = onTryAdd(trimmed)
                    if (result == null) {
                        onDismiss()
                    } else {
                        error = result
                    }
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
