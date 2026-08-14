package com.asridigital.masjiddisplay.protocol

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object MediaTransportPaths {
    const val CREATE_SESSION = "/v1/media/session"
    const val UPLOAD_PREFIX = "/v1/media/upload/"
    const val LIST = "/v1/media/list"
    const val THUMBNAIL = "/v1/media/thumbnail"
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

data class MediaThumbnailRequest(val credentialId: String, val mediaId: String) {
    init {
        require(credentialId.isNotBlank())
        require(mediaId.matches(safeMediaIdPattern))
    }
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
    data class Accepted(val sessionId: String) : MediaSessionResponse { init { require(sessionId.isNotBlank()) } }
    data class Rejected(val code: String, val message: String) : MediaSessionResponse
}

sealed interface MediaListResponse {
    data class Success(val items: List<MediaListItem>) : MediaListResponse
    data class Rejected(val code: String, val message: String) : MediaListResponse
}

sealed interface MediaThumbnailResponse {
    data class Success(val jpegBase64: String) : MediaThumbnailResponse { init { require(jpegBase64.isNotBlank()) } }
    data class Rejected(val code: String, val message: String) : MediaThumbnailResponse
}

sealed interface MediaMutationResponse {
    data object Success : MediaMutationResponse
    data class Rejected(val code: String, val message: String) : MediaMutationResponse
}

object MediaWireContract {
    const val FORM_CONTENT_TYPE = "application/x-www-form-urlencoded; charset=utf-8"
    const val BINARY_CONTENT_TYPE = "application/octet-stream"

    fun encodeSessionRequest(request: MediaUploadSessionRequest): String = encode(linkedMapOf(
        "credentialId" to request.credentialId, "mediaId" to request.mediaId, "filename" to request.filename,
        "mimeType" to request.mimeType, "byteSize" to request.byteSize.toString(), "sha256" to request.sha256.lowercase(),
    ))

    fun decodeSessionRequest(body: String): MediaUploadSessionRequest? = runCatching {
        val v = decode(body)
        MediaUploadSessionRequest(v.getValue("credentialId"), v.getValue("mediaId"), v.getValue("filename"),
            v.getValue("mimeType"), v.getValue("byteSize").toLong(), v.getValue("sha256"))
    }.getOrNull()

    fun encodeListRequest(request: MediaListRequest): String = encode(mapOf("credentialId" to request.credentialId))
    fun decodeListRequest(body: String): MediaListRequest? = runCatching { MediaListRequest(decode(body).getValue("credentialId")) }.getOrNull()

    fun encodeThumbnailRequest(request: MediaThumbnailRequest): String = encode(mapOf("credentialId" to request.credentialId, "mediaId" to request.mediaId))
    fun decodeThumbnailRequest(body: String): MediaThumbnailRequest? = runCatching {
        val v = decode(body); MediaThumbnailRequest(v.getValue("credentialId"), v.getValue("mediaId"))
    }.getOrNull()

    fun encodeDeleteRequest(request: MediaDeleteRequest): String = encode(mapOf("credentialId" to request.credentialId, "mediaId" to request.mediaId))
    fun decodeDeleteRequest(body: String): MediaDeleteRequest? = runCatching {
        val v = decode(body); MediaDeleteRequest(v.getValue("credentialId"), v.getValue("mediaId"))
    }.getOrNull()

    fun encodeSessionResponse(response: MediaSessionResponse): String = when (response) {
        is MediaSessionResponse.Accepted -> encode(mapOf("status" to "ok", "sessionId" to response.sessionId))
        is MediaSessionResponse.Rejected -> encode(mapOf("status" to "error", "code" to response.code, "message" to response.message))
    }
    fun decodeSessionResponse(body: String): MediaSessionResponse? = runCatching {
        val v = decode(body); when (v["status"]) {
            "ok" -> MediaSessionResponse.Accepted(v.getValue("sessionId"))
            "error" -> MediaSessionResponse.Rejected(v.getValue("code"), v.getValue("message"))
            else -> error("Unknown media session response")
        }
    }.getOrNull()

    fun encodeListResponse(response: MediaListResponse): String = when (response) {
        is MediaListResponse.Rejected -> encode(mapOf("status" to "error", "code" to response.code, "message" to response.message))
        is MediaListResponse.Success -> {
            val values = linkedMapOf("status" to "ok", "count" to response.items.size.toString())
            response.items.forEachIndexed { i, item ->
                val p = "item.$i."
                values[p + "mediaId"] = item.mediaId; values[p + "filename"] = item.filename; values[p + "mimeType"] = item.mimeType
                values[p + "byteSize"] = item.byteSize.toString(); values[p + "sha256"] = item.sha256.lowercase()
                values[p + "createdAt"] = item.createdAtEpochMillis.toString(); values[p + "enabled"] = item.enabled.toString()
            }
            encode(values)
        }
    }
    fun decodeListResponse(body: String): MediaListResponse? = runCatching {
        val v = decode(body); when (v["status"]) {
            "error" -> MediaListResponse.Rejected(v.getValue("code"), v.getValue("message"))
            "ok" -> {
                val count = v.getValue("count").toInt(); require(count in 0..500)
                MediaListResponse.Success((0 until count).map { i ->
                    val p = "item.$i."
                    MediaListItem(v.getValue(p + "mediaId"), v.getValue(p + "filename"), v.getValue(p + "mimeType"),
                        v.getValue(p + "byteSize").toLong(), v.getValue(p + "sha256"), v.getValue(p + "createdAt").toLong(),
                        v.getValue(p + "enabled").toBooleanStrict())
                })
            }
            else -> error("Unknown media list response")
        }
    }.getOrNull()

    fun encodeThumbnailResponse(response: MediaThumbnailResponse): String = when (response) {
        is MediaThumbnailResponse.Success -> encode(mapOf("status" to "ok", "jpegBase64" to response.jpegBase64))
        is MediaThumbnailResponse.Rejected -> encode(mapOf("status" to "error", "code" to response.code, "message" to response.message))
    }
    fun decodeThumbnailResponse(body: String): MediaThumbnailResponse? = runCatching {
        val v = decode(body); when (v["status"]) {
            "ok" -> MediaThumbnailResponse.Success(v.getValue("jpegBase64"))
            "error" -> MediaThumbnailResponse.Rejected(v.getValue("code"), v.getValue("message"))
            else -> error("Unknown media thumbnail response")
        }
    }.getOrNull()

    fun encodeMutationResponse(response: MediaMutationResponse): String = when (response) {
        MediaMutationResponse.Success -> encode(mapOf("status" to "ok"))
        is MediaMutationResponse.Rejected -> encode(mapOf("status" to "error", "code" to response.code, "message" to response.message))
    }
    fun decodeMutationResponse(body: String): MediaMutationResponse? = runCatching {
        val v = decode(body); when (v["status"]) {
            "ok" -> MediaMutationResponse.Success
            "error" -> MediaMutationResponse.Rejected(v.getValue("code"), v.getValue("message"))
            else -> error("Unknown media mutation response")
        }
    }.getOrNull()

    private fun encode(values: Map<String, String>): String = values.entries.joinToString("&") { (k, v) -> "${urlEncode(k)}=${urlEncode(v)}" }
    private fun decode(body: String): Map<String, String> = body.split('&').filter(String::isNotEmpty).associate { pair ->
        val s = pair.indexOf('='); require(s > 0); urlDecode(pair.substring(0, s)) to urlDecode(pair.substring(s + 1))
    }
    private fun urlEncode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)
    private fun urlDecode(value: String): String = URLDecoder.decode(value, StandardCharsets.UTF_8)
}
