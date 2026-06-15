package com.typlx.keyboard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.typlx.keyboard.ui.theme.TyplxKeyboardTheme

/**
 * Settings screen for configuring the grammar keyboard's API connection.
 * Uses Jetpack Compose with Material3 for the UI.
 */
class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    var apiUrl by remember { mutableStateOf(prefsManager.apiUrl) }
    var model by remember { mutableStateOf(prefsManager.model) }
    var apiToken by remember { mutableStateOf(prefsManager.apiToken) }
    var tokenVisible by remember { mutableStateOf(false) }

    var apiUrlError by remember { mutableStateOf<String?>(null) }
    var modelError by remember { mutableStateOf<String?>(null) }
    var apiTokenError by remember { mutableStateOf<String?>(null) }

    Scaffold(
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

            // Save button
            Button(
                onClick = {
                    // Validate
                    var hasErrors = false

                    if (apiUrl.isBlank()) {
                        apiUrlError = context.getString(R.string.error_url_required)
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

                        Toast.makeText(
                            context,
                            context.getString(R.string.settings_saved),
                            Toast.LENGTH_SHORT
                        ).show()
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
                Text("Enable Typlx Keyboard in System Settings")
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
