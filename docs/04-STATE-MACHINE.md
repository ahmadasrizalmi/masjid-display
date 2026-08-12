# 04 — Display State Machine

Dokumen ini adalah otoritas utama behavior temporal TV App.

## States

```text
NORMAL
APPROACHING_PRAYER
ADHAN
IQAMAH_COUNTDOWN
PRAYER
FRIDAY
INFORMATION
ERROR
```

Koneksi LAN/internet bukan display state. TV tetap menjalankan state machine dari data lokal.

## Main flow

```text
NORMAL
  ↓ approaching threshold
APPROACHING_PRAYER
  ↓ corrected prayer time
ADHAN
  ↓ configured adhan display duration
IQAMAH_COUNTDOWN
  ↓ iqamah target
PRAYER
  ↓ configured prayer screen duration
NORMAL
```

## Priority

1. ERROR kritis
2. FRIDAY active worship window
3. ADHAN
4. IQAMAH_COUNTDOWN
5. PRAYER
6. APPROACHING_PRAYER
7. NORMAL
8. INFORMATION rotation

Announcement/donation tidak menginterupsi state ibadah prioritas tinggi.

## NORMAL

Current time, next prayer, countdown, daily schedule, mosque identity, date, announcement minimal.

## APPROACHING_PRAYER

Aktif dalam threshold sebelum corrected prayer time. Default MVP 10 menit. Konten sekunder dikurangi.

## ADHAN

Mulai tepat pada corrected prayer timestamp. Nama sholat + waktu + konteks adzan. QRIS/pengumuman disembunyikan.

## IQAMAH_COUNTDOWN

Target absolut = corrected prayer timestamp + iqamah duration. Countdown = target - now. 60 detik terakhir boleh memiliki emphasis visual tanpa state domain baru.

## PRAYER

Mulai saat target iqamah tercapai. Tampilan minimal. Setelah configured prayer screen duration kembali NORMAL.

## FRIDAY

Pada Jumat, FridayConfig menentukan window dan menggantikan presentation Dzuhur yang relevan. Tidak boleh ada dua flow visual yang bertabrakan.

## INFORMATION

Hanya dapat rotate ketika effective state NORMAL dan di luar suppression window menjelang sholat.

## ERROR

Hanya jika konfigurasi lokal tidak cukup/invalid sehingga display tidak dapat menghasilkan informasi yang aman. Pesan actionable untuk pengurus, tanpa stack trace.

Kegagalan HP menemukan TV atau LAN putus **bukan ERROR display**.

## Determinism

Diberikan `(now, canonicalSchedule, config)`, resolver harus menghasilkan state yang sama tanpa bergantung pada urutan Compose render atau timer sebelumnya.

Target pure Kotlin API:

```kotlin
fun resolveDisplayState(
    now: ZonedDateTime,
    schedule: DailyPrayerSchedule,
    config: DisplayRuntimeConfig
): DisplayState
```

Wajib unit test detik sebelum/saat/setelah transition.