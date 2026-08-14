package com.asridigital.masjiddisplay.protocol

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MediaWireContractTest {
    @Test
    fun sessionRequestRoundTripsReservedCharacters() {
        val request = MediaUploadSessionRequest(
            credentialId = "trusted+credential",
            mediaId = "media-1",
            filename = "foto masjid & halaman.jpg",
            mimeType = "image/jpeg",
            byteSize = 1234,
            sha256 = "a".repeat(64),
        )

        assertEquals(request.copy(sha256 = request.sha256.lowercase()), MediaWireContract.decodeSessionRequest(MediaWireContract.encodeSessionRequest(request)))
    }

    @Test
    fun acceptedSessionAndMutationResponsesRoundTrip() {
        val session = MediaSessionResponse.Accepted("session-123")
        assertEquals(session, MediaWireContract.decodeSessionResponse(MediaWireContract.encodeSessionResponse(session)))
        assertEquals(MediaMutationResponse.Success, MediaWireContract.decodeMutationResponse(MediaWireContract.encodeMutationResponse(MediaMutationResponse.Success)))
    }

    @Test
    fun malformedSessionMetadataIsRejectedByDecoder() {
        assertEquals(null, MediaWireContract.decodeSessionRequest("credentialId=x&mediaId=y"))
    }

    @Test
    fun errorResponsePreservesActionableCode() {
        val response = MediaMutationResponse.Rejected("CHECKSUM_MISMATCH", "Checksum media tidak sesuai")
        val decoded = assertIs<MediaMutationResponse.Rejected>(MediaWireContract.decodeMutationResponse(MediaWireContract.encodeMutationResponse(response)))
        assertEquals("CHECKSUM_MISMATCH", decoded.code)
    }
}
