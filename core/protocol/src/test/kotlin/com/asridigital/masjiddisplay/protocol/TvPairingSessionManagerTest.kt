package com.asridigital.masjiddisplay.protocol

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals

class TvPairingSessionManagerTest {
    private val start = Instant.parse("2026-08-13T12:00:00Z")

    @Test
    fun supportedVersionIsAcceptedAndUnsupportedVersionIsRejected() {
        assertEquals(ProtocolNegotiation.Accepted(LocalProtocol.CURRENT_VERSION), negotiateProtocol(1))
        assertEquals(
            ProtocolNegotiation.Rejected(LocalProtocol.supportedVersions),
            negotiateProtocol(LocalProtocol.CURRENT_VERSION + 1),
        )
    }

    @Test
    fun pairingSucceedsOnceAndReplayIsRejected() {
        val manager = manager(tokens = listOf("session-1", "secret-1", "credential-1"))
        val challenge = manager.open()

        val paired = manager.pair(challenge.sessionId, challenge.secretForQrOrFallback, challenge.protocolVersion)
        assertEquals(
            PairingResult.Paired(TrustedAdminCredential("credential-1", start)),
            paired,
        )
        assertEquals(
            PairingResult.ReplayRejected,
            manager.pair(challenge.sessionId, challenge.secretForQrOrFallback, challenge.protocolVersion),
        )
    }

    @Test
    fun wrongSecretDoesNotConsumeActiveSession() {
        val manager = manager(tokens = listOf("session-1", "secret-1", "credential-1"))
        val challenge = manager.open()

        assertEquals(
            PairingResult.SecretMismatch,
            manager.pair(challenge.sessionId, "wrong-secret", challenge.protocolVersion),
        )
        assertIs<PairingResult.Paired>(
            manager.pair(challenge.sessionId, challenge.secretForQrOrFallback, challenge.protocolVersion),
        )
    }

    @Test
    fun unsupportedProtocolDoesNotConsumeActiveSession() {
        val manager = manager(tokens = listOf("session-1", "secret-1", "credential-1"))
        val challenge = manager.open()

        assertEquals(
            PairingResult.ProtocolMismatch,
            manager.pair(challenge.sessionId, challenge.secretForQrOrFallback, LocalProtocol.CURRENT_VERSION + 1),
        )
        assertIs<PairingResult.Paired>(
            manager.pair(challenge.sessionId, challenge.secretForQrOrFallback, challenge.protocolVersion),
        )
    }

    @Test
    fun sessionExpiresAtTtlBoundaryAndThenRejectsReplay() {
        val clock = MutableClock(start)
        val manager = manager(clock = clock, tokens = listOf("session-1", "secret-1"))
        val challenge = manager.open()
        clock.advance(Duration.ofMinutes(5))

        assertEquals(
            PairingResult.SessionExpired,
            manager.pair(challenge.sessionId, challenge.secretForQrOrFallback, challenge.protocolVersion),
        )
        assertEquals(
            PairingResult.ReplayRejected,
            manager.pair(challenge.sessionId, challenge.secretForQrOrFallback, challenge.protocolVersion),
        )
    }

    @Test
    fun explicitClosureConsumesSession() {
        val manager = manager(tokens = listOf("session-1", "secret-1"))
        val challenge = manager.open()
        manager.close(challenge.sessionId)

        assertEquals(
            PairingResult.ReplayRejected,
            manager.pair(challenge.sessionId, challenge.secretForQrOrFallback, challenge.protocolVersion),
        )
    }

    @Test
    fun openingReplacementSessionConsumesPreviousSession() {
        val manager = manager(tokens = listOf("session-1", "secret-1", "session-2", "secret-2", "credential-2"))
        val first = manager.open()
        val second = manager.open()

        assertNotEquals(first.sessionId, second.sessionId)
        assertEquals(
            PairingResult.ReplayRejected,
            manager.pair(first.sessionId, first.secretForQrOrFallback, first.protocolVersion),
        )
        assertIs<PairingResult.Paired>(
            manager.pair(second.sessionId, second.secretForQrOrFallback, second.protocolVersion),
        )
    }

    private fun manager(
        clock: Clock = Clock.fixed(start, ZoneOffset.UTC),
        tokens: List<String>,
    ) = TvPairingSessionManager(clock, TokenQueue(tokens), Duration.ofMinutes(5))

    private class TokenQueue(tokens: List<String>) : PairingMaterialGenerator {
        private val values = tokens.iterator()
        override fun nextToken(): String = checkNotNull(values.next())
    }

    private class MutableClock(private var now: Instant) : Clock() {
        override fun getZone() = ZoneOffset.UTC
        override fun withZone(zone: java.time.ZoneId): Clock = this
        override fun instant(): Instant = now
        fun advance(duration: Duration) { now = now.plus(duration) }
    }
}
