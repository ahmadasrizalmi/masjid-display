package com.asridigital.masjiddisplay.tv

import com.asridigital.masjiddisplay.database.ConfigRepository
import com.asridigital.masjiddisplay.database.PersistedTvConfig
import java.time.Clock
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

sealed interface TvAppState {
    data object Unconfigured : TvAppState
    data class Running(val snapshot: TvRuntimeSnapshot) : TvAppState
    data class ConfigurationError(val reason: String) : TvAppState
}

/** Room config Flow is the operational source of truth. Each valid config creates a fresh runtime. */
class TvAppController private constructor(
    configFlow: Flow<PersistedTvConfig?>,
    private val clock: Clock,
) {
    constructor(repository: ConfigRepository, clock: Clock = Clock.systemUTC()) :
        this(repository.config, clock)

    internal constructor(configFlow: Flow<PersistedTvConfig?>, clock: Clock = Clock.systemUTC(), testOnly: Unit = Unit) :
        this(configFlow, clock)

    val state: Flow<TvAppState> = configFlow.flatMapLatest { persisted ->
        if (persisted == null) flow { emit(TvAppState.Unconfigured) }
        else runtimeFlow(persisted)
    }

    private fun runtimeFlow(config: PersistedTvConfig): Flow<TvAppState> = flow {
        val runtime = try {
            TvRuntime(config.toRuntimeConfig(), clock = clock)
        } catch (error: IllegalArgumentException) {
            emit(TvAppState.ConfigurationError(error.message ?: "Konfigurasi tidak valid"))
            return@flow
        }
        while (true) {
            try {
                emit(TvAppState.Running(runtime.snapshot()))
            } catch (error: IllegalArgumentException) {
                emit(TvAppState.ConfigurationError(error.message ?: "Konfigurasi tidak valid"))
                return@flow
            }
            val nowMillis = clock.millis()
            delay(1_000L - (nowMillis % 1_000L))
        }
    }
}

private fun PersistedTvConfig.toRuntimeConfig() = TvRuntimeConfig(
    mosqueName = mosqueName,
    locationLabel = cityLabel.orEmpty(),
    calculation = calculation,
    display = display,
    layoutMode = when (normalLayoutMode) {
        "HORIZONTAL_MEDIA" -> NormalLayoutMode.HorizontalMedia
        "SIDEBAR_MEDIA" -> NormalLayoutMode.SidebarMedia
        else -> throw IllegalArgumentException("Unsupported layout mode: $normalLayoutMode")
    },
    informationMessage = informationMessage,
)
