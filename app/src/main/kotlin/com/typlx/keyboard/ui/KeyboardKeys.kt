package com.typlx.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.typlx.keyboard.ui.theme.KeyboardColors
import com.typlx.keyboard.ui.theme.LocalKeyboardColors
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Carries the pressed key's label and its top-left position + dimensions in window coordinates.
 * Passing null signals key release.
 */
internal data class KeyPressInfo(
    val label: String,
    val windowX: Float,
    val windowY: Float,
    val widthPx: Float,
    val heightPx: Float,
)

/**
 * CompositionLocal that KeyButton reads to notify the keyboard root about press/release.
 * Null value means key-press previews are disabled.
 */
internal val LocalKeyPressNotifier = compositionLocalOf<((KeyPressInfo?) -> Unit)?> { null }

/**
 * Backspace key with long-press repeat behavior:
 * - Tap: delete one character
 * - Hold 400ms: repeat character delete every 50ms
 * - Hold 1500ms: switch to word delete every 400ms with extra haptic
 */
@Composable
internal fun DeleteButton(
    modifier: Modifier = Modifier,
    colors: KeyboardColors,
    height: Dp = 46.dp,
    onDelete: () -> Unit,
    onDeleteWord: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val cornerRadius = colors.cornerRadiusDp.dp
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(colors.keyActionBg)
            .semantics {
                contentDescription = "Delete, hold to delete word"
                role = Role.Button
            }
            .pointerInput(onDelete, onDeleteWord) {
                coroutineScope {
                val launchScope = this
                awaitPointerEventScope {
                    while (true) {
                        awaitFirstDown(requireUnconsumed = false)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        var repeatFired = false
                        val job = launchScope.launch {
                            delay(400L)
                            repeatFired = true
                            var elapsed = 400L
                            var wordMode = false
                            while (true) {
                                if (elapsed >= 1500L) {
                                    if (!wordMode) {
                                        wordMode = true
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    onDeleteWord()
                                    delay(400L)
                                    elapsed += 400L
                                } else {
                                    onDelete()
                                    delay(50L)
                                    elapsed += 50L
                                }
                            }
                        }
                        do {
                            val event = awaitPointerEvent()
                        } while (event.changes.any { it.pressed })
                        job.cancel()
                        if (!repeatFired) {
                            onDelete()
                        }
                    }
                }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "⌫",
            color = colors.keyText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
        )
    }
}

@Composable
internal fun KeyButton(
    label: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    bgColor: Color,
    textColor: Color,
    height: Dp = 46.dp,
    onClick: () -> Unit,
    onLongPress: (() -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val cornerRadius = LocalKeyboardColors.current.cornerRadiusDp.dp
    val keyPressNotifier = LocalKeyPressNotifier.current
    // Only printable ASCII (0x21-0x7E) triggers the preview; excludes space and Unicode action keys.
    val isCharKey = keyPressNotifier != null && label.length == 1 && label[0].code in 33..126
    val layoutCoords = remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor)
            .then(if (isCharKey) Modifier.onGloballyPositioned { layoutCoords.value = it } else Modifier)
            .semantics {
                this.contentDescription = contentDescription
                this.role = Role.Button
            }
            .then(
                when {
                    onLongPress != null -> Modifier.pointerInput(onClick, onLongPress, isCharKey) {
                        coroutineScope {
                        val launchScope = this
                        awaitPointerEventScope {
                            while (true) {
                                awaitFirstDown(requireUnconsumed = false)
                                if (isCharKey) {
                                    layoutCoords.value?.let { coords ->
                                        val pos = coords.positionInWindow()
                                        keyPressNotifier!!(KeyPressInfo(label, pos.x, pos.y, coords.size.width.toFloat(), coords.size.height.toFloat()))
                                    }
                                }
                                var longFired = false
                                val job = launchScope.launch {
                                    delay(400L)
                                    longFired = true
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onLongPress()
                                }
                                do {
                                    val event = awaitPointerEvent()
                                } while (event.changes.any { it.pressed })
                                job.cancel()
                                if (isCharKey) keyPressNotifier!!(null)
                                if (!longFired) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onClick()
                                }
                            }
                        }
                        }
                    }
                    isCharKey -> Modifier.pointerInput(onClick) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitFirstDown(requireUnconsumed = false)
                                layoutCoords.value?.let { coords ->
                                    val pos = coords.positionInWindow()
                                    keyPressNotifier!!(KeyPressInfo(label, pos.x, pos.y, coords.size.width.toFloat(), coords.size.height.toFloat()))
                                }
                                do {
                                    val event = awaitPointerEvent()
                                } while (event.changes.any { it.pressed })
                                keyPressNotifier!!(null)
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onClick()
                            }
                        }
                    }
                    else -> Modifier.clickable(
                        interactionSource = interactionSource,
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
        Text(
            text = label,
            color = textColor,
            fontSize = if (label.length == 1) 18.sp else 13.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
        )
    }
}
