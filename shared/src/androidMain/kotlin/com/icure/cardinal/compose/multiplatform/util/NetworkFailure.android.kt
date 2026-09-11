package com.icure.cardinal.compose.multiplatform.util

import java.io.IOException
import java.nio.channels.UnresolvedAddressException

/**
 * On the JVM `kotlinx.io.IOException` is an alias for [IOException], so this covers both the ktor
 * timeout exceptions and the raw engine failures (`UnknownHostException`, `SocketTimeoutException`,
 * `SSLException`, `ConnectException`).
 *
 * [UnresolvedAddressException] is the exception to the rule: the CIO engine raises it for a DNS
 * failure and it extends `IllegalArgumentException`, not [IOException].
 */
internal actual fun Throwable.isPlatformIoFailure(): Boolean =
    this is IOException || this is UnresolvedAddressException
