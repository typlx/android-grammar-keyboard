package com.typlx.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.typlx.keyboard.TextShortcut
import com.typlx.keyboard.ToneOption
import com.typlx.keyboard.TranslationLanguage
import com.typlx.keyboard.ui.theme.KeyboardColors

@Composable
internal fun TonePanel(
    isApplying: Boolean,
    error: String?,
    onToneSelect: (ToneOption) -> Unit,
    onDismiss: () -> Unit,
    onErrorDismiss: () -> Unit,
    colors: KeyboardColors,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape((colors.cornerRadiusDp + 2).dp))
            .background(colors.keyActionBg)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (error != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = onErrorDismiss,
                    modifier = Modifier
                        .size(24.dp)
                        .semantics { contentDescription = "Dismiss tone error" },
                ) {
                    Text("✕", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        if (isApplying) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 1.5.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Rewriting tone…",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(28.dp)
                        .semantics { contentDescription = "Close tone panel" },
                ) {
                    Text("✕", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ToneOption.entries.forEach { tone ->
                    FilterChip(
                        selected = false,
                        onClick = { onToneSelect(tone) },
                        label = {
                            Text(
                                text = tone.displayLabel,
                                fontSize = 12.sp,
                            )
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "Rewrite in ${tone.displayLabel} tone"
                        },
                    )
                }
                Spacer(Modifier.width(4.dp))
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(28.dp)
                        .semantics { contentDescription = "Close tone panel" },
                ) {
                    Text("✕", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
internal fun TranslationPanel(
    isApplying: Boolean,
    error: String?,
    onLanguageSelect: (TranslationLanguage) -> Unit,
    onDismiss: () -> Unit,
    onErrorDismiss: () -> Unit,
    colors: KeyboardColors,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape((colors.cornerRadiusDp + 2).dp))
            .background(colors.keyActionBg)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (error != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = onErrorDismiss,
                    modifier = Modifier
                        .size(24.dp)
                        .semantics { contentDescription = "Dismiss translation error" },
                ) {
                    Text("✕", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        if (isApplying) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 1.5.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Translating…",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(28.dp)
                        .semantics { contentDescription = "Close translation panel" },
                ) {
                    Text("✕", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TranslationLanguage.entries.forEach { language ->
                    FilterChip(
                        selected = false,
                        onClick = { onLanguageSelect(language) },
                        label = {
                            Text(
                                text = language.displayLabel,
                                fontSize = 12.sp,
                            )
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "Translate to ${language.displayLabel}"
                        },
                    )
                }
                Spacer(Modifier.width(4.dp))
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(28.dp)
                        .semantics { contentDescription = "Close translation panel" },
                ) {
                    Text("✕", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
internal fun ClipboardPanel(
    items: List<String>,
    onPaste: (String) -> Unit,
    onClear: () -> Unit,
    colors: KeyboardColors,
) {
    val cornerRadius = colors.cornerRadiusDp.dp
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius + 2.dp))
            .background(colors.keyActionBg)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Clipboard",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            if (items.isNotEmpty()) {
                TextButton(
                    onClick = onClear,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier
                        .height(28.dp)
                        .semantics { contentDescription = "Clear clipboard history" },
                ) {
                    Text(text = "Clear", fontSize = 11.sp)
                }
            }
        }

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No clipboard items",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 120.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(items) { text ->
                    val displayText = if (text.length > 80) text.take(80) + "…" else text
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(cornerRadius))
                            .background(colors.keyBg)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple(),
                                onClick = { onPaste(text) },
                            )
                            .semantics { contentDescription = "Paste: $displayText" }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = displayText,
                            fontSize = 12.sp,
                            color = colors.keyText,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun ShortcutsPanel(
    shortcuts: List<TextShortcut>,
    onInsert: (String) -> Unit,
    onManage: () -> Unit,
    colors: KeyboardColors,
) {
    val cornerRadius = colors.cornerRadiusDp.dp
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius + 2.dp))
            .background(colors.keyActionBg)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Shortcuts",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                onClick = onManage,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                modifier = Modifier
                    .height(28.dp)
                    .semantics { contentDescription = "Manage text shortcuts" },
            ) {
                Text(text = "Manage", fontSize = 11.sp)
            }
        }

        if (shortcuts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No shortcuts — tap Manage to add some",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 120.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(shortcuts) { shortcut ->
                    val expansionPreview = if (shortcut.expansion.length > 60)
                        shortcut.expansion.take(60) + "…"
                    else
                        shortcut.expansion
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(cornerRadius))
                            .background(colors.keyBg)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple(),
                                onClick = { onInsert(shortcut.expansion) },
                            )
                            .semantics {
                                contentDescription =
                                    "Insert shortcut ${shortcut.shortcut}: $expansionPreview"
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = shortcut.shortcut,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.widthIn(min = 32.dp),
                            )
                            Text(
                                text = expansionPreview,
                                fontSize = 12.sp,
                                color = colors.keyText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}
