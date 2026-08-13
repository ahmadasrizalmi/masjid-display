package com.asridigital.masjiddisplay.license

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class OfflineLicenseVerifierTest {
    private val config = LicenseVerifierConfig(productCode = "MASJID", verifierSalt = "ASRI-V1")
    private val verifier = OfflineLicenseVerifier(config)
    private val payload = LicensePayload(productCode = "MASJID", serialId = "AB12CD34")

    @Test
    fun validSerialIsAcceptedOffline() {
        val serial = serialFor(payload)
        val result = verifier.validate(serial)
        assertEquals(payload, assertIs<LicenseValidationResult.Valid>(result).payload)
    }

    @Test
    fun tamperedSerialIsRejected() {
        val valid = serialFor(payload)
        val tampered = valid.replace("AB12CD34", "AB12CD35")
        val invalid = assertIs<LicenseValidationResult.Invalid>(verifier.validate(tampered))
        assertEquals(LicenseValidationResult.Reason.SIGNATURE_MISMATCH, invalid.reason)
    }

    @Test
    fun malformedSerialIsRejected() {
        val invalid = assertIs<LicenseValidationResult.Invalid>(verifier.validate("not-a-license"))
        assertEquals(LicenseValidationResult.Reason.MALFORMED, invalid.reason)
    }

    @Test
    fun wrongProductPayloadIsRejected() {
        val otherPayload = LicensePayload(productCode = "OTHER", serialId = "AB12CD34")
        val invalid = assertIs<LicenseValidationResult.Invalid>(verifier.validate(serialFor(otherPayload)))
        assertEquals(LicenseValidationResult.Reason.INVALID_PAYLOAD, invalid.reason)
    }

    @Test
    fun invalidVerifierConfigurationIsRejectedDeterministically() {
        val invalidVerifier = OfflineLicenseVerifier(LicenseVerifierConfig("bad product!", ""))
        val invalid = assertIs<LicenseValidationResult.Invalid>(invalidVerifier.validate(serialFor(payload)))
        assertEquals(LicenseValidationResult.Reason.INVALID_PAYLOAD, invalid.reason)
    }

    @Test
    fun activationPersistsOnlyAfterValidValidation() {
        val store = FakeActivationStore()
        val service = LicenseActivationService(verifier, store)
        assertFalse(service.isActivated())

        service.activate("bad")
        assertFalse(service.isActivated())

        service.activate(serialFor(payload))
        assertTrue(service.isActivated())
        assertEquals(payload, store.payload)
    }

    private fun serialFor(value: LicensePayload): String =
        "${value.productCode}-${value.serialId}-${OfflineLicenseVerifier.signatureFor(value, config.verifierSalt)}"

    private class FakeActivationStore : ActivationStore {
        var payload: LicensePayload? = null
        override fun isActivated(): Boolean = payload != null
        override fun persistActivation(payload: LicensePayload) { this.payload = payload }
    }
}
