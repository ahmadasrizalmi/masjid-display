package com.asridigital.masjiddisplay.tv.pairing

import com.asridigital.masjiddisplay.protocol.PairingMaterialGenerator
import com.asridigital.masjiddisplay.protocol.TvPairingSessionManager
import com.asridigital.masjiddisplay.protocol.TvPairingTransportAdapter
import java.net.ConnectException
import java.net.Socket
import java.time.Clock
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class TvPairingLanRuntimeTest {
    @Test
    fun startsServerBeforeAdvertisingItsDynamicPortAndClosesBoth() {
        val advertiser = RecordingAdvertiser()
        val runtime = TvPairingLanRuntime(server(), advertiser)

        val port = runtime.start()

        assertNotEquals(0, port)
        assertEquals(port, advertiser.startedPort)
        Socket("127.0.0.1", port).use { assertTrue(it.isConnected) }

        runtime.close()

        assertTrue(advertiser.stopped)
        assertFailsWith<ConnectException> { Socket("127.0.0.1", port).use {} }
    }

    private fun server(): PairingLanServer {
        val tokens = listOf("session", "secret", "credential").iterator()
        val adapter = TvPairingTransportAdapter(
            TvPairingSessionManager(Clock.fixed(java.time.Instant.EPOCH, ZoneOffset.UTC), PairingMaterialGenerator { tokens.next() }),
        )
        return PairingLanServer(PairingHttpHandler(adapter))
    }

    private class RecordingAdvertiser : PairingLanAdvertiser {
        var startedPort: Int? = null
        var stopped = false
        override fun start(port: Int) { startedPort = port }
        override fun stop() { stopped = true }
    }
}
