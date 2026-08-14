package com.asridigital.masjiddisplay.protocol

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class TvPairingTransportAdapterTest {
    private val start = Instant.parse("2026-08-13T13:00:00Z")

    @Test
    fun tvDisplayCreatesChallengeAndLanOpenExposesOnlyPublicMetadata() {
        val adapter = adapter(tokens = listOf("session-1", "secret-1", "credential-1"))

        assertNull(adapter.open())
        val challenge = adapter.beginPairingForTvDisplay()
        val public = adapter.open()

        assertEquals("session-1", challenge.sessionId.value)
        assertEquals("secret-1", challenge.secretForQrOrFallback)
        assertEquals("session-1", public?.sessionId)
        assertEquals(LocalProtocol.CURRENT_VERSION, public?.protocolVersion)
        assertEquals(start.plus(Duration.ofMinutes(5)), public?.expiresAt)
    }

    @Test
    fun correctTvCodeProducesTrustedCredential() {
        val adapter = adapter(tokens = listOf("session-1", "secret-1", "credential-1"))
        val challenge = adapter.beginPairingForTvDisplay()

        assertEquals(
            CompletePairingResponse.Success("credential-1", start),
            adapter.complete(request(challenge)),
        )
    }

    @Test
    fun missingOrWrongTvCodeIsRejectedWithoutConsumingSession() {
        val adapter = adapter(tokens = listOf("session-1", "secret-1", "credential-1"))
        val challenge = adapter.beginPairingForTvDisplay()

        assertEquals(
            CompletePairingResponse.Rejected(PairingErrorCode.MALFORMED_REQUEST),
            adapter.complete(CompletePairingRequest(challenge.sessionId.value, "", challenge.protocolVersion)),
        )
        assertEquals(
            CompletePairingResponse.Rejected(PairingErrorCode.SECRET_MISMATCH),
            adapter.complete(request(challenge, secret = "wrong")),
        )
        assertIs<CompletePairingResponse.Success>(adapter.complete(request(challenge)))
    }

    @Test
    fun successfulPairingReplayIsRejected() {
        val adapter = adapter(tokens = listOf("session-1", "secret-1", "credential-1"))
        val challenge = adapter.beginPairingForTvDisplay()
        adapter.complete(request(challenge))

        assertEquals(
            CompletePairingResponse.Rejected(PairingErrorCode.REPLAY_REJECTED),
            adapter.complete(request(challenge)),
        )
    }

    @Test
    fun expiredSessionIsNotVisibleAndCompleteIsRejected() {
        val clock = MutableClock(start)
        val adapter = adapter(clock = clock, tokens = listOf("session-1", "secret-1"))
        val challenge = adapter.beginPairingForTvDisplay()
        clock.advance(Duration.ofMinutes(5))

        assertNull(adapter.open())
        assertEquals(
            CompletePairingResponse.Rejected(PairingErrorCode.REPLAY_REJECTED),
            adapter.complete(request(challenge)),
        )
    }

    private fun request(
        challenge: PairingChallenge,
        secret: String = challenge.secretForQrOrFallback,
        version: Int = challenge.protocolVersion,
    ) = CompletePairingRequest(challenge.sessionId.value, secret, version)

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
