package com.asridigital.masjiddisplay.license

import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class OfflineLicenseVerifierTest {
    // Non-production public key + pre-signed fixture. No private key or generator secret is stored here.
    private val publicKey = "MCowBQYDK2VwAyEAb3vu+98LX2EWhWm028uH2hj/u+hj9cCajmQFoLZ/Nzg="
    private val payloadPart = "MTpNQVNKSUQ6QUIxMkNEMzQ"
    private val signaturePart = "fX6padcp_iSEHViWZim4QokU65cEuKX3BOqXJYfa-8N2aTy1LtwaDTrkMHH1xJj-hsuKZtpv1c5OzM26J08HAw"
    private val validSerial = "$payloadPart.$signaturePart"
    private val verifier = OfflineLicenseVerifier(LicenseVerifierConfig("MASJID", publicKey))

    @Test
    fun validPreSignedFixtureIsAcceptedOffline() {
        val valid = assertIs<LicenseValidationResult.Valid>(verifier.validate(validSerial))
        assertEquals(LicensePayload(1, "MASJID", "AB12CD34"), valid.payload)
    }

    @Test
    fun changedSignatureIsInvalid() {
        val changed = "$payloadPart.${signaturePart.dropLast(1)}A"
        val invalid = assertIs<LicenseValidationResult.Invalid>(verifier.validate(changed))
        assertEquals(LicenseValidationResult.Reason.SIGNATURE_MISMATCH, invalid.reason)
    }

    @Test
    fun changedPayloadIsInvalid() {
        val changedPayload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("1:MASJID:AB12CD35".toByteArray(Charsets.US_ASCII))
        val invalid = assertIs<LicenseValidationResult.Invalid>(verifier.validate("$changedPayload.$signaturePart"))
        assertEquals(LicenseValidationResult.Reason.SIGNATURE_MISMATCH, invalid.reason)
    }

    @Test
    fun malformedSerialOrEncodingIsInvalid() {
        val malformed = assertIs<LicenseValidationResult.Invalid>(verifier.validate("not-a-license"))
        assertEquals(LicenseValidationResult.Reason.MALFORMED, malformed.reason)
        val badEncoding = assertIs<LicenseValidationResult.Invalid>(verifier.validate("***.$signaturePart"))
        assertEquals(LicenseValidationResult.Reason.MALFORMED, badEncoding.reason)
    }

    @Test
    fun invalidPayloadIsRejectedBeforeActivation() {
        val wrongProduct = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("1:OTHER:AB12CD34".toByteArray(Charsets.US_ASCII))
        val invalid = assertIs<LicenseValidationResult.Invalid>(verifier.validate("$wrongProduct.$signaturePart"))
        assertEquals(LicenseValidationResult.Reason.INVALID_PAYLOAD, invalid.reason)
    }

    @Test
    fun verifierRequiresOnlyPublicMaterialAndPersistsOnlyValidResult() {
        val store = FakeActivationStore()
        val service = LicenseActivationService(verifier, store)
        assertFalse(service.isActivated())
        service.activate("bad")
        assertFalse(service.isActivated())
        service.activate(validSerial)
        assertTrue(service.isActivated())
        assertEquals(LicensePayload(1, "MASJID", "AB12CD34"), store.payload)
    }

    @Test
    fun invalidPublicVerifierMaterialFailsClosed() {
        val invalidVerifier = OfflineLicenseVerifier(LicenseVerifierConfig("MASJID", "not-a-key"))
        val invalid = assertIs<LicenseValidationResult.Invalid>(invalidVerifier.validate(validSerial))
        assertEquals(LicenseValidationResult.Reason.INVALID_VERIFIER, invalid.reason)
    }

    private class FakeActivationStore : ActivationStore {
        var payload: LicensePayload? = null
        override fun isActivated(): Boolean = payload != null
        override fun persistActivation(payload: LicensePayload) { this.payload = payload }
    }
}
