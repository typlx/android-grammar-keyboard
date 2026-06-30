package com.typlx.keyboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.typlx.keyboard.ui.theme.ThemePreset
import com.typlx.keyboard.ui.theme.TyplxKeyboardTheme
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!OnboardingManager(this).isComplete()) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        val prefsManager = PreferencesManager(this)

        setContent {
            TyplxKeyboardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    SettingsScreen(
                        prefsManager = prefsManager,
                        onOpenImeSettings = {
                            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    prefsManager: PreferencesManager,
    onOpenImeSettings: () -> Unit,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var apiUrl by remember { mutableStateOf(prefsManager.apiUrl) }
    var model by remember { mutableStateOf(prefsManager.model) }
    var apiToken by remember { mutableStateOf(prefsManager.apiToken) }
    var tokenVisible by remember { mutableStateOf(false) }
    var hapticEnabled by remember { mutableStateOf(prefsManager.hapticFeedbackEnabled) }
    var autoSuggestEnabled by remember { mutableStateOf(prefsManager.autoSuggestEnabled) }

    var apiUrlError by remember { mutableStateOf<String?>(null) }
    var modelError by remember { mutableStateOf<String?>(null) }
    var apiTokenError by remember { mutableStateOf<String?>(null) }

    var wordListCount by remember {
        val prefs = context.getSharedPreferences(GrammarKeyboardService.WORD_LIST_PREFS, Context.MODE_PRIVATE)
        val wl = PersonalWordList()
        prefs.getString(GrammarKeyboardService.WORD_LIST_KEY, null)?.let { wl.loadFromJson(it) }
        mutableIntStateOf(wl.size)
    }

    var shortcutsCount by remember {
        val prefs = context.getSharedPreferences(GrammarKeyboardService.SHORTCUTS_PREFS, Context.MODE_PRIVATE)
        val sm = TextShortcutsManager()
        val stored = prefs.getString(GrammarKeyboardService.SHORTCUTS_KEY, null)
        if (stored != null) sm.loadFromJson(stored)
        else TextShortcutsManager.defaults().forEach { sm.add(it.shortcut, it.expansion) }
        mutableIntStateOf(sm.size)
    }

    var themePreset by remember { mutableStateOf(prefsManager.themePreset) }
    var cornerRadiusDp by remember { mutableIntStateOf(prefsManager.cornerRadiusDp) }
    var keyAlphaPercent by remember { mutableIntStateOf(prefsManager.keyAlphaPercent) }
    var selectedLayoutId by remember { mutableStateOf(prefsManager.keyboardLayoutId) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // API URL field
            OutlinedTextField(
                value = apiUrl,
                onValueChange = {
                    apiUrl = it
                    apiUrlError = null
                },
                label = { Text(stringResource(R.string.settings_api_url_label)) },
                placeholder = { Text(stringResource(R.string.settings_api_url_hint)) },
                isError = apiUrlError != null,
                supportingText = apiUrlError?.let { error -> { Text(error) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Model field
            OutlinedTextField(
                value = model,
                onValueChange = {
                    model = it
                    modelError = null
                },
                label = { Text(stringResource(R.string.settings_model_label)) },
                placeholder = { Text(stringResource(R.string.settings_model_hint)) },
                isError = modelError != null,
                supportingText = modelError?.let { error -> { Text(error) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // API Token field (masked, with visibility toggle)
            OutlinedTextField(
                value = apiToken,
                onValueChange = {
                    apiToken = it
                    apiTokenError = null
                },
                label = { Text(stringResource(R.string.settings_api_token_label)) },
                placeholder = { Text(stringResource(R.string.settings_api_token_hint)) },
                isError = apiTokenError != null,
                supportingText = apiTokenError?.let { error -> { Text(error) } },
                singleLine = true,
                visualTransformation = if (tokenVisible) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { tokenVisible = !tokenVisible }) {
                        Icon(
                            imageVector = if (tokenVisible) Icons.Default.VisibilityOff
                                          else Icons.Default.Visibility,
                            contentDescription = null,
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Haptic feedback toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_haptic_label),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = stringResource(R.string.settings_haptic_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = hapticEnabled,
                    onCheckedChange = {
                        hapticEnabled = it
                        prefsManager.hapticFeedbackEnabled = it
                    },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Auto-suggest toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_auto_suggest_label),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = stringResource(R.string.settings_auto_suggest_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = autoSuggestEnabled,
                    onCheckedChange = {
                        autoSuggestEnabled = it
                        prefsManager.autoSuggestEnabled = it
                    },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Personal word list navigation row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        context.startActivity(Intent(context, WordListActivity::class.java))
                    }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Personal word list",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = if (wordListCount == 0) "No custom words yet"
                               else "$wordListCount word${if (wordListCount == 1) "" else "s"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Text shortcuts navigation row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        context.startActivity(Intent(context, ShortcutsActivity::class.java))
                    }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Text shortcuts",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = if (shortcutsCount == 0) "No shortcuts yet"
                               else "$shortcutsCount shortcut${if (shortcutsCount == 1) "" else "s"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Appearance section
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Theme",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ThemePreset.entries.forEach { preset ->
                    val label = when (preset) {
                        ThemePreset.SYSTEM -> "System"
                        ThemePreset.DARK -> "Dark"
                        ThemePreset.LIGHT -> "Light"
                        ThemePreset.AMOLED -> "AMOLED"
                    }
                    FilterChip(
                        selected = themePreset == preset,
                        onClick = {
                            themePreset = preset
                            prefsManager.themePreset = preset
                        },
                        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Keyboard layout",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ALL_LAYOUTS.forEach { layout ->
                    FilterChip(
                        selected = selectedLayoutId == layout.id,
                        onClick = {
                            selectedLayoutId = layout.id
                            prefsManager.keyboardLayoutId = layout.id
                        },
                        label = { Text(layout.displayName, style = MaterialTheme.typography.labelMedium) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Key corner radius",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                Text(
                    text = "${cornerRadiusDp}dp",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Slider(
                value = cornerRadiusDp.toFloat(),
                onValueChange = { cornerRadiusDp = it.toInt() },
                onValueChangeFinished = { prefsManager.cornerRadiusDp = cornerRadiusDp },
                valueRange = 0f..16f,
                steps = 15,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Key opacity",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                Text(
                    text = "$keyAlphaPercent%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Slider(
                value = keyAlphaPercent.toFloat(),
                onValueChange = { keyAlphaPercent = it.toInt() },
                onValueChangeFinished = { prefsManager.keyAlphaPercent = keyAlphaPercent },
                valueRange = 20f..100f,
                steps = 7,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save button
            Button(
                onClick = {
                    var hasErrors = false

                    if (apiUrl.isBlank()) {
                        apiUrlError = context.getString(R.string.error_url_required)
                        hasErrors = true
                    } else if (!apiUrl.startsWith("http://") && !apiUrl.startsWith("https://")) {
                        apiUrlError = context.getString(R.string.error_url_scheme)
                        hasErrors = true
                    }

                    if (model.isBlank()) {
                        modelError = context.getString(R.string.error_model_required)
                        hasErrors = true
                    }

                    if (apiToken.isBlank()) {
                        apiTokenError = context.getString(R.string.error_token_required)
                        hasErrors = true
                    }

                    if (!hasErrors) {
                        prefsManager.apiUrl = apiUrl.trimEnd('/')
                        prefsManager.model = model.trim()
                        prefsManager.apiToken = apiToken.trim()

                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.settings_saved),
                                duration = SnackbarDuration.Short,
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.settings_save))
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onOpenImeSettings,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.settings_enable_keyboard_button))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.enable_keyboard_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.about_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(
                    R.string.about_version,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 4.dp),
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.about_license),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp),
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
