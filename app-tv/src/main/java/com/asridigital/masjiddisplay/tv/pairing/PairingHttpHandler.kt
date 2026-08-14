package com.asridigital.masjiddisplay.tv.pairing

import com.asridigital.masjiddisplay.protocol.CompletePairingResponse
import com.asridigital.masjiddisplay.protocol.PairingErrorCode
import com.asridigital.masjiddisplay.protocol.PairingTransportPaths
import com.asridigital.masjiddisplay.protocol.PairingWireContract
import com.asridigital.masjiddisplay.protocol.TvPairingTransportAdapter

data class PairingHttpRequest(val method: String, val path: String, val body: String = "")
data class PairingHttpResponse(val status: Int, val contentType: String, val body: String)

class PairingHttpHandler(private val pairing: TvPairingTransportAdapter) {
    fun handle(request: PairingHttpRequest): PairingHttpResponse {
        if (request.method != "POST") return response(405, "")
        return when (request.path) {
            PairingTransportPaths.OPEN -> pairing.open()?.let { response(200, PairingWireContract.encodeOpen(it)) }
                ?: response(404, "")
            PairingTransportPaths.COMPLETE -> {
                val complete = PairingWireContract.decodeComplete(request.body)
                    ?: return response(400, PairingWireContract.encodeResult(CompletePairingResponse.Rejected(PairingErrorCode.MALFORMED_REQUEST)))
                response(200, PairingWireContract.encodeResult(pairing.complete(complete)))
            }
            else -> response(404, "")
        }
    }

    private fun response(status: Int, body: String) = PairingHttpResponse(status, PairingWireContract.CONTENT_TYPE, body)
}
