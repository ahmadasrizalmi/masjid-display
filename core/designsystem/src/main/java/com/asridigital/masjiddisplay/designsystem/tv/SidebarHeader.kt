package com.asridigital.masjiddisplay.designsystem.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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

/** Header row dedicated to the SIDEBAR_MEDIA layout. */
@Composable
fun SidebarHeader(
    currentTime: String,
    mosqueName: String,
    gregorianDate: String,
    hijriDate: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(TvDimensions.PrayerSidebarHeaderHeight)
            .background(MasjidDisplayColors.Surface),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = currentTime,
            modifier = Modifier
                .weight(TvDimensions.PrayerSidebarWidth.value)
                .padding(horizontal = TvDimensions.PrayerSidebarRowHorizontalPadding),
            color = MasjidDisplayColors.TextPrimary,
            fontSize = TvTypography.SidebarClock,
            fontWeight = FontWeight.Bold,
            style = TextStyle(fontFeatureSettings = "tnum"),
            maxLines = 1,
        )

        Row(
            modifier = Modifier
                .weight(1920f - TvDimensions.PrayerSidebarWidth.value)
                .padding(horizontal = TvDimensions.HeaderHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = mosqueName,
                modifier = Modifier.weight(1f),
                color = MasjidDisplayColors.TextPrimary,
                fontSize = TvTypography.MosqueName,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = gregorianDate,
                    color = MasjidDisplayColors.TextPrimary,
                    fontSize = TvTypography.DatePrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    textAlign = TextAlign.End,
                )
                Text(
                    text = hijriDate,
                    color = MasjidDisplayColors.TextSecondary,
                    fontSize = TvTypography.DateSecondary,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

@Preview(widthDp = 1920, heightDp = 180, showBackground = true)
@Composable
private fun SidebarHeaderPreview() {
    SidebarHeader(
        currentTime = "13:48",
        mosqueName = "MASJID NURUL HIKMAH",
        gregorianDate = "Kamis, 13 Agustus 2026",
        hijriDate = "29 Safar 1448 H",
    )
}
