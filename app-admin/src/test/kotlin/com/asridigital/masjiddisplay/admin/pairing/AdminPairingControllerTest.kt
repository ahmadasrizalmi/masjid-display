package com.asridigital.masjiddisplay.admin.pairing

import com.asridigital.masjiddisplay.protocol.DiscoveredTvService
import com.asridigital.masjiddisplay.protocol.LocalProtocol
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AdminPairingControllerTest {
    @Test
    fun discoveryDeduplicatesByResolvedHostAndPort() {
        val controller = AdminPairingController()
        val first = service(name = "Masjid A", host = "192.168.1.10", port = 8080)
        val duplicateEndpoint = service(name = "Renamed", host = "192.168.1.10", port = 8080)
        val other = service(name = "Masjid B", host = "192.168.1.11", port = 8080)

        controller.discovered(listOf(first, duplicateEndpoint, other))

        assertEquals(
            AdminPairingState.Devices(items = listOf(first, other)),
            controller.state,
        )
    }

    @Test
    fun supportedDeviceCanBeSelectedAndPaired() {
        val controller = AdminPairingController()
        val device = service()
        controller.discovered(listOf(device))

        assertTrue(controller.select(device))
        assertEquals(AdminPairingState.Devices(listOf(device), selected = device), controller.state)

        controller.paired("trusted-credential-1")
        assertEquals(AdminPairingState.Paired(device, "trusted-credential-1"), controller.state)
    }

    @Test
    fun unsupportedProtocolIsRejected() {
        val controller = AdminPairingController()
        val incompatible = service(version = LocalProtocol.CURRENT_VERSION + 1)
        controller.discovered(listOf(incompatible))

        assertFalse(controller.select(incompatible))
        assertEquals(AdminPairingState.Error("Versi protokol TV tidak didukung"), controller.state)
    }

    @Test
    fun blankCredentialIsRejectedAfterSelection() {
        val controller = AdminPairingController()
        val device = service()
        controller.discovered(listOf(device))
        controller.select(device)

        controller.paired("  ")

        assertEquals(AdminPairingState.Error("Credential pairing tidak valid"), controller.state)
    }

    @Test
    fun deviceOutsideCurrentDiscoveryListCannotBeSelected() {
        val controller = AdminPairingController()
        controller.discovered(listOf(service(host = "192.168.1.10")))

        assertFalse(controller.select(service(host = "192.168.1.99")))
        assertIs<AdminPairingState.Devices>(controller.state)
    }

    private fun service(
        name: String = "Masjid Display TV",
        host: String = "192.168.1.10",
        port: Int = 8080,
        version: Int = LocalProtocol.CURRENT_VERSION,
    ) = DiscoveredTvService(name, host, port, version)
}
