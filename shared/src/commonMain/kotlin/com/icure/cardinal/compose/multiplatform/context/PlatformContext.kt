package com.icure.cardinal.compose.multiplatform.context

import com.icure.cardinal.sdk.storage.StorageFacade

expect object PlatformContext {
    val applicationId: String?
    val processId: String
    val specId: String

    val cardinalStorageFacade: StorageFacade
}