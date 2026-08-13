package com.asridigital.masjiddisplay.designsystem.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import com.asridigital.masjiddisplay.designsystem.MasjidDisplayColors
import com.asridigital.masjiddisplay.designsystem.TvDimensions

/**
 * SSOT NORMAL / SIDEBAR_MEDIA composition for the 1920x1080 baseline.
 * Header 180dp, content 800dp, bottom information bar 100dp.
 */
@Composable
fun NormalSidebarMediaLayout(
    currentTime: String,
    mosqueName: String,
    gregorianDate: String,
    hijriDate: String,
    prayers: List<PrayerBarItem>,
    informationMessage: String,
    modifier: Modifier = Modifier,
    mediaContent: @Composable BoxScope.(ContentScale) -> Unit,
) {
    require(prayers.size == 6) { "NormalSidebarMediaLayout requires exactly 6 prayer items" }
    require(prayers.count { it.isHighlighted } <= 1) { "Only one sidebar prayer may be highlighted" }
    require(prayers.none { it.countdown != null && !it.isHighlighted }) {
        "Sidebar countdown may only be shown on the highlighted prayer"
    }

    Column(modifier = modifier.fillMaxSize()) {
        SidebarHeader(
            currentTime = currentTime,
            mosqueName = mosqueName,
            gregorianDate = gregorianDate,
            hijriDate = hijriDate,
        )

        Row(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .width(TvDimensions.PrayerSidebarWidth)
                    .background(MasjidDisplayColors.Surface),
            ) {
                prayers.forEachIndexed { index, prayer ->
                    // Weight distributes the available 800dp evenly after 5 fixed 1dp dividers.
                    // PrayerSidebarRow fills that allocation while preserving stable active geometry.
                    PrayerSidebarRow(
                        prayerName = prayer.name,
                        prayerTime = prayer.time,
                        isHighlighted = prayer.isHighlighted,
                        countdown = prayer.countdown,
                        modifier = Modifier.weight(1f),
                    )
                    if (index != prayers.lastIndex) {
                        Box(
                            modifier = Modifier
                                .height(TvDimensions.PrayerSidebarDividerHeight)
                                .width(TvDimensions.PrayerSidebarWidth)
                                .background(MasjidDisplayColors.Divider),
                        )
                    }
                }
            }

            MediaSurface(
                modifier = Modifier.weight(1f),
                content = mediaContent,
            )
        }

        InformationBar(message = informationMessage)
    }
}

@Preview(widthDp = 1920, heightDp = 1080, showBackground = true)
@Composable
private fun NormalSidebarMediaLayoutPreview() {
    NormalSidebarMediaLayout(
        currentTime = "13:48",
        mosqueName = "MASJID NURUL HIKMAH",
        gregorianDate = "Kamis, 13 Agustus 2026",
        hijriDate = "29 Safar 1448 H",
        prayers = listOf(
            PrayerBarItem("SUBUH", "04:32"),
            PrayerBarItem("SYURUQ", "05:47"),
            PrayerBarItem("DZUHUR", "11:46", isHighlighted = true, countdown = "01:18"),
            PrayerBarItem("ASHAR", "15:07"),
            PrayerBarItem("MAGHRIB", "17:42"),
            PrayerBarItem("ISYA", "18:54"),
        ),
        informationMessage = "Kajian Ahad pagi dimulai pukul 07:00. Jamaah dipersilakan hadir bersama keluarga.",
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MasjidDisplayColors.SurfaceMuted),
        )
    }
}
