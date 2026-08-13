package com.asridigital.masjiddisplay.tv

import com.asridigital.masjiddisplay.domain.display.DisplayRuntimeConfig
import com.asridigital.masjiddisplay.domain.display.DisplayState
import com.asridigital.masjiddisplay.domain.prayer.PrayerCalculationConfig
import com.asridigital.masjiddisplay.domain.prayer.PrayerCalculationMethod
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TvRuntimeTest {
    private val zone = ZoneId.of("Asia/Jakarta")
    private val config = TvRuntimeConfig(
        mosqueName = "Masjid Test",
        locationLabel = "Yogyakarta",
        calculation = PrayerCalculationConfig(
            latitude = -7.7956,
            longitude = 110.3695,
            zoneId = zone,
            method = PrayerCalculationMethod.KEMENAG_INDONESIA,
        ),
        display = DisplayRuntimeConfig(),
    )

    @Test
    fun snapshotUsesMosqueTimezoneInsteadOfClockZone() {
        val runtime = TvRuntime(
            config = config,
            clock = Clock.fixed(Instant.parse("2026-08-13T06:48:00Z"), ZoneId.of("UTC")),
        )
        val snapshot = runtime.snapshot()

        assertEquals(zone, snapshot.now.zone)
        assertEquals("13:48", snapshot.normalContent.currentTime)
        assertEquals("Masjid Test", snapshot.normalContent.mosqueName)
        assertEquals(6, snapshot.normalContent.prayers.size)
    }

    @Test
    fun repeatedSnapshotWithFixedClockIsDeterministic() {
        val runtime = TvRuntime(
            config = config,
            clock = Clock.fixed(Instant.parse("2026-08-13T06:48:00Z"), ZoneId.of("UTC")),
        )
        assertEquals(runtime.snapshot(), runtime.snapshot())
    }

    @Test
    fun normalPrayerListKeepsSyuruqInformationalOnly() {
        val runtime = TvRuntime(
            config = config,
            clock = Clock.fixed(Instant.parse("2026-08-13T06:48:00Z"), ZoneId.of("UTC")),
        )
        val snapshot = runtime.snapshot()

        assertEquals(listOf("SUBUH", "SYURUQ", "DZUHUR", "ASHAR", "MAGHRIB", "ISYA"), snapshot.normalContent.prayers.map { it.name })
        assertTrue(snapshot.normalContent.prayers.count { it.isHighlighted } <= 1)
        assertTrue(snapshot.state !is DisplayState.Error)
    }
}
