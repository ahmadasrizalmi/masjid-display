package com.asridigital.masjiddisplay.tv.pairing

import com.asridigital.masjiddisplay.tv.discovery.TvNsdAdvertiser

class TvPairingLanRuntime(
    private val server: PairingLanServer,
    private val advertiser: TvNsdAdvertiser,
) : AutoCloseable {
    private var started = false

    fun start(): Int {
        if (started) return requireNotNull(server.port)
        val port = server.start()
        try {
            advertiser.start(port)
            started = true
            return port
        } catch (failure: Throwable) {
            server.close()
            throw failure
        }
    }

    override fun close() {
        if (!started) {
            server.close()
            return
        }
        started = false
        runCatching { advertiser.stop() }
        server.close()
    }
}
