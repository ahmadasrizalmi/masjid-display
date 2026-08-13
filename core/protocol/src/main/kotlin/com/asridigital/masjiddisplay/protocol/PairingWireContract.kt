package com.asridigital.masjiddisplay.protocol

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant

object PairingWireContract {
    const val CONTENT_TYPE = "application/x-www-form-urlencoded; charset=utf-8"
    const val METHOD_OPEN = "POST"
    const val METHOD_COMPLETE = "POST"

    fun encodeComplete(request: CompletePairingRequest): String = form(
        "sessionId" to request.sessionId,
        "oneTimeSecret" to request.oneTimeSecret,
        "protocolVersion" to request.protocolVersion.toString(),
    )

    fun decodeComplete(body: String): CompletePairingRequest? {
        val values = parse(body) ?: return null
        return CompletePairingRequest(
            sessionId = values["sessionId"]?.takeIf(String::isNotBlank) ?: return null,
            oneTimeSecret = values["oneTimeSecret"]?.takeIf(String::isNotBlank) ?: return null,
            protocolVersion = values["protocolVersion"]?.toIntOrNull() ?: return null,
        )
    }

    fun encodeOpen(response: OpenPairingResponse): String = form(
        "sessionId" to response.sessionId,
        "oneTimeSecret" to response.oneTimeSecret,
        "protocolVersion" to response.protocolVersion.toString(),
        "expiresAt" to response.expiresAt.toString(),
    )

    fun decodeOpen(body: String): OpenPairingResponse? {
        val values = parse(body) ?: return null
        return runCatching {
            OpenPairingResponse(
                sessionId = values.getValue("sessionId"),
                oneTimeSecret = values.getValue("oneTimeSecret"),
                protocolVersion = values.getValue("protocolVersion").toInt(),
                expiresAt = Instant.parse(values.getValue("expiresAt")),
            )
        }.getOrNull()?.takeIf { it.sessionId.isNotBlank() && it.oneTimeSecret.isNotBlank() }
    }

    fun encodeResult(response: CompletePairingResponse): String = when (response) {
        is CompletePairingResponse.Success -> form(
            "status" to "success",
            "credentialId" to response.credentialId,
            "issuedAt" to response.issuedAt.toString(),
        )
        is CompletePairingResponse.Rejected -> form("status" to "rejected", "code" to response.code.name)
    }

    fun decodeResult(body: String): CompletePairingResponse? {
        val values = parse(body) ?: return null
        return when (values["status"]) {
            "success" -> runCatching {
                CompletePairingResponse.Success(values.getValue("credentialId"), Instant.parse(values.getValue("issuedAt")))
            }.getOrNull()?.takeIf { it.credentialId.isNotBlank() }
            "rejected" -> values["code"]?.let { runCatching { PairingErrorCode.valueOf(it) }.getOrNull() }
                ?.let(CompletePairingResponse::Rejected)
            else -> null
        }
    }

    private fun form(vararg values: Pair<String, String>) = values.joinToString("&") { (key, value) ->
        "${encode(key)}=${encode(value)}"
    }

    private fun parse(body: String): Map<String, String>? = runCatching {
        if (body.isBlank()) return null
        body.split('&').associate { field ->
            val parts = field.split('=', limit = 2)
            require(parts.size == 2)
            decode(parts[0]) to decode(parts[1])
        }
    }.getOrNull()

    private fun encode(value: String) = URLEncoder.encode(value, StandardCharsets.UTF_8.name())
    private fun decode(value: String) = URLDecoder.decode(value, StandardCharsets.UTF_8.name())
}
