package com.icure.cardinal.compose.multiplatform.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icure.cardinal.compose.multiplatform.context.PlatformContext
import com.icure.cardinal.compose.multiplatform.util.isNetworkFailure
import com.icure.cardinal.sdk.CardinalSdk
import com.icure.cardinal.sdk.auth.AuthenticationProcessTelecomType
import com.icure.cardinal.sdk.auth.CaptchaOptions
import com.icure.cardinal.sdk.exceptions.SecureStorageException
import com.icure.cardinal.sdk.utils.EntityEncryptionException
import com.icure.cardinal.sdk.utils.IllegalEntityException
import com.icure.cardinal.sdk.utils.InternalCardinalException
import com.icure.cardinal.sdk.utils.RequestStatusException
import com.icure.cardinal.sdk.utils.UnexpectedResponseContentException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

/** The path the message gateway serves the code check on. */
private const val VALIDATE_PATH = "/process/validate/"

/** The gateway issues six-digit codes. */
const val ValidationCodeLength = 6

/**
 * Where authentication has got to, and everything the screen for that step needs.
 *
 * One state carries one screen's data, so a field can only exist while the step it
 * belongs to is current: there is no way to hold a validation code without a
 * `step` to spend it on, or a proof-of-work percentage after the code was sent.
 */
sealed interface AuthState {
    data class Unauthenticated(
        val email: String = ""
    ) : AuthState

    data class SolvingChallenge(
        val email: String,
        val progress: Double
    ) : AuthState

    data class PendingCompletion(
        val email: String,
        val step: CardinalSdk.AuthenticationWithProcessStep,
        val validationCode: String = "",
        val validating: Boolean = false,
        val error: ValidationError? = null
    ) : AuthState

    data class Authenticated(
        val sdk: CardinalSdk,
        val sdkId: String
    ) : AuthState

    sealed interface Error : AuthState {
        /**
         * Starting authentication failed. Keeps the [email] so retrying returns to a
         * filled email step rather than an empty one.
         */
        data class StartAuthentication(
            val email: String,
            val message: String?
        ) : Error
    }
}

/**
 * Why a code was refused.
 *
 * The distinction that matters is whether the code is still spendable: a wrong code
 * can be retyped against the same `step`, a consumed one cannot.
 */
sealed interface ValidationError {
    /** Code refused by the message gateway; `step` is still usable, let the user retype. */
    data object WrongCode : ValidationError
    data object TooManyAttempts : ValidationError
    /** Transient; same code may still work, keep `step`. */
    data object Network : ValidationError
    /** Code was accepted but SDK bootstrap failed: the code is consumed, restart from the email step. */
    data class SetupFailed(val detail: String?) : ValidationError
    /** Device keystore problem; retrying the code changes nothing. */
    data class DeviceStorage(val detail: String?) : ValidationError
    data class Unknown(val detail: String?) : ValidationError
}

sealed interface AuthIntent {
    data object Logout : AuthIntent

    sealed interface Login : AuthIntent {
        data class EmailChanged(val email: String) : Login
        data object SubmitLogin : Login
        /** Leaves [AuthState.Error.StartAuthentication] for the email step. */
        data object ClearError : Login
    }

    sealed interface Validation : AuthIntent {
        data class CodeChanged(val code: String) : Validation
        data object ChangeEmail : Validation
    }
}

class AppViewModel : ViewModel() {
    val authState: StateFlow<AuthState>
        field = MutableStateFlow<AuthState>(AuthState.Unauthenticated())

    fun processIntent(intent: AuthIntent) {
        viewModelScope.launch {
            when (intent) {
                is AuthIntent.Login.EmailChanged -> handleEmailChanged(intent.email)
                is AuthIntent.Login.SubmitLogin -> handleSubmitLogin()
                is AuthIntent.Login.ClearError -> handleLoginClearError()

                is AuthIntent.Validation.CodeChanged -> handleCodeChanged(intent.code)
                is AuthIntent.Validation.ChangeEmail -> handleChangeEmail()

                is AuthIntent.Logout -> handleLogout()
            }
        }
    }

    private fun handleEmailChanged(email: String) {
        authState.update { current ->
            (current as? AuthState.Unauthenticated)?.copy(email = email) ?: current
        }
    }

    /**
     * Starts the SDK, which is what runs the Kerberus proof of work and asks the
     * gateway for a code.
     *
     * The email is read once here rather than inside the coroutine: the state moves
     * to [AuthState.SolvingChallenge] immediately and no longer carries it in a
     * form this function can cast to.
     */
    private fun handleSubmitLogin() {
        val email = (authState.value as? AuthState.Unauthenticated)?.email ?: return
        authState.update { AuthState.SolvingChallenge(email = email, progress = 0.0) }

        viewModelScope.launch(Dispatchers.Default) {
            runCatching {
                CardinalSdk.initializeWithProcess(
                    projectId = PlatformContext.applicationId,
                    baseUrl = "https://api.icure.cloud",
                    messageGatewayUrl = "https://msg-gw.icure.cloud",
                    externalServicesSpecId = PlatformContext.specId,
                    processId = PlatformContext.processId,
                    userTelecomType = AuthenticationProcessTelecomType.Email,
                    userTelecom = email,
                    captcha = CaptchaOptions.Kerberus.Delegated { progress ->
                        authState.update { current ->
                            (current as? AuthState.SolvingChallenge)?.copy(progress = progress)
                                ?: current
                        }
                    },
                    baseStorage = PlatformContext.cardinalStorageFacade,
                )
            }.onSuccess { step ->
                authState.update { AuthState.PendingCompletion(email = email, step = step) }
            }.onFailure { exception ->
                authState.update {
                    AuthState.Error.StartAuthentication(email = email, message = exception.message)
                }
            }
        }
    }

    private fun handleLoginClearError() {
        authState.update { current ->
            when (current) {
                is AuthState.Error.StartAuthentication ->
                    AuthState.Unauthenticated(email = current.email)

                else -> current
            }
        }
    }

    private fun handleChangeEmail() {
        authState.update { current ->
            when (current) {
                is AuthState.PendingCompletion -> AuthState.Unauthenticated(email = current.email)
                else -> current
            }
        }
    }

    /**
     * Records a digit and, on the last one, spends the code.
     *
     * There is no submit intent: the code is checked the moment it is complete,
     * which is also how platform autofill delivers it.
     */
    private fun handleCodeChanged(code: String) {
        val pending = authState.value as? AuthState.PendingCompletion ?: return
        if (code.length > ValidationCodeLength || !code.all { it.isDigit() }) return

        authState.update { current ->
            (current as? AuthState.PendingCompletion)
                ?.copy(validationCode = code, error = null)
                ?: current
        }

        if (code.length == ValidationCodeLength) completeAuthentication(pending.step, code)
    }

    /**
     * Exchanges the code for a signed-in SDK.
     *
     * The call sits outside `update` on purpose: `update` re-runs its lambda when
     * another emission wins the compare-and-set, and this code can only be spent once.
     */
    private fun completeAuthentication(
        step: CardinalSdk.AuthenticationWithProcessStep,
        code: String
    ) {
        authState.update { current ->
            (current as? AuthState.PendingCompletion)?.copy(validating = true) ?: current
        }

        viewModelScope.launch(Dispatchers.Default) {
            runCatching {
                step.completeAuthentication(code)
            }.onSuccess { sdk ->
                authState.update { AuthState.Authenticated(sdk, Uuid.random().toString()) }
            }.onFailure { exception ->
                val error = mapCompletionError(exception)
                authState.update { current ->
                    (current as? AuthState.PendingCompletion)
                        ?.copy(validating = false, error = error)
                        ?: current
                }
            }
        }
    }

    private fun handleLogout() {
        (authState.value as? AuthState.Authenticated)?.sdk?.scope?.cancel()
        authState.update { AuthState.Unauthenticated() }
    }
}

/**
 * Classifies a failure of `completeAuthentication`.
 *
 * The gateway and the SDK bootstrap that follows it fail for different reasons and
 * only the first leaves the code usable, so the URL decides which of the two
 * answered before the status code is read.
 */
internal fun mapCompletionError(t: Throwable): ValidationError = when (t) {
    is RequestStatusException ->
        if (VALIDATE_PATH in t.url) when (t.statusCode) {
            429 -> ValidationError.TooManyAttempts
            in 400..499 -> ValidationError.WrongCode
            else -> ValidationError.Network            // 5xx on the gateway
        } else {
            // The gateway already validated the code: login or bootstrap failed downstream.
            ValidationError.SetupFailed("HTTP ${t.statusCode}")
        }

    is SecureStorageException -> ValidationError.DeviceStorage(t.message)
    is HttpRequestTimeoutException -> ValidationError.Network
    is InternalCardinalException,
    is UnexpectedResponseContentException,
    is EntityEncryptionException,
    is IllegalEntityException -> ValidationError.SetupFailed(t.message)

    else -> if (t.isNetworkFailure()) ValidationError.Network
    else ValidationError.Unknown(t.message)
}
