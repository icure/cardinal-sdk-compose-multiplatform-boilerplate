package com.icure.cardinal.compose.multiplatform.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cardinalcomposemultiplatform.shared.generated.resources.Res
import cardinalcomposemultiplatform.shared.generated.resources.auth_error_body
import cardinalcomposemultiplatform.shared.generated.resources.auth_error_detail_label
import cardinalcomposemultiplatform.shared.generated.resources.auth_error_retry
import cardinalcomposemultiplatform.shared.generated.resources.auth_error_title
import com.icure.cardinal.compose.multiplatform.ui.components.CardinalPrimaryButton
import com.icure.cardinal.compose.multiplatform.ui.viewmodels.AppViewModel
import com.icure.cardinal.compose.multiplatform.ui.viewmodels.AuthIntent
import com.icure.cardinal.compose.multiplatform.ui.viewmodels.AuthState
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Sign-in could not be started — the SDK never got as far as issuing a code.
 *
 * Recovery is always the same here: go back to the email step and try again. Nothing
 * was consumed, so there is one action and no branching.
 */
@Composable
fun AuthErrorScreen(
    appViewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val state by appViewModel.authState.collectAsState()
    val error = state as? AuthState.Error.StartAuthentication ?: return

    AuthErrorScreen(
        detail = error.message,
        onRetry = { appViewModel.processIntent(AuthIntent.Login.ClearError) },
        modifier = modifier
    )
}

/**
 * The failed-start state as pure UI.
 *
 * @param detail whatever the SDK reported, or `null`. It is neither translated nor
 *   written for a user, so it sits below the plain explanation in its own block
 *   rather than replacing it.
 */
@Composable
fun AuthErrorScreen(
    detail: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background
    ) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(40.dp)
                )

                Text(
                    text = stringResource(Res.string.auth_error_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onSurface
                )

                Text(
                    text = stringResource(Res.string.auth_error_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.8f)
                )

                if (!detail.isNullOrBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .background(colors.surfaceVariant)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.auth_error_detail_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurfaceVariant
                        )
                        Text(
                            text = detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                CardinalPrimaryButton(
                    text = stringResource(Res.string.auth_error_retry),
                    onClick = onRetry
                )
            }
        }
    }
}

@Preview
@Composable
private fun AuthErrorScreenPreview() {
    MaterialTheme {
        AuthErrorScreen(detail = null, onRetry = {})
    }
}

@Preview
@Composable
private fun AuthErrorScreenWithDetailPreview() {
    MaterialTheme {
        AuthErrorScreen(
            detail = "HTTP 503 · msg-gw.icure.cloud did not respond within 30s",
            onRetry = {}
        )
    }
}
