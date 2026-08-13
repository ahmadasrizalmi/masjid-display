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
import com.asridigital.masjiddisplay.designsystem.tv.TvHeader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TvHeaderDevelopmentScreen() }
    }
}

@Composable
private fun TvHeaderDevelopmentScreen() {
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
