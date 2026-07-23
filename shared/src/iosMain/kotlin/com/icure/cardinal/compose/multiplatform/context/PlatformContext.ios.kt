package com.icure.cardinal.compose.multiplatform.context

import com.icure.cardinal.sdk.storage.StorageFacade
import com.icure.cardinal.sdk.storage.impl.UserDefaultStorageFacade

actual object PlatformContext {
    actual val applicationId: String?
        get() = requireNotNull(_applicationId) {
            "applicationId is not initialized. Call setupValues() before using this property."
        }.takeIf { it.isNotEmpty() }

    actual val processId: String
        get() = requireNotNull(_processId) {
            "processId is not initialized. Call setupValues() before using this property."
        }

    actual val specId: String
        get() = requireNotNull(_specId) {
            "specId is not initialized. Call setupValues() before using this property."
        }

    private var _applicationId: String? = null
    private var _processId: String? = null
    private var _specId: String? = null

    actual val cardinalStorageFacade: StorageFacade by lazy {
        UserDefaultStorageFacade(
            applicationId?.let { "$it.cardinal" } ?: "cardinal"
        )
    }

    fun setupValues(
        applicationId: String,
        processId: String,
        specId: String
    ) {
        _applicationId = applicationId
        _processId = processId
        _specId = specId
    }
}