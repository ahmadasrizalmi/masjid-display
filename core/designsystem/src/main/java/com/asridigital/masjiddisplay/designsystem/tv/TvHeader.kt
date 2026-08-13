package com.asridigital.masjiddisplay.designsystem.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.asridigital.masjiddisplay.designsystem.MasjidDisplayColors
import com.asridigital.masjiddisplay.designsystem.TvDimensions
import com.asridigital.masjiddisplay.designsystem.TvTypography

/**
 * Header untuk NORMAL / HORIZONTAL_MEDIA TV layout.
 *
 * Pure presentation component: waktu dan tanggal sudah diformat oleh caller.
 * Tidak membaca clock, database, network, atau prayer domain.
 */
@Composable
fun TvHeader(
    currentTime: String,
    mosqueName: String,
    location: String,
    gregorianDate: String,
    hijriDate: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(TvDimensions.HeaderHeight)
            .background(MasjidDisplayColors.Surface)
            .padding(
                horizontal = TvDimensions.HeaderHorizontalPadding,
                vertical = TvDimensions.HeaderVerticalPadding,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = currentTime,
            modifier = Modifier.widthIn(min = 320.dp),
            color = MasjidDisplayColors.TextPrimary,
            fontSize = TvTypography.Clock,
            fontWeight = FontWeight.Bold,
            style = TextStyle(fontFeatureSettings = "tnum"),
            maxLines = 1,
        )

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = mosqueName,
                color = MasjidDisplayColors.TextPrimary,
                fontSize = TvTypography.MosqueName,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            Text(
                text = location,
                color = MasjidDisplayColors.TextSecondary,
                fontSize = TvTypography.Location,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }

        Column(
            modifier = Modifier.widthIn(min = 360.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = gregorianDate,
                color = MasjidDisplayColors.TextPrimary,
                fontSize = TvTypography.DatePrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
            )
            Text(
                text = hijriDate,
                color = MasjidDisplayColors.TextSecondary,
                fontSize = TvTypography.DateSecondary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Preview(widthDp = 1920, heightDp = 1080, showBackground = true)
@Composable
private fun TvHeader1080Preview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MasjidDisplayColors.Background),
    ) {
        TvHeader(
            currentTime = "13:48",
            mosqueName = "MASJID NURUL HIKMAH",
            location = "Sleman, Daerah Istimewa Yogyakarta",
            gregorianDate = "Kamis, 13 Agustus 2026",
            hijriDate = "29 Safar 1448 H",
        )
    }
}
