package com.asridigital.masjiddisplay.protocol

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object MediaTransportPaths {
    const val CREATE_SESSION = "/v1/media/session"
    const val UPLOAD_PREFIX = "/v1/media/upload/"
    const val LIST = "/v1/media/list"
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

data class MediaListRequest(val credentialId: String) {
    init { require(credentialId.isNotBlank()) }
}

data class MediaListItem(
    val mediaId: String,
    val filename: String,
    val mimeType: String,
    val byteSize: Long,
    val sha256: String,
    val createdAtEpochMillis: Long,
    val enabled: Boolean,
) {
    init {
        require(mediaId.matches(safeMediaIdPattern))
        require(filename.isNotBlank())
        require(mimeType.isNotBlank())
        require(byteSize >= 0)
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

sealed interface MediaListResponse {
    data class Success(val items: List<MediaListItem>) : MediaListResponse
    data class Rejected(val code: String, val message: String) : MediaListResponse
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

    fun encodeListRequest(request: MediaListRequest): String = encode(mapOf("credentialId" to request.credentialId))

    fun decodeListRequest(body: String): MediaListRequest? = runCatching {
        MediaListRequest(decode(body).getValue("credentialId"))
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

    fun encodeListResponse(response: MediaListResponse): String = when (response) {
        is MediaListResponse.Rejected -> encode(
            mapOf("status" to "error", "code" to response.code, "message" to response.message),
        )
        is MediaListResponse.Success -> {
            val values = linkedMapOf("status" to "ok", "count" to response.items.size.toString())
            response.items.forEachIndexed { index, item ->
                val prefix = "item.$index."
                values[prefix + "mediaId"] = item.mediaId
                values[prefix + "filename"] = item.filename
                values[prefix + "mimeType"] = item.mimeType
                values[prefix + "byteSize"] = item.byteSize.toString()
                values[prefix + "sha256"] = item.sha256.lowercase()
                values[prefix + "createdAt"] = item.createdAtEpochMillis.toString()
                values[prefix + "enabled"] = item.enabled.toString()
            }
            encode(values)
        }
    }

    fun decodeListResponse(body: String): MediaListResponse? = runCatching {
        val values = decode(body)
        when (values["status"]) {
            "error" -> MediaListResponse.Rejected(values.getValue("code"), values.getValue("message"))
            "ok" -> {
                val count = values.getValue("count").toInt()
                require(count in 0..500)
                MediaListResponse.Success(
                    (0 until count).map { index ->
                        val prefix = "item.$index."
                        MediaListItem(
                            mediaId = values.getValue(prefix + "mediaId"),
                            filename = values.getValue(prefix + "filename"),
                            mimeType = values.getValue(prefix + "mimeType"),
                            byteSize = values.getValue(prefix + "byteSize").toLong(),
                            sha256 = values.getValue(prefix + "sha256"),
                            createdAtEpochMillis = values.getValue(prefix + "createdAt").toLong(),
                            enabled = values.getValue(prefix + "enabled").toBooleanStrict(),
                        )
                    },
                )
            }
            else -> error("Unknown media list response")
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
