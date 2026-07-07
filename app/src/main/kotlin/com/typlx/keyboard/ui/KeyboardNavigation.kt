package com.typlx.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.typlx.keyboard.ui.theme.KeyboardColors
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun CursorNavPanel(
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onWordLeft: () -> Unit,
    onWordRight: () -> Unit,
    onHome: () -> Unit,
    onEnd: () -> Unit,
    onSelectAll: () -> Unit,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onPaste: () -> Unit,
    colors: KeyboardColors,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            NavKeyButton(
                label = "⇤",
                contentDescription = "Move to start of line",
                modifier = Modifier.weight(1f),
                colors = colors,
                onClick = onHome,
            )
            NavKeyButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Move cursor left, long-press for word",
                modifier = Modifier.weight(1f),
                colors = colors,
                onClick = onLeft,
                onLongClick = onWordLeft,
            )
            NavKeyButton(
                icon = Icons.Default.KeyboardArrowUp,
                contentDescription = "Move cursor up",
                modifier = Modifier.weight(1f),
                colors = colors,
                onClick = onUp,
            )
            NavKeyButton(
                icon = Icons.Default.KeyboardArrowDown,
                contentDescription = "Move cursor down",
                modifier = Modifier.weight(1f),
                colors = colors,
                onClick = onDown,
            )
            NavKeyButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Move cursor right, long-press for word",
                modifier = Modifier.weight(1f),
                colors = colors,
                onClick = onRight,
                onLongClick = onWordRight,
            )
            NavKeyButton(
                label = "⇥",
                contentDescription = "Move to end of line",
                modifier = Modifier.weight(1f),
                colors = colors,
                onClick = onEnd,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            NavKeyButton(
                label = "Select\nAll",
                contentDescription = "Select all text",
                modifier = Modifier.weight(1f),
                colors = colors,
                onClick = onSelectAll,
            )
            NavKeyButton(
                label = "Cut",
                contentDescription = "Cut selected text",
                modifier = Modifier.weight(1f),
                colors = colors,
                onClick = onCut,
            )
            NavKeyButton(
                label = "Copy",
                contentDescription = "Copy selected text",
                modifier = Modifier.weight(1f),
                colors = colors,
                onClick = onCopy,
            )
            NavKeyButton(
                label = "Paste",
                contentDescription = "Paste from clipboard",
                modifier = Modifier.weight(1f),
                colors = colors,
                onClick = onPaste,
            )
        }
    }
}

@Composable
internal fun NavKeyButton(
    modifier: Modifier = Modifier,
    label: String? = null,
    icon: ImageVector? = null,
    contentDescription: String,
    colors: KeyboardColors,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current
    val cornerRadius = colors.cornerRadiusDp.dp
    Box(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(colors.keyActionBg)
            .semantics {
                this.contentDescription = contentDescription
                role = Role.Button
            }
            .then(
                if (onLongClick != null) {
                    Modifier.pointerInput(onClick, onLongClick) {
                        coroutineScope {
                        val launchScope = this
                        awaitPointerEventScope {
                            while (true) {
                                awaitFirstDown(requireUnconsumed = false)
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                var longPressed = false
                                val job = launchScope.launch {
                                    delay(500L)
                                    longPressed = true
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onLongClick()
                                }
                                do {
                                    val event = awaitPointerEvent()
                                } while (event.changes.any { it.pressed })
                                job.cancel()
                                if (!longPressed) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onClick()
                                }
                            }
                        }
                        }
                    }
                } else {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onClick()
                        },
                    )
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.keyText,
                modifier = Modifier.size(22.dp),
            )
        } else if (label != null) {
            Text(
                text = label,
                color = colors.keyText,
                fontSize = if (label.length <= 2) 18.sp else 11.sp,
                maxLines = 2,
                textAlign = TextAlign.Center,
            )
        }
    }
}
