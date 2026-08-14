package com.asridigital.masjiddisplay.admin.pairing

import com.asridigital.masjiddisplay.protocol.CompletePairingRequest
import com.asridigital.masjiddisplay.protocol.CompletePairingResponse
import com.asridigital.masjiddisplay.protocol.DiscoveredTvService
import com.asridigital.masjiddisplay.protocol.OpenPairingResponse
import com.asridigital.masjiddisplay.protocol.PairingBootstrap
import com.asridigital.masjiddisplay.protocol.ProtocolNegotiation

interface AdminPairingTransportClient {
    fun open(device: DiscoveredTvService): Result<OpenPairingResponse>
    fun complete(device: DiscoveredTvService, request: CompletePairingRequest): Result<CompletePairingResponse>
}

sealed interface AdminRuntimeState {
    data object Discovering : AdminRuntimeState
    data class Devices(val items: List<DiscoveredTvService>) : AdminRuntimeState
    data class Pairing(val device: DiscoveredTvService) : AdminRuntimeState
    data class Paired(val device: DiscoveredTvService, val credentialId: String) : AdminRuntimeState
    data class Error(val message: String) : AdminRuntimeState
}

class AdminPairingRuntime(private val transport: AdminPairingTransportClient) {
    var state: AdminRuntimeState = AdminRuntimeState.Discovering
        private set

    fun startDiscovery() {
        state = AdminRuntimeState.Discovering
    }

    fun onDiscovered(services: List<DiscoveredTvService>) {
        if (state !is AdminRuntimeState.Discovering && state !is AdminRuntimeState.Devices) return
        state = AdminRuntimeState.Devices(services.distinctBy { "${it.hostAddress}:${it.port}" })
    }

    fun onDiscoveryFailure(errorCode: Int) {
        if (state is AdminRuntimeState.Pairing || state is AdminRuntimeState.Paired) return
        state = AdminRuntimeState.Error("Discovery LAN gagal (kode $errorCode)")
    }

    /** [bootstrap] must originate from the TV QR/fallback code, never from the LAN endpoint. */
    fun pair(
        device: DiscoveredTvService,
        bootstrap: PairingBootstrap,
        onStateChanged: (AdminRuntimeState) -> Unit = {},
    ) {
        if (device.negotiation !is ProtocolNegotiation.Accepted) {
            transition(AdminRuntimeState.Error("Versi protokol TV tidak didukung"), onStateChanged)
            return
        }
        transition(AdminRuntimeState.Pairing(device), onStateChanged)
        val challenge = transport.open(device).getOrElse {
            transition(AdminRuntimeState.Error("TV tidak dapat membuka sesi pairing"), onStateChanged)
            return
        }
        val response = transport.complete(
            device,
            CompletePairingRequest(challenge.sessionId, bootstrap.oneTimeSecret, challenge.protocolVersion),
        ).getOrElse {
            transition(AdminRuntimeState.Error("Koneksi pairing lokal gagal"), onStateChanged)
            return
        }
        when (response) {
            is CompletePairingResponse.Success -> if (response.credentialId.isBlank()) {
                transition(AdminRuntimeState.Error("Credential pairing tidak valid"), onStateChanged)
            } else {
                transition(AdminRuntimeState.Paired(device, response.credentialId), onStateChanged)
            }
            is CompletePairingResponse.Rejected -> {
                transition(AdminRuntimeState.Error("Pairing ditolak: ${response.code.name}"), onStateChanged)
            }
        }
    }

    fun stop() {
        if (state is AdminRuntimeState.Pairing) state = AdminRuntimeState.Error("Pairing dibatalkan")
    }

    private fun transition(next: AdminRuntimeState, onStateChanged: (AdminRuntimeState) -> Unit) {
        state = next
        onStateChanged(next)
    }
}
