package com.asridigital.masjiddisplay.protocol

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ConfigWireContractTest {
    @Test
    fun requestRoundTripPreservesLocalOperationalConfig() {
        val request = TvConfigUpdateRequest(
            credentialId = "trusted-admin",
            mosqueId = "mosque-1",
            mosqueName = "Masjid Al Ikhlas",
            locationLabel = "Sleman, Yogyakarta",
            latitude = -7.75,
            longitude = 110.37,
            timezoneId = "Asia/Jakarta",
            prayerMethod = "KEMENAG_INDONESIA",
            informationMessage = "Jaga kebersihan area masjid",
            prayerOffsetsMinutes = TvConfigUpdateRequest.canonicalPrayerNames.associateWith { 2 },
            iqamahMinutes = TvConfigUpdateRequest.canonicalPrayerNames.associateWith { 12 },
        )

        assertEquals(request, ConfigWireContract.decodeRequest(ConfigWireContract.encodeRequest(request)))
    }

    @Test
    fun malformedOrOutOfRangeRequestIsRejectedByDecoder() {
        assertNull(ConfigWireContract.decodeRequest("credentialId=x&mosqueId=y"))

        val valid = TvConfigUpdateRequest(
            credentialId = "trusted-admin",
            mosqueId = "mosque-1",
            mosqueName = "Masjid",
            locationLabel = "Kota",
            latitude = -7.0,
            longitude = 110.0,
            timezoneId = "Asia/Jakarta",
            prayerMethod = "KEMENAG_INDONESIA",
        )
        val invalid = ConfigWireContract.encodeRequest(valid).replace("latitude=-7.0", "latitude=99.0")
        assertNull(ConfigWireContract.decodeRequest(invalid))
    }

    @Test
    fun responseRoundTripKeepsActionableError() {
        val response = TvConfigUpdateResponse.Rejected("UNAUTHORIZED", "Credential tidak dikenal")
        assertEquals(response, ConfigWireContract.decodeResponse(ConfigWireContract.encodeResponse(response)))
        assertEquals(
            TvConfigUpdateResponse.Success,
            ConfigWireContract.decodeResponse(ConfigWireContract.encodeResponse(TvConfigUpdateResponse.Success)),
        )
    }
}
