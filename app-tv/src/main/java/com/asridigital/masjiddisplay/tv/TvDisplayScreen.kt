package com.asridigital.masjiddisplay.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import com.asridigital.masjiddisplay.designsystem.MasjidDisplayColors
import com.asridigital.masjiddisplay.designsystem.tv.AdhanScreen
import com.asridigital.masjiddisplay.designsystem.tv.ApproachingScreen
import com.asridigital.masjiddisplay.designsystem.tv.FridayScreen
import com.asridigital.masjiddisplay.designsystem.tv.IqamahScreen
import com.asridigital.masjiddisplay.designsystem.tv.NormalHorizontalMediaLayout
import com.asridigital.masjiddisplay.designsystem.tv.NormalSidebarMediaLayout
import com.asridigital.masjiddisplay.designsystem.tv.NoticeScreen
import com.asridigital.masjiddisplay.designsystem.tv.PrayerBarItem
import com.asridigital.masjiddisplay.designsystem.tv.PrayerScreen
import com.asridigital.masjiddisplay.domain.display.DisplayState
import java.time.Duration

sealed interface NormalLayoutMode {
    data object HorizontalMedia : NormalLayoutMode
    data object SidebarMedia : NormalLayoutMode
}

data class TvNormalContent(
    val currentTime: String,
    val mosqueName: String,
    val location: String,
    val gregorianDate: String,
    val hijriDate: String,
    val prayers: List<PrayerBarItem>,
    val informationMessage: String,
)

/**
 * Thin presentation mapper: domain DisplayState decides what state exists; this layer only chooses
 * the already-reviewed Compose screen. It contains no prayer timing/state-machine logic.
 */
@Composable
fun TvDisplayScreen(
    state: DisplayState,
    normalContent: TvNormalContent,
    normalLayoutMode: NormalLayoutMode,
    modifier: Modifier = Modifier,
    mediaContent: @Composable androidx.compose.foundation.layout.BoxScope.(ContentScale) -> Unit,
) {
    when (state) {
        is DisplayState.Normal -> when (normalLayoutMode) {
            NormalLayoutMode.HorizontalMedia -> NormalHorizontalMediaLayout(
                currentTime = normalContent.currentTime,
                mosqueName = normalContent.mosqueName,
                location = normalContent.location,
                gregorianDate = normalContent.gregorianDate,
                hijriDate = normalContent.hijriDate,
                prayers = normalContent.prayers,
                modifier = modifier,
                mediaContent = mediaContent,
            )
            NormalLayoutMode.SidebarMedia -> NormalSidebarMediaLayout(
                currentTime = normalContent.currentTime,
                mosqueName = normalContent.mosqueName,
                gregorianDate = normalContent.gregorianDate,
                hijriDate = normalContent.hijriDate,
                prayers = normalContent.prayers,
                informationMessage = normalContent.informationMessage,
                modifier = modifier,
                mediaContent = mediaContent,
            )
        }
        is DisplayState.ApproachingPrayer -> ApproachingScreen(
            prayerName = state.prayer.displayName(),
            countdown = state.remaining.asClock(),
            adhanTime = state.prayerAt.toLocalTime().toString().take(5),
            modifier = modifier,
        )
        is DisplayState.Adhan -> AdhanScreen(
            prayerName = state.prayer.displayName(),
            adhanTime = state.prayerAt.toLocalTime().toString().take(5),
            modifier = modifier,
        )
        is DisplayState.IqamahCountdown -> IqamahScreen(
            prayerName = state.prayer.displayName(),
            countdown = state.remaining.asClock(),
            modifier = modifier,
        )
        is DisplayState.Prayer -> PrayerScreen(
            prayerName = state.prayer.displayName(),
            modifier = modifier,
        )
        DisplayState.Friday -> FridayScreen(modifier = modifier)
        DisplayState.Information -> NoticeScreen(
            title = "PENGUMUMAN",
            message = normalContent.informationMessage,
            modifier = modifier,
        )
        is DisplayState.Error -> ErrorDisplayScreen(state.reason, modifier)
    }
}

@Composable
private fun ErrorDisplayScreen(reason: String, modifier: Modifier) {
    Box(
        modifier = modifier.fillMaxSize().background(MasjidDisplayColors.Background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Konfigurasi display perlu diperiksa\n$reason",
            color = MasjidDisplayColors.Error,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun com.asridigital.masjiddisplay.domain.prayer.PrayerName.displayName(): String = when (this) {
    com.asridigital.masjiddisplay.domain.prayer.PrayerName.FAJR -> "SUBUH"
    com.asridigital.masjiddisplay.domain.prayer.PrayerName.DHUHR -> "DZUHUR"
    com.asridigital.masjiddisplay.domain.prayer.PrayerName.ASR -> "ASHAR"
    com.asridigital.masjiddisplay.domain.prayer.PrayerName.MAGHRIB -> "MAGHRIB"
    com.asridigital.masjiddisplay.domain.prayer.PrayerName.ISHA -> "ISYA"
}

private fun Duration.asClock(): String {
    val safeSeconds = seconds.coerceAtLeast(0)
    val minutes = safeSeconds / 60
    val secondsPart = safeSeconds % 60
    return "%02d:%02d".format(minutes, secondsPart)
}
