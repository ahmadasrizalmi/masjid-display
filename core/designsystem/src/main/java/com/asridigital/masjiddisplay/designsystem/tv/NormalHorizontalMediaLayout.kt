package com.asridigital.masjiddisplay.designsystem.tv

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.weight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview

/**
 * SSOT NORMAL / HORIZONTAL_MEDIA composition.
 *
 * Geometry at the 1920x1080 baseline:
 * - TvHeader: 170dp
 * - MediaSurface: remaining 720dp
 * - PrayerBar: 190dp
 *
 * Presentation only. Clock updates, media rotation and prayer-state resolution belong to runtime.
 */
@Composable
fun NormalHorizontalMediaLayout(
    currentTime: String,
    mosqueName: String,
    location: String,
    gregorianDate: String,
    hijriDate: String,
    prayers: List<PrayerBarItem>,
    modifier: Modifier = Modifier,
    mediaContent: @Composable BoxScope.(ContentScale) -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TvHeader(
            currentTime = currentTime,
            mosqueName = mosqueName,
            location = location,
            gregorianDate = gregorianDate,
            hijriDate = hijriDate,
        )

        MediaSurface(
            modifier = Modifier.weight(1f),
            content = mediaContent,
        )

        PrayerBar(prayers = prayers)
    }
}

@Preview(widthDp = 1920, heightDp = 1080, showBackground = true)
@Composable
private fun NormalHorizontalMediaLayoutPreview() {
    NormalHorizontalMediaLayout(
        currentTime = "13:48",
        mosqueName = "MASJID NURUL HIKMAH",
        location = "Sleman, Daerah Istimewa Yogyakarta",
        gregorianDate = "Kamis, 13 Agustus 2026",
        hijriDate = "29 Safar 1448 H",
        prayers = listOf(
            PrayerBarItem("SUBUH", "04:32"),
            PrayerBarItem("SYURUQ", "05:47"),
            PrayerBarItem("DZUHUR", "11:46", isHighlighted = true, countdown = "dalam 01:18"),
            PrayerBarItem("ASHAR", "15:07"),
            PrayerBarItem("MAGHRIB", "17:42"),
            PrayerBarItem("ISYA", "18:54"),
        ),
    ) { _ ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize(),
        )
    }
}
