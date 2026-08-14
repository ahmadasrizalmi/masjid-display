package com.asridigital.masjiddisplay.admin.media

import com.asridigital.masjiddisplay.protocol.DiscoveredTvService

sealed interface MediaTransferState {
    data object Pending : MediaTransferState
    data class Sending(val sentBytes: Long, val totalBytes: Long) : MediaTransferState
    data object Success : MediaTransferState
    data class Failed(val message: String) : MediaTransferState
}

data class MediaTransferItem(
    val source: LocalMediaSource,
    val state: MediaTransferState = MediaTransferState.Pending,
)

/** Deterministic queue coordinator: successful items are never resent; failed items retry individually. */
class MediaTransferCoordinator(
    private val client: MediaTransferClient,
    private val device: DiscoveredTvService,
    private val credentialId: String,
) {
    private val mutableItems = linkedMapOf<String, MediaTransferItem>()
    val items: List<MediaTransferItem> get() = mutableItems.values.toList()

    fun enqueue(sources: List<LocalMediaSource>) {
        sources.forEach { source ->
            mutableItems.putIfAbsent(source.mediaId, MediaTransferItem(source))
        }
    }

    fun uploadPending(onChanged: (List<MediaTransferItem>) -> Unit = {}) {
        mutableItems.values.toList().forEach { item ->
            if (item.state is MediaTransferState.Pending) uploadOne(item.source.mediaId, onChanged)
        }
    }

    fun retry(mediaId: String, onChanged: (List<MediaTransferItem>) -> Unit = {}) {
        val current = mutableItems[mediaId] ?: return
        if (current.state !is MediaTransferState.Failed) return
        mutableItems[mediaId] = current.copy(state = MediaTransferState.Pending)
        onChanged(items)
        uploadOne(mediaId, onChanged)
    }

    fun delete(mediaId: String, onChanged: (List<MediaTransferItem>) -> Unit = {}) {
        val current = mutableItems[mediaId] ?: return
        if (current.state is MediaTransferState.Sending) return
        val result = client.delete(device, credentialId, mediaId)
        if (result.isSuccess) {
            mutableItems.remove(mediaId)
        } else {
            mutableItems[mediaId] = current.copy(
                state = MediaTransferState.Failed(result.exceptionOrNull()?.message ?: "Media gagal dihapus"),
            )
        }
        onChanged(items)
    }

    private fun uploadOne(mediaId: String, onChanged: (List<MediaTransferItem>) -> Unit) {
        val current = mutableItems[mediaId] ?: return
        if (current.state is MediaTransferState.Success || current.state is MediaTransferState.Sending) return
        mutableItems[mediaId] = current.copy(state = MediaTransferState.Sending(0, current.source.byteSize))
        onChanged(items)
        val result = client.upload(device, credentialId, current.source) { sent, total ->
            mutableItems[mediaId] = current.copy(state = MediaTransferState.Sending(sent, total))
            onChanged(items)
        }
        mutableItems[mediaId] = if (result.isSuccess) {
            current.copy(state = MediaTransferState.Success)
        } else {
            current.copy(
                state = MediaTransferState.Failed(result.exceptionOrNull()?.message ?: "Transfer media gagal"),
            )
        }
        onChanged(items)
    }
}
