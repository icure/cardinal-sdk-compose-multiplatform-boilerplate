package com.icure.cardinal.compose.multiplatform.util

import java.io.IOException
import java.nio.channels.UnresolvedAddressException

/**
 * Same rule as the Android actual — the desktop app runs the same JVM engine, where
 * `kotlinx.io.IOException` is an alias for [IOException] and only the CIO engine's DNS failure
 * ([UnresolvedAddressException]) falls outside that hierarchy.
 */
internal actual fun Throwable.isPlatformIoFailure(): Boolean =
    this is IOException || this is UnresolvedAddressException
