package com.asridigital.masjiddisplay.tv.discovery

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import com.asridigital.masjiddisplay.protocol.LocalProtocol

object MasjidDisplayNsdContract {
    const val SERVICE_TYPE = "_masjid-display._tcp."
    const val DEFAULT_SERVICE_NAME = "Masjid Display TV"
    const val TXT_PROTOCOL_VERSION = "protocol"
}

sealed interface NsdAdvertiseState {
    data object Idle : NsdAdvertiseState
    data class Registered(val serviceName: String, val port: Int) : NsdAdvertiseState
    data class Failed(val errorCode: Int) : NsdAdvertiseState
}

/**
 * Thin Android NSD boundary. It advertises only local service metadata; it opens no socket and
 * performs no internet/cloud communication. The caller supplies the already-bound LAN port.
 */
class TvNsdAdvertiser(
    private val nsdManager: NsdManager,
    private val serviceName: String = MasjidDisplayNsdContract.DEFAULT_SERVICE_NAME,
) {
    private var listener: NsdManager.RegistrationListener? = null

    @Volatile
    var state: NsdAdvertiseState = NsdAdvertiseState.Idle
        private set

    fun start(port: Int) {
        require(port in 1..65535)
        if (listener != null) return

        val info = NsdServiceInfo().apply {
            this.serviceName = this@TvNsdAdvertiser.serviceName
            serviceType = MasjidDisplayNsdContract.SERVICE_TYPE
            this.port = port
            setAttribute(
                MasjidDisplayNsdContract.TXT_PROTOCOL_VERSION,
                LocalProtocol.CURRENT_VERSION.toString(),
            )
        }
        val registration = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
                state = NsdAdvertiseState.Registered(serviceInfo.serviceName, serviceInfo.port)
            }

            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                state = NsdAdvertiseState.Failed(errorCode)
                listener = null
            }

            override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
                state = NsdAdvertiseState.Idle
            }

            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                state = NsdAdvertiseState.Failed(errorCode)
            }
        }
        listener = registration
        nsdManager.registerService(info, NsdManager.PROTOCOL_DNS_SD, registration)
    }

    fun stop() {
        val current = listener ?: return
        listener = null
        nsdManager.unregisterService(current)
        state = NsdAdvertiseState.Idle
    }
}
