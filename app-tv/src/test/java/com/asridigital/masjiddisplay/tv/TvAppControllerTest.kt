package com.asridigital.masjiddisplay.tv

import com.asridigital.masjiddisplay.database.PersistedTvConfig
import com.asridigital.masjiddisplay.domain.display.DisplayRuntimeConfig
import com.asridigital.masjiddisplay.domain.prayer.PrayerCalculationConfig
import com.asridigital.masjiddisplay.domain.prayer.PrayerCalculationMethod
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class TvAppControllerTest {
    private val zone = ZoneId.of("Asia/Jakarta")
    private val clock = Clock.fixed(Instant.parse("2026-08-13T05:00:00Z"), zone)

    @Test
    fun nullConfigEmitsUnconfigured() = runTest {
        val controller = TvAppController(flowOf(null), clock, Unit)
        assertEquals(TvAppState.Unconfigured, controller.state.first())
    }

    @Test
    fun validConfigEmitsRunningSnapshot() = runTest {
        val controller = TvAppController(flowOf(validConfig()), clock, Unit)
        val running = assertIs<TvAppState.Running>(controller.state.first())
        assertEquals("MASJID TEST", running.snapshot.normalContent.mosqueName)
        assertEquals(zone, running.snapshot.now.zone)
    }

    @Test
    fun unsupportedLayoutEmitsConfigurationErrorInsteadOfCrashingFlow() = runTest {
        val controller = TvAppController(
            flowOf(validConfig().copy(normalLayoutMode = "UNSUPPORTED_LAYOUT")),
            clock,
            Unit,
        )
        val error = assertIs<TvAppState.ConfigurationError>(controller.state.first())
        assertEquals("Unsupported layout mode: UNSUPPORTED_LAYOUT", error.reason)
    }

    private fun validConfig() = PersistedTvConfig(
        mosqueId = "masjid-test",
        mosqueName = "MASJID TEST",
        cityLabel = "Yogyakarta",
        calculation = PrayerCalculationConfig(
            latitude = -7.7956,
            longitude = 110.3695,
            zoneId = zone,
            method = PrayerCalculationMethod.KEMENAG_INDONESIA,
        ),
        display = DisplayRuntimeConfig(),
        normalLayoutMode = "HORIZONTAL_MEDIA",
        informationMessage = "Selamat datang",
        hijriAdjustmentDays = 0,
    )
}
