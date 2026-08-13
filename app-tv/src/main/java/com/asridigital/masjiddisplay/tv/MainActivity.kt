package com.asridigital.masjiddisplay.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.room.Room
import com.asridigital.masjiddisplay.database.ConfigRepository
import com.asridigital.masjiddisplay.database.MasjidDisplayDatabase
import com.asridigital.masjiddisplay.designsystem.MasjidDisplayColors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TvRuntimeRoot() }
    }
}

/** No bootstrap mosque fixture: Room is the TV operational source of truth. */
@Composable
private fun TvRuntimeRoot() {
    val database = remember {
        Room.databaseBuilder(
            androidx.compose.ui.platform.LocalContext.current.applicationContext,
            MasjidDisplayDatabase::class.java,
            "masjid-display.db",
        ).build()
    }
    val controller = remember(database) {
        TvAppController(ConfigRepository(database.mosqueConfigDao()))
    }
    val state by controller.state.collectAsState(initial = TvAppState.Unconfigured)

    when (val current = state) {
        TvAppState.Unconfigured -> UnconfiguredScreen()
        is TvAppState.ConfigurationError -> ConfigurationErrorScreen(current.reason)
        is TvAppState.Running -> TvDisplayScreen(
            state = current.snapshot.state,
            normalContent = current.snapshot.normalContent,
            normalLayoutMode = current.snapshot.layoutMode,
        ) { _ ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MasjidDisplayColors.SurfaceMuted),
            )
        }
    }
}

@Composable
private fun UnconfiguredScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(MasjidDisplayColors.Background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "MASJID DISPLAY\nBelum dikonfigurasi\nHubungkan aplikasi Admin untuk memulai",
            color = MasjidDisplayColors.TextPrimary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ConfigurationErrorScreen(reason: String) {
    Box(
        modifier = Modifier.fillMaxSize().background(MasjidDisplayColors.Background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Konfigurasi display perlu diperiksa\n$reason",
            color = MasjidDisplayColors.Error,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
