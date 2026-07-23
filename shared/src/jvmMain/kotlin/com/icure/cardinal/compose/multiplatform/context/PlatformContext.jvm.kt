package com.icure.cardinal.compose.multiplatform.context

import com.icure.cardinal.sdk.storage.StorageFacade
import com.icure.cardinal.sdk.storage.impl.JavaFileStorageFacade

actual object PlatformContext {
    actual val applicationId: String? = System.getProperty("APPLICATION_ID").takeIf { it.isNotEmpty() }

    actual val processId: String = System.getProperty("PROCESS_ID")

    actual val specId: String = System.getProperty("EXTERNAL_SERVICES_SPEC_ID")

    actual val cardinalStorageFacade: StorageFacade = JavaFileStorageFacade(System.getProperty("KEY_STORAGE_PATH").also { println("cardinalStorageFacade $it")})
}