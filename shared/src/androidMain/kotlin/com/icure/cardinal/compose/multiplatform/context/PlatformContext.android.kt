package com.icure.cardinal.compose.multiplatform.context

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.icure.cardinal.sdk.storage.StorageFacade
import com.icure.cardinal.sdk.storage.impl.DataStorePreferenceStorage

private val Context.cardinalDataStore: DataStore<Preferences> by preferencesDataStore(
    name = PlatformContext.applicationId?.let {
        "$it.cardinal"
    } ?: "cardinal"
)

actual object PlatformContext {

    private var _applicationContext: Application? = null
    private var _applicationId: String? = null
    private var _processId: String? = null
    private var _specId: String? = null

    actual val applicationId: String?
        get() = _applicationId?.takeIf { it.isNotEmpty() }

    actual val processId: String
        get() = requireNotNull(_processId) {
            "processId is not initialized. Call setupValues() before using this property."
        }

    actual val specId: String
        get() = requireNotNull(_specId) {
            "specId is not initialized. Call setupValues() before using this property."
        }

    actual val cardinalStorageFacade: StorageFacade by lazy {
        requireNotNull(_applicationContext) {
            "applicationContext is not initialized. Call setupValues() before using this property."
        }
        DataStorePreferenceStorage(_applicationContext!!.cardinalDataStore)
    }

    // Config values are injected by the Android app entry point (androidApp module),
    // which owns the generated BuildConfig. The shared library can no longer read it directly.
    fun setupValues(
        applicationContext: Application,
        applicationId: String,
        processId: String,
        specId: String
    ) {
        _applicationContext = applicationContext
        _applicationId = applicationId
        _processId = processId
        _specId = specId
    }
}