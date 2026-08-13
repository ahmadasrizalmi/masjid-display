package com.asridigital.masjiddisplay.prayer

import com.asridigital.masjiddisplay.domain.prayer.DailyPrayerSchedule
import com.asridigital.masjiddisplay.domain.prayer.PrayerCalculationConfig
import com.asridigital.masjiddisplay.domain.prayer.PrayerName
import com.asridigital.masjiddisplay.domain.prayer.PrayerTimes
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

/** Pure local calculator. It performs no network/API access and never reads device timezone. */
class LocalPrayerCalculator {
    fun calculate(date: LocalDate, config: PrayerCalculationConfig): DailyPrayerSchedule {
        val raw = calculateRaw(date, config)
        val corrected = PrayerTimes(
            fajr = raw.fajr.plusMinutes(config.offsetsMinutes[PrayerName.FAJR]?.toLong() ?: 0),
            sunrise = raw.sunrise,
            dhuhr = raw.dhuhr.plusMinutes(config.offsetsMinutes[PrayerName.DHUHR]?.toLong() ?: 0),
            asr = raw.asr.plusMinutes(config.offsetsMinutes[PrayerName.ASR]?.toLong() ?: 0),
            maghrib = raw.maghrib.plusMinutes(config.offsetsMinutes[PrayerName.MAGHRIB]?.toLong() ?: 0),
            isha = raw.isha.plusMinutes(config.offsetsMinutes[PrayerName.ISHA]?.toLong() ?: 0),
        )
        return DailyPrayerSchedule(date, config.zoneId, raw, corrected)
    }

    private fun calculateRaw(date: LocalDate, config: PrayerCalculationConfig): PrayerTimes {
        val day = date.dayOfYear
        val gamma = 2.0 * PI / 365.0 * (day - 1)
        val equationOfTime = 229.18 * (
            0.000075 + 0.001868 * cos(gamma) - 0.032077 * sin(gamma) -
                0.014615 * cos(2 * gamma) - 0.040849 * sin(2 * gamma)
            )
        val declination =
            0.006918 - 0.399912 * cos(gamma) + 0.070257 * sin(gamma) -
                0.006758 * cos(2 * gamma) + 0.000907 * sin(2 * gamma) -
                0.002697 * cos(3 * gamma) + 0.00148 * sin(3 * gamma)

        val zoneOffsetHours = config.zoneId.rules.getOffset(date.atStartOfDay()).totalSeconds / 3600.0
        val solarNoonMinutes = 720.0 - 4.0 * config.longitude - equationOfTime + zoneOffsetHours * 60.0
        val latitudeRad = Math.toRadians(config.latitude)

        fun timeForSolarAltitude(altitudeDegrees: Double, morning: Boolean): Double {
            val altitude = Math.toRadians(altitudeDegrees)
            val cosHourAngle = (sin(altitude) - sin(latitudeRad) * sin(declination)) /
                (cos(latitudeRad) * cos(declination))
            require(cosHourAngle in -1.0..1.0) {
                "Solar event is unavailable for this date/location"
            }
            val deltaMinutes = Math.toDegrees(acos(cosHourAngle)) * 4.0
            return solarNoonMinutes + if (morning) -deltaMinutes else deltaMinutes
        }

        val sunrise = timeForSolarAltitude(-0.833, morning = true)
        val sunset = timeForSolarAltitude(-0.833, morning = false)
        val fajr = timeForSolarAltitude(-config.method.fajrAngle, morning = true)
        val isha = config.method.ishaAngle?.let { timeForSolarAltitude(-it, morning = false) }
            ?: (sunset + requireNotNull(config.method.ishaIntervalMinutes))

        // Asr altitude: arccot(shadowFactor + tan(abs(latitude - declination))).
        val declinationDegrees = Math.toDegrees(declination)
        val angle = Math.toDegrees(
            atan(1.0 / (config.asrMethod.shadowFactor + tan(Math.toRadians(abs(config.latitude - declinationDegrees)))))
        )
        val asr = timeForSolarAltitude(angle, morning = false)

        return PrayerTimes(
            fajr = minutesToLocalTime(fajr),
            sunrise = minutesToLocalTime(sunrise),
            dhuhr = minutesToLocalTime(solarNoonMinutes),
            asr = minutesToLocalTime(asr),
            maghrib = minutesToLocalTime(sunset),
            isha = minutesToLocalTime(isha),
        )
    }

    private fun minutesToLocalTime(minutes: Double): LocalTime {
        val normalized = ((minutes % 1440.0) + 1440.0) % 1440.0
        val totalSeconds = floor(normalized * 60.0 + 0.5).toLong()
        return LocalTime.ofSecondOfDay(totalSeconds % 86_400)
    }
}
