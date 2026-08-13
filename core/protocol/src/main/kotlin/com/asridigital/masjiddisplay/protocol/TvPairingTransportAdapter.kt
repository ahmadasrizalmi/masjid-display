package com.asridigital.masjiddisplay.protocol

class TvPairingTransportAdapter(private val sessions: TvPairingSessionManager) {
    fun open(): OpenPairingResponse {
        val challenge = sessions.open()
        return OpenPairingResponse(
            sessionId = challenge.sessionId.value,
            oneTimeSecret = challenge.secretForQrOrFallback,
            protocolVersion = challenge.protocolVersion,
            expiresAt = challenge.expiresAt,
        )
    }

    fun complete(request: CompletePairingRequest): CompletePairingResponse {
        if (request.sessionId.isBlank() || request.oneTimeSecret.isBlank()) {
            return CompletePairingResponse.Rejected(PairingErrorCode.MALFORMED_REQUEST)
        }
        return when (
            val result = sessions.pair(
                sessionId = PairingSessionId(request.sessionId),
                secret = request.oneTimeSecret,
                protocolVersion = request.protocolVersion,
            )
        ) {
            is PairingResult.Paired -> CompletePairingResponse.Success(
                credentialId = result.credential.credentialId,
                issuedAt = result.credential.issuedAt,
            )
            PairingResult.NoActiveSession -> CompletePairingResponse.Rejected(PairingErrorCode.NO_ACTIVE_SESSION)
            PairingResult.SessionExpired -> CompletePairingResponse.Rejected(PairingErrorCode.SESSION_EXPIRED)
            PairingResult.ProtocolMismatch -> CompletePairingResponse.Rejected(PairingErrorCode.PROTOCOL_MISMATCH)
            PairingResult.SecretMismatch -> CompletePairingResponse.Rejected(PairingErrorCode.SECRET_MISMATCH)
            PairingResult.ReplayRejected -> CompletePairingResponse.Rejected(PairingErrorCode.REPLAY_REJECTED)
        }
    }
}
