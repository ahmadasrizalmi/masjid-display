package com.asridigital.masjiddisplay.designsystem.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.asridigital.masjiddisplay.designsystem.MasjidDisplayColors
import com.asridigital.masjiddisplay.designsystem.TvDimensions

/** Pure presentation model for one item inside [PrayerBar]. */
data class PrayerBarItem(
    val name: String,
    val time: String,
    val isHighlighted: Boolean = false,
    val countdown: String? = null,
)

/**
 * Full-width prayer schedule for HORIZONTAL_MEDIA.
 *
 * SSOT contract:
 * - exactly six equal-width cells: Subuh, Syuruq, Dzuhur, Ashar, Maghrib, Isya;
 * - fixed 190dp baseline height and no size change when highlighted;
 * - at most one highlighted/current-next cell;
 * - countdown is only rendered by the highlighted cell;
 * - presentation-only: no prayer calculation/state resolution lives here.
 */
@Composable
fun PrayerBar(
    prayers: List<PrayerBarItem>,
    modifier: Modifier = Modifier,
) {
    require(prayers.size == 6) { "PrayerBar requires exactly 6 prayer items" }
    require(prayers.count { it.isHighlighted } <= 1) { "PrayerBar supports at most one highlighted item" }
    require(prayers.none { it.countdown != null && !it.isHighlighted }) {
        "Countdown may only be shown on the highlighted prayer item"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(TvDimensions.PrayerBarHeight)
            .background(MasjidDisplayColors.Surface),
    ) {
        prayers.forEachIndexed { index, prayer ->
            PrayerCell(
                prayerName = prayer.name,
                prayerTime = prayer.time,
                isHighlighted = prayer.isHighlighted,
                countdown = prayer.countdown,
                modifier = Modifier.weight(1f),
            )

            if (index != prayers.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier
                        .height(TvDimensions.PrayerBarHeight)
                        .weight(0.002f),
                    color = MasjidDisplayColors.Divider,
                )
            }
        }
    }
}

@Preview(widthDp = 1920, heightDp = 190, showBackground = true)
@Composable
private fun PrayerBar1080Preview() {
    PrayerBar(
        prayers = listOf(
            PrayerBarItem("SUBUH", "04:32"),
            PrayerBarItem("SYURUQ", "05:47"),
            PrayerBarItem("DZUHUR", "11:46", isHighlighted = true, countdown = "dalam 01:18"),
            PrayerBarItem("ASHAR", "15:07"),
            PrayerBarItem("MAGHRIB", "17:42"),
            PrayerBarItem("ISYA", "18:54"),
        ),
    )
}
