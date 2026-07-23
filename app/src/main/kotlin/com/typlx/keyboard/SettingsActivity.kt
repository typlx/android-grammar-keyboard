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

    private val batteryIgnoring = mutableStateOf(false)

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
                        batteryIgnoring = batteryIgnoring,
                        onOpenImeSettings = {
                            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                        },
                        onRequestBatteryExemption = {
                            startActivity(BatteryOptimizationHelper.buildExemptionIntent(this@SettingsActivity))
                        },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        batteryIgnoring.value = BatteryOptimizationHelper.isIgnoring(this)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    prefsManager: PreferencesManager,
    batteryIgnoring: State<Boolean>,
    onOpenImeSettings: () -> Unit,
    onRequestBatteryExemption: () -> Unit,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var apiUrl by remember { mutableStateOf(prefsManager.apiUrl) }
    var model by remember { mutableStateOf(prefsManager.model) }
    var apiToken by remember { mutableStateOf(prefsManager.apiToken) }
    var tokenVisible by remember { mutableStateOf(false) }
    var selectedProvider by remember { mutableStateOf(ApiProvider.inferFromUrl(prefsManager.apiUrl)) }
    var hapticEnabled by remember { mutableStateOf(prefsManager.hapticFeedbackEnabled) }
    var keyPressPreviewEnabled by remember { mutableStateOf(prefsManager.keyPressPreviewEnabled) }
    var autoSuggestEnabled by remember { mutableStateOf(prefsManager.autoSuggestEnabled) }
    var grammarInstructionSuffix by remember { mutableStateOf(prefsManager.grammarInstructionSuffix) }
    var crashReportingEnabled by remember { mutableStateOf(prefsManager.crashReportingEnabled) }

    var apiUrlError by remember { mutableStateOf<String?>(null) }
    var modelError by remember { mutableStateOf<String?>(null) }
    var apiTokenError by remember { mutableStateOf<String?>(null) }
    var isTestingConnection by remember { mutableStateOf(false) }

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
    var enabledInputLanguages by remember { mutableStateOf(prefsManager.enabledInputLanguages) }
    var keySizePreset by remember { mutableStateOf(prefsManager.keySizePreset) }
    var keyHeightDp by remember { mutableIntStateOf(prefsManager.keyHeightDp) }
    var showNumberRow by remember { mutableStateOf(prefsManager.showNumberRow) }
    var doubleSpacePeriodEnabled by remember { mutableStateOf(prefsManager.doubleSpacePeriodEnabled) }
    var autoCapEnabled by remember { mutableStateOf(prefsManager.autoCapEnabled) }
    var autocorrectEnabled by remember { mutableStateOf(prefsManager.autocorrectEnabled) }
    var wordPredictionEnabled by remember { mutableStateOf(prefsManager.wordPredictionEnabled) }
    var smartComposeEnabled by remember { mutableStateOf(prefsManager.smartComposeEnabled) }
    var emojiSuggestionsEnabled by remember { mutableStateOf(prefsManager.emojiSuggestionsEnabled) }
    var swipeTypingEnabled by remember { mutableStateOf(prefsManager.swipeTypingEnabled) }
    var landscapeSplitEnabled by remember { mutableStateOf(prefsManager.landscapeSplitEnabled) }
    var oneHandedMode by remember { mutableStateOf(prefsManager.oneHandedMode) }

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

            // Battery optimization warning
            if (!batteryIgnoring.value) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Battery optimization is on",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            )
                            Text(
                                text = "Grammar correction may be interrupted. Grant an exemption to keep Typlx running reliably.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = onRequestBatteryExemption,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            ),
                        ) {
                            Text("Fix")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // API provider presets
            Text(
                text = "API provider",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ApiProvider.entries.forEach { provider ->
                    val label = when (provider) {
                        ApiProvider.OPENAI -> "OpenAI"
                        ApiProvider.GROQ -> "Groq"
                        ApiProvider.OLLAMA -> "Ollama"
                        ApiProvider.CUSTOM -> "Custom"
                    }
                    FilterChip(
                        selected = selectedProvider == provider,
                        onClick = {
                            selectedProvider = provider
                            if (provider != ApiProvider.CUSTOM) {
                                apiUrl = provider.apiUrl
                                model = provider.defaultModel
                                apiUrlError = null
                                modelError = null
                            }
                        },
                        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // API URL field
            OutlinedTextField(
                value = apiUrl,
                onValueChange = {
                    apiUrl = it
                    apiUrlError = null
                    selectedProvider = ApiProvider.CUSTOM
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
                    selectedProvider = ApiProvider.CUSTOM
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

            // Key press preview toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_key_press_preview_label),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = stringResource(R.string.settings_key_press_preview_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = keyPressPreviewEnabled,
                    onCheckedChange = {
                        keyPressPreviewEnabled = it
                        prefsManager.keyPressPreviewEnabled = it
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

            Spacer(modifier = Modifier.height(12.dp))

            // Grammar instruction suffix
            OutlinedTextField(
                value = grammarInstructionSuffix,
                onValueChange = {
                    grammarInstructionSuffix = it
                    prefsManager.grammarInstructionSuffix = it
                },
                label = { Text("Writing style instructions (optional)") },
                placeholder = { Text("e.g. Use formal language; keep medical terms") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                supportingText = {
                    Text(
                        "Appended to the grammar correction prompt. Leave blank for default behavior.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Double-space to period toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Double-space for period",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Tap space twice quickly to insert a period",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = doubleSpacePeriodEnabled,
                    onCheckedChange = {
                        doubleSpacePeriodEnabled = it
                        prefsManager.doubleSpacePeriodEnabled = it
                    },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Auto-capitalize toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Auto-capitalize",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Capitalize the first letter of sentences automatically",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = autoCapEnabled,
                    onCheckedChange = {
                        autoCapEnabled = it
                        prefsManager.autoCapEnabled = it
                    },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Autocorrect toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Autocorrect",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Fix common typos automatically when you press space",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = autocorrectEnabled,
                    onCheckedChange = {
                        autocorrectEnabled = it
                        prefsManager.autocorrectEnabled = it
                    },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Word prediction toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Word prediction",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Show word completion suggestions while typing",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = wordPredictionEnabled,
                    onCheckedChange = {
                        wordPredictionEnabled = it
                        prefsManager.wordPredictionEnabled = it
                    },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Smart Compose toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Smart Compose",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Double-tap space to complete your sentence with AI",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = smartComposeEnabled,
                    onCheckedChange = {
                        smartComposeEnabled = it
                        prefsManager.smartComposeEnabled = it
                    },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Emoji suggestions toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Emoji suggestions",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Show relevant emoji chips after completing a word",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = emojiSuggestionsEnabled,
                    onCheckedChange = {
                        emojiSuggestionsEnabled = it
                        prefsManager.emojiSuggestionsEnabled = it
                    },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Swipe typing toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Swipe typing",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Drag across keys to type a word without lifting your finger",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = swipeTypingEnabled,
                    onCheckedChange = {
                        swipeTypingEnabled = it
                        prefsManager.swipeTypingEnabled = it
                    },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Split keyboard in landscape",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Split the keyboard into two halves when the phone is horizontal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = landscapeSplitEnabled,
                    onCheckedChange = {
                        landscapeSplitEnabled = it
                        prefsManager.landscapeSplitEnabled = it
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

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Keyboard languages",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            Text(
                text = "Free tier: up to ${InputLanguage.FREE_TIER_LIMIT} languages. Enable a language to show the quick-switch button on the keyboard.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            )
            val maxLangs = FeatureGate.maxEnabledLanguages()
            InputLanguage.ALL.forEach { lang ->
                val isEnabled = enabledInputLanguages.contains(lang)
                val isEnglish = lang == InputLanguage.ENGLISH
                val atLimit = enabledInputLanguages.size >= maxLangs && !isEnabled
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${lang.code} — ${lang.displayName}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        if (isEnglish) {
                            Text(
                                text = "Always on (required)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else if (lang.defaultLayoutId == LayoutId.CYRILLIC) {
                            Text(
                                text = "Cyrillic layout",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (atLimit && !isEnglish) {
                            Text(
                                text = "Upgrade to unlock more languages",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    Switch(
                        checked = isEnabled,
                        enabled = !isEnglish && (isEnabled || !atLimit),
                        onCheckedChange = { checked ->
                            val newList = if (checked) {
                                (enabledInputLanguages + lang).distinct()
                            } else {
                                enabledInputLanguages.filter { it != lang }
                            }
                            enabledInputLanguages = newList
                            prefsManager.enabledInputLanguages = newList
                            if (!newList.contains(prefsManager.activeInputLanguage)) {
                                prefsManager.activeInputLanguage = newList.firstOrNull() ?: InputLanguage.ENGLISH
                            }
                        },
                        modifier = Modifier.padding(start = 8.dp),
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

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Key height",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                Text(
                    text = "$keyHeightDp dp",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                val presets = listOf(
                    "Compact" to 39,
                    "Normal" to 46,
                    "Large" to 53,
                )
                presets.forEach { (label, dp) ->
                    FilterChip(
                        selected = keyHeightDp == dp,
                        onClick = {
                            keyHeightDp = dp
                            keySizePreset = when (dp) {
                                39 -> KeySizePreset.COMPACT
                                53 -> KeySizePreset.LARGE
                                else -> KeySizePreset.NORMAL
                            }
                            prefsManager.keyHeightDp = dp
                            prefsManager.keySizePreset = keySizePreset
                        },
                        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Slider(
                value = keyHeightDp.toFloat(),
                onValueChange = { v ->
                    val snapped = v.toInt()
                    keyHeightDp = snapped
                    prefsManager.keyHeightDp = snapped
                    val matchedPreset = when (snapped) {
                        39 -> KeySizePreset.COMPACT
                        46 -> KeySizePreset.NORMAL
                        53 -> KeySizePreset.LARGE
                        else -> null
                    }
                    if (matchedPreset != null) {
                        keySizePreset = matchedPreset
                        prefsManager.keySizePreset = matchedPreset
                    }
                },
                valueRange = PreferencesManager.KEY_HEIGHT_DP_MIN.toFloat()..PreferencesManager.KEY_HEIGHT_DP_MAX.toFloat(),
                steps = PreferencesManager.KEY_HEIGHT_DP_MAX - PreferencesManager.KEY_HEIGHT_DP_MIN - 1,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Number row toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Number row",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                    Text(
                        text = "Show digit row above the letter keys",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
                Switch(
                    checked = showNumberRow,
                    onCheckedChange = {
                        showNumberRow = it
                        prefsManager.showNumberRow = it
                    },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "One-handed mode",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            Text(
                text = "Shrink keyboard to left or right for easier one-handed typing",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OneHandedMode.entries.forEach { mode ->
                    FilterChip(
                        selected = oneHandedMode == mode,
                        onClick = {
                            oneHandedMode = mode
                            prefsManager.oneHandedMode = mode
                        },
                        label = {
                            Text(
                                text = when (mode) {
                                    OneHandedMode.OFF -> "Off"
                                    OneHandedMode.LEFT -> "Left"
                                    OneHandedMode.RIGHT -> "Right"
                                },
                            )
                        },
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.settings_privacy_section),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_crash_reporting_label),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = stringResource(R.string.settings_crash_reporting_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = crashReportingEnabled,
                    onCheckedChange = {
                        crashReportingEnabled = it
                        prefsManager.crashReportingEnabled = it
                        CrashReporter.configure(it)
                    },
                )
            }

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

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    val testUrl = apiUrl.trim()
                    val testModel = model.trim()
                    val testToken = apiToken.trim()
                    if (testUrl.isBlank() || testModel.isBlank() || testToken.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Fill in API URL, model, and token first")
                        }
                        return@OutlinedButton
                    }
                    isTestingConnection = true
                    scope.launch {
                        try {
                            val ms = GrammarService().testConnection(testUrl, testModel, testToken)
                            snackbarHostState.showSnackbar("Connected (${ms}ms)", duration = SnackbarDuration.Short)
                        } catch (e: GrammarServiceException) {
                            snackbarHostState.showSnackbar(e.message ?: "Connection failed", duration = SnackbarDuration.Long)
                        } finally {
                            isTestingConnection = false
                        }
                    }
                },
                enabled = !isTestingConnection,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isTestingConnection) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Test connection")
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
