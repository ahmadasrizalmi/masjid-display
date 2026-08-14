package com.asridigital.masjiddisplay.protocol

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object ConfigTransportPaths {
    const val UPDATE = "/v1/config/update"
}

data class TvConfigUpdateRequest(
    val credentialId: String,
    val mosqueId: String,
    val mosqueName: String,
    val locationLabel: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String,
    val prayerMethod: String,
    val normalLayoutMode: String = "HORIZONTAL_MEDIA",
    val informationMessage: String = "",
    val hijriAdjustmentDays: Int = 0,
    val fridayEnabled: Boolean = false,
    val fridayStart: String = "11:30",
    val fridayEnd: String = "13:30",
    val prayerOffsetsMinutes: Map<String, Int> = canonicalPrayerNames.associateWith { 0 },
    val iqamahMinutes: Map<String, Int> = canonicalPrayerNames.associateWith { 10 },
) {
    init {
        require(credentialId.isNotBlank())
        require(mosqueId.isNotBlank())
        require(mosqueName.isNotBlank())
        require(latitude in -90.0..90.0)
        require(longitude in -180.0..180.0)
        require(timezoneId.isNotBlank())
        require(prayerMethod.isNotBlank())
        require(normalLayoutMode in setOf("HORIZONTAL_MEDIA", "SIDEBAR_MEDIA"))
        require(hijriAdjustmentDays in -3..3)
        require(prayerOffsetsMinutes.keys == canonicalPrayerNames)
        require(iqamahMinutes.keys == canonicalPrayerNames)
        require(prayerOffsetsMinutes.values.all { it in -120..120 })
        require(iqamahMinutes.values.all { it in 0..180 })
    }

    companion object {
        val canonicalPrayerNames = setOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA")
    }
}

sealed interface TvConfigUpdateResponse {
    data object Success : TvConfigUpdateResponse
    data class Rejected(val code: String, val message: String) : TvConfigUpdateResponse
}

object ConfigWireContract {
    const val CONTENT_TYPE = "application/x-www-form-urlencoded; charset=utf-8"

    fun encodeRequest(request: TvConfigUpdateRequest): String {
        val values = linkedMapOf(
            "credentialId" to request.credentialId,
            "mosqueId" to request.mosqueId,
            "mosqueName" to request.mosqueName,
            "locationLabel" to request.locationLabel,
            "latitude" to request.latitude.toString(),
            "longitude" to request.longitude.toString(),
            "timezoneId" to request.timezoneId,
            "prayerMethod" to request.prayerMethod,
            "normalLayoutMode" to request.normalLayoutMode,
            "informationMessage" to request.informationMessage,
            "hijriAdjustmentDays" to request.hijriAdjustmentDays.toString(),
            "fridayEnabled" to request.fridayEnabled.toString(),
            "fridayStart" to request.fridayStart,
            "fridayEnd" to request.fridayEnd,
        )
        TvConfigUpdateRequest.canonicalPrayerNames.sorted().forEach { prayer ->
            values["offset.$prayer"] = requireNotNull(request.prayerOffsetsMinutes[prayer]).toString()
            values["iqamah.$prayer"] = requireNotNull(request.iqamahMinutes[prayer]).toString()
        }
        return encode(values)
    }

    fun decodeRequest(body: String): TvConfigUpdateRequest? = runCatching {
        val values = decode(body)
        val offsets = TvConfigUpdateRequest.canonicalPrayerNames.associateWith { prayer ->
            values.getValue("offset.$prayer").toInt()
        }
        val iqamah = TvConfigUpdateRequest.canonicalPrayerNames.associateWith { prayer ->
            values.getValue("iqamah.$prayer").toInt()
        }
        TvConfigUpdateRequest(
            credentialId = values.getValue("credentialId"),
            mosqueId = values.getValue("mosqueId"),
            mosqueName = values.getValue("mosqueName"),
            locationLabel = values.getValue("locationLabel"),
            latitude = values.getValue("latitude").toDouble(),
            longitude = values.getValue("longitude").toDouble(),
            timezoneId = values.getValue("timezoneId"),
            prayerMethod = values.getValue("prayerMethod"),
            normalLayoutMode = values.getValue("normalLayoutMode"),
            informationMessage = values.getValue("informationMessage"),
            hijriAdjustmentDays = values.getValue("hijriAdjustmentDays").toInt(),
            fridayEnabled = values.getValue("fridayEnabled").toBooleanStrict(),
            fridayStart = values.getValue("fridayStart"),
            fridayEnd = values.getValue("fridayEnd"),
            prayerOffsetsMinutes = offsets,
            iqamahMinutes = iqamah,
        )
    }.getOrNull()

    fun encodeResponse(response: TvConfigUpdateResponse): String = when (response) {
        TvConfigUpdateResponse.Success -> encode(mapOf("status" to "ok"))
        is TvConfigUpdateResponse.Rejected -> encode(
            mapOf("status" to "error", "code" to response.code, "message" to response.message),
        )
    }

    fun decodeResponse(body: String): TvConfigUpdateResponse? = runCatching {
        val values = decode(body)
        when (values["status"]) {
            "ok" -> TvConfigUpdateResponse.Success
            "error" -> TvConfigUpdateResponse.Rejected(
                code = values.getValue("code"),
                message = values.getValue("message"),
            )
            else -> error("Unknown config response")
        }
    }.getOrNull()

    private fun encode(values: Map<String, String>): String = values.entries.joinToString("&") { (key, value) ->
        "${urlEncode(key)}=${urlEncode(value)}"
    }

    private fun decode(body: String): Map<String, String> = body
        .split('&')
        .filter { it.isNotEmpty() }
        .associate { pair ->
            val separator = pair.indexOf('=')
            require(separator > 0)
            urlDecode(pair.substring(0, separator)) to urlDecode(pair.substring(separator + 1))
        }

    private fun urlEncode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)
    private fun urlDecode(value: String): String = URLDecoder.decode(value, StandardCharsets.UTF_8)
}
