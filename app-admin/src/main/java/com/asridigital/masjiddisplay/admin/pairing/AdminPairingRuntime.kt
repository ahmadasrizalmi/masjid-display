package com.asridigital.masjiddisplay.admin.pairing

import com.asridigital.masjiddisplay.protocol.CompletePairingRequest
import com.asridigital.masjiddisplay.protocol.CompletePairingResponse
import com.asridigital.masjiddisplay.protocol.DiscoveredTvService
import com.asridigital.masjiddisplay.protocol.OpenPairingResponse

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

    fun onDiscovered(services: List<DiscoveredTvService>) {
        state = AdminRuntimeState.Devices(services.distinctBy { "${it.hostAddress}:${it.port}" })
    }

    fun pair(device: DiscoveredTvService) {
        if (device.negotiation !is com.asridigital.masjiddisplay.protocol.ProtocolNegotiation.Accepted) {
            state = AdminRuntimeState.Error("Versi protokol TV tidak didukung")
            return
        }
        state = AdminRuntimeState.Pairing(device)
        val challenge = transport.open(device).getOrElse {
            state = AdminRuntimeState.Error("TV tidak dapat membuka sesi pairing")
            return
        }
        val response = transport.complete(
            device,
            CompletePairingRequest(challenge.sessionId, challenge.oneTimeSecret, challenge.protocolVersion),
        ).getOrElse {
            state = AdminRuntimeState.Error("Koneksi pairing lokal gagal")
            return
        }
        state = when (response) {
            is CompletePairingResponse.Success -> AdminRuntimeState.Paired(device, response.credentialId)
            is CompletePairingResponse.Rejected -> AdminRuntimeState.Error("Pairing ditolak: ${response.code.name}")
        }
    }

    fun stop() {
        if (state is AdminRuntimeState.Pairing) state = AdminRuntimeState.Error("Pairing dibatalkan")
    }
}
