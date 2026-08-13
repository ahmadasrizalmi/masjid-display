package com.asridigital.masjiddisplay.protocol

/** Platform-neutral boundary used by the future Admin NSD adapter after Android resolves a service. */
data class ResolvedLocalService(
    val serviceName: String,
    val hostAddress: String,
    val port: Int,
    val txtAttributes: Map<String, String>,
)

sealed interface DiscoveryResolutionResult {
    data class Compatible(val service: DiscoveredTvService) : DiscoveryResolutionResult
    data class Incompatible(val service: DiscoveredTvService, val supportedVersions: Set<Int>) : DiscoveryResolutionResult
    data object InvalidMetadata : DiscoveryResolutionResult
}

fun resolveDiscoveredTv(input: ResolvedLocalService): DiscoveryResolutionResult {
    val protocol = input.txtAttributes[LocalDiscoveryContract.TXT_PROTOCOL_VERSION]?.toIntOrNull()
        ?: return DiscoveryResolutionResult.InvalidMetadata
    val service = try {
        DiscoveredTvService(input.serviceName, input.hostAddress, input.port, protocol)
    } catch (_: IllegalArgumentException) {
        return DiscoveryResolutionResult.InvalidMetadata
    }
    return when (val negotiation = service.negotiation) {
        is ProtocolNegotiation.Accepted -> DiscoveryResolutionResult.Compatible(service)
        is ProtocolNegotiation.Rejected -> DiscoveryResolutionResult.Incompatible(service, negotiation.supportedVersions)
    }
}
