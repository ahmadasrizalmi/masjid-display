package com.asridigital.masjiddisplay.protocol

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class TvPairingTransportAdapterTest {
    private val start = Instant.parse("2026-08-13T13:00:00Z")

    @Test
    fun openReturnsChallengeAndCompleteReturnsTrustedCredential() {
        val adapter = adapter(tokens = listOf("session-1", "secret-1", "credential-1"))
        val challenge = adapter.open()

        assertEquals("session-1", challenge.sessionId)
        assertEquals("secret-1", challenge.oneTimeSecret)
        assertEquals(LocalProtocol.CURRENT_VERSION, challenge.protocolVersion)
        assertEquals(start.plus(Duration.ofMinutes(5)), challenge.expiresAt)

        assertEquals(
            CompletePairingResponse.Success("credential-1", start),
            adapter.complete(request(challenge)),
        )
    }

    @Test
    fun malformedRequestIsRejectedBeforeSessionManager() {
        val adapter = adapter(tokens = listOf("session-1", "secret-1", "credential-1"))
        val challenge = adapter.open()

        assertEquals(
            CompletePairingResponse.Rejected(PairingErrorCode.MALFORMED_REQUEST),
            adapter.complete(CompletePairingRequest("", challenge.oneTimeSecret, challenge.protocolVersion)),
        )
        assertIs<CompletePairingResponse.Success>(adapter.complete(request(challenge)))
    }

    @Test
    fun wrongSecretMapsToSecretMismatchWithoutConsumingSession() {
        val adapter = adapter(tokens = listOf("session-1", "secret-1", "credential-1"))
        val challenge = adapter.open()

        assertEquals(
            CompletePairingResponse.Rejected(PairingErrorCode.SECRET_MISMATCH),
            adapter.complete(request(challenge, secret = "wrong")),
        )
        assertIs<CompletePairingResponse.Success>(adapter.complete(request(challenge)))
    }

    @Test
    fun unsupportedVersionMapsToProtocolMismatchWithoutConsumingSession() {
        val adapter = adapter(tokens = listOf("session-1", "secret-1", "credential-1"))
        val challenge = adapter.open()

        assertEquals(
            CompletePairingResponse.Rejected(PairingErrorCode.PROTOCOL_MISMATCH),
            adapter.complete(request(challenge, version = LocalProtocol.CURRENT_VERSION + 1)),
        )
        assertIs<CompletePairingResponse.Success>(adapter.complete(request(challenge)))
    }

    @Test
    fun successfulPairingReplayMapsToReplayRejected() {
        val adapter = adapter(tokens = listOf("session-1", "secret-1", "credential-1"))
        val challenge = adapter.open()
        adapter.complete(request(challenge))

        assertEquals(
            CompletePairingResponse.Rejected(PairingErrorCode.REPLAY_REJECTED),
            adapter.complete(request(challenge)),
        )
    }

    @Test
    fun expiredSessionMapsToSessionExpiredThenReplayRejected() {
        val clock = MutableClock(start)
        val adapter = adapter(clock = clock, tokens = listOf("session-1", "secret-1"))
        val challenge = adapter.open()
        clock.advance(Duration.ofMinutes(5))

        assertEquals(
            CompletePairingResponse.Rejected(PairingErrorCode.SESSION_EXPIRED),
            adapter.complete(request(challenge)),
        )
        assertEquals(
            CompletePairingResponse.Rejected(PairingErrorCode.REPLAY_REJECTED),
            adapter.complete(request(challenge)),
        )
    }

    @Test
    fun unknownSessionMapsToNoActiveSession() {
        val adapter = adapter(tokens = emptyList())

        assertEquals(
            CompletePairingResponse.Rejected(PairingErrorCode.NO_ACTIVE_SESSION),
            adapter.complete(CompletePairingRequest("unknown", "secret", LocalProtocol.CURRENT_VERSION)),
        )
    }

    private fun request(
        challenge: OpenPairingResponse,
        secret: String = challenge.oneTimeSecret,
        version: Int = challenge.protocolVersion,
    ) = CompletePairingRequest(challenge.sessionId, secret, version)

    private fun adapter(
        clock: Clock = Clock.fixed(start, ZoneOffset.UTC),
        tokens: List<String>,
    ) = TvPairingTransportAdapter(
        TvPairingSessionManager(clock, TokenQueue(tokens), Duration.ofMinutes(5)),
    )

    private class TokenQueue(tokens: List<String>) : PairingMaterialGenerator {
        private val values = tokens.iterator()
        override fun nextToken(): String = checkNotNull(values.next())
    }

    private class MutableClock(private var now: Instant) : Clock() {
        override fun getZone(): ZoneId = ZoneOffset.UTC
        override fun withZone(zone: ZoneId): Clock = this
        override fun instant(): Instant = now
        fun advance(duration: Duration) { now = now.plus(duration) }
    }
}
