package com.typlx.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.typlx.keyboard.ui.theme.KeyboardColors

private val NUMPAD_ROWS = listOf(
    listOf("1", "2", "3"),
    listOf("4", "5", "6"),
    listOf("7", "8", "9"),
)

/**
 * Compact numpad layout shown when the focused field is TYPE_CLASS_NUMBER or TYPE_CLASS_PHONE.
 *
 * Phone mode adds *, +, # keys in the bottom row.
 * Decimal mode adds a "." key; signed mode adds a "-" key.
 */
@Composable
fun NumPadKeyboard(
    isPhoneMode: Boolean = false,
    isDecimalAllowed: Boolean = false,
    isSignedAllowed: Boolean = false,
    returnKeyDescription: String = "Return",
    onKeyPress: (String) -> Unit,
    onDelete: () -> Unit,
    onDeleteWord: () -> Unit,
    onReturn: () -> Unit,
    colors: KeyboardColors,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.keyboardBg)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Digit rows 1–9
        NUMPAD_ROWS.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                row.forEach { digit ->
                    KeyButton(
                        label = digit,
                        contentDescription = digit,
                        modifier = Modifier.weight(1f),
                        bgColor = colors.keyBg,
                        textColor = colors.keyText,
                        height = 52.dp,
                        onClick = { onKeyPress(digit) },
                    )
                }
            }
        }

        // Bottom row: optional extra key | 0 | delete
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val leftKey = when {
                isPhoneMode -> "*"
                isDecimalAllowed -> "."
                isSignedAllowed -> "-"
                else -> null
            }
            if (leftKey != null) {
                KeyButton(
                    label = leftKey,
                    contentDescription = leftKey,
                    modifier = Modifier.weight(1f),
                    bgColor = colors.keyActionBg,
                    textColor = colors.keyText,
                    height = 52.dp,
                    onClick = { onKeyPress(leftKey) },
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            KeyButton(
                label = "0",
                contentDescription = "0",
                modifier = Modifier.weight(1f),
                bgColor = colors.keyBg,
                textColor = colors.keyText,
                height = 52.dp,
                onClick = { onKeyPress("0") },
            )

            DeleteButton(
                modifier = Modifier.weight(1f),
                colors = colors,
                height = 52.dp,
                onDelete = onDelete,
                onDeleteWord = onDeleteWord,
            )
        }

        // Phone-mode: extra row for +, # and return; otherwise return spans full width
        if (isPhoneMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                KeyButton(
                    label = "+",
                    contentDescription = "+",
                    modifier = Modifier.weight(1f),
                    bgColor = colors.keyActionBg,
                    textColor = colors.keyText,
                    height = 52.dp,
                    onClick = { onKeyPress("+") },
                )
                KeyButton(
                    label = "#",
                    contentDescription = "#",
                    modifier = Modifier.weight(1f),
                    bgColor = colors.keyActionBg,
                    textColor = colors.keyText,
                    height = 52.dp,
                    onClick = { onKeyPress("#") },
                )
                KeyButton(
                    label = returnKeyDescription,
                    contentDescription = returnKeyDescription,
                    modifier = Modifier.weight(1f),
                    bgColor = colors.keyActionBg,
                    textColor = colors.keyText,
                    height = 52.dp,
                    onClick = onReturn,
                )
            }
        } else {
            KeyButton(
                label = returnKeyDescription,
                contentDescription = returnKeyDescription,
                modifier = Modifier.fillMaxWidth(),
                bgColor = colors.keyActionBg,
                textColor = colors.keyText,
                height = 52.dp,
                onClick = onReturn,
            )
        }
    }
}
