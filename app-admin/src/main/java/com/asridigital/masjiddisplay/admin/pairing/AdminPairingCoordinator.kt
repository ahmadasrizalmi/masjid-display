package com.asridigital.masjiddisplay.admin.pairing

import com.asridigital.masjiddisplay.protocol.DiscoveredTvService
import com.asridigital.masjiddisplay.protocol.PairingBootstrap

class AdminPairingCoordinator(
    private val runtime: AdminPairingRuntime,
    private val startDiscovery: () -> Unit,
    private val stopDiscovery: () -> Unit,
    private val executePairing: ((() -> Unit) -> Unit) = { it() },
    private val dispatchState: ((() -> Unit) -> Unit) = { it() },
    private val onStateChanged: (AdminRuntimeState) -> Unit = {},
) {
    private var active = false

    fun onStart() {
        if (active) return
        active = true
        runtime.startDiscovery()
        publish(runtime.state)
        startDiscovery()
    }

    fun onStop() {
        if (!active) return
        active = false
        stopDiscovery()
        runtime.stop()
    }

    fun onServicesChanged(services: List<DiscoveredTvService>) {
        if (!active) return
        runtime.onDiscovered(services)
        publish(runtime.state)
    }

    fun onDiscoveryFailure(errorCode: Int) {
        if (!active) return
        runtime.onDiscoveryFailure(errorCode)
        publish(runtime.state)
    }

    fun pair(device: DiscoveredTvService, fallbackCode: String) {
        if (!active || fallbackCode.isBlank()) return
        executePairing {
            runtime.pair(device, PairingBootstrap(fallbackCode)) { state -> publish(state) }
        }
    }

    fun retryDiscovery() {
        if (!active) return
        stopDiscovery()
        runtime.startDiscovery()
        publish(runtime.state)
        startDiscovery()
    }

    private fun publish(state: AdminRuntimeState) {
        dispatchState {
            if (active) onStateChanged(state)
        }
    }
}
