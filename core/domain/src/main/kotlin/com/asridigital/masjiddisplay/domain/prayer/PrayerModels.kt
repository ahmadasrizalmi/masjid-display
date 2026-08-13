package com.asridigital.masjiddisplay.domain.prayer

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

enum class PrayerName {
    FAJR,
    DHUHR,
    ASR,
    MAGHRIB,
    ISHA,
}

data class PrayerTimes(
    val fajr: LocalTime,
    val sunrise: LocalTime,
    val dhuhr: LocalTime,
    val asr: LocalTime,
    val maghrib: LocalTime,
    val isha: LocalTime,
) {
    operator fun get(prayer: PrayerName): LocalTime = when (prayer) {
        PrayerName.FAJR -> fajr
        PrayerName.DHUHR -> dhuhr
        PrayerName.ASR -> asr
        PrayerName.MAGHRIB -> maghrib
        PrayerName.ISHA -> isha
    }
}

data class DailyPrayerSchedule(
    val date: LocalDate,
    val zoneId: ZoneId,
    val raw: PrayerTimes,
    val corrected: PrayerTimes,
) {
    fun correctedAt(prayer: PrayerName): ZonedDateTime = ZonedDateTime.of(date, corrected[prayer], zoneId)
    fun sunriseAt(): ZonedDateTime = ZonedDateTime.of(date, corrected.sunrise, zoneId)
}

data class PrayerCalculationConfig(
    val latitude: Double,
    val longitude: Double,
    val zoneId: ZoneId,
    val method: PrayerCalculationMethod,
    val asrMethod: AsrMethod = AsrMethod.STANDARD,
    val offsetsMinutes: Map<PrayerName, Int> = emptyMap(),
) {
    init {
        require(latitude in -90.0..90.0) { "Latitude must be within -90..90" }
        require(longitude in -180.0..180.0) { "Longitude must be within -180..180" }
        require(offsetsMinutes.values.all { it in -120..120 }) {
            "Prayer correction offsets must be within -120..120 minutes"
        }
    }
}

enum class AsrMethod(val shadowFactor: Int) {
    STANDARD(1),
    HANAFI(2),
}

data class PrayerCalculationMethod(
    val fajrAngle: Double,
    val ishaAngle: Double? = null,
    val ishaIntervalMinutes: Int? = null,
) {
    init {
        require(fajrAngle in 0.0..30.0)
        require(ishaAngle == null || ishaAngle in 0.0..30.0)
        require(ishaIntervalMinutes == null || ishaIntervalMinutes >= 0)
        require((ishaAngle == null) xor (ishaIntervalMinutes == null)) {
            "Configure exactly one Isha rule: angle or interval"
        }
    }

    companion object {
        val KEMENAG_INDONESIA = PrayerCalculationMethod(fajrAngle = 20.0, ishaAngle = 18.0)
    }
}
