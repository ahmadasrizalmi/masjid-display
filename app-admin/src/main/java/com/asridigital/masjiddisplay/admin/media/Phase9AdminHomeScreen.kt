package com.asridigital.masjiddisplay.admin.media

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.asridigital.masjiddisplay.admin.config.AdminOperationalDraft
import com.asridigital.masjiddisplay.admin.config.AdminPhase8Screen

private val HomeBackground = Color(0xFFF6F7F5)
private val HomeCard = Color.White
private val HomePrimary = Color(0xFF17201B)
private val HomeSecondary = Color(0xFF68736D)

@Composable
internal fun Phase9AdminHomeScreen(
    draft: AdminOperationalDraft,
    deviceName: String,
    onNavigate: (AdminPhase8Screen) -> Unit,
    onMedia: () -> Unit,
) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = HomeBackground) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(draft.mosque.name, style = MaterialTheme.typography.headlineMedium, color = HomePrimary, fontWeight = FontWeight.Bold)
                Text(draft.mosque.locationLabel, color = HomeSecondary)
                HomeRow("TV Utama · Terhubung langsung", deviceName) {}
                Section("PENGATURAN UTAMA")
                HomeRow("Informasi Masjid", "Nama, lokasi, koordinat, timezone") { onNavigate(AdminPhase8Screen.MOSQUE) }
                HomeRow("Jadwal Sholat", "Koreksi waktu per sholat") { onNavigate(AdminPhase8Screen.PRAYER) }
                HomeRow("Adzan & Iqamah", "Durasi iqamah per sholat") { onNavigate(AdminPhase8Screen.IQAMAH) }
                HomeRow("Jumat", if (draft.fridayEnabled) "Aktif" else "Nonaktif") { onNavigate(AdminPhase8Screen.FRIDAY) }
                Section("KONTEN")
                HomeRow("Pengumuman", draft.announcement.ifBlank { "Belum ada pengumuman" }) { onNavigate(AdminPhase8Screen.ANNOUNCEMENTS) }
                HomeRow("Media", "Foto lokal · multi-select · transfer LAN") { onMedia() }
                Section("DISPLAY")
                HomeRow("Tampilan", draft.normalLayoutMode) { onNavigate(AdminPhase8Screen.APPEARANCE) }
                HomeRow("Perangkat", "Status dan koneksi TV") { onNavigate(AdminPhase8Screen.DEVICE) }
            }
        }
    }
}

@Composable
private fun HomeRow(title: String, supporting: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = HomeCard,
    ) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = HomePrimary, fontWeight = FontWeight.SemiBold)
                Text(supporting, color = HomeSecondary, style = MaterialTheme.typography.bodySmall)
            }
            Text("›", color = HomeSecondary)
        }
    }
}

@Composable
private fun Section(text: String) {
    Text(text, color = HomeSecondary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
}
