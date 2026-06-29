package com.typlx.keyboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.typlx.keyboard.ui.theme.TyplxKeyboardTheme

class OnboardingActivity : ComponentActivity() {

    private val imeEnabled = mutableStateOf(false)
    private val isDefault = mutableStateOf(false)

    private lateinit var onboardingManager: OnboardingManager
    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onboardingManager = OnboardingManager(this)
        prefsManager = PreferencesManager(this)

        setContent {
            TyplxKeyboardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    OnboardingWizard(
                        imeEnabled = imeEnabled,
                        isDefault = isDefault,
                        prefsManager = prefsManager,
                        onOpenImeSettings = {
                            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                        },
                        onShowImePicker = {
                            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            @Suppress("DEPRECATION")
                            imm.showInputMethodPicker()
                        },
                        onFinished = {
                            onboardingManager.markComplete()
                            startActivity(Intent(this@OnboardingActivity, SettingsActivity::class.java))
                            finish()
                        },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        imeEnabled.value = onboardingManager.isImeEnabled()
        isDefault.value = onboardingManager.isImeDefault()
    }
}

@Composable
private fun OnboardingWizard(
    imeEnabled: State<Boolean>,
    isDefault: State<Boolean>,
    prefsManager: PreferencesManager,
    onOpenImeSettings: () -> Unit,
    onShowImePicker: () -> Unit,
    onFinished: () -> Unit,
) {
    var step by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            if (step > 0) {
                LinearProgressIndicator(
                    progress = { step.toFloat() / 3f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                )
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
        ) {
            AnimatedContent(targetState = step, label = "onboarding_step") { s ->
                when (s) {
                    0 -> WelcomeStep(onNext = { step = 1 })
                    1 -> EnableImeStep(
                        imeEnabled = imeEnabled.value,
                        onOpenSettings = onOpenImeSettings,
                        onNext = { step = 2 },
                    )
                    2 -> SetDefaultStep(
                        isDefault = isDefault.value,
                        onShowPicker = onShowImePicker,
                        onNext = { step = 3 },
                    )
                    else -> ConfigureApiStep(
                        prefsManager = prefsManager,
                        onFinished = onFinished,
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("⌨️", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Welcome to Typlx Keyboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "AI-powered grammar correction for everything you type. " +
                "Private by design — corrections use your own API key and stay between you and your chosen provider.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(48.dp))
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
            Text("Get Started")
        }
    }
}

@Composable
private fun EnableImeStep(
    imeEnabled: Boolean,
    onOpenSettings: () -> Unit,
    onNext: () -> Unit,
) {
    LaunchedEffect(imeEnabled) {
        if (imeEnabled) onNext()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Step 1 of 3",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(16.dp))
        Text("🔧", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Enable the Keyboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Open Android Settings and toggle on Typlx Keyboard in the on-screen keyboard list.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))
        if (imeEnabled) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Keyboard enabled",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text("Continue")
            }
        } else {
            Button(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
                Text("Open Keyboard Settings")
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "We'll detect automatically when Typlx is enabled.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SetDefaultStep(
    isDefault: Boolean,
    onShowPicker: () -> Unit,
    onNext: () -> Unit,
) {
    LaunchedEffect(isDefault) {
        if (isDefault) onNext()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Step 2 of 3",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(16.dp))
        Text("⭐", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Set as Default",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Make Typlx your default keyboard so grammar correction is always available.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))
        if (isDefault) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Typlx is your default keyboard",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text("Continue")
            }
        } else {
            Button(onClick = onShowPicker, modifier = Modifier.fillMaxWidth()) {
                Text("Choose Default Keyboard")
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text("Skip for now")
            }
        }
    }
}

@Composable
private fun ConfigureApiStep(
    prefsManager: PreferencesManager,
    onFinished: () -> Unit,
) {
    val context = LocalContext.current
    var apiUrl by remember { mutableStateOf(prefsManager.apiUrl) }
    var model by remember { mutableStateOf(prefsManager.model) }
    var apiToken by remember { mutableStateOf(prefsManager.apiToken) }
    var tokenVisible by remember { mutableStateOf(false) }
    var apiUrlError by remember { mutableStateOf<String?>(null) }
    var modelError by remember { mutableStateOf<String?>(null) }
    var apiTokenError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Step 3 of 3",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(16.dp))
        Text("🔑", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Configure Grammar API",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Connect Typlx to an OpenAI-compatible API. Your API key is stored encrypted on-device only.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = apiUrl,
            onValueChange = { apiUrl = it; apiUrlError = null },
            label = { Text("API URL") },
            placeholder = { Text("https://api.openai.com") },
            isError = apiUrlError != null,
            supportingText = apiUrlError?.let { e -> { Text(e) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = model,
            onValueChange = { model = it; modelError = null },
            label = { Text("Model") },
            placeholder = { Text("gpt-4o-mini") },
            isError = modelError != null,
            supportingText = modelError?.let { e -> { Text(e) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = apiToken,
            onValueChange = { apiToken = it; apiTokenError = null },
            label = { Text("API Token") },
            placeholder = { Text("sk-…") },
            isError = apiTokenError != null,
            supportingText = apiTokenError?.let { e -> { Text(e) } },
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

        Spacer(Modifier.height(24.dp))

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
                    onFinished()
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save and Finish")
        }

        Spacer(Modifier.height(8.dp))

        TextButton(onClick = onFinished, modifier = Modifier.fillMaxWidth()) {
            Text("Skip — configure later in Settings")
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "🔒 Privacy: Text is sent to your configured API endpoint only — never to Typlx servers. Grammar correction can be disabled at any time in Settings.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
    }
}
