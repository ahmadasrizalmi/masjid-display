# 03 — Domain & Data

## Canonical entities

### MosqueConfig

```ts
type MosqueConfig = {
  mosqueId: string
  name: string
  cityLabel?: string
  latitude: number
  longitude: number
  timezone: string
  hijriAdjustmentDays: number
  prayerMethod: string
  asrMethod: string
  prayerOffsetsMinutes: Partial<Record<PrayerName, number>>
  iqamahMinutes: Partial<Record<PrayerName, number>>
  friday: FridayConfig
  branding: BrandingConfig
}
```

### PrayerName

`fajr | dhuhr | asr | maghrib | isha`

`sunrise` boleh tampil sebagai informasi tetapi **bukan prayer state** dan tidak memicu adzan/iqamah.

### DailyPrayerSchedule

Menyimpan local date, timezone, calculated/raw prayer times, corrected prayer times, dan source metadata. Corrected time adalah waktu yang digunakan state engine.

### Announcement

Minimal: `id`, `title?`, `body`, `activeFrom?`, `activeUntil?`, `priority`, `enabled`.

### FridayConfig

Minimal: enabled, Friday-specific display window, khutbah/jumuah time opsional, petugas opsional.

## Prayer time pipeline

```text
coordinates + timezone + method
          ↓
 calculation adapter
          ↓
 raw prayer schedule
          ↓
 per-prayer correction offsets
          ↓
 canonical corrected schedule
          ↓
 display state engine
```

Hanya canonical corrected schedule yang digunakan untuk transition boundary.

## Time rules

- Semua timestamp internal harus unambiguous.
- UI menampilkan waktu lokal masjid.
- Timezone berasal dari konfigurasi masjid, bukan timezone browser/device secara implisit.
- Pergantian hari memicu pembuatan/load schedule tanggal berikutnya.
- Clock drift device adalah risiko; sinkronisasi waktu OS/NTP direkomendasikan pada deployment.

## Iqamah

Iqamah disimpan sebagai durasi menit setelah prayer time untuk setiap prayer. Target iqamah dihitung sebagai prayer timestamp + configured duration.

Durasi 0 berarti behavior harus eksplisit; MVP: skip countdown dan masuk state PRAYER sesuai aturan state machine.

## Hijri date

Hijri display adalah informational. Admin dapat memberi adjustment hari (-2..+2 pada MVP). Perubahan Hijri tidak mengubah prayer calculation.

## Validation

Config invalid tidak boleh mengganti last-known-good config. Nilai penting yang divalidasi:

- latitude -90..90
- longitude -180..180
- valid IANA timezone
- prayer offsets dalam range aman yang ditentukan UI
- iqamah duration non-negative dan memiliki maximum UI
- date ranges announcement konsisten

## Versioning

Persisted config memiliki `schemaVersion`. Migration harus eksplisit ketika schema berubah. Jangan silently membaca struktur lama sebagai struktur baru.