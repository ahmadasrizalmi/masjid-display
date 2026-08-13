package com.asridigital.masjiddisplay.domain.display

import com.asridigital.masjiddisplay.domain.prayer.DailyPrayerSchedule
import com.asridigital.masjiddisplay.domain.prayer.PrayerName
import java.time.DayOfWeek
import java.time.Duration
import java.time.ZonedDateTime

private val orderedPrayers = PrayerName.entries

fun resolveDisplayState(
    now: ZonedDateTime,
    schedule: DailyPrayerSchedule,
    config: DisplayRuntimeConfig,
): DisplayState {
    if (now.zone != schedule.zoneId || now.toLocalDate() != schedule.date) {
        return DisplayState.Error("Schedule date/timezone does not match mosque local time")
    }

    if (config.friday.enabled && now.dayOfWeek == DayOfWeek.FRIDAY) {
        val localTime = now.toLocalTime()
        if (!localTime.isBefore(config.friday.start) && localTime.isBefore(config.friday.end)) {
            return DisplayState.Friday
        }
    }

    // Evaluate current-day worship windows from latest prayer backwards so overlapping configured
    // durations resolve to the most recent prayer deterministically.
    for (prayer in orderedPrayers.asReversed()) {
        val prayerAt = schedule.correctedAt(prayer)
        if (now.isBefore(prayerAt)) continue

        val adhanEnd = prayerAt.plus(config.adhanDisplayDuration)
        if (now.isBefore(adhanEnd)) return DisplayState.Adhan(prayer, prayerAt)

        val iqamahMinutes = config.iqamahMinutes[prayer] ?: 0
        val iqamahTarget = prayerAt.plusMinutes(iqamahMinutes.toLong())
        if (iqamahMinutes > 0 && now.isBefore(iqamahTarget)) {
            return DisplayState.IqamahCountdown(
                prayer = prayer,
                target = iqamahTarget,
                remaining = Duration.between(now, iqamahTarget),
            )
        }

        val prayerStart = if (iqamahMinutes > 0) iqamahTarget else adhanEnd
        val prayerEnd = prayerStart.plus(config.prayerScreenDuration)
        if (!now.isBefore(prayerStart) && now.isBefore(prayerEnd)) {
            return DisplayState.Prayer(prayer)
        }
    }

    val nextPrayer = orderedPrayers.firstOrNull { schedule.correctedAt(it).isAfter(now) }
    if (nextPrayer != null) {
        val prayerAt = schedule.correctedAt(nextPrayer)
        val approachingAt = prayerAt.minus(config.approachingThreshold)
        if (!now.isBefore(approachingAt)) {
            return DisplayState.ApproachingPrayer(
                prayer = nextPrayer,
                prayerAt = prayerAt,
                remaining = Duration.between(now, prayerAt),
            )
        }
    }

    return DisplayState.Normal(nextPrayer)
}
