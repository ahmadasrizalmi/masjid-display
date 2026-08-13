package com.asridigital.masjiddisplay.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.asridigital.masjiddisplay.designsystem.MasjidDisplayColors
import com.asridigital.masjiddisplay.domain.display.DisplayRuntimeConfig
import com.asridigital.masjiddisplay.domain.prayer.PrayerCalculationConfig
import com.asridigital.masjiddisplay.domain.prayer.PrayerCalculationMethod
import kotlinx.coroutines.delay
import java.time.ZoneId

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TvRuntimeRoot() }
    }
}

/**
 * Runtime vertical slice. The bootstrap config is temporary until Phase 5 Room persistence replaces
 * it; prayer calculation/state resolution already runs locally and requires no Admin phone/network.
 */
@Composable
private fun TvRuntimeRoot() {
    val runtime = remember {
        TvRuntime(
            config = TvRuntimeConfig(
                mosqueName = "MASJID NURUL HIKMAH",
                locationLabel = "Sleman, Daerah Istimewa Yogyakarta",
                calculation = PrayerCalculationConfig(
                    latitude = -7.7956,
                    longitude = 110.3695,
                    zoneId = ZoneId.of("Asia/Jakarta"),
                    method = PrayerCalculationMethod.KEMENAG_INDONESIA,
                ),
                display = DisplayRuntimeConfig(),
                informationMessage = "Selamat datang. Jaga ketenangan dan kebersihan masjid.",
            ),
        )
    }

    var snapshot by remember { mutableStateOf(runtime.snapshot()) }
    LaunchedEffect(runtime) {
        while (true) {
            val millisToNextSecond = 1_000L - (System.currentTimeMillis() % 1_000L)
            delay(millisToNextSecond)
            snapshot = runtime.snapshot()
        }
    }

    TvDisplayScreen(
        state = snapshot.state,
        normalContent = snapshot.normalContent,
        normalLayoutMode = snapshot.layoutMode,
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MasjidDisplayColors.SurfaceMuted),
        )
    }
}
