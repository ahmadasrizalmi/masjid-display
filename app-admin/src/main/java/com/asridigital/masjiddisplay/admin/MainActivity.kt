package com.asridigital.masjiddisplay.admin

import android.content.Context
import android.net.nsd.NsdManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.asridigital.masjiddisplay.admin.config.AdminHomeScreen
import com.asridigital.masjiddisplay.admin.config.AdminOperationalDraft
import com.asridigital.masjiddisplay.admin.config.AdminPhase8Screen
import com.asridigital.masjiddisplay.admin.config.AnnouncementsScreen
import com.asridigital.masjiddisplay.admin.config.ConfigSaveState
import com.asridigital.masjiddisplay.admin.config.DeviceStatusScreen
import com.asridigital.masjiddisplay.admin.config.DisplayAppearanceScreen
import com.asridigital.masjiddisplay.admin.config.FridaySettingsScreen
import com.asridigital.masjiddisplay.admin.config.IqamahSettingsScreen
import com.asridigital.masjiddisplay.admin.config.LanConfigTransportClient
import com.asridigital.masjiddisplay.admin.config.MosqueInformationScreen
import com.asridigital.masjiddisplay.admin.config.PrayerSettingsScreen
import com.asridigital.masjiddisplay.admin.config.SetupReviewScreen
import com.asridigital.masjiddisplay.admin.discovery.AdminNsdDiscovery
import com.asridigital.masjiddisplay.admin.pairing.AdminPairingCoordinator
import com.asridigital.masjiddisplay.admin.pairing.AdminPairingRuntime
import com.asridigital.masjiddisplay.admin.pairing.AdminRuntimeState
import com.asridigital.masjiddisplay.admin.pairing.LanPairingTransportClient
import com.asridigital.masjiddisplay.admin.setup.MosqueSetupDraft
import com.asridigital.masjiddisplay.admin.setup.MosqueSetupScreen
import com.asridigital.masjiddisplay.protocol.DiscoveredTvService
import com.asridigital.masjiddisplay.protocol.ProtocolNegotiation
import com.asridigital.masjiddisplay.protocol.TvConfigUpdateResponse
import java.time.ZoneId
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private val AdminBackground = Color(0xFFF6F7F5)
private val AdminSurface = Color(0xFFFFFFFF)
private val AdminTextPrimary = Color(0xFF17201B)
private val AdminTextSecondary = Color(0xFF68736D)
private val AdminAccent = Color(0xFF176B45)
private val AdminOnAccent = Color(0xFFFFFFFF)

class MainActivity : ComponentActivity() {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val pairingExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val configClient = LanConfigTransportClient()
    private lateinit var coordinator: AdminPairingCoordinator
    private lateinit var mosqueId: String

    private var uiState by mutableStateOf<AdminRuntimeState>(AdminRuntimeState.Discovering)
    private var fallbackCode by mutableStateOf("")
    private var mosqueDraft by mutableStateOf(MosqueSetupDraft(timezoneId = ZoneId.systemDefault().id))
    private var setupSubmitted by mutableStateOf(false)
    private var operationalDraft by mutableStateOf<AdminOperationalDraft?>(null)
    private var workingDraft by mutableStateOf<AdminOperationalDraft?>(null)
    private var phase8Screen by mutableStateOf(AdminPhase8Screen.REVIEW)
    private var configSaveState by mutableStateOf<ConfigSaveState>(ConfigSaveState.Idle)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mosqueId = stableMosqueId()

        val runtime = AdminPairingRuntime(LanPairingTransportClient())
        val discovery = AdminNsdDiscovery(
            nsdManager = getSystemService(NsdManager::class.java),
            onServicesChanged = { services ->
                mainHandler.post {
                    if (::coordinator.isInitialized) coordinator.onServicesChanged(services)
                }
            },
            onFailure = { errorCode ->
                mainHandler.post {
                    if (::coordinator.isInitialized) coordinator.onDiscoveryFailure(errorCode)
                }
            },
        )
        coordinator = AdminPairingCoordinator(
            runtime = runtime,
            startDiscovery = discovery::start,
            stopDiscovery = discovery::stop,
            executePairing = { task -> pairingExecutor.execute { task() } },
            dispatchState = { task -> mainHandler.post { task() } },
            onStateChanged = { uiState = it },
        )

        setContent {
            val paired = uiState as? AdminRuntimeState.Paired
            when {
                paired == null -> AdminPairingScreen(
                    state = uiState,
                    fallbackCode = fallbackCode,
                    onFallbackCodeChanged = { fallbackCode = it },
                    onPair = { device -> coordinator.pair(device, fallbackCode) },
                    onRetry = coordinator::retryDiscovery,
                )

                operationalDraft == null && !setupSubmitted -> MosqueSetupScreen(
                    draft = mosqueDraft,
                    onDraftChanged = { mosqueDraft = it },
                    onContinue = { submitted ->
                        mosqueDraft = submitted
                        workingDraft = AdminOperationalDraft(submitted)
                        setupSubmitted = true
                        phase8Screen = AdminPhase8Screen.REVIEW
                        configSaveState = ConfigSaveState.Idle
                    },
                )

                operationalDraft == null -> SetupReviewScreen(
                    draft = requireNotNull(workingDraft),
                    deviceName = paired.device.serviceName,
                    saveState = configSaveState,
                    onStartDisplay = {
                        val candidate = requireNotNull(workingDraft)
                        saveConfig(candidate, paired) {
                            operationalDraft = candidate
                            workingDraft = candidate
                            phase8Screen = AdminPhase8Screen.HOME
                        }
                    },
                )

                else -> Phase8Runtime(
                    paired = paired,
                    committed = requireNotNull(operationalDraft),
                )
            }
        }
    }

    @Composable
    private fun Phase8Runtime(
        paired: AdminRuntimeState.Paired,
        committed: AdminOperationalDraft,
    ) {
        val editing = workingDraft ?: committed
        val goHome = {
            workingDraft = committed
            configSaveState = ConfigSaveState.Idle
            phase8Screen = AdminPhase8Screen.HOME
        }
        val updateWorking: (AdminOperationalDraft) -> Unit = {
            workingDraft = it
            configSaveState = ConfigSaveState.Idle
        }
        val saveEditing = {
            val candidate = workingDraft ?: committed
            saveConfig(candidate, paired) {
                operationalDraft = candidate
                workingDraft = candidate
                phase8Screen = AdminPhase8Screen.HOME
            }
        }
        val navigate: (AdminPhase8Screen) -> Unit = { target ->
            workingDraft = committed
            configSaveState = ConfigSaveState.Idle
            phase8Screen = target
        }

        when (phase8Screen) {
            AdminPhase8Screen.REVIEW,
            AdminPhase8Screen.HOME -> AdminHomeScreen(committed, paired.device.serviceName, navigate)

            AdminPhase8Screen.MOSQUE -> MosqueInformationScreen(
                draft = editing,
                saveState = configSaveState,
                onDraftChanged = updateWorking,
                onSave = saveEditing,
                onBack = goHome,
            )

            AdminPhase8Screen.PRAYER -> PrayerSettingsScreen(
                draft = editing,
                saveState = configSaveState,
                onDraftChanged = updateWorking,
                onSave = saveEditing,
                onBack = goHome,
            )

            AdminPhase8Screen.IQAMAH -> IqamahSettingsScreen(
                draft = editing,
                saveState = configSaveState,
                onDraftChanged = updateWorking,
                onSave = saveEditing,
                onBack = goHome,
            )

            AdminPhase8Screen.FRIDAY -> FridaySettingsScreen(
                draft = editing,
                saveState = configSaveState,
                onDraftChanged = updateWorking,
                onSave = saveEditing,
                onBack = goHome,
            )

            AdminPhase8Screen.ANNOUNCEMENTS -> AnnouncementsScreen(
                draft = editing,
                saveState = configSaveState,
                onDraftChanged = updateWorking,
                onSave = saveEditing,
                onBack = goHome,
            )

            AdminPhase8Screen.APPEARANCE -> DisplayAppearanceScreen(
                draft = editing,
                saveState = configSaveState,
                onDraftChanged = updateWorking,
                onSave = saveEditing,
                onBack = goHome,
            )

            AdminPhase8Screen.DEVICE -> DeviceStatusScreen(paired.device.serviceName, goHome)
        }
    }

    private fun saveConfig(
        draft: AdminOperationalDraft,
        paired: AdminRuntimeState.Paired,
        onSuccess: () -> Unit,
    ) {
        if (configSaveState is ConfigSaveState.Sending) return
        val request = runCatching { draft.toRequest(paired.credentialId, mosqueId) }.getOrElse {
            configSaveState = ConfigSaveState.Error(it.message ?: "Konfigurasi tidak valid")
            return
        }
        configSaveState = ConfigSaveState.Sending
        pairingExecutor.execute {
            val result = configClient.update(paired.device, request)
            mainHandler.post {
                result.fold(
                    onSuccess = { response ->
                        when (response) {
                            TvConfigUpdateResponse.Success -> {
                                configSaveState = ConfigSaveState.Saved
                                onSuccess()
                            }
                            is TvConfigUpdateResponse.Rejected -> {
                                configSaveState = ConfigSaveState.Error("${response.code}: ${response.message}")
                            }
                        }
                    },
                    onFailure = {
                        configSaveState = ConfigSaveState.Error("TV tidak dapat menyimpan konfigurasi melalui LAN")
                    },
                )
            }
        }
    }

    private fun stableMosqueId(): String {
        val prefs = getSharedPreferences(ADMIN_SETUP_PREFS, Context.MODE_PRIVATE)
        prefs.getString(KEY_MOSQUE_ID, null)?.let { return it }
        return UUID.randomUUID().toString().also { generated ->
            prefs.edit().putString(KEY_MOSQUE_ID, generated).apply()
        }
    }

    override fun onStart() {
        super.onStart()
        coordinator.onStart()
    }

    override fun onStop() {
        coordinator.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        pairingExecutor.shutdownNow()
        super.onDestroy()
    }

    private companion object {
        const val ADMIN_SETUP_PREFS = "admin-setup"
        const val KEY_MOSQUE_ID = "mosque-id"
    }
}

@Composable
internal fun AdminPairingScreen(
    state: AdminRuntimeState,
    fallbackCode: String,
    onFallbackCodeChanged: (String) -> Unit,
    onPair: (DiscoveredTvService) -> Unit,
    onRetry: () -> Unit,
) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = AdminBackground) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("Masjid Display", color = AdminTextPrimary, fontWeight = FontWeight.SemiBold)
                Text(
                    "Hubungkan TV",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AdminTextPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "TV ditemukan otomatis melalui jaringan lokal. Tidak perlu memasukkan alamat IP.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AdminTextSecondary,
                )

                OutlinedTextField(
                    value = fallbackCode,
                    onValueChange = onFallbackCodeChanged,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Kode pairing dari TV") },
                    supportingText = { Text("Scan QR akan tersedia; masukkan kode fallback yang tampil di TV.") },
                )

                when (state) {
                    AdminRuntimeState.Discovering -> StatusCard(
                        title = "Mencari TV di jaringan lokal…",
                        body = "Pastikan TV dan perangkat Admin berada pada LAN/Wi-Fi yang sama dan menu pairing TV aktif.",
                    )

                    is AdminRuntimeState.Devices -> {
                        if (state.items.isEmpty()) {
                            StatusCard(
                                title = "Belum ada TV ditemukan",
                                body = "Discovery tetap lokal. Coba lagi setelah memastikan TV siap dipasangkan.",
                            )
                            ActionButton("Cari lagi", onRetry)
                        } else {
                            Text(
                                "Pilih TV",
                                style = MaterialTheme.typography.titleMedium,
                                color = AdminTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            state.items.take(4).forEach { device ->
                                DeviceCard(device = device, onPair = { onPair(device) })
                            }
                        }
                    }

                    is AdminRuntimeState.Pairing -> StatusCard(
                        title = "Menghubungkan ${state.device.serviceName}",
                        body = "Melakukan handshake pairing langsung melalui LAN…",
                    )

                    is AdminRuntimeState.Paired -> StatusCard(
                        title = "TV berhasil terhubung",
                        body = "${state.device.serviceName} menerima credential pairing tepercaya.",
                    )

                    is AdminRuntimeState.Error -> {
                        StatusCard(title = "Pairing belum berhasil", body = state.message)
                        ActionButton("Coba lagi", onRetry)
                    }
                }

                Spacer(Modifier.weight(1f))
                Text(
                    "Koneksi pairing berlangsung langsung ke TV di jaringan lokal.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AdminTextSecondary,
                )
            }
        }
    }
}

@Composable
private fun DeviceCard(device: DiscoveredTvService, onPair: () -> Unit) {
    val compatible = device.negotiation is ProtocolNegotiation.Accepted
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = AdminSurface,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(device.serviceName, color = AdminTextPrimary, fontWeight = FontWeight.SemiBold)
            Text(
                if (compatible) "Siap dipasangkan" else "Versi protokol tidak kompatibel",
                color = AdminTextSecondary,
            )
            Button(
                onClick = onPair,
                enabled = compatible,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AdminAccent, contentColor = AdminOnAccent),
            ) {
                Text("Hubungkan TV", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun StatusCard(title: String, body: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = AdminSurface,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = AdminTextPrimary, fontWeight = FontWeight.SemiBold)
            Text(body, color = AdminTextSecondary)
        }
    }
}

@Composable
private fun ActionButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AdminAccent, contentColor = AdminOnAccent),
    ) {
        Text(label, fontWeight = FontWeight.SemiBold)
    }
}
