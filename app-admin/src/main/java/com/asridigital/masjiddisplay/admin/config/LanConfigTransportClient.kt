package com.asridigital.masjiddisplay.admin.config

import com.asridigital.masjiddisplay.protocol.ConfigTransportPaths
import com.asridigital.masjiddisplay.protocol.ConfigWireContract
import com.asridigital.masjiddisplay.protocol.DiscoveredTvService
import com.asridigital.masjiddisplay.protocol.TvConfigUpdateRequest
import com.asridigital.masjiddisplay.protocol.TvConfigUpdateResponse
import java.net.HttpURLConnection
import java.net.URL

class LanConfigTransportClient(
    private val connectTimeoutMs: Int = 3_000,
    private val readTimeoutMs: Int = 3_000,
) {
    fun update(device: DiscoveredTvService, request: TvConfigUpdateRequest): Result<TvConfigUpdateResponse> = runCatching {
        val connection = URL("http", device.hostAddress, device.port, ConfigTransportPaths.UPDATE)
            .openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = connectTimeoutMs
            connection.readTimeout = readTimeoutMs
            connection.useCaches = false
            connection.doInput = true
            connection.doOutput = true
            connection.setRequestProperty("Accept", ConfigWireContract.CONTENT_TYPE)
            connection.setRequestProperty("Content-Type", ConfigWireContract.CONTENT_TYPE)
            connection.outputStream.bufferedWriter(Charsets.UTF_8).use {
                it.write(ConfigWireContract.encodeRequest(request))
            }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            ConfigWireContract.decodeResponse(body) ?: error("Malformed config response (HTTP $code)")
        } finally {
            connection.disconnect()
        }
    }
}
