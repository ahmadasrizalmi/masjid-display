package com.asridigital.masjiddisplay.media

import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.security.MessageDigest
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AtomicMediaStoreTest {
    @Test
    fun verifiedUploadIsPromotedToFinalMediaPath() {
        val root = Files.createTempDirectory("media-store-test")
        val bytes = "local-media".encodeToByteArray()
        val spec = MediaUploadSpec("media-1", "photo.jpg", "image/jpeg", bytes.size.toLong(), sha256(bytes))

        val result = AtomicMediaStore(root).store(spec, ByteArrayInputStream(bytes))

        val success = assertIs<MediaStoreResult.Success>(result)
        assertEquals(bytes.size.toLong(), success.media.byteSize)
        assertTrue(success.media.path.exists())
        assertEquals(bytes.toList(), Files.readAllBytes(success.media.path).toList())
        assertTrue(Files.list(root.resolve(".incoming")).use { it.findAny().isEmpty })
    }

    @Test
    fun checksumMismatchDeletesTemporaryFileAndDoesNotReplaceExistingMedia() {
        val root = Files.createTempDirectory("media-store-test")
        val store = AtomicMediaStore(root)
        val good = "good".encodeToByteArray()
        val goodSpec = MediaUploadSpec("media-1", "photo.jpg", "image/jpeg", good.size.toLong(), sha256(good))
        assertIs<MediaStoreResult.Success>(store.store(goodSpec, ByteArrayInputStream(good)))

        val bad = "evil".encodeToByteArray()
        val rejected = store.store(
            MediaUploadSpec("media-1", "photo.jpg", "image/jpeg", bad.size.toLong(), sha256(good)),
            ByteArrayInputStream(bad),
        )

        assertEquals(MediaStoreError.CHECKSUM_MISMATCH, assertIs<MediaStoreResult.Rejected>(rejected).reason)
        assertEquals(good.toList(), Files.readAllBytes(root.resolve("media/media-1.jpg")).toList())
        assertTrue(Files.list(root.resolve(".incoming")).use { it.findAny().isEmpty })
    }

    @Test
    fun sizeMismatchIsRejectedAndTemporaryFileIsCleaned() {
        val root = Files.createTempDirectory("media-store-test")
        val bytes = "short".encodeToByteArray()
        val spec = MediaUploadSpec("media-2", "photo.png", "image/png", bytes.size.toLong() + 1, sha256(bytes))

        val rejected = AtomicMediaStore(root).store(spec, ByteArrayInputStream(bytes))

        assertEquals(MediaStoreError.SIZE_MISMATCH, assertIs<MediaStoreResult.Rejected>(rejected).reason)
        assertFalse(root.resolve("media/media-2.png").exists())
        assertTrue(Files.list(root.resolve(".incoming")).use { it.findAny().isEmpty })
    }

    @Test
    fun deleteRemovesOnlyRequestedFinalMedia() {
        val root = Files.createTempDirectory("media-store-test")
        val store = AtomicMediaStore(root)
        val bytes = "photo".encodeToByteArray()
        val spec = MediaUploadSpec("media-delete", "photo.webp", "image/webp", bytes.size.toLong(), sha256(bytes))
        assertIs<MediaStoreResult.Success>(store.store(spec, ByteArrayInputStream(bytes)))

        assertTrue(store.delete("media-delete"))
        assertFalse(root.resolve("media/media-delete.webp").exists())
        assertFalse(store.delete("media-delete"))
    }

    private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes)
        .joinToString("") { "%02x".format(it) }
}
