package com.asridigital.masjiddisplay.database

import com.asridigital.masjiddisplay.domain.display.DisplayRuntimeConfig
import com.asridigital.masjiddisplay.domain.display.FridayRuntimeConfig
import com.asridigital.masjiddisplay.domain.prayer.AsrMethod
import com.asridigital.masjiddisplay.domain.prayer.PrayerCalculationConfig
import com.asridigital.masjiddisplay.domain.prayer.PrayerCalculationMethod
import com.asridigital.masjiddisplay.domain.prayer.PrayerName
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class PersistedTvConfig(
    val mosqueId: String,
    val mosqueName: String,
    val cityLabel: String?,
    val calculation: PrayerCalculationConfig,
    val display: DisplayRuntimeConfig,
    val normalLayoutMode: String,
    val informationMessage: String,
    val hijriAdjustmentDays: Int,
)

class ConfigRepository(private val dao: MosqueConfigDao) {
    val config: Flow<PersistedTvConfig?> = combine(
        dao.observe(),
        dao.observePrayerSettings(),
    ) { entity, settings -> entity?.toDomain(settings) }

    suspend fun current(): PersistedTvConfig? = dao.get()?.toDomain(dao.getPrayerSettings())

    suspend fun save(config: MosqueConfigEntity, settings: List<PrayerSettingEntity>) {
        // Constructing the domain object validates coordinates, timezone, method and bounded values
        // before the Room transaction can replace the last known-good operational config.
        config.toDomain(settings)
        dao.replaceOperationalConfig(config, settings)
    }
}

private fun MosqueConfigEntity.toDomain(settings: List<PrayerSettingEntity>): PersistedTvConfig {
    require(singletonId == 1)
    require(mosqueId.isNotBlank() && name.isNotBlank())
    require(hijriAdjustmentDays in -3..3) { "Hijri adjustment must be within -3..3 days" }
    require(normalLayoutMode in setOf("HORIZONTAL_MEDIA", "SIDEBAR_MEDIA"))

    val prayerSettings = settings.associateBy { PrayerName.valueOf(it.prayerName) }
    require(prayerSettings.size == PrayerName.entries.size) { "All canonical prayers must be configured" }
    require(prayerSettings.values.all { it.offsetMinutes in -120..120 && it.iqamahMinutes in 0..180 })

    val method = when (prayerMethod) {
        "KEMENAG_INDONESIA" -> PrayerCalculationMethod.KEMENAG_INDONESIA
        else -> error("Unsupported prayer method: $prayerMethod")
    }
    val calculation = PrayerCalculationConfig(
        latitude = latitude,
        longitude = longitude,
        zoneId = ZoneId.of(timezone),
        method = method,
        asrMethod = AsrMethod.valueOf(asrMethod),
        offsetsMinutes = prayerSettings.mapValues { it.value.offsetMinutes },
    )
    val display = DisplayRuntimeConfig(
        iqamahMinutes = prayerSettings.mapValues { it.value.iqamahMinutes },
        friday = FridayRuntimeConfig(
            enabled = fridayEnabled,
            start = LocalTime.parse(fridayStart),
            end = LocalTime.parse(fridayEnd),
        ),
    )
    return PersistedTvConfig(
        mosqueId = mosqueId,
        mosqueName = name,
        cityLabel = cityLabel,
        calculation = calculation,
        display = display,
        normalLayoutMode = normalLayoutMode,
        informationMessage = informationMessage,
        hijriAdjustmentDays = hijriAdjustmentDays,
    )
}
