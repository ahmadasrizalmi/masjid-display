package com.asridigital.masjiddisplay.admin.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val AdminBackground = Color(0xFFF6F7F5)
private val AdminSurface = Color(0xFFFFFFFF)
private val AdminSurfaceMuted = Color(0xFFEEF1ED)
private val AdminTextPrimary = Color(0xFF17201B)
private val AdminTextSecondary = Color(0xFF68736D)
private val AdminAccent = Color(0xFF176B45)
private val AdminOnAccent = Color(0xFFFFFFFF)

@Composable
internal fun MosqueSetupScreen(
    draft: MosqueSetupDraft,
    onDraftChanged: (MosqueSetupDraft) -> Unit,
    onContinue: (MosqueSetupDraft) -> Unit,
) {
    val validation = draft.validate()
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = AdminBackground) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("Masjid Display", color = AdminTextPrimary, fontWeight = FontWeight.SemiBold)
                SetupStepper(currentStep = 3)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Data Masjid",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AdminTextPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Isi identitas dan lokasi yang akan dipakai TV untuk menghitung jadwal sholat secara lokal.",
                    color = AdminTextSecondary,
                )

                SetupField(
                    value = draft.name,
                    onValueChange = { onDraftChanged(draft.copy(name = it)) },
                    label = "Nama masjid",
                    error = validation[MosqueSetupField.NAME],
                )
                SetupField(
                    value = draft.locationLabel,
                    onValueChange = { onDraftChanged(draft.copy(locationLabel = it)) },
                    label = "Lokasi",
                    supporting = "Contoh: kecamatan, kota/kabupaten",
                    error = validation[MosqueSetupField.LOCATION_LABEL],
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SetupField(
                        value = draft.latitude,
                        onValueChange = { onDraftChanged(draft.copy(latitude = it)) },
                        label = "Latitude",
                        error = validation[MosqueSetupField.LATITUDE],
                        modifier = Modifier.weight(1f),
                    )
                    SetupField(
                        value = draft.longitude,
                        onValueChange = { onDraftChanged(draft.copy(longitude = it)) },
                        label = "Longitude",
                        error = validation[MosqueSetupField.LONGITUDE],
                        modifier = Modifier.weight(1f),
                    )
                }
                SetupField(
                    value = draft.timezoneId,
                    onValueChange = { onDraftChanged(draft.copy(timezoneId = it)) },
                    label = "Timezone",
                    supporting = "Gunakan ID timezone, misalnya Asia/Jakarta",
                    error = validation[MosqueSetupField.TIMEZONE],
                )
                OutlinedTextField(
                    value = draft.prayerMethod.displayName,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    singleLine = true,
                    label = { Text("Metode jadwal sholat") },
                    supportingText = { Text("Metode yang tersedia untuk MVP") },
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = AdminSurface,
                ) {
                    Text(
                        "Koordinat dan timezone tetap dapat dikoreksi sebelum konfigurasi dikirim ke TV.",
                        modifier = Modifier.padding(16.dp),
                        color = AdminTextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Button(
                    onClick = { onContinue(draft.normalized()) },
                    enabled = validation.isValid,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AdminAccent, contentColor = AdminOnAccent),
                ) {
                    Text("Lanjutkan", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
internal fun MosqueSetupReadyScreen(draft: MosqueSetupDraft) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = AdminBackground) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("Masjid Display", color = AdminTextPrimary, fontWeight = FontWeight.SemiBold)
                SetupStepper(currentStep = 3)
                Text(
                    "Data masjid siap ditinjau",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AdminTextPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "${draft.name} · ${draft.locationLabel}",
                    color = AdminTextSecondary,
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = AdminSurface,
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("Koordinat", color = AdminTextSecondary)
                        Text("${draft.latitude}, ${draft.longitude}", color = AdminTextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Timezone", color = AdminTextSecondary)
                        Text(draft.timezoneId, color = AdminTextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Metode", color = AdminTextSecondary)
                        Text(draft.prayerMethod.displayName, color = AdminTextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
                Text(
                    "Tahap berikutnya akan meninjau data ini bersama TV yang sudah terhubung sebelum konfigurasi dikirim.",
                    color = AdminTextSecondary,
                )
            }
        }
    }
}

@Composable
private fun SetupField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    modifier: Modifier = Modifier.fillMaxWidth(),
    supporting: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        isError = error != null,
        label = { Text(label) },
        supportingText = when {
            error != null -> ({ Text(error) })
            supporting != null -> ({ Text(supporting) })
            else -> null
        },
    )
}

@Composable
private fun SetupStepper(currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        (1..4).forEach { step ->
            Surface(
                modifier = Modifier.weight(1f).height(if (step == currentStep) 10.dp else 8.dp),
                shape = CircleShape,
                color = if (step <= currentStep) AdminAccent else AdminSurfaceMuted,
            ) {}
        }
    }
}
