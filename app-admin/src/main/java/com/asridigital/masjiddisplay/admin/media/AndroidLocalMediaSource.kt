package com.asridigital.masjiddisplay.admin.media

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import java.io.InputStream
import java.security.MessageDigest
import java.util.UUID

class AndroidLocalMediaSource private constructor(
    private val resolver: ContentResolver,
    private val uri: Uri,
    override val mediaId: String,
    override val filename: String,
    override val mimeType: String,
    override val byteSize: Long,
    override val sha256: String,
) : LocalMediaSource {
    override fun openStream(): InputStream = resolver.openInputStream(uri)
        ?: error("Media tidak dapat dibuka")

    companion object {
        fun from(resolver: ContentResolver, uri: Uri): AndroidLocalMediaSource {
            val mimeType = resolver.getType(uri) ?: error("Tipe media tidak diketahui")
            require(mimeType in setOf("image/jpeg", "image/png", "image/webp")) {
                "Format media belum didukung"
            }
            val displayName = resolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null,
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            } ?: "media"

            val digest = MessageDigest.getInstance("SHA-256")
            var bytes = 0L
            resolver.openInputStream(uri)?.use { input ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    if (count == 0) continue
                    bytes += count
                    require(bytes <= 50L * 1024L * 1024L) { "Ukuran media melebihi 50 MB" }
                    digest.update(buffer, 0, count)
                }
            } ?: error("Media tidak dapat dibuka")
            require(bytes > 0) { "Media kosong" }

            return AndroidLocalMediaSource(
                resolver = resolver,
                uri = uri,
                mediaId = UUID.randomUUID().toString(),
                filename = displayName,
                mimeType = mimeType,
                byteSize = bytes,
                sha256 = digest.digest().joinToString("") { "%02x".format(it) },
            )
        }
    }
}
