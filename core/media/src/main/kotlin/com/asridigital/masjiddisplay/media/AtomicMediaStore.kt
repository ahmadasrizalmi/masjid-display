package com.asridigital.masjiddisplay.media

import java.io.Closeable
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.UUID

private const val MAX_MEDIA_BYTES = 50L * 1024L * 1024L
private val allowedMimeTypes = setOf("image/jpeg", "image/png", "image/webp")
private val safeMediaId = Regex("[A-Za-z0-9_-]{1,64}")

data class MediaUploadSpec(
    val mediaId: String,
    val originalFilename: String,
    val mimeType: String,
    val expectedBytes: Long,
    val expectedSha256: String,
) {
    init {
        require(mediaId.matches(safeMediaId)) { "Media id contains unsafe characters" }
        require(originalFilename.isNotBlank())
        require(mimeType in allowedMimeTypes) { "Unsupported media type" }
        require(expectedBytes in 1..MAX_MEDIA_BYTES) { "Media size is outside allowed bounds" }
        require(expectedSha256.matches(Regex("[0-9a-fA-F]{64}"))) { "SHA-256 must be 64 hex characters" }
    }
}

data class StoredMedia(
    val mediaId: String,
    val filename: String,
    val byteSize: Long,
    val sha256: String,
    val path: Path,
)

sealed interface MediaStoreResult {
    data class Success(val media: StoredMedia) : MediaStoreResult
    data class Rejected(val reason: MediaStoreError) : MediaStoreResult
}

enum class MediaStoreError {
    SIZE_MISMATCH,
    CHECKSUM_MISMATCH,
    IO_FAILURE,
}

class AtomicMediaStore(private val root: Path) : Closeable {
    private val tempDir = root.resolve(".incoming")
    private val mediaDir = root.resolve("media")

    init {
        Files.createDirectories(tempDir)
        Files.createDirectories(mediaDir)
    }

    fun store(spec: MediaUploadSpec, source: InputStream): MediaStoreResult {
        val temp = tempDir.resolve("${spec.mediaId}-${UUID.randomUUID()}.part")
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            var written = 0L
            var overflow = false
            Files.newOutputStream(temp).use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    val count = source.read(buffer)
                    if (count < 0) break
                    if (count == 0) continue
                    written += count
                    if (written > spec.expectedBytes || written > MAX_MEDIA_BYTES) {
                        overflow = true
                        break
                    }
                    digest.update(buffer, 0, count)
                    output.write(buffer, 0, count)
                }
            }
            if (overflow || written != spec.expectedBytes) return rejectAndDelete(temp, MediaStoreError.SIZE_MISMATCH)
            val actualSha = digest.digest().joinToString("") { "%02x".format(it) }
            if (!actualSha.equals(spec.expectedSha256, ignoreCase = true)) {
                return rejectAndDelete(temp, MediaStoreError.CHECKSUM_MISMATCH)
            }
            val destination = mediaDir.resolve("${spec.mediaId}.${extensionFor(spec.mimeType)}")
            try {
                Files.move(temp, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            } catch (_: java.nio.file.AtomicMoveNotSupportedException) {
                Files.move(temp, destination, StandardCopyOption.REPLACE_EXISTING)
            }
            MediaStoreResult.Success(StoredMedia(spec.mediaId, destination.fileName.toString(), written, actualSha, destination))
        } catch (_: Exception) {
            Files.deleteIfExists(temp)
            MediaStoreResult.Rejected(MediaStoreError.IO_FAILURE)
        }
    }

    fun pathFor(mediaId: String): Path? {
        require(mediaId.matches(safeMediaId)) { "Media id contains unsafe characters" }
        val prefix = "$mediaId."
        Files.list(mediaDir).use { paths ->
            return paths.filter { it.fileName.toString().startsWith(prefix) }.findFirst().orElse(null)
        }
    }

    fun delete(mediaId: String): Boolean = pathFor(mediaId)?.let(Files::deleteIfExists) ?: false

    override fun close() = Unit

    private fun rejectAndDelete(temp: Path, error: MediaStoreError): MediaStoreResult {
        Files.deleteIfExists(temp)
        return MediaStoreResult.Rejected(error)
    }

    private fun extensionFor(mimeType: String): String = when (mimeType) {
        "image/jpeg" -> "jpg"
        "image/png" -> "png"
        "image/webp" -> "webp"
        else -> error("Unsupported media type")
    }
}
