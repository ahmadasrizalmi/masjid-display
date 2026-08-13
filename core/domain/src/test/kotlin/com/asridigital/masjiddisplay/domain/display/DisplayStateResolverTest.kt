package com.asridigital.masjiddisplay.domain.display

import com.asridigital.masjiddisplay.domain.prayer.DailyPrayerSchedule
import com.asridigital.masjiddisplay.domain.prayer.PrayerName
import com.asridigital.masjiddisplay.domain.prayer.PrayerTimes
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DisplayStateResolverTest {
    private val zone = ZoneId.of("Asia/Jakarta")
    private val date = LocalDate.of(2026, 8, 13) // Thursday
    private val times = PrayerTimes(
        fajr = LocalTime.of(4, 30), sunrise = LocalTime.of(5, 45), dhuhr = LocalTime.of(11, 45),
        asr = LocalTime.of(15, 5), maghrib = LocalTime.of(17, 40), isha = LocalTime.of(18, 52),
    )
    private val schedule = DailyPrayerSchedule(date, zone, times, times)
    private val config = DisplayRuntimeConfig(
        approachingThreshold = Duration.ofMinutes(10),
        adhanDisplayDuration = Duration.ofMinutes(3),
        prayerScreenDuration = Duration.ofMinutes(10),
        iqamahMinutes = mapOf(PrayerName.MAGHRIB to 10),
    )

    private fun at(hour: Int, minute: Int, second: Int = 0) =
        ZonedDateTime.of(date, LocalTime.of(hour, minute, second), zone)

    @Test fun secondBeforeApproachingIsNormal() {
        assertIs<DisplayState.Normal>(resolveDisplayState(at(17, 29, 59), schedule, config))
    }

    @Test fun exactApproachingBoundaryStartsApproaching() {
        val state = assertIs<DisplayState.ApproachingPrayer>(resolveDisplayState(at(17, 30), schedule, config))
        assertEquals(Duration.ofMinutes(10), state.remaining)
    }

    @Test fun exactPrayerTimeStartsAdhan() {
        assertIs<DisplayState.Adhan>(resolveDisplayState(at(17, 40), schedule, config))
    }

    @Test fun secondBeforeAdhanEndRemainsAdhan() {
        assertIs<DisplayState.Adhan>(resolveDisplayState(at(17, 42, 59), schedule, config))
    }

    @Test fun exactAdhanEndStartsIqamahCountdown() {
        val state = assertIs<DisplayState.IqamahCountdown>(resolveDisplayState(at(17, 43), schedule, config))
        assertEquals(Duration.ofMinutes(7), state.remaining)
    }

    @Test fun exactIqamahTargetStartsPrayer() {
        assertIs<DisplayState.Prayer>(resolveDisplayState(at(17, 50), schedule, config))
    }

    @Test fun exactPrayerScreenEndReturnsNormal() {
        assertIs<DisplayState.Normal>(resolveDisplayState(at(18, 0), schedule, config))
    }

    @Test fun zeroIqamahSkipsCountdown() {
        val noIqamah = config.copy(iqamahMinutes = emptyMap())
        assertIs<DisplayState.Prayer>(resolveDisplayState(at(17, 43), schedule, noIqamah))
    }

    @Test fun mismatchScheduleReturnsActionableErrorState() {
        val tomorrow = at(10, 0).plusDays(1)
        assertIs<DisplayState.Error>(resolveDisplayState(tomorrow, schedule, config))
    }

    @Test fun fridayWindowHasPriority() {
        val fridayDate = LocalDate.of(2026, 8, 14)
        val fridaySchedule = schedule.copy(date = fridayDate)
        val now = ZonedDateTime.of(fridayDate, LocalTime.NOON, zone)
        assertEquals(DisplayState.Friday, resolveDisplayState(now, fridaySchedule, config))
    }
}
