package com.asridigital.masjiddisplay.admin.pairing

import com.asridigital.masjiddisplay.protocol.CompletePairingRequest
import com.asridigital.masjiddisplay.protocol.CompletePairingResponse
import com.asridigital.masjiddisplay.protocol.DiscoveredTvService
import com.asridigital.masjiddisplay.protocol.OpenPairingResponse
import com.asridigital.masjiddisplay.protocol.PairingTransportPaths
import com.asridigital.masjiddisplay.protocol.PairingWireContract
import java.net.HttpURLConnection
import java.net.URL

class LanPairingTransportClient(
    private val connectTimeoutMs: Int = 3_000,
    private val readTimeoutMs: Int = 3_000,
) : AdminPairingTransportClient {
    override fun open(device: DiscoveredTvService): Result<OpenPairingResponse> = runCatching {
        val body = request(device, PairingTransportPaths.OPEN, "")
        PairingWireContract.decodeOpen(body) ?: error("Malformed pairing open response")
    }

    override fun complete(
        device: DiscoveredTvService,
        request: CompletePairingRequest,
    ): Result<CompletePairingResponse> = runCatching {
        val body = request(device, PairingTransportPaths.COMPLETE, PairingWireContract.encodeComplete(request))
        PairingWireContract.decodeResult(body) ?: error("Malformed pairing complete response")
    }

    private fun request(device: DiscoveredTvService, path: String, body: String): String {
        val connection = URL("http", device.hostAddress, device.port, path).openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "POST"
            connection.connectTimeout = connectTimeoutMs
            connection.readTimeout = readTimeoutMs
            connection.useCaches = false
            connection.doInput = true
            connection.setRequestProperty("Accept", PairingWireContract.CONTENT_TYPE)
            if (body.isNotEmpty()) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", PairingWireContract.CONTENT_TYPE)
                connection.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(body) }
            }
            val code = connection.responseCode
            if (code !in 200..299) error("Local pairing HTTP $code")
            connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}
