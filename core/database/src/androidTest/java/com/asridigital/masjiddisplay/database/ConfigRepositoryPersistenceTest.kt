package com.asridigital.masjiddisplay.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asridigital.masjiddisplay.domain.prayer.PrayerName
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFails
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConfigRepositoryPersistenceTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dbName = "config-repository-persistence-test.db"
    private var database: MasjidDisplayDatabase? = null

    @After
    fun cleanup() {
        database?.close()
        database = null
        context.deleteDatabase(dbName)
    }

    @Test
    fun invalidPayloadCannotOverwritePreviouslyPersistedValidConfiguration() = runTest {
        val db = openDatabase()
        val repository = ConfigRepository(db.mosqueConfigDao())
        val valid = validConfig(name = "Masjid Nurul Hikmah")
        val settings = validPrayerSettings()
        repository.save(valid, settings)

        val invalid = valid.copy(name = "")
        assertFails { repository.save(invalid, settings) }

        val persisted = repository.current()
        assertNotNull(persisted)
        assertEquals("Masjid Nurul Hikmah", persisted!!.mosqueName)
        assertEquals("masjid-nurul-hikmah", persisted.mosqueId)
    }

    @Test
    fun validReplacementRemainsObservableAfterDatabaseAndRepositoryReopen() = runTest {
        var db = openDatabase()
        var repository = ConfigRepository(db.mosqueConfigDao())
        repository.save(validConfig(name = "Masjid Lama"), validPrayerSettings())
        repository.save(
            validConfig(name = "Masjid Baru", informationMessage = "Kajian Ahad pukul 07:00"),
            validPrayerSettings(maghribOffset = 2),
        )

        db.close()
        database = null

        db = openDatabase()
        repository = ConfigRepository(db.mosqueConfigDao())
        val reopened = repository.config.first { it != null }

        assertNotNull(reopened)
        assertEquals("Masjid Baru", reopened!!.mosqueName)
        assertEquals("Kajian Ahad pukul 07:00", reopened.informationMessage)
        assertEquals(2, reopened.calculation.offsetsMinutes[PrayerName.MAGHRIB])
    }

    private fun openDatabase(): MasjidDisplayDatabase {
        val db = Room.databaseBuilder(context, MasjidDisplayDatabase::class.java, dbName)
            .allowMainThreadQueries()
            .build()
        database = db
        return db
    }

    private fun validConfig(
        name: String,
        informationMessage: String = "Selamat datang",
    ) = MosqueConfigEntity(
        mosqueId = "masjid-nurul-hikmah",
        name = name,
        cityLabel = "Sleman, DI Yogyakarta",
        latitude = -7.7956,
        longitude = 110.3695,
        timezone = "Asia/Jakarta",
        hijriAdjustmentDays = 0,
        prayerMethod = "KEMENAG_INDONESIA",
        asrMethod = "STANDARD",
        fridayEnabled = true,
        fridayStart = "11:30",
        fridayEnd = "13:30",
        normalLayoutMode = "HORIZONTAL_MEDIA",
        informationMessage = informationMessage,
    )

    private fun validPrayerSettings(maghribOffset: Int = 0): List<PrayerSettingEntity> =
        PrayerName.entries.map { prayer ->
            PrayerSettingEntity(
                prayerName = prayer.name,
                offsetMinutes = if (prayer == PrayerName.MAGHRIB) maghribOffset else 0,
                iqamahMinutes = 10,
            )
        }
}
