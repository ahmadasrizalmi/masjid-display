package com.asridigital.masjiddisplay.tv.pairing

import android.net.nsd.NsdManager
import com.asridigital.masjiddisplay.protocol.PairingChallenge
import com.asridigital.masjiddisplay.protocol.TvPairingSessionManager
import com.asridigital.masjiddisplay.protocol.TvPairingTransportAdapter
import com.asridigital.masjiddisplay.tv.discovery.TvNsdAdvertiser
import java.time.Clock

/** Owns one active TV pairing lifecycle. A closed LAN server is never reused. */
class TvPairingRuntimeOwner(
    private val nsdManager: NsdManager,
    private val clock: Clock = Clock.systemUTC(),
) : AutoCloseable {
    private var runtime: TvPairingLanRuntime? = null

    /** Starts listener before NSD advertisement and returns QR/fallback material for TV-local display. */
    fun start(): PairingChallenge {
        runtime?.let { return error("TV pairing runtime is already active") }
        val adapter = TvPairingTransportAdapter(TvPairingSessionManager(clock))
        val created = TvPairingLanRuntime(
            server = PairingLanServer(PairingHttpHandler(adapter)),
            advertiser = TvNsdAdvertiser(nsdManager),
        )
        return try {
            created.start()
            adapter.beginPairingForTvDisplay().also { runtime = created }
        } catch (failure: Throwable) {
            created.close()
            throw failure
        }
    }

    override fun close() {
        runtime?.close()
        runtime = null
    }
}
