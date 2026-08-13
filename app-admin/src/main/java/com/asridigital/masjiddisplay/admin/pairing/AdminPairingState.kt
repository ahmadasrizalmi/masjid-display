package com.asridigital.masjiddisplay.admin.pairing

import com.asridigital.masjiddisplay.protocol.DiscoveredTvService
import com.asridigital.masjiddisplay.protocol.ProtocolNegotiation

sealed interface AdminPairingState {
    data object Discovering : AdminPairingState
    data class Devices(val items: List<DiscoveredTvService>, val selected: DiscoveredTvService? = null) : AdminPairingState
    data class Paired(val device: DiscoveredTvService, val credentialId: String) : AdminPairingState
    data class Error(val message: String) : AdminPairingState
}

class AdminPairingController {
    var state: AdminPairingState = AdminPairingState.Discovering
        private set

    fun discovered(services: List<DiscoveredTvService>) {
        state = AdminPairingState.Devices(services.distinctBy { "${it.hostAddress}:${it.port}" })
    }

    fun select(service: DiscoveredTvService): Boolean {
        if (service.negotiation !is ProtocolNegotiation.Accepted) {
            state = AdminPairingState.Error("Versi protokol TV tidak didukung")
            return false
        }
        val devices = state as? AdminPairingState.Devices ?: return false
        if (service !in devices.items) return false
        state = devices.copy(selected = service)
        return true
    }

    fun paired(credentialId: String) {
        val selected = (state as? AdminPairingState.Devices)?.selected ?: return
        if (credentialId.isBlank()) {
            state = AdminPairingState.Error("Credential pairing tidak valid")
            return
        }
        state = AdminPairingState.Paired(selected, credentialId)
    }
}
