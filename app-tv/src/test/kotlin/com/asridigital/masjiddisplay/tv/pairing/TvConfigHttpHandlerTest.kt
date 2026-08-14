package com.asridigital.masjiddisplay.tv.pairing

import com.asridigital.masjiddisplay.protocol.ConfigTransportPaths
import com.asridigital.masjiddisplay.protocol.ConfigWireContract
import com.asridigital.masjiddisplay.protocol.TvConfigUpdateRequest
import com.asridigital.masjiddisplay.protocol.TvConfigUpdateResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TvConfigHttpHandlerTest {
    @Test
    fun trustedCredentialPersistsValidatedConfig() {
        var persisted: TvConfigUpdateRequest? = null
        val handler = TvConfigHttpHandler(
            sink = TvConfigSink { persisted = it },
            isCredentialTrusted = { it == "trusted" },
        )
        val request = configRequest("trusted")

        val response = handler.handle(
            PairingHttpRequest("POST", ConfigTransportPaths.UPDATE, ConfigWireContract.encodeRequest(request)),
        )

        assertEquals(200, response.status)
        assertEquals(TvConfigUpdateResponse.Success, ConfigWireContract.decodeResponse(response.body))
        assertEquals(request, persisted)
    }

    @Test
    fun unknownCredentialCannotMutateConfig() {
        var persisted: TvConfigUpdateRequest? = null
        val handler = TvConfigHttpHandler(
            sink = TvConfigSink { persisted = it },
            isCredentialTrusted = { false },
        )

        val response = handler.handle(
            PairingHttpRequest("POST", ConfigTransportPaths.UPDATE, ConfigWireContract.encodeRequest(configRequest("intruder"))),
        )

        assertEquals(403, response.status)
        assertNull(persisted)
        assertEquals(
            TvConfigUpdateResponse.Rejected("UNAUTHORIZED", "Credential Admin tidak dikenal"),
            ConfigWireContract.decodeResponse(response.body),
        )
    }

    @Test
    fun malformedConfigDoesNotReachPersistenceBoundary() {
        var writes = 0
        val handler = TvConfigHttpHandler(
            sink = TvConfigSink { writes++ },
            isCredentialTrusted = { true },
        )

        val response = handler.handle(PairingHttpRequest("POST", ConfigTransportPaths.UPDATE, "broken"))

        assertEquals(400, response.status)
        assertEquals(0, writes)
    }

    private fun configRequest(credential: String) = TvConfigUpdateRequest(
        credentialId = credential,
        mosqueId = "mosque-1",
        mosqueName = "Masjid Al Ikhlas",
        locationLabel = "Sleman",
        latitude = -7.75,
        longitude = 110.37,
        timezoneId = "Asia/Jakarta",
        prayerMethod = "KEMENAG_INDONESIA",
    )
}
