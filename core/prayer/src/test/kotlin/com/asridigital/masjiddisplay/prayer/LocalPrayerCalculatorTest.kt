package com.asridigital.masjiddisplay.prayer

import com.asridigital.masjiddisplay.domain.prayer.PrayerCalculationConfig
import com.asridigital.masjiddisplay.domain.prayer.PrayerCalculationMethod
import com.asridigital.masjiddisplay.domain.prayer.PrayerName
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LocalPrayerCalculatorTest {
    private val calculator = LocalPrayerCalculator()
    private val date = LocalDate.of(2026, 8, 13)
    private val baseConfig = PrayerCalculationConfig(
        latitude = -7.7956,
        longitude = 110.3695,
        zoneId = ZoneId.of("Asia/Jakarta"),
        method = PrayerCalculationMethod.KEMENAG_INDONESIA,
    )

    @Test
    fun sameInputProducesSameSchedule() {
        assertEquals(calculator.calculate(date, baseConfig), calculator.calculate(date, baseConfig))
    }

    @Test
    fun scheduleIsChronologicalForYogyakartaFixture() {
        val times = calculator.calculate(date, baseConfig).raw
        assertTrue(times.fajr < times.sunrise)
        assertTrue(times.sunrise < times.dhuhr)
        assertTrue(times.dhuhr < times.asr)
        assertTrue(times.asr < times.maghrib)
        assertTrue(times.maghrib < times.isha)
    }

    @Test
    fun correctionOffsetsOnlyChangeCanonicalPrayerTimes() {
        val rawSchedule = calculator.calculate(date, baseConfig)
        val correctedSchedule = calculator.calculate(
            date,
            baseConfig.copy(offsetsMinutes = mapOf(PrayerName.MAGHRIB to 2, PrayerName.ISHA to -1)),
        )

        assertEquals(rawSchedule.raw, correctedSchedule.raw)
        assertEquals(rawSchedule.raw.maghrib.plusMinutes(2), correctedSchedule.corrected.maghrib)
        assertEquals(rawSchedule.raw.isha.minusMinutes(1), correctedSchedule.corrected.isha)
        assertEquals(rawSchedule.raw.sunrise, correctedSchedule.corrected.sunrise)
    }

    @Test
    fun scheduleKeepsExplicitMosqueTimezone() {
        val schedule = calculator.calculate(date, baseConfig)
        assertEquals(ZoneId.of("Asia/Jakarta"), schedule.zoneId)
        assertEquals(date, schedule.date)
    }
}
