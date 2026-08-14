package com.asridigital.masjiddisplay.protocol

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PairingWireContractTest {
    @Test fun completeRequestRoundTrips() {
        val request = CompletePairingRequest("session & 1", "secret=1", 1)
        assertEquals(request, PairingWireContract.decodeComplete(PairingWireContract.encodeComplete(request)))
    }

    @Test fun openResponseRoundTrips() {
        val response = OpenPairingResponse("session-1", 1, Instant.parse("2026-08-13T10:05:00Z"))
        val encoded = PairingWireContract.encodeOpen(response)
        assertEquals(response, PairingWireContract.decodeOpen(encoded))
        assertNull(encoded.split('&').firstOrNull { it.startsWith("oneTimeSecret=") })
    }

    @Test fun successAndRejectedResultsRoundTrip() {
        val success = CompletePairingResponse.Success("credential-1", Instant.parse("2026-08-13T10:00:00Z"))
        val rejected = CompletePairingResponse.Rejected(PairingErrorCode.REPLAY_REJECTED)
        assertEquals(success, PairingWireContract.decodeResult(PairingWireContract.encodeResult(success)))
        assertEquals(rejected, PairingWireContract.decodeResult(PairingWireContract.encodeResult(rejected)))
    }

    @Test fun malformedBodiesAreRejected() {
        assertNull(PairingWireContract.decodeComplete("broken"))
        assertNull(PairingWireContract.decodeOpen("status=wat"))
        assertNull(PairingWireContract.decodeResult("status=rejected&code=UNKNOWN"))
    }
}
