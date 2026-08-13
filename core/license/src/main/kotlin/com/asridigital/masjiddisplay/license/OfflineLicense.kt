package com.asridigital.masjiddisplay.license

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/** Public, non-secret product verifier material. Serial issuance remains outside the APK. */
data class LicenseVerifierConfig(
    val productCode: String,
    val verifierSalt: String,
)

data class LicensePayload(
    val productCode: String,
    val serialId: String,
)

sealed interface LicenseValidationResult {
    data class Valid(val payload: LicensePayload) : LicenseValidationResult
    data class Invalid(val reason: Reason) : LicenseValidationResult

    enum class Reason { MALFORMED, INVALID_PAYLOAD, SIGNATURE_MISMATCH }
}

/**
 * Pure offline validator. Canonical format:
 *   PRODUCT-SERIALID-SIGNATURE
 * where each field uses uppercase ASCII letters/digits, SERIALID is 8 chars, and SIGNATURE is
 * the first 12 hex chars of SHA-256(canonical payload + verifier material).
 *
 * This is intentionally a lightweight entitlement token, not aggressive DRM. It performs no
 * network, account, device-binding, expiry, activation-count, periodic-check or revocation logic.
 */
class OfflineLicenseVerifier(private val config: LicenseVerifierConfig) {
    fun validate(input: String): LicenseValidationResult {
        if (!PRODUCT.matches(config.productCode) || config.verifierSalt.isBlank()) {
            return LicenseValidationResult.Invalid(LicenseValidationResult.Reason.INVALID_PAYLOAD)
        }

        val normalized = input.trim().uppercase()
        val parts = normalized.split('-')
        if (parts.size != 3) {
            return LicenseValidationResult.Invalid(LicenseValidationResult.Reason.MALFORMED)
        }
        val (product, serialId, signature) = parts
        if (!PRODUCT.matches(product) || !SERIAL_ID.matches(serialId) || !SIGNATURE.matches(signature)) {
            return LicenseValidationResult.Invalid(LicenseValidationResult.Reason.MALFORMED)
        }
        if (product != config.productCode) {
            return LicenseValidationResult.Invalid(LicenseValidationResult.Reason.INVALID_PAYLOAD)
        }

        val payload = LicensePayload(productCode = product, serialId = serialId)
        val expected = signatureFor(payload, config.verifierSalt)
        return if (constantTimeEquals(signature, expected)) {
            LicenseValidationResult.Valid(payload)
        } else {
            LicenseValidationResult.Invalid(LicenseValidationResult.Reason.SIGNATURE_MISMATCH)
        }
    }

    companion object {
        private val PRODUCT = Regex("[A-Z0-9]{2,12}")
        private val SERIAL_ID = Regex("[A-Z0-9]{8}")
        private val SIGNATURE = Regex("[A-F0-9]{12}")

        /** Intended for the trusted serial-generation tool/site, not as a list of valid serials. */
        fun signatureFor(payload: LicensePayload, verifierSalt: String): String {
            require(PRODUCT.matches(payload.productCode))
            require(SERIAL_ID.matches(payload.serialId))
            require(verifierSalt.isNotBlank())
            val canonical = "${payload.productCode}:${payload.serialId}:$verifierSalt"
            val digest = MessageDigest.getInstance("SHA-256")
                .digest(canonical.toByteArray(StandardCharsets.US_ASCII))
            return digest.joinToString("") { "%02X".format(it) }.take(12)
        }

        private fun constantTimeEquals(left: String, right: String): Boolean =
            MessageDigest.isEqual(
                left.toByteArray(StandardCharsets.US_ASCII),
                right.toByteArray(StandardCharsets.US_ASCII),
            )
    }
}

/** Persistence is an outer boundary; validator itself knows nothing about Room/files/network. */
interface ActivationStore {
    fun isActivated(): Boolean
    fun persistActivation(payload: LicensePayload)
}

class LicenseActivationService(
    private val verifier: OfflineLicenseVerifier,
    private val store: ActivationStore,
) {
    fun activate(serial: String): LicenseValidationResult {
        val result = verifier.validate(serial)
        if (result is LicenseValidationResult.Valid) store.persistActivation(result.payload)
        return result
    }

    fun isActivated(): Boolean = store.isActivated()
}
