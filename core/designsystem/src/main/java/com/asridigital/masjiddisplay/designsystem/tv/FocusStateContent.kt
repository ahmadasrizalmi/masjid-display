package com.asridigital.masjiddisplay.designsystem.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.asridigital.masjiddisplay.designsystem.MasjidDisplayColors
import com.asridigital.masjiddisplay.designsystem.TvDimensions
import com.asridigital.masjiddisplay.designsystem.TvTypography

/**
 * Shared presentation grammar for APPROACHING, ADHAN, IQAMAH, PRAYER, FRIDAY and NOTICE.
 * Caller owns state priority and wording. No timer/domain/network/media behavior lives here.
 */
@Composable
fun FocusStateContent(
    primaryLabel: String,
    modifier: Modifier = Modifier,
    stateLabel: String? = null,
    stateIcon: String? = null,
    heroValue: String? = null,
    contextTime: String? = null,
    secondaryMessage: String? = null,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MasjidDisplayColors.TvFocusBg)
            .padding(
                horizontal = TvDimensions.FocusHorizontalSafeInset,
                vertical = TvDimensions.FocusVerticalSafeInset,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = TvDimensions.FocusContentMaxWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (stateIcon != null) {
                Text(
                    text = stateIcon,
                    color = MasjidDisplayColors.TvFocusAccent,
                    fontSize = TvTypography.FocusIcon,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
                FocusSpacer()
            }

            if (stateLabel != null) {
                Text(
                    text = stateLabel,
                    color = MasjidDisplayColors.TvFocusSecondary,
                    fontSize = TvTypography.FocusStateLabel,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                FocusSpacer()
            }

            Text(
                text = primaryLabel,
                color = MasjidDisplayColors.TvFocusText,
                fontSize = TvTypography.FocusPrimaryLabel,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (heroValue != null) {
                FocusSpacer()
                Text(
                    text = heroValue,
                    color = MasjidDisplayColors.TvFocusText,
                    fontSize = TvTypography.FocusHeroValue,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(fontFeatureSettings = "tnum"),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }

            if (contextTime != null) {
                FocusSpacer()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FocusAccentLine()
                    Spacer(modifier = Modifier.width(TvDimensions.FocusSectionSpacing))
                    Text(
                        text = contextTime,
                        color = MasjidDisplayColors.TvFocusSecondary,
                        fontSize = TvTypography.FocusContextTime,
                        fontWeight = FontWeight.Medium,
                        style = TextStyle(fontFeatureSettings = "tnum"),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                    Spacer(modifier = Modifier.width(TvDimensions.FocusSectionSpacing))
                    FocusAccentLine()
                }
            }

            if (secondaryMessage != null) {
                FocusSpacer()
                Text(
                    text = secondaryMessage,
                    color = MasjidDisplayColors.TvFocusSecondary,
                    fontSize = TvTypography.FocusSecondary,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun FocusSpacer() = Spacer(modifier = Modifier.height(TvDimensions.FocusSectionSpacing))

@Composable
private fun FocusAccentLine() = Box(
    Modifier
        .width(TvDimensions.FocusAccentLineWidth)
        .height(TvDimensions.FocusAccentLineHeight)
        .background(MasjidDisplayColors.TvFocusAccent),
)

@Preview(widthDp = 1920, heightDp = 1080, showBackground = true)
@Composable
private fun ApproachingFocusPreview() {
    FocusStateContent(
        stateLabel = "SHOLAT BERIKUTNYA",
        primaryLabel = "MAGHRIB",
        heroValue = "09:54",
        contextTime = "Adzan · 17:42",
        secondaryMessage = "Bersiap untuk menunaikan sholat berjamaah",
    )
}

@Preview(widthDp = 1920, heightDp = 1080, showBackground = true)
@Composable
private fun AdhanFocusPreview() {
    FocusStateContent(
        stateIcon = "◇",
        primaryLabel = "MAGHRIB",
        heroValue = "17:42",
        secondaryMessage = "WAKTU ADZAN",
    )
}

@Preview(widthDp = 1920, heightDp = 1080, showBackground = true)
@Composable
private fun IqamahFocusPreview() {
    FocusStateContent(
        stateIcon = "◇",
        primaryLabel = "IQAMAH MAGHRIB",
        heroValue = "08:42",
        secondaryMessage = "Persiapkan dan rapatkan shaf",
    )
}

@Preview(widthDp = 1920, heightDp = 1080, showBackground = true)
@Composable
private fun PrayerFocusPreview() {
    FocusStateContent(
        stateIcon = "◇",
        primaryLabel = "SHOLAT MAGHRIB",
        secondaryMessage = "Luruskan dan rapatkan shaf",
    )
}

@Preview(widthDp = 1920, heightDp = 1080, showBackground = true)
@Composable
private fun FridayFocusPreview() {
    FocusStateContent(
        stateIcon = "◇",
        primaryLabel = "SHOLAT JUMAT",
        secondaryMessage = "Dengarkan khutbah dengan tenang",
    )
}

@Preview(widthDp = 1920, heightDp = 1080, showBackground = true)
@Composable
private fun NoticeFocusPreview() {
    FocusStateContent(
        stateIcon = "◇",
        primaryLabel = "SENYAPKAN PERANGKAT",
        secondaryMessage = "Mohon aktifkan mode senyap sebelum sholat dimulai",
    )
}
