# 03 — Domain & Data

## Canonical entities

### MosqueConfig

```kotlin
data class MosqueConfig(
    val mosqueId: String,
    val name: String,
    val cityLabel: String?,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val hijriAdjustmentDays: Int,
    val prayerMethod: String,
    val asrMethod: String,
    val prayerOffsetsMinutes: Map<PrayerName, Int>,
    val iqamahMinutes: Map<PrayerName, Int>,
    val friday: FridayConfig,
    val branding: BrandingConfig
)
```

### PrayerName

`FAJR | DHUHR | ASR | MAGHRIB | ISHA`

Sunrise/Syuruq boleh ditampilkan sebagai informasi tetapi bukan prayer state dan tidak memicu adzan/iqamah.

### DailyPrayerSchedule

Menyimpan local date, timezone, raw/calculated times dan corrected prayer times. Corrected time digunakan state engine.

### Announcement

Minimal: id, title opsional, body, activeFrom/activeUntil opsional, priority, enabled.

### MediaItem

Minimal: id, local filename internal, media type, byte size, checksum, width/height bila gambar, createdAt, enabled. Jangan menyimpan arbitrary client filesystem path.

### PairedAdmin

Identitas trusted Admin App dan credential metadata yang diperlukan local protocol. Secret sensitif disimpan menggunakan mekanisme Android yang sesuai, bukan plain UI preference.

## Prayer pipeline

```text
coordinates + timezone + method
          ↓
 local prayer calculator
          ↓
 raw schedule
          ↓
 correction offsets
          ↓
 canonical corrected schedule
          ↓
 display state engine
```

Tidak ada network/API call pada pipeline.

## Time rules

- Gunakan `java.time` types yang sesuai.
- Timezone masjid eksplisit dan valid.
- Jangan mengandalkan timezone device secara implisit.
- Pergantian tanggal lokal masjid menghasilkan schedule baru.
- Countdown dihitung dari absolute target.

## Iqamah

Target = corrected prayer time + configured iqamah minutes. Nilai 0 berarti skip countdown sesuai state machine.

## Hijri

Informasional dan memiliki adjustment lokal terbatas. Adjustment tidak mengubah prayer calculation.

## Persistence ownership

TV Room database adalah source of truth operasional untuk konfigurasi/display. Admin App dapat menyimpan data UI/perangkat paired sendiri, tetapi TV tidak bergantung pada database HP untuk terus berjalan.

## Validation

Sebelum transaction persist:

- latitude -90..90
- longitude -180..180
- timezone valid
- prayer offset dalam batas UI
- iqamah non-negative dan dalam batas UI
- announcement range konsisten
- protocol payload version didukung
- media type/size/checksum valid

Payload invalid ditolak dan konfigurasi TV yang valid tidak berubah.

## Versioning

Room schema menggunakan migration eksplisit. Local protocol memiliki protocol version terpisah dari database schema version. Jangan menganggap keduanya sama.