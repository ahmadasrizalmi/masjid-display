package com.asridigital.masjiddisplay.protocol

class TvPairingTransportAdapter(
    private val sessions: TvPairingSessionManager,
    private val onCredentialIssued: (TrustedAdminCredential) -> Unit = {},
) {
    /** Must be invoked by the TV pairing screen; only that screen may display the returned secret. */
    fun beginPairingForTvDisplay(): PairingChallenge = sessions.open()

    /** LAN endpoint may read public metadata for the session, but never its bootstrap secret. */
    fun open(): OpenPairingResponse? = sessions.activeSessionMetadata()

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
            is PairingResult.Paired -> {
                onCredentialIssued(result.credential)
                CompletePairingResponse.Success(
                    credentialId = result.credential.credentialId,
                    issuedAt = result.credential.issuedAt,
                )
            }
            PairingResult.NoActiveSession -> CompletePairingResponse.Rejected(PairingErrorCode.NO_ACTIVE_SESSION)
            PairingResult.SessionExpired -> CompletePairingResponse.Rejected(PairingErrorCode.SESSION_EXPIRED)
            PairingResult.ProtocolMismatch -> CompletePairingResponse.Rejected(PairingErrorCode.PROTOCOL_MISMATCH)
            PairingResult.SecretMismatch -> CompletePairingResponse.Rejected(PairingErrorCode.SECRET_MISMATCH)
            PairingResult.ReplayRejected -> CompletePairingResponse.Rejected(PairingErrorCode.REPLAY_REJECTED)
        }
    }
}
