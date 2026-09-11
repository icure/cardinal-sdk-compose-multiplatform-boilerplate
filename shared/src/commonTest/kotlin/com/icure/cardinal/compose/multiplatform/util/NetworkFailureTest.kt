package com.icure.cardinal.compose.multiplatform.util

import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NetworkFailureTest {

    @Test
    fun `a transport failure is recognised directly`() {
        assertTrue(IOException("connection reset").isNetworkFailure())
    }

    @Test
    fun `a transport failure is recognised through its wrappers`() {
        val wrapped = IllegalStateException(
            "sdk bootstrap failed",
            RuntimeException("ktor plugin", IOException("no route to host"))
        )
        assertTrue(wrapped.isNetworkFailure())
    }

    @Test
    fun `an unrelated failure is not a transport failure`() {
        assertFalse(IllegalArgumentException("bad code").isNetworkFailure())
    }

    @Test
    fun `the cause chain is not followed past the depth cap`() {
        // Nine links, with the only transport failure at the very bottom: the walk
        // gives up before reaching it rather than paying for an unbounded chain.
        var deep: Throwable = IOException("dns")
        repeat(9) { deep = RuntimeException("wrapper", deep) }
        assertFalse(deep.isNetworkFailure())
    }

    @Test
    fun `a self-referencing cause terminates the walk`() {
        assertFalse(SelfCausedException().isNetworkFailure())
    }

    /** Some HTTP engines produce these; walking one naively never returns. */
    private class SelfCausedException : Exception("loops back on itself") {
        override val cause: Throwable get() = this
    }
}
