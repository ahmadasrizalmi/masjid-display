package com.asridigital.masjiddisplay.tv.pairing

import com.asridigital.masjiddisplay.database.ConfigRepository
import com.asridigital.masjiddisplay.database.MosqueConfigEntity
import com.asridigital.masjiddisplay.database.PrayerSettingEntity
import com.asridigital.masjiddisplay.protocol.ConfigTransportPaths
import com.asridigital.masjiddisplay.protocol.ConfigWireContract
import com.asridigital.masjiddisplay.protocol.TvConfigUpdateRequest
import com.asridigital.masjiddisplay.protocol.TvConfigUpdateResponse
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.runBlocking

fun interface TvConfigSink {
    fun persist(update: TvConfigUpdateRequest)
}

class RoomTvConfigSink(private val repository: ConfigRepository) : TvConfigSink {
    override fun persist(update: TvConfigUpdateRequest) {
        runBlocking {
            repository.save(update.toConfigEntity(), update.toPrayerSettings())
        }
    }

    private fun TvConfigUpdateRequest.toConfigEntity() = MosqueConfigEntity(
        mosqueId = mosqueId,
        name = mosqueName,
        cityLabel = locationLabel.ifBlank { null },
        latitude = latitude,
        longitude = longitude,
        timezone = timezoneId,
        hijriAdjustmentDays = hijriAdjustmentDays,
        prayerMethod = prayerMethod,
        asrMethod = "STANDARD",
        fridayEnabled = fridayEnabled,
        fridayStart = fridayStart,
        fridayEnd = fridayEnd,
        normalLayoutMode = normalLayoutMode,
        informationMessage = informationMessage,
    )

    private fun TvConfigUpdateRequest.toPrayerSettings(): List<PrayerSettingEntity> =
        TvConfigUpdateRequest.canonicalPrayerNames.sorted().map { prayer ->
            PrayerSettingEntity(
                prayerName = prayer,
                offsetMinutes = requireNotNull(prayerOffsetsMinutes[prayer]),
                iqamahMinutes = requireNotNull(iqamahMinutes[prayer]),
            )
        }
}

/** Applies trusted Admin configuration directly to the TV-local persistence boundary. */
class TvConfigHttpHandler(
    private val sink: TvConfigSink,
    private val isCredentialTrusted: (String) -> Boolean,
) {
    fun handle(request: PairingHttpRequest): PairingHttpResponse {
        if (request.path != ConfigTransportPaths.UPDATE) return response(404, "")
        if (request.method != "POST") return response(405, "")

        val update = ConfigWireContract.decodeRequest(request.body)
            ?: return rejected(400, "MALFORMED_REQUEST", "Konfigurasi tidak dapat dibaca")
        if (!isCredentialTrusted(update.credentialId)) {
            return rejected(403, "UNAUTHORIZED", "Credential Admin tidak dikenal")
        }

        return try {
            validate(update)
            sink.persist(update)
            response(200, ConfigWireContract.encodeResponse(TvConfigUpdateResponse.Success))
        } catch (failure: IllegalArgumentException) {
            rejected(400, "VALIDATION_FAILED", failure.message ?: "Konfigurasi tidak valid")
        } catch (failure: IllegalStateException) {
            rejected(400, "VALIDATION_FAILED", failure.message ?: "Konfigurasi tidak valid")
        }
    }

    private fun validate(update: TvConfigUpdateRequest) {
        ZoneId.of(update.timezoneId)
        require(update.prayerMethod == "KEMENAG_INDONESIA") { "Metode jadwal sholat tidak didukung" }
        LocalTime.parse(update.fridayStart)
        LocalTime.parse(update.fridayEnd)
    }

    private fun rejected(status: Int, code: String, message: String): PairingHttpResponse = response(
        status,
        ConfigWireContract.encodeResponse(TvConfigUpdateResponse.Rejected(code, message)),
    )

    private fun response(status: Int, body: String) = PairingHttpResponse(status, ConfigWireContract.CONTENT_TYPE, body)
}
