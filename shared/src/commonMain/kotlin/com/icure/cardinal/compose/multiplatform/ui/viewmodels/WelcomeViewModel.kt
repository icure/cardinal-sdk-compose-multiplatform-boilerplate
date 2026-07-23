package com.icure.cardinal.compose.multiplatform.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icure.cardinal.sdk.CardinalSdk
import com.icure.cardinal.sdk.filters.PatientFilters
import com.icure.cardinal.sdk.model.DecryptedPatient
import com.icure.kryptom.crypto.defaultCryptoService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


sealed interface WelcomeIntent {
    interface Demo {
        data class CreatePatients(val count: Int) : WelcomeIntent
        data object GetPatients : WelcomeIntent
    }
}

class WelcomeViewModel(private val sdk: CardinalSdk) : ViewModel() {
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    fun processIntent(intent: WelcomeIntent) {
        when (intent) {
            is WelcomeIntent.Demo.CreatePatients -> handleCreatePatients(intent.count)
            WelcomeIntent.Demo.GetPatients -> handleGetPatients()
        }
    }

    private fun doAsyncWithBusy(action: suspend () -> Unit) {
        if (_busy.compareAndSet(expect = false, update = true)) {
            viewModelScope.launch(Dispatchers.Default) {
                try {
                    action()
                } finally {
                    _busy.value = false
                }
            }
        }
    }

    private fun handleCreatePatients(count: Int) = doAsyncWithBusy {
        val demoId = defaultCryptoService.strongRandom.randomUUID()
        val created = sdk.patient.createPatients(
            List(count) {
                sdk.patient.withEncryptionMetadata(
                    base = DecryptedPatient(
                        id = defaultCryptoService.strongRandom.randomUUID(),
                        firstName = "Demo $demoId",
                        lastName = "Patient $it"
                    )
                )
            }
        )
        println("Created patients $created")
    }

    private fun handleGetPatients() = doAsyncWithBusy {
        // For this boilerplate we didn't setup proper key management across devices, use the "tryAndRecover"
        // flavour to make sure that data created from other devices is not going to cause errors during decryption.
        val patients = sdk.patient.tryAndRecover.filterPatientsBy(PatientFilters.allPatientsForSelf())
        while (patients.hasNext()) {
            println("Fetched patients page ${patients.next(10)}")
        }
    }
}

