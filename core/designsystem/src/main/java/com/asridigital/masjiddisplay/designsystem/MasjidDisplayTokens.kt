package com.asridigital.masjiddisplay.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object MasjidDisplayColors {
    val Background = Color(0xFFF6F7F5)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceMuted = Color(0xFFEEF1ED)
    val TextPrimary = Color(0xFF17201B)
    val TextSecondary = Color(0xFF68736D)
    val Divider = Color(0xFFDDE2DE)
    val Accent = Color(0xFF176B45)
    val OnAccent = Color(0xFFFFFFFF)
    val Warning = Color(0xFFD99518)
    val Error = Color(0xFFB3261E)
    val Success = Color(0xFF176B45)

    val TvFocusBg = Color(0xFFF4EFE4)
    val TvFocusSurface = Color(0xFFFFFDF8)
    val TvFocusText = Color(0xFF171A17)
    val TvFocusSecondary = Color(0xFF62665F)
    val TvFocusAccent = Color(0xFFB9842C)
    val TvFocusPattern = Color(0x12B9842C)
}

object TvDimensions {
    val HeaderHeight = 170.dp
    val HeaderHorizontalPadding = 96.dp
    val HeaderVerticalPadding = 24.dp

    val PrayerBarHeight = 190.dp
    val PrayerCellWidth = 320.dp
    val PrayerCellHeight = PrayerBarHeight
    val PrayerCellHorizontalPadding = 24.dp
    val PrayerCellVerticalPadding = 20.dp
    val PrayerCellDividerWidth = 1.dp
    val ActiveIndicatorHeight = 6.dp

    // 180 header + 6*133 prayer rows + 1px separators + 100 info bar fits 1080 baseline.
    val PrayerSidebarWidth = 430.dp
    val PrayerSidebarHeaderHeight = 180.dp
    val PrayerSidebarInfoBarHeight = 100.dp
    val PrayerSidebarRowHeight = 133.dp
    val PrayerSidebarRowHorizontalPadding = 28.dp
    val PrayerSidebarActiveIndicatorWidth = 6.dp
    val PrayerSidebarDividerHeight = 1.dp

    val FocusHorizontalSafeInset = 96.dp
    val FocusVerticalSafeInset = 54.dp
    val FocusContentMaxWidth = 1200.dp
    val FocusSectionSpacing = 28.dp
    val FocusAccentLineWidth = 120.dp
    val FocusAccentLineHeight = 2.dp
}

object TvTypography {
    val Clock = 120.sp
    val MosqueName = 44.sp
    val Location = 26.sp
    val DatePrimary = 30.sp
    val DateSecondary = 25.sp
    val PrayerLabel = 34.sp
    val PrayerTime = 48.sp
    val PrayerCountdown = 26.sp
    val PrayerSidebarLabel = 30.sp
    val PrayerSidebarTime = 40.sp
    val PrayerSidebarCountdown = 22.sp

    val FocusStateLabel = 30.sp
    val FocusPrimaryLabel = 72.sp
    val FocusHeroValue = 184.sp
    val FocusContextTime = 34.sp
    val FocusSecondary = 30.sp
    val FocusIcon = 56.sp
}
