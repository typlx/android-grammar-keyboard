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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
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
 * Registry mapping lowercase key label -> window-coordinate Rect.
 * Populated by each character KeyButton via onGloballyPositioned.
 * Used by the swipe decoder to map pointer positions to key labels.
 */
internal val LocalKeyPositionRegistry = compositionLocalOf<MutableMap<String, Rect>?> { null }

/**
 * Callback invoked by KeyButton when a swipe gesture completes.
 * Receives the ordered list of lowercase key labels crossed during the swipe.
 * Null means swipe typing is disabled.
 */
internal val LocalSwipePathReceiver = compositionLocalOf<((List<String>) -> Unit)?> { null }

/**
 * Called by KeyButton on every pointer-move event during a swipe, with the pointer's
 * window-coordinate position. Null argument signals swipe end (pointer released).
 * The keyboard root accumulates these into a visual trail.
 */
internal val LocalSwipeTrailUpdater = compositionLocalOf<((Offset?) -> Unit)?> { null }

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
    val keyRegistry = LocalKeyPositionRegistry.current
    val swipeReceiver = LocalSwipePathReceiver.current
    val swipeTrailUpdater = LocalSwipeTrailUpdater.current
    // Only printable ASCII (0x21-0x7E) triggers the preview and swipe; excludes space and Unicode action keys.
    val isCharKey = label.length == 1 && label[0].code in 33..126
    val swipeEnabled = isCharKey && swipeReceiver != null && keyRegistry != null
    val layoutCoords = remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor)
            .then(
                if (isCharKey) Modifier.onGloballyPositioned { coords ->
                    layoutCoords.value = coords
                    // Register this key's window rect in the shared registry so swipe detection
                    // can map pointer positions to key labels.
                    if (keyRegistry != null) {
                        val pos = coords.positionInWindow()
                        keyRegistry[label.lowercase()] = Rect(
                            pos.x, pos.y,
                            pos.x + coords.size.width,
                            pos.y + coords.size.height,
                        )
                    }
                } else Modifier
            )
            .semantics {
                this.contentDescription = contentDescription
                this.role = Role.Button
            }
            .then(
                when {
                    onLongPress != null -> Modifier.pointerInput(onClick, onLongPress, isCharKey, swipeEnabled) {
                        coroutineScope {
                        val launchScope = this
                        awaitPointerEventScope {
                            while (true) {
                                awaitFirstDown(requireUnconsumed = false)
                                if (isCharKey && keyPressNotifier != null) {
                                    layoutCoords.value?.let { coords ->
                                        val pos = coords.positionInWindow()
                                        keyPressNotifier(KeyPressInfo(label, pos.x, pos.y, coords.size.width.toFloat(), coords.size.height.toFloat()))
                                    }
                                }

                                val pathKeys = if (swipeEnabled) mutableListOf(label.lowercase()) else null
                                var isSwipe = false
                                var longFired = false

                                val job = launchScope.launch {
                                    delay(400L)
                                    if (!isSwipe) {
                                        longFired = true
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onLongPress()
                                    }
                                }

                                do {
                                    val event = awaitPointerEvent()
                                    if (pathKeys != null && keyRegistry != null) {
                                        val change = event.changes.firstOrNull()
                                        if (change != null && change.pressed) {
                                            val keyOrigin = layoutCoords.value?.positionInWindow() ?: Offset.Zero
                                            val windowPos = keyOrigin + change.position
                                            swipeTrailUpdater?.invoke(windowPos)
                                            val hit = keyRegistry.entries.firstOrNull { (_, rect) -> rect.contains(windowPos) }?.key
                                            if (hit != null && hit != pathKeys.lastOrNull()) {
                                                pathKeys.add(hit)
                                                if (pathKeys.distinct().size >= 2) {
                                                    isSwipe = true
                                                    job.cancel()
                                                }
                                            }
                                        }
                                    }
                                } while (event.changes.any { it.pressed })

                                job.cancel()
                                if (isCharKey) keyPressNotifier?.invoke(null)
                                swipeTrailUpdater?.invoke(null)

                                when {
                                    isSwipe && pathKeys != null && swipeReceiver != null -> swipeReceiver(pathKeys)
                                    longFired -> { /* long press already handled */ }
                                    else -> {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onClick()
                                    }
                                }
                            }
                        }
                        }
                    }
                    isCharKey -> Modifier.pointerInput(onClick, swipeEnabled) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitFirstDown(requireUnconsumed = false)
                                if (keyPressNotifier != null) {
                                    layoutCoords.value?.let { coords ->
                                        val pos = coords.positionInWindow()
                                        keyPressNotifier(KeyPressInfo(label, pos.x, pos.y, coords.size.width.toFloat(), coords.size.height.toFloat()))
                                    }
                                }

                                val pathKeys = if (swipeEnabled) mutableListOf(label.lowercase()) else null
                                var isSwipe = false

                                do {
                                    val event = awaitPointerEvent()
                                    if (pathKeys != null && keyRegistry != null) {
                                        val change = event.changes.firstOrNull()
                                        if (change != null && change.pressed) {
                                            val keyOrigin = layoutCoords.value?.positionInWindow() ?: Offset.Zero
                                            val windowPos = keyOrigin + change.position
                                            swipeTrailUpdater?.invoke(windowPos)
                                            val hit = keyRegistry.entries.firstOrNull { (_, rect) -> rect.contains(windowPos) }?.key
                                            if (hit != null && hit != pathKeys.lastOrNull()) {
                                                pathKeys.add(hit)
                                                if (pathKeys.distinct().size >= 2) isSwipe = true
                                            }
                                        }
                                    }
                                } while (event.changes.any { it.pressed })

                                keyPressNotifier?.invoke(null)
                                swipeTrailUpdater?.invoke(null)

                                if (isSwipe && pathKeys != null && swipeReceiver != null) {
                                    swipeReceiver(pathKeys)
                                } else {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onClick()
                                }
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
