package com.typlx.keyboard

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Settings screen for configuring the grammar keyboard's API connection.
 * Uses Jetpack Compose with Material3 for the UI.
 */
class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefsManager = PreferencesManager(this)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SettingsScreen(prefsManager = prefsManager)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(prefsManager: PreferencesManager) {
    val context = LocalContext.current

    var apiUrl by remember { mutableStateOf(prefsManager.apiUrl) }
    var model by remember { mutableStateOf(prefsManager.model) }
    var apiToken by remember { mutableStateOf(prefsManager.apiToken) }

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

            // API Token field (masked)
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
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
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
                    } else if (!apiUrl.startsWith("https://")) {
                        apiUrlError = context.getString(R.string.error_url_https)
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

            // Hint about enabling the keyboard
            Text(
                text = stringResource(R.string.enable_keyboard_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
