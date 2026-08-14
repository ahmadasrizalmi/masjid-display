package com.asridigital.masjiddisplay.protocol

import java.time.Instant

object PairingTransportPaths {
    const val OPEN = "/v1/pairing/open"
    const val COMPLETE = "/v1/pairing/complete"
}

/** Public LAN metadata. The bootstrap secret stays on the TV QR/fallback code. */
data class OpenPairingResponse(val sessionId: String, val protocolVersion: Int, val expiresAt: Instant)

/** Material obtained by scanning the TV QR or entering its locally displayed fallback code. */
data class PairingBootstrap(val oneTimeSecret: String) {
    init { require(oneTimeSecret.isNotBlank()) }
}
data class CompletePairingRequest(val sessionId: String, val oneTimeSecret: String, val protocolVersion: Int)

enum class PairingErrorCode { NO_ACTIVE_SESSION, SESSION_EXPIRED, PROTOCOL_MISMATCH, SECRET_MISMATCH, REPLAY_REJECTED, MALFORMED_REQUEST }

sealed interface CompletePairingResponse {
    data class Success(val credentialId: String, val issuedAt: Instant) : CompletePairingResponse
    data class Rejected(val code: PairingErrorCode) : CompletePairingResponse
}
