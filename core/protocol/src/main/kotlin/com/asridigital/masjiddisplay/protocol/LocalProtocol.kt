package com.asridigital.masjiddisplay.protocol

import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.Base64

object LocalProtocol {
    const val CURRENT_VERSION = 1
    val supportedVersions: Set<Int> = setOf(CURRENT_VERSION)
}

sealed interface LocalRequest {
    val protocolVersion: Int
    data class GetDeviceInfo(override val protocolVersion: Int) : LocalRequest
    data class GetStatus(override val protocolVersion: Int) : LocalRequest
    data class GetConfig(override val protocolVersion: Int) : LocalRequest
    data class ListMedia(override val protocolVersion: Int) : LocalRequest
}

sealed interface ProtocolNegotiation {
    data class Accepted(val version: Int) : ProtocolNegotiation
    data class Rejected(val supportedVersions: Set<Int>) : ProtocolNegotiation
}

fun negotiateProtocol(requestedVersion: Int): ProtocolNegotiation =
    if (requestedVersion in LocalProtocol.supportedVersions) ProtocolNegotiation.Accepted(requestedVersion)
    else ProtocolNegotiation.Rejected(LocalProtocol.supportedVersions)

@JvmInline
value class PairingSessionId(val value: String)

/** Secret is ephemeral transport material only and must never be persisted as trusted identity. */
class PairingSecret private constructor(private val bytes: ByteArray) {
    fun matches(candidate: String): Boolean = MessageDigest.isEqual(
        bytes,
        candidate.toByteArray(Charsets.UTF_8),
    )

    companion object {
        internal fun from(value: String) = PairingSecret(value.toByteArray(Charsets.UTF_8))
    }
}

data class PairingChallenge(
    val sessionId: PairingSessionId,
    val secretForQrOrFallback: String,
    val protocolVersion: Int,
    val expiresAt: Instant,
)

data class TrustedAdminCredential(
    val credentialId: String,
    val issuedAt: Instant,
)

sealed interface PairingResult {
    data class Paired(val credential: TrustedAdminCredential) : PairingResult
    data object NoActiveSession : PairingResult
    data object SessionExpired : PairingResult
    data object ProtocolMismatch : PairingResult
    data object SecretMismatch : PairingResult
    data object ReplayRejected : PairingResult
}

fun interface PairingMaterialGenerator {
    fun nextToken(): String
}

class SecurePairingMaterialGenerator : PairingMaterialGenerator {
    private val random = SecureRandom()
    override fun nextToken(): String {
        val bytes = ByteArray(18).also(random::nextBytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}

/**
 * TV-side in-memory pairing session. A session is one-shot: success, explicit close, or expiry
 * permanently consumes its session id for this manager lifetime, rejecting replay attempts.
 */
class TvPairingSessionManager(
    private val clock: Clock,
    private val material: PairingMaterialGenerator = SecurePairingMaterialGenerator(),
    private val ttl: Duration = Duration.ofMinutes(5),
) {
    private data class Active(
        val id: PairingSessionId,
        val secret: PairingSecret,
        val expiresAt: Instant,
    )

    private var active: Active? = null
    private val consumedSessionIds = mutableSetOf<PairingSessionId>()

    init { require(!ttl.isZero && !ttl.isNegative) }

    fun open(): PairingChallenge {
        active?.let { consumedSessionIds += it.id }
        val now = clock.instant()
        val id = PairingSessionId(material.nextToken())
        require(id.value.isNotBlank())
        val secretText = material.nextToken()
        require(secretText.isNotBlank())
        val session = Active(id, PairingSecret.from(secretText), now.plus(ttl))
        active = session
        return PairingChallenge(id, secretText, LocalProtocol.CURRENT_VERSION, session.expiresAt)
    }

    /** LAN-visible session information; the bootstrap secret never leaves the TV display channel. */
    fun activeSessionMetadata(): OpenPairingResponse? {
        val session = active ?: return null
        if (!clock.instant().isBefore(session.expiresAt)) {
            consumedSessionIds += session.id
            active = null
            return null
        }
        return OpenPairingResponse(
            sessionId = session.id.value,
            protocolVersion = LocalProtocol.CURRENT_VERSION,
            expiresAt = session.expiresAt,
        )
    }

    fun close(sessionId: PairingSessionId) {
        if (active?.id == sessionId) {
            consumedSessionIds += sessionId
            active = null
        }
    }

    fun pair(sessionId: PairingSessionId, secret: String, protocolVersion: Int): PairingResult {
        if (sessionId in consumedSessionIds) return PairingResult.ReplayRejected
        val session = active ?: return PairingResult.NoActiveSession
        if (session.id != sessionId) return PairingResult.NoActiveSession
        if (!clock.instant().isBefore(session.expiresAt)) {
            consumedSessionIds += session.id
            active = null
            return PairingResult.SessionExpired
        }
        if (negotiateProtocol(protocolVersion) !is ProtocolNegotiation.Accepted) {
            return PairingResult.ProtocolMismatch
        }
        if (!session.secret.matches(secret)) return PairingResult.SecretMismatch

        consumedSessionIds += session.id
        active = null
        return PairingResult.Paired(
            TrustedAdminCredential(
                credentialId = material.nextToken(),
                issuedAt = clock.instant(),
            ),
        )
    }
}
