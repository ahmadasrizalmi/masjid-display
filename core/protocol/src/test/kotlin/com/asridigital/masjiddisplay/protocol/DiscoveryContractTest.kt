package com.asridigital.masjiddisplay.protocol

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DiscoveryContractTest {
    @Test
    fun supportedServiceNegotiatesCurrentProtocol() {
        val service = DiscoveredTvService("Masjid Display TV", "192.0.2.10", 8787, 1)
        assertEquals(ProtocolNegotiation.Accepted(1), service.negotiation)
    }

    @Test
    fun unsupportedServiceReportsSupportedVersions() {
        val service = DiscoveredTvService("Masjid Display TV", "192.0.2.10", 8787, 99)
        assertEquals(ProtocolNegotiation.Rejected(setOf(1)), service.negotiation)
    }

    @Test
    fun invalidResolvedPortIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            DiscoveredTvService("Masjid Display TV", "192.0.2.10", 0, 1)
        }
    }

    @Test
    fun blankResolvedHostIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            DiscoveredTvService("Masjid Display TV", "", 8787, 1)
        }
    }
}
