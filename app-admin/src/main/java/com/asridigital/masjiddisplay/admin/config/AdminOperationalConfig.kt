package com.asridigital.masjiddisplay.admin.config

import com.asridigital.masjiddisplay.admin.setup.MosqueSetupDraft
import com.asridigital.masjiddisplay.protocol.TvConfigUpdateRequest

private val prayers = TvConfigUpdateRequest.canonicalPrayerNames

data class AdminOperationalDraft(
    val mosque: MosqueSetupDraft,
    val prayerOffsetsMinutes: Map<String, Int> = prayers.associateWith { 0 },
    val iqamahMinutes: Map<String, Int> = prayers.associateWith { 10 },
    val fridayEnabled: Boolean = false,
    val fridayStart: String = "11:30",
    val fridayEnd: String = "13:30",
    val announcement: String = "",
    val normalLayoutMode: String = "HORIZONTAL_MEDIA",
    val hijriAdjustmentDays: Int = 0,
) {
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        if (!mosque.validate().isValid) errors += "Data masjid belum valid"
        if (prayerOffsetsMinutes.keys != prayers || prayerOffsetsMinutes.values.any { it !in -120..120 }) {
            errors += "Koreksi jadwal sholat tidak valid"
        }
        if (iqamahMinutes.keys != prayers || iqamahMinutes.values.any { it !in 0..180 }) {
            errors += "Durasi iqamah tidak valid"
        }
        if (normalLayoutMode !in setOf("HORIZONTAL_MEDIA", "SIDEBAR_MEDIA")) {
            errors += "Mode tampilan tidak valid"
        }
        if (hijriAdjustmentDays !in -3..3) errors += "Koreksi Hijriah tidak valid"
        return errors
    }

    fun toRequest(credentialId: String, mosqueId: String): TvConfigUpdateRequest {
        require(validate().isEmpty()) { validate().joinToString() }
        val normalized = mosque.normalized()
        return TvConfigUpdateRequest(
            credentialId = credentialId,
            mosqueId = mosqueId,
            mosqueName = normalized.name,
            locationLabel = normalized.locationLabel,
            latitude = normalized.latitude.toDouble(),
            longitude = normalized.longitude.toDouble(),
            timezoneId = normalized.timezoneId,
            prayerMethod = normalized.prayerMethod.name,
            normalLayoutMode = normalLayoutMode,
            informationMessage = announcement.trim(),
            hijriAdjustmentDays = hijriAdjustmentDays,
            fridayEnabled = fridayEnabled,
            fridayStart = fridayStart,
            fridayEnd = fridayEnd,
            prayerOffsetsMinutes = prayerOffsetsMinutes,
            iqamahMinutes = iqamahMinutes,
        )
    }

    fun withPrayerOffset(prayer: String, delta: Int): AdminOperationalDraft {
        val current = prayerOffsetsMinutes.getValue(prayer)
        return copy(prayerOffsetsMinutes = prayerOffsetsMinutes + (prayer to (current + delta).coerceIn(-120, 120)))
    }

    fun withIqamah(prayer: String, delta: Int): AdminOperationalDraft {
        val current = iqamahMinutes.getValue(prayer)
        return copy(iqamahMinutes = iqamahMinutes + (prayer to (current + delta).coerceIn(0, 180)))
    }
}

sealed interface ConfigSaveState {
    data object Idle : ConfigSaveState
    data object Sending : ConfigSaveState
    data object Saved : ConfigSaveState
    data class Error(val message: String) : ConfigSaveState
}
