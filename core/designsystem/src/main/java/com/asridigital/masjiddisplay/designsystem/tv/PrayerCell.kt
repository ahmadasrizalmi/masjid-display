package com.asridigital.masjiddisplay.designsystem.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.asridigital.masjiddisplay.designsystem.MasjidDisplayColors
import com.asridigital.masjiddisplay.designsystem.TvDimensions
import com.asridigital.masjiddisplay.designsystem.TvTypography

/**
 * Satu schedule cell untuk prayer bar HORIZONTAL_MEDIA.
 *
 * Contract SSOT:
 * - ukuran selalu tetap; active state tidak menyebabkan layout jump;
 * - nama berada di atas dan waktu besar di bawah;
 * - countdown hanya ditampilkan pada cell yang relevan;
 * - semua nilai waktu memakai tabular numerals;
 * - active/next memakai satu accent treatment, tanpa heavy card/border.
 */
@Composable
fun PrayerCell(
    prayerName: String,
    prayerTime: String,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
    countdown: String? = null,
) {
    val background = if (isHighlighted) {
        MasjidDisplayColors.SurfaceMuted
    } else {
        MasjidDisplayColors.Surface
    }

    Box(
        modifier = modifier
            .size(
                width = TvDimensions.PrayerCellWidth,
                height = TvDimensions.PrayerCellHeight,
            )
            .background(background),
    ) {
        if (isHighlighted) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(TvDimensions.ActiveIndicatorHeight)
                    .background(MasjidDisplayColors.Accent),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(
                    horizontal = TvDimensions.PrayerCellHorizontalPadding,
                    vertical = TvDimensions.PrayerCellVerticalPadding,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = prayerName,
                color = if (isHighlighted) MasjidDisplayColors.Accent else MasjidDisplayColors.TextSecondary,
                fontSize = TvTypography.PrayerLabel,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            Text(
                text = prayerTime,
                color = MasjidDisplayColors.TextPrimary,
                fontSize = TvTypography.PrayerTime,
                fontWeight = FontWeight.Bold,
                style = TextStyle(fontFeatureSettings = "tnum"),
                maxLines = 1,
                textAlign = TextAlign.Center,
            )
            if (countdown != null) {
                Text(
                    text = countdown,
                    color = if (isHighlighted) MasjidDisplayColors.Accent else MasjidDisplayColors.TextSecondary,
                    fontSize = TvTypography.PrayerCountdown,
                    fontWeight = FontWeight.Medium,
                    style = TextStyle(fontFeatureSettings = "tnum"),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(widthDp = 960, heightDp = 260, showBackground = true)
@Composable
private fun PrayerCellStatesPreview() {
    Row {
        PrayerCell(
            prayerName = "SUBUH",
            prayerTime = "04:32",
        )
        PrayerCell(
            prayerName = "DZUHUR",
            prayerTime = "11:46",
            isHighlighted = true,
            countdown = "dalam 01:18",
        )
        PrayerCell(
            prayerName = "ASHAR",
            prayerTime = "15:07",
        )
    }
}
