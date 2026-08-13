package com.asridigital.masjiddisplay.license

import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

data class LicenseVerifierConfig(
    val productCode: String,
    val publicKeyBase64: String,
)

data class LicensePayload(
    val version: Int,
    val productCode: String,
    val serialId: String,
) {
    fun canonical(): String = "$version:$productCode:$serialId"
}

sealed interface LicenseValidationResult {
    data class Valid(val payload: LicensePayload) : LicenseValidationResult
    data class Invalid(val reason: Reason) : LicenseValidationResult
    enum class Reason { MALFORMED, INVALID_PAYLOAD, SIGNATURE_MISMATCH, INVALID_VERIFIER }
}

/**
 * Pure offline verifier. Wire format is Base64Url(payload) + '.' + Base64Url(signature).
 * Payload is canonical ASCII: version:productCode:serialId.
 * Only an X.509 Ed25519 public key is required. Private signing material stays outside the APK.
 */
class OfflineLicenseVerifier(private val config: LicenseVerifierConfig) {
    fun validate(input: String): LicenseValidationResult {
        if (!PRODUCT.matches(config.productCode)) return invalid(LicenseValidationResult.Reason.INVALID_VERIFIER)
        val publicKey = decodePublicKey(config.publicKeyBase64)
            ?: return invalid(LicenseValidationResult.Reason.INVALID_VERIFIER)

        val parts = input.trim().split('.')
        if (parts.size != 2 || parts.any(String::isBlank)) return invalid(LicenseValidationResult.Reason.MALFORMED)
        val payloadBytes = decodeUrl(parts[0]) ?: return invalid(LicenseValidationResult.Reason.MALFORMED)
        val signatureBytes = decodeUrl(parts[1]) ?: return invalid(LicenseValidationResult.Reason.MALFORMED)
        val payloadText = payloadBytes.toString(StandardCharsets.US_ASCII)
        val payload = parsePayload(payloadText) ?: return invalid(LicenseValidationResult.Reason.INVALID_PAYLOAD)
        if (payload.productCode != config.productCode) return invalid(LicenseValidationResult.Reason.INVALID_PAYLOAD)

        return try {
            val verifier = Signature.getInstance("Ed25519")
            verifier.initVerify(publicKey)
            verifier.update(payloadBytes)
            if (verifier.verify(signatureBytes)) LicenseValidationResult.Valid(payload)
            else invalid(LicenseValidationResult.Reason.SIGNATURE_MISMATCH)
        } catch (_: Exception) {
            invalid(LicenseValidationResult.Reason.SIGNATURE_MISMATCH)
        }
    }

    private fun parsePayload(value: String): LicensePayload? {
        val parts = value.split(':')
        if (parts.size != 3) return null
        val version = parts[0].toIntOrNull() ?: return null
        val product = parts[1]
        val serialId = parts[2]
        if (version != SUPPORTED_VERSION || !PRODUCT.matches(product) || !SERIAL_ID.matches(serialId)) return null
        return LicensePayload(version, product, serialId).takeIf { it.canonical() == value }
    }

    private fun decodePublicKey(value: String): PublicKey? = try {
        val encoded = Base64.getDecoder().decode(value)
        KeyFactory.getInstance("Ed25519").generatePublic(X509EncodedKeySpec(encoded))
    } catch (_: Exception) { null }

    private fun decodeUrl(value: String): ByteArray? = try {
        Base64.getUrlDecoder().decode(value)
    } catch (_: IllegalArgumentException) { null }

    private fun invalid(reason: LicenseValidationResult.Reason) = LicenseValidationResult.Invalid(reason)

    companion object {
        const val SUPPORTED_VERSION = 1
        private val PRODUCT = Regex("[A-Z0-9]{2,12}")
        private val SERIAL_ID = Regex("[A-Z0-9]{8,32}")
    }
}

/** Persistence boundary only. The verifier has no Room, file-system, account or network dependency. */
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
