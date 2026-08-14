package com.asridigital.masjiddisplay.admin.pairing

import com.asridigital.masjiddisplay.protocol.CompletePairingRequest
import com.asridigital.masjiddisplay.protocol.CompletePairingResponse
import com.asridigital.masjiddisplay.protocol.DiscoveredTvService
import com.asridigital.masjiddisplay.protocol.LocalProtocol
import com.asridigital.masjiddisplay.protocol.OpenPairingResponse
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdminPairingRuntimeTest {
    @Test
    fun discoveryDeduplicatesResolvedEndpoint() {
        val runtime = AdminPairingRuntime(FakeTransport())
        val first = device(name = "TV A", host = "192.168.1.10")
        val duplicate = device(name = "TV A renamed", host = "192.168.1.10")
        val other = device(name = "TV B", host = "192.168.1.11")

        runtime.onDiscovered(listOf(first, duplicate, other))

        assertEquals(AdminRuntimeState.Devices(listOf(first, other)), runtime.state)
    }

    @Test
    fun compatibleDeviceTransitionsToPairedWithTrustedCredential() {
        val transport = FakeTransport()
        val runtime = AdminPairingRuntime(transport)
        val device = device()

        runtime.pair(device)

        assertEquals(AdminRuntimeState.Paired(device, "trusted-credential"), runtime.state)
        assertEquals(1, transport.openCalls)
        assertEquals(1, transport.completeCalls)
        assertEquals(
            CompletePairingRequest("session", "secret", LocalProtocol.CURRENT_VERSION),
            transport.lastRequest,
        )
    }

    @Test
    fun incompatibleProtocolIsRejectedBeforeTransportIsCalled() {
        val transport = FakeTransport()
        val runtime = AdminPairingRuntime(transport)

        runtime.pair(device(version = LocalProtocol.CURRENT_VERSION + 1))

        assertEquals(AdminRuntimeState.Error("Versi protokol TV tidak didukung"), runtime.state)
        assertEquals(0, transport.openCalls)
        assertEquals(0, transport.completeCalls)
    }

    @Test
    fun openTransportFailureProducesExplicitError() {
        val runtime = AdminPairingRuntime(FakeTransport(openResult = Result.failure(IllegalStateException())))

        runtime.pair(device())

        assertEquals(AdminRuntimeState.Error("TV tidak dapat membuka sesi pairing"), runtime.state)
    }

    @Test
    fun completeTransportFailureProducesExplicitLocalConnectionError() {
        val runtime = AdminPairingRuntime(FakeTransport(completeResult = Result.failure(IllegalStateException())))

        runtime.pair(device())

        assertEquals(AdminRuntimeState.Error("Koneksi pairing lokal gagal"), runtime.state)
    }

    @Test
    fun rejectedPairingProducesExplicitRejectionError() {
        val runtime = AdminPairingRuntime(
            FakeTransport(completeResult = Result.success(CompletePairingResponse.Rejected(com.asridigital.masjiddisplay.protocol.PairingErrorCode.SECRET_MISMATCH))),
        )

        runtime.pair(device())

        assertEquals(AdminRuntimeState.Error("Pairing ditolak: SECRET_MISMATCH"), runtime.state)
    }

    @Test
    fun blankCredentialIsNotAcceptedAsPaired() {
        val runtime = AdminPairingRuntime(
            FakeTransport(completeResult = Result.success(CompletePairingResponse.Success("", Instant.EPOCH))),
        )

        runtime.pair(device())

        assertEquals(AdminRuntimeState.Error("Credential pairing tidak valid"), runtime.state)
    }


    private fun device(
        name: String = "Masjid Display TV",
        host: String = "192.168.1.10",
        version: Int = LocalProtocol.CURRENT_VERSION,
    ) = DiscoveredTvService(name, host, 8080, version)

    private class FakeTransport(
        private val openResult: Result<OpenPairingResponse> = Result.success(
            OpenPairingResponse("session", "secret", LocalProtocol.CURRENT_VERSION, Instant.EPOCH),
        ),
        private val completeResult: Result<CompletePairingResponse> = Result.success(
            CompletePairingResponse.Success("trusted-credential", Instant.EPOCH),
        ),
    ) : AdminPairingTransportClient {
        var openCalls = 0
        var completeCalls = 0
        var lastRequest: CompletePairingRequest? = null

        override fun open(device: DiscoveredTvService): Result<OpenPairingResponse> {
            openCalls++
            return openResult
        }

        override fun complete(device: DiscoveredTvService, request: CompletePairingRequest): Result<CompletePairingResponse> {
            completeCalls++
            lastRequest = request
            return completeResult
        }
    }
}
