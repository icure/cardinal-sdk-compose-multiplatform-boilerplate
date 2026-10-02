package com.icure.cardinal.compose.multiplatform.ui.viewmodels

import com.icure.cardinal.sdk.exceptions.SecureStorageException
import com.icure.cardinal.sdk.utils.EntityEncryptionException
import com.icure.cardinal.sdk.utils.InternalCardinalException
import com.icure.cardinal.sdk.utils.RequestStatusException
import io.ktor.http.HttpMethod
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CompletionErrorMappingTest {

    private fun gatewayFailure(statusCode: Int) = RequestStatusException(
        requestMethod = HttpMethod.Post,
        url = "https://msg-gw.icure.cloud/process/validate/some-process/1234",
        statusCode = statusCode,
        body = null
    )

    @Test
    fun `a rejected code leaves the step usable`() {
        assertEquals(ValidationError.WrongCode, mapCompletionError(gatewayFailure(400)))
        assertEquals(ValidationError.WrongCode, mapCompletionError(gatewayFailure(401)))
        assertEquals(ValidationError.WrongCode, mapCompletionError(gatewayFailure(404)))
    }

    @Test
    fun `rate limiting is told apart from a wrong code`() {
        assertEquals(ValidationError.TooManyAttempts, mapCompletionError(gatewayFailure(429)))
    }

    @Test
    fun `a gateway server error is transient and not the user's fault`() {
        assertEquals(ValidationError.Network, mapCompletionError(gatewayFailure(503)))
    }

    @Test
    fun `a failure past the gateway means the code was already spent`() {
        val downstream = RequestStatusException(
            requestMethod = HttpMethod.Get,
            url = "https://api.icure.cloud/rest/v2/user/current",
            statusCode = 500,
            body = null
        )
        assertEquals(ValidationError.SetupFailed("HTTP 500"), mapCompletionError(downstream))
    }

    @Test
    fun `a keystore problem is not worth a retry`() {
        val error = mapCompletionError(SecureStorageException("keystore key unavailable"))
        assertEquals(ValidationError.DeviceStorage("keystore key unavailable"), error)
    }

    @Test
    fun `an sdk bootstrap failure sends the user back to the email step`() {
        assertEquals(
            ValidationError.SetupFailed("no data owner"),
            mapCompletionError(InternalCardinalException("no data owner"))
        )
        assertEquals(
            ValidationError.SetupFailed("cannot decrypt"),
            mapCompletionError(EntityEncryptionException("cannot decrypt"))
        )
    }

    @Test
    fun `a wrapped transport failure is transient`() {
        val wrapped = IllegalStateException("bootstrap", IOException("connection reset"))
        assertEquals(ValidationError.Network, mapCompletionError(wrapped))
    }

    @Test
    fun `anything else carries its message for support`() {
        val error = mapCompletionError(RuntimeException("something odd"))
        assertIs<ValidationError.Unknown>(error)
        assertEquals("something odd", error.detail)
    }
}
