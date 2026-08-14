package com.asridigital.masjiddisplay.admin.setup

import java.time.ZoneId

enum class MosqueSetupField {
    NAME,
    LOCATION_LABEL,
    LATITUDE,
    LONGITUDE,
    TIMEZONE,
}

enum class MosquePrayerMethod(val displayName: String) {
    KEMENAG_INDONESIA("Kemenag Indonesia"),
}

data class MosqueSetupDraft(
    val name: String = "",
    val locationLabel: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val timezoneId: String = "",
    val prayerMethod: MosquePrayerMethod = MosquePrayerMethod.KEMENAG_INDONESIA,
) {
    fun validate(): MosqueSetupValidation {
        val errors = linkedMapOf<MosqueSetupField, String>()
        if (name.trim().isEmpty()) errors[MosqueSetupField.NAME] = "Nama masjid wajib diisi"
        if (locationLabel.trim().isEmpty()) errors[MosqueSetupField.LOCATION_LABEL] = "Label lokasi wajib diisi"

        val parsedLatitude = latitude.normalizedNumber().toDoubleOrNull()
        if (parsedLatitude == null || parsedLatitude !in -90.0..90.0) {
            errors[MosqueSetupField.LATITUDE] = "Latitude harus antara -90 dan 90"
        }

        val parsedLongitude = longitude.normalizedNumber().toDoubleOrNull()
        if (parsedLongitude == null || parsedLongitude !in -180.0..180.0) {
            errors[MosqueSetupField.LONGITUDE] = "Longitude harus antara -180 dan 180"
        }

        if (timezoneId.trim().isEmpty() || runCatching { ZoneId.of(timezoneId.trim()) }.isFailure) {
            errors[MosqueSetupField.TIMEZONE] = "Timezone tidak valid"
        }

        return MosqueSetupValidation(errors)
    }

    fun normalized(): MosqueSetupDraft = copy(
        name = name.trim(),
        locationLabel = locationLabel.trim(),
        latitude = latitude.normalizedNumber(),
        longitude = longitude.normalizedNumber(),
        timezoneId = timezoneId.trim(),
    )

    private fun String.normalizedNumber(): String = trim().replace(',', '.')
}

data class MosqueSetupValidation(
    val errors: Map<MosqueSetupField, String>,
) {
    val isValid: Boolean get() = errors.isEmpty()
    operator fun get(field: MosqueSetupField): String? = errors[field]
}
