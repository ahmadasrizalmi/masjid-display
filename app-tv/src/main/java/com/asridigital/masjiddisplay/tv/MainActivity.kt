package com.asridigital.masjiddisplay.tv

import android.content.Context
import android.net.nsd.NsdManager
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.room.Room
import com.asridigital.masjiddisplay.database.ConfigRepository
import com.asridigital.masjiddisplay.database.MasjidDisplayDatabase
import com.asridigital.masjiddisplay.designsystem.MasjidDisplayColors
import com.asridigital.masjiddisplay.protocol.PairingChallenge
import com.asridigital.masjiddisplay.tv.pairing.TvPairingRuntimeOwner

class MainActivity : ComponentActivity() {
    private lateinit var database: MasjidDisplayDatabase
    private lateinit var configRepository: ConfigRepository
    private lateinit var pairingRuntime: TvPairingRuntimeOwner
    private var pairingChallenge by mutableStateOf<PairingChallenge?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = Room.databaseBuilder(
            applicationContext,
            MasjidDisplayDatabase::class.java,
            "masjid-display.db",
        ).build()
        configRepository = ConfigRepository(database.mosqueConfigDao())

        val trustedCredentials = getSharedPreferences(TRUSTED_CREDENTIALS_PREFS, Context.MODE_PRIVATE)
        pairingRuntime = TvPairingRuntimeOwner(
            nsdManager = getSystemService(NsdManager::class.java),
            configRepository = configRepository,
            onCredentialIssued = { credentialId ->
                trustedCredentials.edit().putBoolean(credentialId, true).apply()
            },
            isCredentialTrusted = { credentialId -> trustedCredentials.getBoolean(credentialId, false) },
        )
        setContent { TvRuntimeRoot(pairingChallenge, configRepository) }
    }

    override fun onStart() {
        super.onStart()
        pairingChallenge = pairingRuntime.start()
    }

    override fun onStop() {
        pairingRuntime.close()
        pairingChallenge = null
        super.onStop()
    }

    override fun onDestroy() {
        database.close()
        super.onDestroy()
    }

    private companion object {
        const val TRUSTED_CREDENTIALS_PREFS = "trusted-admin-credentials"
    }
}

/** No bootstrap mosque fixture: Room is the TV operational source of truth. */
@Composable
private fun TvRuntimeRoot(pairingChallenge: PairingChallenge?, repository: ConfigRepository) {
    val controller = remember(repository) { TvAppController(repository) }
    val state by controller.state.collectAsState(initial = TvAppState.Unconfigured)

    when (val current = state) {
        TvAppState.Unconfigured -> UnconfiguredScreen(pairingChallenge)
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
private fun UnconfiguredScreen(pairingChallenge: PairingChallenge?) {
    Box(
        modifier = Modifier.fillMaxSize().background(MasjidDisplayColors.Background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = buildString {
                append("MASJID DISPLAY\nBelum dikonfigurasi\nHubungkan aplikasi Admin untuk memulai")
                pairingChallenge?.let { append("\n\nKode pairing: ${it.secretForQrOrFallback}") }
            },
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
