package com.asridigital.masjiddisplay.admin.discovery

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import com.asridigital.masjiddisplay.protocol.DiscoveredTvService
import com.asridigital.masjiddisplay.protocol.DiscoveryResolutionResult
import com.asridigital.masjiddisplay.protocol.LocalDiscoveryContract
import com.asridigital.masjiddisplay.protocol.ResolvedLocalService
import com.asridigital.masjiddisplay.protocol.resolveDiscoveredTv

/**
 * Android-only NSD/mDNS boundary for Admin discovery. It discovers and resolves local TV services
 * but never opens a pairing socket or performs internet/cloud communication.
 */
class AdminNsdDiscovery(
    private val nsdManager: NsdManager,
    private val onServicesChanged: (List<DiscoveredTvService>) -> Unit,
    private val onFailure: (Int) -> Unit = {},
) {
    private val servicesByEndpoint = linkedMapOf<String, DiscoveredTvService>()
    private var listener: NsdManager.DiscoveryListener? = null

    fun start() {
        if (listener != null) return
        val discovery = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(serviceType: String) = Unit

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                if (serviceInfo.serviceType != LocalDiscoveryContract.SERVICE_TYPE) return
                nsdManager.resolveService(serviceInfo, resolver())
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                val removed = servicesByEndpoint.entries.removeAll {
                    it.value.serviceName == serviceInfo.serviceName
                }
                if (removed) publish()
            }

            override fun onDiscoveryStopped(serviceType: String) = Unit

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                listener = null
                onFailure(errorCode)
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                listener = null
                onFailure(errorCode)
            }
        }
        listener = discovery
        nsdManager.discoverServices(LocalDiscoveryContract.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discovery)
    }

    fun stop() {
        val active = listener ?: return
        listener = null
        servicesByEndpoint.clear()
        nsdManager.stopServiceDiscovery(active)
        publish()
    }

    private fun resolver() = object : NsdManager.ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            onFailure(errorCode)
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            val hostAddress = serviceInfo.host?.hostAddress ?: return
            val txtAttributes = serviceInfo.attributes.mapValues { (_, value) ->
                value.toString(Charsets.UTF_8)
            }
            when (
                val result = resolveDiscoveredTv(
                    ResolvedLocalService(
                        serviceName = serviceInfo.serviceName,
                        hostAddress = hostAddress,
                        port = serviceInfo.port,
                        txtAttributes = txtAttributes,
                    ),
                )
            ) {
                is DiscoveryResolutionResult.Compatible -> put(result.service)
                is DiscoveryResolutionResult.Incompatible -> put(result.service)
                DiscoveryResolutionResult.InvalidMetadata -> Unit
            }
        }
    }

    private fun put(service: DiscoveredTvService) {
        servicesByEndpoint["${service.hostAddress}:${service.port}"] = service
        publish()
    }

    private fun publish() {
        onServicesChanged(servicesByEndpoint.values.toList())
    }
}
