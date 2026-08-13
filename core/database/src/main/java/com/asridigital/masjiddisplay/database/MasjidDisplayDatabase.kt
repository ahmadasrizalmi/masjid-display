package com.asridigital.masjiddisplay.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "mosque_config")
data class MosqueConfigEntity(
    @PrimaryKey val singletonId: Int = 1,
    val mosqueId: String,
    val name: String,
    val cityLabel: String?,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val hijriAdjustmentDays: Int,
    val prayerMethod: String,
    val asrMethod: String,
    val fridayEnabled: Boolean,
    val fridayStart: String,
    val fridayEnd: String,
    val normalLayoutMode: String,
    val informationMessage: String,
)

@Entity(tableName = "prayer_settings")
data class PrayerSettingEntity(
    @PrimaryKey val prayerName: String,
    val offsetMinutes: Int,
    val iqamahMinutes: Int,
)

@Entity(tableName = "media_items")
data class MediaItemEntity(
    @PrimaryKey val id: String,
    val localFilename: String,
    val mediaType: String,
    val byteSize: Long,
    val checksum: String,
    val width: Int?,
    val height: Int?,
    val createdAtEpochMillis: Long,
    val enabled: Boolean,
)

@Dao
interface MosqueConfigDao {
    @Query("SELECT * FROM mosque_config WHERE singletonId = 1 LIMIT 1")
    fun observe(): Flow<MosqueConfigEntity?>

    @Query("SELECT * FROM mosque_config WHERE singletonId = 1 LIMIT 1")
    suspend fun get(): MosqueConfigEntity?

    @Query("SELECT * FROM prayer_settings")
    fun observePrayerSettings(): Flow<List<PrayerSettingEntity>>

    @Query("SELECT * FROM prayer_settings")
    suspend fun getPrayerSettings(): List<PrayerSettingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConfig(config: MosqueConfigEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPrayerSettings(settings: List<PrayerSettingEntity>)

    @Transaction
    suspend fun replaceOperationalConfig(
        config: MosqueConfigEntity,
        settings: List<PrayerSettingEntity>,
    ) {
        upsertConfig(config)
        upsertPrayerSettings(settings)
    }
}

@Dao
interface MediaItemDao {
    @Query("SELECT * FROM media_items ORDER BY createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<MediaItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: MediaItemEntity)

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Database(
    entities = [MosqueConfigEntity::class, PrayerSettingEntity::class, MediaItemEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class MasjidDisplayDatabase : RoomDatabase() {
    abstract fun mosqueConfigDao(): MosqueConfigDao
    abstract fun mediaItemDao(): MediaItemDao
}
