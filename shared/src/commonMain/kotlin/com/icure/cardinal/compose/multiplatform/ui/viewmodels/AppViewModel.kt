package com.icure.cardinal.compose.multiplatform.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icure.cardinal.compose.multiplatform.context.PlatformContext
import com.icure.cardinal.sdk.CardinalSdk
import com.icure.cardinal.sdk.auth.AuthenticationProcessTelecomType
import com.icure.cardinal.sdk.auth.CaptchaOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface AppState {
    data object Unauthenticated : AppState
    data object PendingValidation : AppState
    data class Authenticated(val sdk: CardinalSdk, val sdkId: String) : AppState
}

data class LoginState(
    val email: String = "",
    val isLoading: Boolean = false,
    val progress: Float = 0f,
    val error: String? = null,
)

data class ValidationState(
    val code: String = "",
    val isValidating: Boolean = false,
    val error: String? = null
)

sealed interface AppIntent {
    data object CompleteLogin : AppIntent
    data object Logout : AppIntent

    sealed interface Login : AppIntent {
        data class EmailChanged(val email: String) : Login
        data object SubmitLogin : Login
        data object ClearError : Login
    }

    sealed interface Validation : AppIntent {
        data class CodeChanged(val code: String) : Validation
        data object SubmitValidation : Validation
        data object ClearError : Validation
    }
}

class AppViewModel : ViewModel() {
    private val _authState = MutableStateFlow<AppState>(AppState.Unauthenticated)
    val authState: StateFlow<AppState> = _authState.asStateFlow()

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _validationState = MutableStateFlow(ValidationState())
    val validationState: StateFlow<ValidationState> = _validationState.asStateFlow()

    private var _sdk: CardinalSdk? = null
    private var _step: CardinalSdk.AuthenticationWithProcessStep? = null

    fun processIntent(intent: AppIntent) {
        viewModelScope.launch {
            when (intent) {
                is AppIntent.CompleteLogin -> handleCompleteLogin()
                is AppIntent.Logout -> handleLogout()

                is AppIntent.Login.EmailChanged -> handleEmailChanged(intent.email)
                is AppIntent.Login.SubmitLogin -> handleSubmitLogin()
                is AppIntent.Login.ClearError -> handleLoginClearError()

                is AppIntent.Validation.CodeChanged -> handleCodeChanged(intent.code)
                is AppIntent.Validation.SubmitValidation -> handleSubmitValidation()
                is AppIntent.Validation.ClearError -> handleValidationClearError()
            }
        }
    }

    private fun handleCompleteLogin() {
        _authState.update { AppState.PendingValidation }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun handleCompleteValidation(sdk: CardinalSdk) {
        _sdk = sdk

        viewModelScope.launch(Dispatchers.Default) {
            _authState.update { AppState.Authenticated(sdk, Uuid.random().toString()) }
        }
    }

    private fun handleLogout() {
        _sdk?.scope?.cancel()
        _authState.update { AppState.Unauthenticated }
        _loginState.update { LoginState() }
        _validationState.update { ValidationState() }
    }

    private fun handleEmailChanged(email: String) {
        _loginState.update { it.copy(email = email, error = null) }
    }

    private fun handleSubmitLogin() {
        _loginState.update { it.copy(isLoading = true, error = null, progress = 0f) }

        viewModelScope.launch(Dispatchers.Default) {
            runCatching {
                CardinalSdk.initializeWithProcess(
                    projectId = PlatformContext.applicationId,
                    baseUrl = "https://api.icure.cloud",
                    messageGatewayUrl = "https://msg-gw.icure.cloud",
                    externalServicesSpecId = PlatformContext.specId,
                    processId = PlatformContext.processId,
                    userTelecomType = AuthenticationProcessTelecomType.Email,
                    userTelecom = _loginState.value.email,
                    captcha = CaptchaOptions.Kerberus.Delegated { progress ->
                        _loginState.update {
                            it.copy(progress = progress.toFloat())
                        }
                    },
                    baseStorage = PlatformContext.cardinalStorageFacade,
                )
            }.onSuccess { step ->
                _step = step
                _loginState.update { it.copy(isLoading = false) }
                _authState.update { AppState.PendingValidation }
            }.onFailure { exception ->
                _loginState.update { it.copy(isLoading = false, error = exception.message) }
            }
        }
    }

    private fun handleLoginClearError() {
        _loginState.update { it.copy(error = null) }
    }

    private fun handleCodeChanged(code: String) {
        if (code.length <= 6 && code.all { it.isDigit() }) {
            _validationState.update { it.copy(code = code, error = null) }
        }
    }

    private fun handleSubmitValidation() {
        _validationState.update { it.copy(isValidating = true, error = null) }

        viewModelScope.launch(Dispatchers.Default) {
            runCatching {
                requireNotNull(_step) {
                    "AuthenticationWithProcessStep is null. Cannot proceed with validation."
                }.completeAuthentication(_validationState.value.code)
            }.onSuccess { sdk ->
                handleCompleteValidation(sdk)
            }.onFailure { exception ->
                _validationState.update {
                    it.copy(isValidating = false, error = exception.message)
                }
            }
        }
    }

    private fun handleValidationClearError() {
        _validationState.update { it.copy(error = null) }
    }
}

