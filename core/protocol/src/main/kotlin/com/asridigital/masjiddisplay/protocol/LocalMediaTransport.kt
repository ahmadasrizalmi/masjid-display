package com.asridigital.masjiddisplay.protocol

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object MediaTransportPaths {
    const val CREATE_SESSION = "/v1/media/session"
    const val UPLOAD_PREFIX = "/v1/media/upload/"
    const val DELETE = "/v1/media/delete"
}

private val safeMediaIdPattern = Regex("[A-Za-z0-9_-]{1,64}")

data class MediaUploadSessionRequest(
    val credentialId: String,
    val mediaId: String,
    val filename: String,
    val mimeType: String,
    val byteSize: Long,
    val sha256: String,
) {
    init {
        require(credentialId.isNotBlank())
        require(mediaId.matches(safeMediaIdPattern))
        require(filename.isNotBlank())
        require(mimeType in setOf("image/jpeg", "image/png", "image/webp"))
        require(byteSize in 1..50L * 1024L * 1024L)
        require(sha256.matches(Regex("[0-9a-fA-F]{64}")))
    }
}

data class MediaDeleteRequest(val credentialId: String, val mediaId: String) {
    init {
        require(credentialId.isNotBlank())
        require(mediaId.matches(safeMediaIdPattern))
    }
}

sealed interface MediaSessionResponse {
    data class Accepted(val sessionId: String) : MediaSessionResponse {
        init { require(sessionId.isNotBlank()) }
    }
    data class Rejected(val code: String, val message: String) : MediaSessionResponse
}

sealed interface MediaMutationResponse {
    data object Success : MediaMutationResponse
    data class Rejected(val code: String, val message: String) : MediaMutationResponse
}

object MediaWireContract {
    const val FORM_CONTENT_TYPE = "application/x-www-form-urlencoded; charset=utf-8"
    const val BINARY_CONTENT_TYPE = "application/octet-stream"

    fun encodeSessionRequest(request: MediaUploadSessionRequest): String = encode(
        linkedMapOf(
            "credentialId" to request.credentialId,
            "mediaId" to request.mediaId,
            "filename" to request.filename,
            "mimeType" to request.mimeType,
            "byteSize" to request.byteSize.toString(),
            "sha256" to request.sha256.lowercase(),
        ),
    )

    fun decodeSessionRequest(body: String): MediaUploadSessionRequest? = runCatching {
        val values = decode(body)
        MediaUploadSessionRequest(
            credentialId = values.getValue("credentialId"),
            mediaId = values.getValue("mediaId"),
            filename = values.getValue("filename"),
            mimeType = values.getValue("mimeType"),
            byteSize = values.getValue("byteSize").toLong(),
            sha256 = values.getValue("sha256"),
        )
    }.getOrNull()

    fun encodeDeleteRequest(request: MediaDeleteRequest): String = encode(
        mapOf("credentialId" to request.credentialId, "mediaId" to request.mediaId),
    )

    fun decodeDeleteRequest(body: String): MediaDeleteRequest? = runCatching {
        val values = decode(body)
        MediaDeleteRequest(values.getValue("credentialId"), values.getValue("mediaId"))
    }.getOrNull()

    fun encodeSessionResponse(response: MediaSessionResponse): String = when (response) {
        is MediaSessionResponse.Accepted -> encode(mapOf("status" to "ok", "sessionId" to response.sessionId))
        is MediaSessionResponse.Rejected -> encode(
            mapOf("status" to "error", "code" to response.code, "message" to response.message),
        )
    }

    fun decodeSessionResponse(body: String): MediaSessionResponse? = runCatching {
        val values = decode(body)
        when (values["status"]) {
            "ok" -> MediaSessionResponse.Accepted(values.getValue("sessionId"))
            "error" -> MediaSessionResponse.Rejected(values.getValue("code"), values.getValue("message"))
            else -> error("Unknown media session response")
        }
    }.getOrNull()

    fun encodeMutationResponse(response: MediaMutationResponse): String = when (response) {
        MediaMutationResponse.Success -> encode(mapOf("status" to "ok"))
        is MediaMutationResponse.Rejected -> encode(
            mapOf("status" to "error", "code" to response.code, "message" to response.message),
        )
    }

    fun decodeMutationResponse(body: String): MediaMutationResponse? = runCatching {
        val values = decode(body)
        when (values["status"]) {
            "ok" -> MediaMutationResponse.Success
            "error" -> MediaMutationResponse.Rejected(values.getValue("code"), values.getValue("message"))
            else -> error("Unknown media mutation response")
        }
    }.getOrNull()

    private fun encode(values: Map<String, String>): String = values.entries.joinToString("&") { (key, value) ->
        "${urlEncode(key)}=${urlEncode(value)}"
    }

    private fun decode(body: String): Map<String, String> = body
        .split('&')
        .filter(String::isNotEmpty)
        .associate { pair ->
            val separator = pair.indexOf('=')
            require(separator > 0)
            urlDecode(pair.substring(0, separator)) to urlDecode(pair.substring(separator + 1))
        }

    private fun urlEncode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)
    private fun urlDecode(value: String): String = URLDecoder.decode(value, StandardCharsets.UTF_8)
}
