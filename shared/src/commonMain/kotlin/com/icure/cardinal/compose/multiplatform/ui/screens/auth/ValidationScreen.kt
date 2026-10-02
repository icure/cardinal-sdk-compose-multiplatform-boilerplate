package com.icure.cardinal.compose.multiplatform.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cardinalcomposemultiplatform.shared.generated.resources.Res
import cardinalcomposemultiplatform.shared.generated.resources.validation_checking
import cardinalcomposemultiplatform.shared.generated.resources.validation_error_device_storage
import cardinalcomposemultiplatform.shared.generated.resources.validation_error_network
import cardinalcomposemultiplatform.shared.generated.resources.validation_error_setup_failed
import cardinalcomposemultiplatform.shared.generated.resources.validation_error_too_many_attempts
import cardinalcomposemultiplatform.shared.generated.resources.validation_error_unknown
import cardinalcomposemultiplatform.shared.generated.resources.validation_error_with_detail
import cardinalcomposemultiplatform.shared.generated.resources.validation_error_wrong_code
import cardinalcomposemultiplatform.shared.generated.resources.validation_expiry_note
import cardinalcomposemultiplatform.shared.generated.resources.validation_resend
import cardinalcomposemultiplatform.shared.generated.resources.validation_resend_in
import cardinalcomposemultiplatform.shared.generated.resources.validation_sent_to
import cardinalcomposemultiplatform.shared.generated.resources.validation_title
import com.icure.cardinal.compose.multiplatform.ui.components.CardinalGhostButton
import com.icure.cardinal.compose.multiplatform.ui.components.CardinalNotice
import com.icure.cardinal.compose.multiplatform.ui.components.CardinalOtpField
import com.icure.cardinal.compose.multiplatform.ui.components.CardinalTopBar
import com.icure.cardinal.compose.multiplatform.ui.viewmodels.AppViewModel
import com.icure.cardinal.compose.multiplatform.ui.viewmodels.AuthIntent
import com.icure.cardinal.compose.multiplatform.ui.viewmodels.AuthState
import com.icure.cardinal.compose.multiplatform.ui.viewmodels.ValidationCodeLength
import com.icure.cardinal.compose.multiplatform.ui.viewmodels.ValidationError
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

/** Digits that do not shift width as the countdown runs. */
private val TabularFigures = TextStyle(fontFeatureSettings = "tnum")

/**
 * The one-time code sent to the address from [LoginScreen].
 *
 * There is no submit button: the code is checked the moment the last digit lands,
 * which is also how the platform's autofill delivers it.
 *
 * Renders nothing unless authentication is waiting for a code, so it is safe to leave
 * composed while [AppViewModel] moves to another state.
 */
@Composable
fun ValidationScreen(
    appViewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val state by appViewModel.authState.collectAsState()
    val pending = state as? AuthState.PendingCompletion ?: return

    ValidationScreen(
        email = pending.email,
        code = pending.validationCode,
        isValidating = pending.validating,
        error = pending.error?.let { validationErrorMessage(it) },
        onCodeChange = { appViewModel.processIntent(AuthIntent.Validation.CodeChanged(it)) },
        onBack = { appViewModel.processIntent(AuthIntent.Validation.ChangeEmail) },
        // No resend intent exists yet, so the row stays out rather than shipping a
        // button that does nothing. Add `AuthIntent.Validation.Resend` and pass it here.
        onResend = null,
        modifier = modifier
    )
}

/**
 * The code step as pure UI.
 *
 * @param error a message to show under the cells, or `null`. The cells keep their
 *   digits so a mistyped code can be corrected rather than retyped.
 * @param onResend pass `null` when resending is not wired up — the resend row is then
 *   omitted instead of rendered dead.
 * @param resendCooldownSeconds how long after arriving before **Resend code** becomes
 *   available. Ignored when [onResend] is `null`.
 */
@Composable
fun ValidationScreen(
    email: String,
    code: String,
    isValidating: Boolean,
    error: String?,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onResend: (() -> Unit)? = null,
    resendCooldownSeconds: Int = 45
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
                .imePadding()
        ) {
            CardinalTopBar(title = stringResource(Res.string.validation_title), onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = stringResource(Res.string.validation_sent_to, email),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.8f)
                )

                CardinalOtpField(
                    code = code,
                    onCodeChange = onCodeChange,
                    length = ValidationCodeLength,
                    enabled = !isValidating
                )

                if (isValidating) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = colors.primary
                        )
                        Text(
                            text = stringResource(Res.string.validation_checking),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }

                if (error != null) {
                    CardinalNotice(text = error)
                }

                if (onResend != null) {
                    ResendRow(
                        cooldownSeconds = resendCooldownSeconds,
                        restartKey = email,
                        onResend = onResend
                    )
                }

                Text(
                    text = stringResource(Res.string.validation_expiry_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * **Resend code** plus the countdown that gates it.
 *
 * The countdown is presentation, not state worth hoisting: it restarts whenever
 * [restartKey] changes, which is what happens when a new code goes out.
 */
@Composable
private fun ResendRow(
    cooldownSeconds: Int,
    restartKey: Any?,
    onResend: () -> Unit,
    modifier: Modifier = Modifier
) {
    var secondsLeft by remember(restartKey) { mutableIntStateOf(cooldownSeconds) }

    LaunchedEffect(restartKey) {
        while (secondsLeft > 0) {
            delay(1_000)
            secondsLeft--
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CardinalGhostButton(
            text = stringResource(Res.string.validation_resend),
            onClick = onResend,
            enabled = secondsLeft == 0
        )
        if (secondsLeft > 0) {
            Text(
                text = stringResource(
                    Res.string.validation_resend_in,
                    "0:" + secondsLeft.toString().padStart(2, '0')
                ),
                style = MaterialTheme.typography.bodySmall.merge(TabularFigures),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Turns a [ValidationError] into something worth reading.
 *
 * Each message says whether retyping the code can help, because the recoveries
 * differ: a wrong code is retypeable, a consumed code is not.
 */
@Composable
private fun validationErrorMessage(error: ValidationError): String = when (error) {
    ValidationError.WrongCode ->
        stringResource(Res.string.validation_error_wrong_code)

    ValidationError.TooManyAttempts ->
        stringResource(Res.string.validation_error_too_many_attempts)

    ValidationError.Network ->
        stringResource(Res.string.validation_error_network)

    is ValidationError.SetupFailed ->
        stringResource(Res.string.validation_error_setup_failed).withDetail(error.detail)

    is ValidationError.DeviceStorage ->
        stringResource(Res.string.validation_error_device_storage).withDetail(error.detail)

    is ValidationError.Unknown ->
        error.detail ?: stringResource(Res.string.validation_error_unknown)
}

/**
 * Appends the technical cause in brackets, when there is one.
 *
 * The detail comes off an exception, so it is neither translated nor guaranteed to be
 * readable — it rides along for a support conversation, behind the plain message.
 */
@Composable
private fun String.withDetail(detail: String?): String =
    if (detail.isNullOrBlank()) this
    else stringResource(Res.string.validation_error_with_detail, this, detail)

@Preview
@Composable
private fun ValidationScreenEmptyPreview() {
    MaterialTheme {
        ValidationScreen(
            email = "jane.doe@icure.com",
            code = "",
            isValidating = false,
            error = null,
            onCodeChange = {},
            onBack = {},
            onResend = {}
        )
    }
}

@Preview
@Composable
private fun ValidationScreenPartialPreview() {
    MaterialTheme {
        ValidationScreen(
            email = "jane.doe@icure.com",
            code = "4190",
            isValidating = false,
            error = null,
            onCodeChange = {},
            onBack = {},
            onResend = {}
        )
    }
}

@Preview
@Composable
private fun ValidationScreenErrorPreview() {
    MaterialTheme {
        ValidationScreen(
            email = "jane.doe@icure.com",
            code = "419055",
            isValidating = false,
            error = validationErrorMessage(ValidationError.WrongCode),
            onCodeChange = {},
            onBack = {},
            onResend = {}
        )
    }
}

@Preview
@Composable
private fun ValidationScreenValidatingPreview() {
    MaterialTheme {
        ValidationScreen(
            email = "jane.doe@icure.com",
            code = "419055",
            isValidating = true,
            error = null,
            onCodeChange = {},
            onBack = {},
            onResend = {}
        )
    }
}
