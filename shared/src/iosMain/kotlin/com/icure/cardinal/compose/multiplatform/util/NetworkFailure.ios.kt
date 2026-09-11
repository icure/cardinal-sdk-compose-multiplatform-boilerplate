package com.icure.cardinal.compose.multiplatform.util

import kotlinx.io.IOException

/**
 * The Darwin engine wraps every `NSURLErrorDomain` failure in `DarwinHttpRequestException`, and
 * ktor's own `HttpRequestTimeoutException` / `ConnectTimeoutException` / `SocketTimeoutException`
 * all extend [IOException] as well, so the single supertype check is enough here.
 */
internal actual fun Throwable.isPlatformIoFailure(): Boolean = this is IOException
