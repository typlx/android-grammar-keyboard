package com.typlx.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.typlx.keyboard.DiffKind
import com.typlx.keyboard.SuggestionState
import com.typlx.keyboard.ui.theme.KeyboardColors

@Composable
internal fun SuggestionStrip(
    state: SuggestionState,
    isSmartComposing: Boolean = false,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    onWordSuggestionAccepted: (String) -> Unit = {},
    onEmojiSuggestionTapped: (String) -> Unit = {},
    onSmartCompose: () -> Unit = {},
    onUndoAutocorrect: () -> Unit = {},
    onSmartClipboardPaste: () -> Unit = {},
    onSmartClipboardDismiss: () -> Unit = {},
) {
    when {
        state == SuggestionState.Loading -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                strokeWidth = 1.5.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Checking grammar…",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        isSmartComposing -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                strokeWidth = 1.5.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Composing…",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        state is SuggestionState.WordSuggestions -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            state.words.forEach { word ->
                SuggestionChip(
                    onClick = { onWordSuggestionAccepted(word) },
                    label = {
                        Text(
                            text = word,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = "Word suggestion: $word" },
                )
            }
            if (state.emojis.isNotEmpty() && state.words.isNotEmpty()) {
                VerticalDivider(
                    modifier = Modifier
                        .height(20.dp)
                        .padding(horizontal = 2.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
            state.emojis.forEach { emoji ->
                SuggestionChip(
                    onClick = { onEmojiSuggestionTapped(emoji) },
                    label = {
                        Text(
                            text = emoji,
                            fontSize = 16.sp,
                            maxLines = 1,
                        )
                    },
                    modifier = Modifier
                        .wrapContentWidth()
                        .semantics { contentDescription = "Emoji suggestion: $emoji" },
                )
            }
        }
        state == SuggestionState.Idle -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            SuggestionChip(
                onClick = onSmartCompose,
                label = {
                    Text(
                        text = "Continue →",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                modifier = Modifier.semantics {
                    contentDescription = "Smart Compose: continue text with AI"
                },
            )
        }
        state is SuggestionState.Available -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val addedColor = MaterialTheme.colorScheme.primary
            val removedColor = MaterialTheme.colorScheme.error
            val chipLabel: AnnotatedString = if (state.diff.isEmpty()) {
                AnnotatedString(state.corrected)
            } else {
                buildAnnotatedString {
                    for (seg in state.diff) {
                        when (seg.kind) {
                            DiffKind.UNCHANGED -> append(seg.text)
                            DiffKind.ADDED -> {
                                pushStyle(SpanStyle(color = addedColor, fontWeight = FontWeight.Bold))
                                append(seg.text)
                                pop()
                            }
                            DiffKind.REMOVED -> {
                                pushStyle(SpanStyle(color = removedColor, textDecoration = TextDecoration.LineThrough))
                                append(seg.text)
                                pop()
                            }
                        }
                    }
                }
            }
            SuggestionChip(
                onClick = onAccept,
                label = {
                    Text(
                        text = chipLabel,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp,
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Grammar suggestion: ${state.corrected}. Tap to accept." },
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(28.dp)
                    .semantics { contentDescription = "Dismiss grammar suggestion" },
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        state is SuggestionState.AutoCorrected -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SuggestionChip(
                onClick = onUndoAutocorrect,
                label = {
                    Text(
                        text = "${state.original} → ${state.corrected} ↩",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp,
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        contentDescription =
                            "Autocorrected: ${state.original} to ${state.corrected}. Tap to undo."
                    },
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(28.dp)
                    .semantics { contentDescription = "Dismiss autocorrect indicator" },
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        state is SuggestionState.ClipboardPaste -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SuggestionChip(
                onClick = onSmartClipboardPaste,
                label = {
                    Text(
                        text = state.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp,
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = state.label },
            )
            IconButton(
                onClick = onSmartClipboardDismiss,
                modifier = Modifier
                    .size(28.dp)
                    .semantics { contentDescription = "Dismiss clipboard suggestion" },
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

/**
 * Horizontal bar showing accent/variant alternatives for a long-pressed key.
 * Horizontally scrollable so it accommodates keys with many alternatives (e.g. 'a' has 7).
 */
@Composable
internal fun AlternativesBar(
    originalKey: String,
    alternatives: List<String>,
    colors: KeyboardColors,
    onSelectAlternative: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val cornerRadius = colors.cornerRadiusDp.dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(cornerRadius + 2.dp))
            .background(colors.keyActionBg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(42.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(cornerRadius))
                .background(MaterialTheme.colorScheme.primary)
                .clickable { onSelectAlternative(originalKey) }
                .semantics { contentDescription = "$originalKey (original)" },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = originalKey,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        Spacer(Modifier.width(4.dp))
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            alternatives.forEach { alt ->
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(cornerRadius))
                        .background(colors.keyBg)
                        .clickable { onSelectAlternative(alt) }
                        .semantics { contentDescription = alt },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = alt,
                        color = colors.keyText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                    )
                }
            }
        }
        Spacer(Modifier.width(4.dp))
        Box(
            modifier = Modifier
                .width(38.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(cornerRadius))
                .background(colors.keyBg)
                .clickable { onDismiss() }
                .semantics { contentDescription = "Dismiss alternatives" },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "✕",
                color = colors.keyText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
            )
        }
    }
}
