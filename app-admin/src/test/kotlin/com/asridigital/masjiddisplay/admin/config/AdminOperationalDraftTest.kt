package com.asridigital.masjiddisplay.admin.config

import com.asridigital.masjiddisplay.admin.setup.MosqueSetupDraft
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdminOperationalDraftTest {
    private val mosque = MosqueSetupDraft(
        name = "Masjid Al Ikhlas",
        locationLabel = "Sleman",
        latitude = "-7.75",
        longitude = "110.37",
        timezoneId = "Asia/Jakarta",
    )

    @Test
    fun setupDraftMapsToAuthenticatedTvRequest() {
        val draft = AdminOperationalDraft(mosque = mosque, announcement = "Informasi jamaah")

        val request = draft.toRequest("credential-1", "mosque-1")

        assertEquals("credential-1", request.credentialId)
        assertEquals("mosque-1", request.mosqueId)
        assertEquals("Masjid Al Ikhlas", request.mosqueName)
        assertEquals("Informasi jamaah", request.informationMessage)
        assertEquals(setOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA"), request.prayerOffsetsMinutes.keys)
    }

    @Test
    fun prayerAndIqamahSteppersStayInsideSsotBounds() {
        var draft = AdminOperationalDraft(mosque)
        repeat(300) { draft = draft.withPrayerOffset("FAJR", 1) }
        repeat(300) { draft = draft.withIqamah("FAJR", 1) }

        assertEquals(120, draft.prayerOffsetsMinutes.getValue("FAJR"))
        assertEquals(180, draft.iqamahMinutes.getValue("FAJR"))
        assertTrue(draft.validate().isEmpty())
    }
}
