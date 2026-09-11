package com.icure.cardinal.compose.multiplatform.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cardinalcomposemultiplatform.shared.generated.resources.Res
import cardinalcomposemultiplatform.shared.generated.resources.login_action_send
import cardinalcomposemultiplatform.shared.generated.resources.login_email_label
import cardinalcomposemultiplatform.shared.generated.resources.login_email_placeholder
import cardinalcomposemultiplatform.shared.generated.resources.login_intro
import cardinalcomposemultiplatform.shared.generated.resources.login_title
import com.icure.cardinal.compose.multiplatform.ui.components.CardinalPrimaryButton
import com.icure.cardinal.compose.multiplatform.ui.components.CardinalTextField
import com.icure.cardinal.compose.multiplatform.ui.components.CardinalTopBar
import com.icure.cardinal.compose.multiplatform.ui.viewmodels.AppViewModel
import com.icure.cardinal.compose.multiplatform.ui.viewmodels.AuthIntent
import com.icure.cardinal.compose.multiplatform.ui.viewmodels.AuthState
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * The email step.
 *
 * Cardinal holds the identity and no password is ever typed, so this screen collects
 * one address and nothing else. Submitting it initialises the SDK, which is what
 * starts the Kerberus check on [SolvingChallengeScreen].
 *
 * This overload only reads [AppViewModel]; all rendering lives in the stateless
 * overload below, which is what the previews and any test exercise.
 */
@Composable
fun LoginScreen(
    appViewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val state by appViewModel.authState.collectAsState()
    val unauthenticated = state as? AuthState.Unauthenticated ?: return

    LoginScreen(
        email = unauthenticated.email,
        onEmailChange = { appViewModel.processIntent(AuthIntent.Login.EmailChanged(it.trim())) },
        onSubmit = { appViewModel.processIntent(AuthIntent.Login.SubmitLogin) },
        modifier = modifier
    )
}

/**
 * The email step as pure UI.
 *
 * @param onBack pass `null` when nothing precedes this screen — the arrow is then
 *   omitted rather than rendered dead.
 */
@Composable
fun LoginScreen(
    email: String,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val canSubmit = email.isNotBlank()

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
            CardinalTopBar(title = stringResource(Res.string.login_title), onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = stringResource(Res.string.login_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.8f)
                )

                CardinalTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = stringResource(Res.string.login_email_label),
                    placeholder = stringResource(Res.string.login_email_placeholder),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(onGo = { if (canSubmit) onSubmit() })
                )
            }

            Column(modifier = Modifier.padding(24.dp)) {
                CardinalPrimaryButton(
                    text = stringResource(Res.string.login_action_send),
                    onClick = onSubmit,
                    enabled = canSubmit
                )
            }
        }
    }
}

@Preview
@Composable
private fun LoginScreenEmptyPreview() {
    MaterialTheme {
        LoginScreen(email = "", onEmailChange = {}, onSubmit = {})
    }
}

@Preview
@Composable
private fun LoginScreenFilledPreview() {
    MaterialTheme {
        LoginScreen(
            email = "jane.doe@icure.com",
            onEmailChange = {},
            onSubmit = {}
        )
    }
}
