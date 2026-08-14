package com.asridigital.masjiddisplay.admin.setup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MosqueSetupStateTest {
    @Test
    fun validDraftPassesDeterministicValidation() {
        val draft = MosqueSetupDraft(
            name = "Masjid Al Ikhlas",
            locationLabel = "Sleman, Yogyakarta",
            latitude = "-7.7500",
            longitude = "110.3700",
            timezoneId = "Asia/Jakarta",
        )

        assertTrue(draft.validate().isValid)
    }

    @Test
    fun requiredFieldsCoordinatesAndTimezoneAreValidated() {
        val validation = MosqueSetupDraft(
            name = " ",
            locationLabel = "",
            latitude = "91",
            longitude = "-181",
            timezoneId = "Not/AZone",
        ).validate()

        assertFalse(validation.isValid)
        assertTrue(MosqueSetupField.NAME in validation.errors)
        assertTrue(MosqueSetupField.LOCATION_LABEL in validation.errors)
        assertTrue(MosqueSetupField.LATITUDE in validation.errors)
        assertTrue(MosqueSetupField.LONGITUDE in validation.errors)
        assertTrue(MosqueSetupField.TIMEZONE in validation.errors)
    }

    @Test
    fun normalizedDraftTrimsTextAndAcceptsDecimalComma() {
        val normalized = MosqueSetupDraft(
            name = "  Masjid Al Ikhlas  ",
            locationLabel = "  Sleman  ",
            latitude = " -7,75 ",
            longitude = " 110,37 ",
            timezoneId = " Asia/Jakarta ",
        ).normalized()

        assertEquals("Masjid Al Ikhlas", normalized.name)
        assertEquals("Sleman", normalized.locationLabel)
        assertEquals("-7.75", normalized.latitude)
        assertEquals("110.37", normalized.longitude)
        assertEquals("Asia/Jakarta", normalized.timezoneId)
        assertTrue(normalized.validate().isValid)
    }
}
