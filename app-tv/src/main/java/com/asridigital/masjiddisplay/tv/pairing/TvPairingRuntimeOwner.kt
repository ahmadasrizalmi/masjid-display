package com.asridigital.masjiddisplay.tv.pairing

import android.net.nsd.NsdManager
import com.asridigital.masjiddisplay.database.ConfigRepository
import com.asridigital.masjiddisplay.database.MediaItemDao
import com.asridigital.masjiddisplay.media.AtomicMediaStore
import com.asridigital.masjiddisplay.protocol.PairingChallenge
import com.asridigital.masjiddisplay.protocol.TvPairingSessionManager
import com.asridigital.masjiddisplay.protocol.TvPairingTransportAdapter
import com.asridigital.masjiddisplay.tv.discovery.TvNsdAdvertiser
import java.time.Clock

/** Owns one active TV LAN lifecycle for pairing, config updates, and local media transfer. */
class TvPairingRuntimeOwner(
    private val nsdManager: NsdManager,
    private val configRepository: ConfigRepository,
    private val mediaStore: AtomicMediaStore,
    private val mediaDao: MediaItemDao,
    private val onCredentialIssued: (String) -> Unit,
    private val isCredentialTrusted: (String) -> Boolean,
    private val clock: Clock = Clock.systemUTC(),
) : AutoCloseable {
    private var runtime: TvPairingLanRuntime? = null

    /** Starts listener before NSD advertisement and returns QR/fallback material for TV-local display. */
    fun start(): PairingChallenge {
        runtime?.let { return error("TV pairing runtime is already active") }
        val adapter = TvPairingTransportAdapter(TvPairingSessionManager(clock)) { credential ->
            onCredentialIssued(credential.credentialId)
        }
        val configHandler = TvConfigHttpHandler(RoomTvConfigSink(configRepository), isCredentialTrusted)
        val mediaHandler = TvMediaHttpHandler(mediaStore, mediaDao, isCredentialTrusted, clock)
        val fallback: (PairingHttpRequest) -> PairingHttpResponse = { request ->
            val mediaResponse = mediaHandler.handleControl(request)
            if (mediaResponse.status != 404) mediaResponse else configHandler.handle(request)
        }
        val created = TvPairingLanRuntime(
            server = PairingLanServer(
                handler = PairingHttpHandler(adapter, fallback),
                streamHandler = mediaHandler::handleUpload,
            ),
            advertiser = TvNsdAdvertiser(nsdManager),
        )
        val challenge = adapter.beginPairingForTvDisplay()
        return try {
            created.start()
            runtime = created
            challenge
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
