# 04 — Display State Machine

Dokumen ini adalah otoritas utama behavior temporal Display.

## States

```text
NORMAL
APPROACHING_PRAYER
ADHAN
IQAMAH_COUNTDOWN
PRAYER
FRIDAY
INFORMATION
OFFLINE
ERROR
```

`OFFLINE` adalah connectivity condition yang umumnya berupa indicator, bukan state yang mengalahkan prayer flow. `ERROR` hanya mengambil alih jika core display tidak dapat menghasilkan informasi aman.

## Main flow

```text
NORMAL
  ↓ threshold approaching
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

Dari tertinggi:

1. ERROR kritis
2. FRIDAY active worship window
3. ADHAN
4. IQAMAH_COUNTDOWN
5. PRAYER
6. APPROACHING_PRAYER
7. NORMAL
8. INFORMATION overlay/rotation

Announcement/donation tidak boleh menginterupsi state ibadah prioritas tinggi.

## NORMAL

Menampilkan current time, next prayer, countdown, daily schedule, mosque identity, date, dan announcement minimal.

## APPROACHING_PRAYER

Aktif dalam configurable threshold sebelum corrected prayer time. MVP default: 10 menit. Next prayer menjadi focal point; konten sekunder dikurangi.

## ADHAN

Mulai tepat pada corrected prayer timestamp. Tampilan minimal: nama sholat + waktu + label waktu adzan. Tidak menampilkan QRIS/promosi.

Durasi layar adzan adalah konfigurasi display, bukan asumsi panjang audio adzan.

## IQAMAH_COUNTDOWN

Target absolut = corrected prayer timestamp + iqamah duration. Countdown selalu dihitung dari target-now.

Pada 60 detik terakhir, UI boleh masuk visual emphasis tanpa membuat domain state baru.

## PRAYER

Mulai ketika target iqamah tercapai. Tampilan sangat minimal; announcement dan donation disembunyikan. Setelah configured prayer screen duration, kembali ke NORMAL.

## FRIDAY

Hanya Jumat. Detail waktu/trigger dikonfigurasi pada FridayConfig. Saat window Jumat aktif, Friday mengalahkan normal Dhuhr presentation. Implementasi detail khutbah/petugas dapat bertahap, tetapi tidak boleh menjalankan dua flow visual yang saling bertabrakan.

## INFORMATION

Bukan interrupt bebas. Information hanya boleh muncul/rotate ketika state efektif NORMAL dan tidak berada pada suppression window menjelang sholat.

## OFFLINE

Jika internet putus namun config/schedule valid tersedia, prayer state engine tetap berjalan. Tampilkan indikator kecil dan tidak mengganggu focal point.

## ERROR

Gunakan hanya jika tidak ada last-known-good config/schedule yang cukup untuk operasi aman. Pesan harus actionable untuk pengurus, tetapi tidak menampilkan stack trace.

## Determinism requirement

Diberikan `(now, canonical schedule, config)`, fungsi resolver harus menghasilkan state efektif yang sama tanpa bergantung pada urutan render React atau timer sebelumnya.

Target API:

```ts
resolveDisplayState({ now, schedule, config }): DisplayState
```

Ini wajib memiliki unit test pada boundary detik sebelum/saat/setelah setiap transition.