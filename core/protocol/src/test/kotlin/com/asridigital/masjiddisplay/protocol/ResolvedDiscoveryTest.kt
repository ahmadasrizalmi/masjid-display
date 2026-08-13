package com.asridigital.masjiddisplay.protocol

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ResolvedDiscoveryTest {
    @Test
    fun compatibleMetadataProducesDiscoveredTv() {
        val result = resolveDiscoveredTv(service(protocol = "1"))
        val compatible = assertIs<DiscoveryResolutionResult.Compatible>(result)
        assertEquals("tv.local", compatible.service.hostAddress)
        assertEquals(8787, compatible.service.port)
    }

    @Test
    fun unsupportedVersionIsVisibleAsIncompatible() {
        val result = assertIs<DiscoveryResolutionResult.Incompatible>(
            resolveDiscoveredTv(service(protocol = "99")),
        )
        assertEquals(setOf(1), result.supportedVersions)
    }

    @Test
    fun missingOrMalformedProtocolMetadataIsRejected() {
        assertEquals(
            DiscoveryResolutionResult.InvalidMetadata,
            resolveDiscoveredTv(service(protocol = null)),
        )
        assertEquals(
            DiscoveryResolutionResult.InvalidMetadata,
            resolveDiscoveredTv(service(protocol = "abc")),
        )
    }

    private fun service(protocol: String?) = ResolvedLocalService(
        serviceName = "Masjid Display TV",
        hostAddress = "tv.local",
        port = 8787,
        txtAttributes = protocol?.let { mapOf(LocalDiscoveryContract.TXT_PROTOCOL_VERSION to it) }.orEmpty(),
    )
}
