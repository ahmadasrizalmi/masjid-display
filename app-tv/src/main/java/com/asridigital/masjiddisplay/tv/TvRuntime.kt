package com.asridigital.masjiddisplay.tv

import com.asridigital.masjiddisplay.designsystem.tv.PrayerBarItem
import com.asridigital.masjiddisplay.domain.display.DisplayRuntimeConfig
import com.asridigital.masjiddisplay.domain.display.DisplayState
import com.asridigital.masjiddisplay.domain.display.resolveDisplayState
import com.asridigital.masjiddisplay.domain.prayer.DailyPrayerSchedule
import com.asridigital.masjiddisplay.domain.prayer.PrayerCalculationConfig
import com.asridigital.masjiddisplay.domain.prayer.PrayerName
import com.asridigital.masjiddisplay.prayer.LocalPrayerCalculator
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** Runtime input that will later be supplied by Room. No Android or network dependency is required. */
data class TvRuntimeConfig(
    val mosqueName: String,
    val locationLabel: String,
    val calculation: PrayerCalculationConfig,
    val display: DisplayRuntimeConfig,
    val layoutMode: NormalLayoutMode = NormalLayoutMode.HorizontalMedia,
    val informationMessage: String = "",
)

data class TvRuntimeSnapshot(
    val now: ZonedDateTime,
    val schedule: DailyPrayerSchedule,
    val state: DisplayState,
    val normalContent: TvNormalContent,
    val layoutMode: NormalLayoutMode,
)

/**
 * Stateless coordinator for the TV vertical slice.
 * Every snapshot is derived from the supplied Clock + local config, making behavior deterministic
 * in tests and independent from wall-clock callbacks or a connected Admin phone.
 */
class TvRuntime(
    private val config: TvRuntimeConfig,
    private val calculator: LocalPrayerCalculator = LocalPrayerCalculator(),
    private val clock: Clock = Clock.systemUTC(),
) {
    private var cachedSchedule: DailyPrayerSchedule? = null

    fun snapshot(): TvRuntimeSnapshot {
        val now = ZonedDateTime.now(clock).withZoneSameInstant(config.calculation.zoneId)
        val schedule = scheduleFor(now.toLocalDate())
        val state = resolveDisplayState(now, schedule, config.display)
        return TvRuntimeSnapshot(
            now = now,
            schedule = schedule,
            state = state,
            normalContent = schedule.toNormalContent(now, state, config),
            layoutMode = config.layoutMode,
        )
    }

    private fun scheduleFor(date: LocalDate): DailyPrayerSchedule {
        val cached = cachedSchedule
        if (cached != null && cached.date == date && cached.zoneId == config.calculation.zoneId) return cached
        return calculator.calculate(date, config.calculation).also { cachedSchedule = it }
    }
}

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("id", "ID"))

private fun DailyPrayerSchedule.toNormalContent(
    now: ZonedDateTime,
    state: DisplayState,
    config: TvRuntimeConfig,
): TvNormalContent {
    val highlighted = when (state) {
        is DisplayState.Normal -> state.nextPrayer
        is DisplayState.ApproachingPrayer -> state.prayer
        else -> null
    }
    val countdown = (state as? DisplayState.ApproachingPrayer)?.remaining

    fun item(name: String, prayer: PrayerName) = PrayerBarItem(
        name = name,
        time = corrected[prayer].format(timeFormatter),
        isHighlighted = highlighted == prayer,
        countdown = if (highlighted == prayer) countdown?.asIndonesianCountdown() else null,
    )

    return TvNormalContent(
        currentTime = now.format(timeFormatter),
        mosqueName = config.mosqueName,
        location = config.locationLabel,
        gregorianDate = now.format(dateFormatter),
        // Hijri conversion is intentionally not fabricated here; persistence/config phase will
        // provide the canonical formatted value once its SSOT-backed source is implemented.
        hijriDate = "",
        prayers = listOf(
            item("SUBUH", PrayerName.FAJR),
            PrayerBarItem("SYURUQ", corrected.sunrise.format(timeFormatter)),
            item("DZUHUR", PrayerName.DHUHR),
            item("ASHAR", PrayerName.ASR),
            item("MAGHRIB", PrayerName.MAGHRIB),
            item("ISYA", PrayerName.ISHA),
        ),
        informationMessage = config.informationMessage,
    )
}

private fun Duration.asIndonesianCountdown(): String {
    val totalSeconds = seconds.coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val secondsPart = totalSeconds % 60
    return "dalam %02d:%02d".format(minutes, secondsPart)
}
