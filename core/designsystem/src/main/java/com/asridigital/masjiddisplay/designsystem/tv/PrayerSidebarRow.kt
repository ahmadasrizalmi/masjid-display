package com.asridigital.masjiddisplay.designsystem.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
 * One fixed/equal prayer row for the SIDEBAR_MEDIA family.
 *
 * The parent owns equal row distribution. This component guarantees the SSOT minimum baseline,
 * stable active-state geometry, left-aligned label/context, right-aligned tabular prayer time,
 * and optional countdown only for the relevant row.
 */
@Composable
fun PrayerSidebarRow(
    prayerName: String,
    prayerTime: String,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
    countdown: String? = null,
) {
    val background = if (isHighlighted) MasjidDisplayColors.SurfaceMuted else MasjidDisplayColors.Surface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = TvDimensions.PrayerSidebarRowMinHeight)
            .background(background),
    ) {
        if (isHighlighted) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .width(TvDimensions.PrayerSidebarActiveIndicatorWidth)
                    .background(MasjidDisplayColors.Accent),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = TvDimensions.PrayerSidebarRowHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = prayerName,
                    color = if (isHighlighted) MasjidDisplayColors.Accent else MasjidDisplayColors.TextPrimary,
                    fontSize = TvTypography.PrayerSidebarLabel,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (countdown != null) {
                    Text(
                        text = countdown,
                        color = if (isHighlighted) MasjidDisplayColors.Accent else MasjidDisplayColors.TextSecondary,
                        fontSize = TvTypography.PrayerSidebarCountdown,
                        fontWeight = FontWeight.Medium,
                        style = TextStyle(fontFeatureSettings = "tnum"),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.width(TvDimensions.PrayerSidebarRowHorizontalPadding))

            Text(
                text = prayerTime,
                color = MasjidDisplayColors.TextPrimary,
                fontSize = TvTypography.PrayerSidebarTime,
                fontWeight = FontWeight.Bold,
                style = TextStyle(fontFeatureSettings = "tnum"),
                maxLines = 1,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Preview(widthDp = 430, heightDp = 266, showBackground = true)
@Composable
private fun PrayerSidebarRowStatesPreview() {
    Column {
        PrayerSidebarRow(
            prayerName = "SUBUH",
            prayerTime = "04:32",
        )
        PrayerSidebarRow(
            prayerName = "DZUHUR",
            prayerTime = "11:46",
            isHighlighted = true,
            countdown = "dalam 01:18",
        )
    }
}
