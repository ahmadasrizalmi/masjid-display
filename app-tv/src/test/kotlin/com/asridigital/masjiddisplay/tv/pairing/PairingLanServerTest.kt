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
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PairingLanServerTest {
    @Test
    fun startsOnDynamicPortAndServesOnlyPairingPostEndpoints() {
        PairingLanServer(handler()).use { server ->
            val port = server.start()
            assertNotEquals(0, port)
            assertEquals(port, server.port)

            val open = request(port, "POST /v1/pairing/open HTTP/1.1\r\nHost: localhost\r\nContent-Length: 0\r\n\r\n")
            assertEquals("HTTP/1.1 200 OK", open.first())
            assertTrue(open.none { it.contains("oneTimeSecret") })

            val methodRejected = request(port, "GET /v1/pairing/open HTTP/1.1\r\nHost: localhost\r\n\r\n")
            assertEquals("HTTP/1.1 405 Method Not Allowed", methodRejected.first())

            val pathRejected = request(port, "POST /not-pairing HTTP/1.1\r\nHost: localhost\r\nContent-Length: 0\r\n\r\n")
            assertEquals("HTTP/1.1 404 Not Found", pathRejected.first())
        }
    }

    @Test
    fun closeStopsAcceptingConnections() {
        val server = PairingLanServer(handler())
        val port = server.start()
        server.close()

        assertFailsWith<ConnectException> { Socket("127.0.0.1", port).use {} }
    }

    private fun request(port: Int, request: String): List<String> =
        Socket("127.0.0.1", port).use { socket ->
            socket.getOutputStream().write(request.toByteArray())
            socket.getOutputStream().flush()
            socket.getInputStream().bufferedReader().readLines()
        }

    private fun handler(): PairingHttpHandler {
        val tokens = listOf("session", "secret", "credential").iterator()
        val manager = TvPairingSessionManager(
            clock = Clock.fixed(java.time.Instant.EPOCH, ZoneOffset.UTC),
            material = PairingMaterialGenerator { tokens.next() },
        )
        val adapter = TvPairingTransportAdapter(manager)
        adapter.beginPairingForTvDisplay()
        return PairingHttpHandler(adapter)
    }
}
