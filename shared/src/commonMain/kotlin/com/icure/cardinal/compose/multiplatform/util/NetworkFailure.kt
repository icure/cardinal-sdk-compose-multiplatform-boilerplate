package com.icure.cardinal.compose.multiplatform.util

/**
 * True when [this] is a transport-level failure raised by the platform HTTP stack: no connectivity,
 * DNS resolution failure, TLS handshake failure, connection reset or timeout.
 *
 * Only the leaf predicate is platform-specific, see the actual declarations.
 */
internal expect fun Throwable.isPlatformIoFailure(): Boolean

/**
 * Maximum number of links followed while walking the cause chain. Guards against the self- or
 * mutually-referencing chains some HTTP engines produce.
 */
private const val MAX_CAUSE_DEPTH = 8

/**
 * True when [this] or any of its causes is a transport-level failure.
 *
 * The Cardinal SDK lets engine exceptions propagate unwrapped, but the coroutine machinery and the
 * ktor plugins it installs often wrap them, so the whole chain has to be inspected. Distinct from
 * `RequestStatusException`, which means the server answered with a non-2xx status.
 */
internal fun Throwable.isNetworkFailure(): Boolean {
    var current: Throwable? = this
    var depth = 0
    while (current != null && depth < MAX_CAUSE_DEPTH) {
        val throwable = current
        if (throwable.isPlatformIoFailure()) return true
        current = throwable.cause?.takeIf { it !== throwable }
        depth++
    }
    return false
}
