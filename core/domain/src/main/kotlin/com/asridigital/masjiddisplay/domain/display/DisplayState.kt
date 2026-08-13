package com.asridigital.masjiddisplay.domain.display

import com.asridigital.masjiddisplay.domain.prayer.PrayerName
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime

sealed interface DisplayState {
    data class Normal(val nextPrayer: PrayerName?) : DisplayState
    data class ApproachingPrayer(
        val prayer: PrayerName,
        val prayerAt: ZonedDateTime,
        val remaining: Duration,
    ) : DisplayState
    data class Adhan(val prayer: PrayerName, val prayerAt: ZonedDateTime) : DisplayState
    data class IqamahCountdown(
        val prayer: PrayerName,
        val target: ZonedDateTime,
        val remaining: Duration,
    ) : DisplayState
    data class Prayer(val prayer: PrayerName) : DisplayState
    data object Friday : DisplayState
    data object Information : DisplayState
    data class Error(val reason: String) : DisplayState
}

data class DisplayRuntimeConfig(
    val approachingThreshold: Duration = Duration.ofMinutes(10),
    val adhanDisplayDuration: Duration = Duration.ofMinutes(3),
    val prayerScreenDuration: Duration = Duration.ofMinutes(10),
    val iqamahMinutes: Map<PrayerName, Int> = emptyMap(),
    val friday: FridayRuntimeConfig = FridayRuntimeConfig(),
) {
    init {
        require(!approachingThreshold.isNegative)
        require(!adhanDisplayDuration.isNegative && !adhanDisplayDuration.isZero)
        require(!prayerScreenDuration.isNegative && !prayerScreenDuration.isZero)
        require(iqamahMinutes.values.all { it in 0..180 })
    }
}

data class FridayRuntimeConfig(
    val enabled: Boolean = true,
    val start: LocalTime = LocalTime.of(11, 30),
    val end: LocalTime = LocalTime.of(13, 30),
) {
    init {
        require(end.isAfter(start)) { "Friday window end must be after start" }
    }
}
