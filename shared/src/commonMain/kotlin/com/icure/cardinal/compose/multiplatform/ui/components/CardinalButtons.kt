package com.icure.cardinal.compose.multiplatform.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Disabled controls dim rather than recolour. */
private const val DisabledAlpha = 0.45f

/** The screen's single main action: full width, 48 dp tall, filled. */
@Composable
fun CardinalPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp),
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.titleSmall)
    }
}

/**
 * The quieter block button — an outline with plain text. It sits under a primary,
 * never alone.
 */
@Composable
fun CardinalSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = MaterialTheme.colorScheme
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp),
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(width = 1.dp, color = colors.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = colors.onSurface,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = colors.onSurface.copy(alpha = DisabledAlpha)
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.titleSmall)
    }
}

/** Accent text, no border, tight padding — for an action inline in the content. */
@Composable
fun CardinalGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val accent = MaterialTheme.colorScheme.primary
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.defaultMinSize(minHeight = 44.dp),
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.textButtonColors(
            contentColor = accent,
            disabledContentColor = accent.copy(alpha = DisabledAlpha)
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}
