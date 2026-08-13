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
}

object TvDimensions {
    // 1920x1080 SSOT baseline. Critical header text stays >= 5% from physical edge.
    val HeaderHeight = 170.dp
    val HeaderHorizontalPadding = 96.dp
    val HeaderVerticalPadding = 24.dp

    // HORIZONTAL_MEDIA prayer bar is 190px/dp high at the 1080p baseline.
    val PrayerBarHeight = 190.dp
    val PrayerCellWidth = 320.dp
    val PrayerCellHeight = PrayerBarHeight
    val PrayerCellHorizontalPadding = 24.dp
    val PrayerCellVerticalPadding = 20.dp
    val PrayerCellDividerWidth = 1.dp
    val ActiveIndicatorHeight = 6.dp

    // SIDEBAR_MEDIA baseline: 430px sidebar, 180px header, 100px info bar.
    // Remaining 800px is shared by six equal rows; the parent layout owns exact distribution.
    val PrayerSidebarWidth = 430.dp
    val PrayerSidebarHeaderHeight = 180.dp
    val PrayerSidebarInfoBarHeight = 100.dp
    val PrayerSidebarRowMinHeight = 133.dp
    val PrayerSidebarRowHorizontalPadding = 28.dp
    val PrayerSidebarActiveIndicatorWidth = 6.dp
    val PrayerSidebarDividerHeight = 1.dp
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
}
