package com.asridigital.masjiddisplay.admin.config

import com.asridigital.masjiddisplay.admin.setup.MosqueSetupDraft
import kotlin.test.Test
import kotlin.test.assertTrue

class AdminOperationalValidationTest {
    private val mosque = MosqueSetupDraft(
        name = "Masjid Al Ikhlas",
        locationLabel = "Sleman",
        latitude = "-7.75",
        longitude = "110.37",
        timezoneId = "Asia/Jakarta",
    )

    @Test
    fun invalidFridayClockIsRejectedBeforeLanSend() {
        val errors = AdminOperationalDraft(
            mosque = mosque,
            fridayEnabled = true,
            fridayStart = "not-a-time",
        ).validate()

        assertTrue(errors.any { it.contains("Jumat") })
    }
}
