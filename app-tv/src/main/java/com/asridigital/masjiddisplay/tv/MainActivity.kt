package com.asridigital.masjiddisplay.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.asridigital.masjiddisplay.designsystem.MasjidDisplayColors
import com.asridigital.masjiddisplay.designsystem.tv.NormalHorizontalMediaLayout
import com.asridigital.masjiddisplay.designsystem.tv.PrayerBarItem

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NormalHorizontalMediaDevelopmentScreen() }
    }
}

@Composable
private fun NormalHorizontalMediaDevelopmentScreen() {
    NormalHorizontalMediaLayout(
        currentTime = "13:48",
        mosqueName = "MASJID NURUL HIKMAH",
        location = "Sleman, Daerah Istimewa Yogyakarta",
        gregorianDate = "Kamis, 13 Agustus 2026",
        hijriDate = "29 Safar 1448 H",
        prayers = listOf(
            PrayerBarItem("SUBUH", "04:32"),
            PrayerBarItem("SYURUQ", "05:47"),
            PrayerBarItem("DZUHUR", "11:46", isHighlighted = true, countdown = "dalam 01:18"),
            PrayerBarItem("ASHAR", "15:07"),
            PrayerBarItem("MAGHRIB", "17:42"),
            PrayerBarItem("ISYA", "18:54"),
        ),
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MasjidDisplayColors.SurfaceMuted),
        )
    }
}
