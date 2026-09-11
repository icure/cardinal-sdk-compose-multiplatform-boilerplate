package com.icure.cardinal.compose.multiplatform.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * The ringed glyph at the top of a full-screen working state — an 84 dp
 * accent-outlined circle inside a 12 dp halo.
 *
 * The halo is what keeps a thin 1 dp ring from disappearing against the background.
 */
@Composable
fun CardinalHaloIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .size(108.dp)
            .background(colors.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .border(1.dp, colors.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = colors.primary,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}
