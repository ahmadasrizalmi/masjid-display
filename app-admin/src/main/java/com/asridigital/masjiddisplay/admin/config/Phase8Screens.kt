package com.asridigital.masjiddisplay.admin.config

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val Background = Color(0xFFF6F7F5)
private val SurfaceColor = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF17201B)
private val TextSecondary = Color(0xFF68736D)
private val Accent = Color(0xFF176B45)
private val OnAccent = Color(0xFFFFFFFF)
private val ErrorColor = Color(0xFFB3261E)

internal enum class AdminPhase8Screen {
    REVIEW,
    HOME,
    PRAYER,
    IQAMAH,
    FRIDAY,
    ANNOUNCEMENTS,
    APPEARANCE,
    DEVICE,
}

@Composable
internal fun SetupReviewScreen(
    draft: AdminOperationalDraft,
    deviceName: String,
    saveState: ConfigSaveState,
    onStartDisplay: () -> Unit,
) {
    Screen(title = "Selesai", subtitle = "Periksa data sebelum dikirim ke TV melalui LAN.") {
        SummaryCard("TV", deviceName)
        SummaryCard("Masjid", "${draft.mosque.name}\n${draft.mosque.locationLabel}")
        SummaryCard("Timezone", draft.mosque.timezoneId)
        SummaryCard("Metode", draft.mosque.prayerMethod.displayName)
        SaveStatus(saveState)
        PrimaryButton(
            label = if (saveState is ConfigSaveState.Sending) "Mengirim konfigurasi…" else "Mulai Display",
            enabled = saveState !is ConfigSaveState.Sending,
            onClick = onStartDisplay,
        )
    }
}

@Composable
internal fun AdminHomeScreen(
    draft: AdminOperationalDraft,
    deviceName: String,
    onNavigate: (AdminPhase8Screen) -> Unit,
) {
    Screen(title = draft.mosque.name, subtitle = draft.mosque.locationLabel) {
        SummaryCard("TV Utama · Terhubung langsung", deviceName)
        SectionTitle("PENGATURAN UTAMA")
        SettingsCard("Jadwal Sholat", "Koreksi waktu per sholat") { onNavigate(AdminPhase8Screen.PRAYER) }
        SettingsCard("Adzan & Iqamah", "Durasi iqamah per sholat") { onNavigate(AdminPhase8Screen.IQAMAH) }
        SettingsCard("Jumat", if (draft.fridayEnabled) "Aktif" else "Nonaktif") { onNavigate(AdminPhase8Screen.FRIDAY) }
        SectionTitle("KONTEN")
        SettingsCard("Pengumuman", draft.announcement.ifBlank { "Belum ada pengumuman" }) { onNavigate(AdminPhase8Screen.ANNOUNCEMENTS) }
        SectionTitle("DISPLAY")
        SettingsCard("Tampilan", draft.normalLayoutMode) { onNavigate(AdminPhase8Screen.APPEARANCE) }
        SettingsCard("Perangkat", "Status dan koneksi TV") { onNavigate(AdminPhase8Screen.DEVICE) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PrayerSettingsScreen(
    draft: AdminOperationalDraft,
    saveState: ConfigSaveState,
    onDraftChanged: (AdminOperationalDraft) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    var selectedPrayer by remember { mutableStateOf<String?>(null) }
    Screen(title = "Jadwal Sholat", subtitle = "Koreksi waktu dihitung lokal oleh TV.", onBack = onBack) {
        orderedPrayers.forEach { prayer ->
            val offset = draft.prayerOffsetsMinutes.getValue(prayer)
            SettingsCard(prayerLabel(prayer), "Koreksi ${signed(offset)} menit") { selectedPrayer = prayer }
        }
        SaveStatus(saveState)
        PrimaryButton("Simpan", saveState !is ConfigSaveState.Sending, onSave)
    }

    selectedPrayer?.let { prayer ->
        ModalBottomSheet(onDismissRequest = { selectedPrayer = null }) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(prayerLabel(prayer), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Koreksi: ${signed(draft.prayerOffsetsMinutes.getValue(prayer))} menit", color = TextSecondary)
                StepperRow(
                    onMinus = { onDraftChanged(draft.withPrayerOffset(prayer, -1)) },
                    value = "${signed(draft.prayerOffsetsMinutes.getValue(prayer))} menit",
                    onPlus = { onDraftChanged(draft.withPrayerOffset(prayer, 1)) },
                )
                PrimaryButton("Selesai", true) { selectedPrayer = null }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
internal fun IqamahSettingsScreen(
    draft: AdminOperationalDraft,
    saveState: ConfigSaveState,
    onDraftChanged: (AdminOperationalDraft) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    Screen(title = "Adzan & Iqamah", subtitle = "Atur jeda iqamah setelah adzan.", onBack = onBack) {
        orderedPrayers.forEach { prayer ->
            Text(prayerLabel(prayer), color = TextPrimary, fontWeight = FontWeight.SemiBold)
            StepperRow(
                onMinus = { onDraftChanged(draft.withIqamah(prayer, -1)) },
                value = "${draft.iqamahMinutes.getValue(prayer)} menit",
                onPlus = { onDraftChanged(draft.withIqamah(prayer, 1)) },
            )
        }
        SaveStatus(saveState)
        PrimaryButton("Simpan", saveState !is ConfigSaveState.Sending, onSave)
    }
}

@Composable
internal fun FridaySettingsScreen(
    draft: AdminOperationalDraft,
    saveState: ConfigSaveState,
    onDraftChanged: (AdminOperationalDraft) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    Screen(title = "Jumat", subtitle = "Konfigurasi state Jumat pada TV.", onBack = onBack) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Aktifkan mode Jumat", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text("TV menggunakan jadwal ini secara lokal.", color = TextSecondary)
            }
            Switch(checked = draft.fridayEnabled, onCheckedChange = { onDraftChanged(draft.copy(fridayEnabled = it)) })
        }
        OutlinedTextField(
            value = draft.fridayStart,
            onValueChange = { onDraftChanged(draft.copy(fridayStart = it)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Mulai") },
        )
        OutlinedTextField(
            value = draft.fridayEnd,
            onValueChange = { onDraftChanged(draft.copy(fridayEnd = it)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Selesai") },
        )
        SaveStatus(saveState)
        PrimaryButton("Simpan", saveState !is ConfigSaveState.Sending, onSave)
    }
}

@Composable
internal fun AnnouncementsScreen(
    draft: AdminOperationalDraft,
    saveState: ConfigSaveState,
    onDraftChanged: (AdminOperationalDraft) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    Screen(title = "Pengumuman", subtitle = "Pesan informasi singkat untuk display.", onBack = onBack) {
        OutlinedTextField(
            value = draft.announcement,
            onValueChange = { onDraftChanged(draft.copy(announcement = it)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            label = { Text("Pesan") },
        )
        SaveStatus(saveState)
        PrimaryButton("Simpan", saveState !is ConfigSaveState.Sending, onSave)
    }
}

@Composable
internal fun DisplayAppearanceScreen(
    draft: AdminOperationalDraft,
    saveState: ConfigSaveState,
    onDraftChanged: (AdminOperationalDraft) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    Screen(title = "Tampilan", subtitle = "Pilih layout normal display.", onBack = onBack) {
        LayoutChoice("Horizontal + Media", "HORIZONTAL_MEDIA", draft, onDraftChanged)
        LayoutChoice("Sidebar + Media", "SIDEBAR_MEDIA", draft, onDraftChanged)
        SaveStatus(saveState)
        PrimaryButton("Simpan", saveState !is ConfigSaveState.Sending, onSave)
    }
}

@Composable
internal fun DeviceStatusScreen(deviceName: String, onBack: () -> Unit) {
    Screen(title = "Perangkat", subtitle = "Status koneksi lokal TV.", onBack = onBack) {
        SummaryCard("$deviceName · Terhubung langsung", "Credential trusted aktif. Koneksi menggunakan LAN, bukan internet.")
    }
}

@Composable
private fun LayoutChoice(
    label: String,
    value: String,
    draft: AdminOperationalDraft,
    onDraftChanged: (AdminOperationalDraft) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onDraftChanged(draft.copy(normalLayoutMode = value)) },
        shape = RoundedCornerShape(14.dp),
        color = if (draft.normalLayoutMode == value) Color(0xFFE4F1E9) else SurfaceColor,
    ) {
        Text(label, modifier = Modifier.padding(18.dp), color = TextPrimary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun Screen(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Background) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (onBack != null) {
                    Text("‹ Kembali", modifier = Modifier.clickable { onBack() }, color = Accent, fontWeight = FontWeight.SemiBold)
                } else {
                    Text("Masjid Display", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
                Text(title, style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text(subtitle, color = TextSecondary)
                content()
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, supporting: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = SurfaceColor,
    ) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(supporting, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
            Text("›", color = TextSecondary)
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = SurfaceColor) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = TextSecondary)
            Text(value, color = TextPrimary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = TextSecondary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun SaveStatus(state: ConfigSaveState) {
    when (state) {
        ConfigSaveState.Idle -> Unit
        ConfigSaveState.Sending -> Text("Mengirim konfigurasi langsung ke TV…", color = TextSecondary)
        ConfigSaveState.Saved -> Text("Tersimpan di TV", color = Accent, fontWeight = FontWeight.SemiBold)
        is ConfigSaveState.Error -> Text(state.message, color = ErrorColor)
    }
}

@Composable
private fun PrimaryButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = OnAccent),
    ) {
        Text(label, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun StepperRow(onMinus: () -> Unit, value: String, onPlus: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(onClick = onMinus) { Text("−") }
        Text(value, modifier = Modifier.weight(1f), color = TextPrimary, fontWeight = FontWeight.Bold)
        Button(onClick = onPlus) { Text("+") }
    }
}

private val orderedPrayers = listOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA")

private fun prayerLabel(prayer: String): String = when (prayer) {
    "FAJR" -> "Subuh"
    "DHUHR" -> "Dzuhur"
    "ASR" -> "Ashar"
    "MAGHRIB" -> "Maghrib"
    "ISHA" -> "Isya"
    else -> prayer
}

private fun signed(value: Int): String = if (value > 0) "+$value" else value.toString()
