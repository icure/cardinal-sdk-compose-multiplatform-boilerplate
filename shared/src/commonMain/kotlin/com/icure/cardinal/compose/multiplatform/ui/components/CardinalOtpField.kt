package com.icure.cardinal.compose.multiplatform.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cardinalcomposemultiplatform.shared.generated.resources.Res
import cardinalcomposemultiplatform.shared.generated.resources.validation_field_description
import org.jetbrains.compose.resources.stringResource

/**
 * One-time-code entry: [length] equal-width cells that share a single hidden text
 * field.
 *
 * One field rather than one per digit is what makes SMS and email autofill work:
 * the platform delivers the whole code to one target. The cells are decoration.
 *
 * @param onCodeChange receives digits only, never longer than [length].
 * @param enabled set to `false` while the code is being checked; the cells dim and
 *   the caret stops.
 */
@Composable
fun CardinalOtpField(
    code: String,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 6,
    enabled: Boolean = true,
    autoFocus: Boolean = true,
    onFilled: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.small
    val focusRequester = remember { FocusRequester() }
    val fieldDescription = stringResource(Res.string.validation_field_description, length.toString())

    val caretAlpha by rememberInfiniteTransition(label = "caret").animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(650), RepeatMode.Reverse),
        label = "caretAlpha"
    )

    if (autoFocus) {
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }

    BasicTextField(
        value = code,
        onValueChange = { raw ->
            val digits = raw.filter(Char::isDigit).take(length)
            if (digits != code) {
                onCodeChange(digits)
                if (digits.length == length) onFilled()
            }
        },
        enabled = enabled,
        singleLine = true,
        cursorBrush = SolidColor(Color.Transparent),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { if (code.length == length) onFilled() }
        ),
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .semantics { contentDescription = fieldDescription },
        // The real text is never drawn: `innerTextField` is deliberately not called,
        // so the cells below are the only visible representation of the value.
        decorationBox = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(length) { index ->
                    val char = code.getOrNull(index)
                    val active = enabled && index == code.length
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .clip(shape)
                            .background(colors.surfaceVariant)
                            .border(
                                width = 1.dp,
                                color = if (active) colors.primary else colors.outline,
                                shape = shape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            char != null -> Text(
                                text = char.toString(),
                                style = MaterialTheme.typography.headlineSmall,
                                color = if (enabled) {
                                    colors.onSurface
                                } else {
                                    colors.onSurface.copy(alpha = 0.45f)
                                }
                            )

                            active -> Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(26.dp)
                                    .background(colors.primary.copy(alpha = caretAlpha))
                            )
                        }
                    }
                }
            }
        }
    )
}
