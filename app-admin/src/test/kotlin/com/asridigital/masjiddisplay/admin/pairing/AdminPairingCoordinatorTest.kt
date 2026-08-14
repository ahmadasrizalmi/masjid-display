package com.asridigital.masjiddisplay.admin.pairing

import com.asridigital.masjiddisplay.protocol.CompletePairingRequest
import com.asridigital.masjiddisplay.protocol.CompletePairingResponse
import com.asridigital.masjiddisplay.protocol.DiscoveredTvService
import com.asridigital.masjiddisplay.protocol.LocalProtocol
import com.asridigital.masjiddisplay.protocol.OpenPairingResponse
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdminPairingCoordinatorTest {
    @Test
    fun lifecycleStartsDiscoveryOnceAndPublishesDiscovering() {
        val states = mutableListOf<AdminRuntimeState>()
        var starts = 0
        val coordinator = coordinator(
            onStartDiscovery = { starts++ },
            onStateChanged = states::add,
        )

        coordinator.onStart()
        coordinator.onStart()

        assertEquals(1, starts)
        assertEquals(listOf<AdminRuntimeState>(AdminRuntimeState.Discovering), states)
    }

    @Test
    fun resolvedServicesArePublishedOnlyWhileActive() {
        val states = mutableListOf<AdminRuntimeState>()
        val coordinator = coordinator(onStateChanged = states::add)
        val device = device()

        coordinator.onServicesChanged(listOf(device))
        coordinator.onStart()
        coordinator.onServicesChanged(listOf(device))
        coordinator.onStop()
        coordinator.onServicesChanged(emptyList())

        assertEquals(
            listOf(AdminRuntimeState.Discovering, AdminRuntimeState.Devices(listOf(device))),
            states,
        )
    }

    @Test
    fun pairingPublishesPairingThenPairedThroughExecutor() {
        val states = mutableListOf<AdminRuntimeState>()
        val queued = mutableListOf<() -> Unit>()
        val device = device()
        val coordinator = coordinator(
            executePairing = queued::add,
            onStateChanged = states::add,
        )

        coordinator.onStart()
        coordinator.onServicesChanged(listOf(device))
        coordinator.pair(device)

        assertEquals(1, queued.size)
        queued.single().invoke()

        assertEquals(AdminRuntimeState.Pairing(device), states[2])
        assertEquals(AdminRuntimeState.Paired(device, "trusted-credential"), states[3])
    }

    @Test
    fun discoveryFailureAndRetryAreExplicitAndRestartDiscovery() {
        val states = mutableListOf<AdminRuntimeState>()
        var starts = 0
        var stops = 0
        val coordinator = coordinator(
            onStartDiscovery = { starts++ },
            onStopDiscovery = { stops++ },
            onStateChanged = states::add,
        )

        coordinator.onStart()
        coordinator.onDiscoveryFailure(7)
        coordinator.retryDiscovery()

        assertEquals(AdminRuntimeState.Error("Discovery LAN gagal (kode 7)"), states[1])
        assertEquals(AdminRuntimeState.Discovering, states.last())
        assertEquals(2, starts)
        assertEquals(1, stops)
    }

    @Test
    fun stoppedCoordinatorSuppressesLatePairingStateDelivery() {
        val states = mutableListOf<AdminRuntimeState>()
        val queuedPairing = mutableListOf<() -> Unit>()
        val queuedDispatch = mutableListOf<() -> Unit>()
        val device = device()
        val coordinator = coordinator(
            executePairing = queuedPairing::add,
            dispatchState = queuedDispatch::add,
            onStateChanged = states::add,
        )

        coordinator.onStart()
        queuedDispatch.removeAt(0).invoke()
        coordinator.pair(device)
        queuedPairing.single().invoke()
        coordinator.onStop()
        queuedDispatch.forEach { it.invoke() }

        assertEquals(listOf<AdminRuntimeState>(AdminRuntimeState.Discovering), states)
        assertTrue(queuedDispatch.isNotEmpty())
    }

    private fun coordinator(
        onStartDiscovery: () -> Unit = {},
        onStopDiscovery: () -> Unit = {},
        executePairing: ((() -> Unit) -> Unit) = { it() },
        dispatchState: ((() -> Unit) -> Unit) = { it() },
        onStateChanged: (AdminRuntimeState) -> Unit = {},
    ): AdminPairingCoordinator = AdminPairingCoordinator(
        runtime = AdminPairingRuntime(FakeTransport()),
        startDiscovery = onStartDiscovery,
        stopDiscovery = onStopDiscovery,
        executePairing = executePairing,
        dispatchState = dispatchState,
        onStateChanged = onStateChanged,
    )

    private fun device() = DiscoveredTvService(
        serviceName = "Masjid Display TV",
        hostAddress = "192.168.1.10",
        port = 8080,
        protocolVersion = LocalProtocol.CURRENT_VERSION,
    )

    private class FakeTransport : AdminPairingTransportClient {
        override fun open(device: DiscoveredTvService): Result<OpenPairingResponse> = Result.success(
            OpenPairingResponse("session", "secret", LocalProtocol.CURRENT_VERSION, Instant.EPOCH),
        )

        override fun complete(
            device: DiscoveredTvService,
            request: CompletePairingRequest,
        ): Result<CompletePairingResponse> = Result.success(
            CompletePairingResponse.Success("trusted-credential", Instant.EPOCH),
        )
    }
}
