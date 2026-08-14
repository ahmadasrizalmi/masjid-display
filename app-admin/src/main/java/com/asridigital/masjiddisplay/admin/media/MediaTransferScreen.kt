package com.asridigital.masjiddisplay.admin.media

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val Background = Color(0xFFF6F7F5)
private val Card = Color.White
private val Primary = Color(0xFF17201B)
private val Secondary = Color(0xFF68736D)
private val Accent = Color(0xFF176B45)
private val Error = Color(0xFFB3261E)

@Composable
internal fun MediaTransferScreen(
    transferItems: List<MediaTransferItem>,
    selectionError: String?,
    onPicked: (List<Uri>) -> Unit,
    onRetry: (String) -> Unit,
    onDelete: (String) -> Unit,
    onBack: () -> Unit,
) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(12)) { uris ->
        if (uris.isNotEmpty()) onPicked(uris)
    }
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Background) {
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                OutlinedButton(onClick = onBack) { Text("Kembali") }
                Text("Media", style = MaterialTheme.typography.headlineMedium, color = Primary, fontWeight = FontWeight.Bold)
                Text(
                    "Pilih beberapa foto. Transfer berlangsung langsung ke TV melalui LAN; item gagal dapat diulang sendiri.",
                    color = Secondary,
                )
                Button(
                    onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Pilih Foto") }
                selectionError?.let { Text(it, color = Error) }

                if (transferItems.isEmpty()) {
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = Card) {
                        Text("Belum ada media dipilih.", modifier = Modifier.padding(18.dp), color = Secondary)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(transferItems, key = { it.source.mediaId }) { item ->
                            TransferCard(item, onRetry, onDelete)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransferCard(
    item: MediaTransferItem,
    onRetry: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    Surface(shape = RoundedCornerShape(16.dp), color = Card) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(item.source.filename, color = Primary, fontWeight = FontWeight.SemiBold, maxLines = 2)
            when (val state = item.state) {
                MediaTransferState.Pending -> Text("Menunggu", color = Secondary)
                is MediaTransferState.Sending -> {
                    val progress = if (state.totalBytes <= 0) 0 else ((state.sentBytes * 100L) / state.totalBytes).toInt().coerceIn(0, 100)
                    Text("Mengirim $progress%", color = Secondary)
                }
                MediaTransferState.Success -> {
                    Text("Tersimpan di TV", color = Accent)
                    OutlinedButton(onClick = { onDelete(item.source.mediaId) }) { Text("Hapus") }
                }
                is MediaTransferState.Failed -> {
                    Text(state.message, color = Error, style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { onRetry(item.source.mediaId) }) { Text("Coba lagi") }
                        OutlinedButton(onClick = { onDelete(item.source.mediaId) }) { Text("Hapus") }
                    }
                }
            }
        }
    }
}
