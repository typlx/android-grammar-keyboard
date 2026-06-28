package com.typlx.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.typlx.keyboard.ui.theme.KeyboardColors

@Composable
fun EmojiKeyboard(
    recents: List<String>,
    onEmojiClick: (String) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    colors: KeyboardColors,
) {
    var selectedCategory by remember { mutableStateOf<EmojiCategory?>(null) }

    val showRecents = recents.isNotEmpty()
    val activeCategory = selectedCategory
    val displayEmojis: List<EmojiEntry> = when {
        activeCategory != null -> EMOJI_DATA[activeCategory] ?: emptyList()
        showRecents -> recents.map { EmojiEntry(it, it) }
        else -> EMOJI_DATA[EmojiCategory.SMILEYS] ?: emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.keyboardBg),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        EmojiCategoryTabs(
            showRecents = showRecents,
            selectedCategory = activeCategory,
            colors = colors,
            onSelectRecents = { selectedCategory = null },
            onSelectCategory = { selectedCategory = it },
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(horizontal = 4.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            items(displayEmojis, key = { it.emoji }) { entry ->
                EmojiCell(
                    entry = entry,
                    colors = colors,
                    onClick = { onEmojiClick(entry.emoji) },
                )
            }
        }

        EmojiBottomBar(colors = colors, onDelete = onDelete, onBack = onBack)
    }
}

@Composable
private fun EmojiCategoryTabs(
    showRecents: Boolean,
    selectedCategory: EmojiCategory?,
    colors: KeyboardColors,
    onSelectRecents: () -> Unit,
    onSelectCategory: (EmojiCategory) -> Unit,
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (showRecents) {
            CategoryTab(
                icon = "🕐",
                label = "Recent",
                isSelected = selectedCategory == null,
                colors = colors,
                onClick = onSelectRecents,
            )
        }
        EmojiCategory.entries.forEach { cat ->
            val isSelected = selectedCategory == cat || (!showRecents && selectedCategory == null && cat == EmojiCategory.SMILEYS)
            CategoryTab(
                icon = cat.icon,
                label = cat.displayName,
                isSelected = isSelected,
                colors = colors,
                onClick = { onSelectCategory(cat) },
            )
        }
    }
}

@Composable
private fun CategoryTab(
    icon: String,
    label: String,
    isSelected: Boolean,
    colors: KeyboardColors,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val bg = if (isSelected) MaterialTheme.colorScheme.primaryContainer else colors.keyActionBg
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else colors.keyText
    Box(
        modifier = Modifier
            .height(32.dp)
            .widthIn(min = 36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .semantics {
                contentDescription = "$label emoji category"
                role = Role.Tab
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = icon, fontSize = 18.sp, color = textColor, maxLines = 1)
    }
}

@Composable
private fun EmojiCell(
    entry: EmojiEntry,
    colors: KeyboardColors,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .semantics {
                contentDescription = entry.name
                role = Role.Button
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = entry.emoji,
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun EmojiBottomBar(
    colors: KeyboardColors,
    onDelete: () -> Unit,
    onBack: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Back to QWERTY button
        EmojiActionKey(
            label = "⌨",
            contentDescription = "Switch to keyboard",
            modifier = Modifier.weight(1.5f),
            colors = colors,
            onClick = onBack,
        )
        Spacer(modifier = Modifier.weight(7f))
        // Delete button
        EmojiActionKey(
            label = "⌫",
            contentDescription = "Delete",
            modifier = Modifier.weight(1.5f),
            colors = colors,
            onClick = onDelete,
        )
    }
}

@Composable
private fun EmojiActionKey(
    label: String,
    contentDescription: String,
    modifier: Modifier,
    colors: KeyboardColors,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(colors.keyActionBg)
            .semantics {
                this.contentDescription = contentDescription
                role = Role.Button
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, fontSize = 18.sp, color = colors.keyText, maxLines = 1)
    }
}
