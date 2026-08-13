package com.asridigital.masjiddisplay.protocol

/** Shared metadata contract for Android NSD/mDNS. No address or hardcoded IP belongs here. */
object LocalDiscoveryContract {
    const val SERVICE_TYPE = "_masjid-display._tcp."
    const val TXT_PROTOCOL_VERSION = "protocol"
}

data class DiscoveredTvService(
    val serviceName: String,
    val hostAddress: String,
    val port: Int,
    val protocolVersion: Int,
) {
    init {
        require(serviceName.isNotBlank())
        require(hostAddress.isNotBlank())
        require(port in 1..65535)
    }

    val negotiation: ProtocolNegotiation
        get() = negotiateProtocol(protocolVersion)
}
