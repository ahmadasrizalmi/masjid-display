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
 *
 * The caller owns state priority and wording. This component intentionally contains no timer,
 * prayer calculation, carousel, ticker, QRIS, media, database or networking behavior.
 */
@Composable
fun FocusStateContent(
    primaryLabel: String,
    modifier: Modifier = Modifier,
    stateLabel: String? = null,
    stateIcon: String? = null,
    contextTime: String? = null,
    heroValue: String? = null,
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
                Spacer(modifier = Modifier.height(TvDimensions.FocusSectionSpacing))
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
                Spacer(modifier = Modifier.height(TvDimensions.FocusSectionSpacing))
            }

            if (contextTime != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .width(TvDimensions.FocusAccentLineWidth)
                            .height(TvDimensions.FocusAccentLineHeight)
                            .background(MasjidDisplayColors.TvFocusAccent),
                    )
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
                    Box(
                        Modifier
                            .width(TvDimensions.FocusAccentLineWidth)
                            .height(TvDimensions.FocusAccentLineHeight)
                            .background(MasjidDisplayColors.TvFocusAccent),
                    )
                }
                Spacer(modifier = Modifier.height(TvDimensions.FocusSectionSpacing))
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
                Spacer(modifier = Modifier.height(TvDimensions.FocusSectionSpacing))
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

            if (secondaryMessage != null) {
                Spacer(modifier = Modifier.height(TvDimensions.FocusSectionSpacing))
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
