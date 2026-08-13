package com.asridigital.masjiddisplay.designsystem.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.asridigital.masjiddisplay.designsystem.MasjidDisplayColors
import com.asridigital.masjiddisplay.designsystem.TvDimensions
import com.asridigital.masjiddisplay.designsystem.TvTypography

/**
 * Full-width announcement/information bar for NORMAL TV layouts.
 *
 * This component only renders one prepared message. Rotation, marquee/ticker movement, scheduling,
 * priority and announcement selection are runtime concerns and intentionally live outside it.
 */
@Composable
fun InformationBar(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(TvDimensions.InformationBarHeight)
            .background(MasjidDisplayColors.Surface)
            .padding(horizontal = TvDimensions.InformationBarHorizontalPadding),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = message,
            color = MasjidDisplayColors.TextPrimary,
            fontSize = TvTypography.InformationBar,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(widthDp = 1920, heightDp = 100, showBackground = true)
@Composable
private fun InformationBarPreview() {
    InformationBar(
        message = "Kajian Ahad pagi dimulai pukul 07:00. Jamaah dipersilakan hadir bersama keluarga.",
    )
}
