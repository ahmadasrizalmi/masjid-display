# 05 — UI TV

Target design canvas: **1920×1080 (16:9)**. Layout harus scale secara proporsional ke 1280×720 dan 4K.

## Safe area

Gunakan minimum 5% inset dari setiap sisi untuk konten kritis. Pada 1920×1080 baseline: sekitar 96px horizontal dan 54px vertical. Jangan menaruh jam, prayer time, atau status kritis dekat edge.

## Visual hierarchy

Urutan NORMAL:

1. Current time / next prayer context
2. Next prayer countdown
3. Daily prayer schedule
4. Mosque identity + dates
5. Announcement
6. Donation/secondary content

## Wireframe — NORMAL

```text
┌──────────────────────────────────────────────────────────────────────┐
│ [LOGO] MASJID AL-IKHLAS                    KAMIS · 13 AGUSTUS 2026   │
│        Sleman, Yogyakarta                    29 SAFAR 1448 H          │
│                                                                      │
│                         SHOLAT BERIKUTNYA                             │
│                              DZUHUR                                  │
│                                                                      │
│                             10:42:38                                 │
│                                                                      │
│                          dalam 01:18:22                              │
│                                                                      │
│   SUBUH       DZUHUR       ASHAR       MAGHRIB        ISYA           │
│   04:51       12:01        15:23        18:13          19:25          │
│                ▲                                                     │
│                                                                      │
├──────────────────────────────────────────────────────────────────────┤
│ Kajian ba'da Maghrib · Tema ...                                     │
└──────────────────────────────────────────────────────────────────────┘
```

Catatan: jangan bungkus setiap prayer dalam card berat. Gunakan alignment, whitespace, weight, dan satu highlight untuk next prayer.

## Wireframe — APPROACHING_PRAYER

```text
┌──────────────────────────────────────────────────────────────────────┐
│ MASJID AL-IKHLAS                                      11:54          │
│                                                                      │
│                              DZUHUR                                  │
│                                                                      │
│                              06:42                                   │
│                         MENUJU ADZAN                                 │
│                                                                      │
│                            12:01                                     │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

Konten non-esensial disuppress.

## Wireframe — ADHAN

```text
┌──────────────────────────────────────────────────────────────────────┐
│                                                                      │
│                              DZUHUR                                  │
│                                                                      │
│                              12:01                                   │
│                                                                      │
│                          WAKTU ADZAN                                 │
│                                                                      │
│                    حَيَّ عَلَى الصَّلَاةِ                            │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

Tidak ada ticker, QRIS, carousel, atau jadwal penuh.

## Wireframe — IQAMAH COUNTDOWN

```text
┌──────────────────────────────────────────────────────────────────────┐
│                                                                      │
│                         IQAMAH DZUHUR                                │
│                                                                      │
│                             08:42                                    │
│                                                                      │
│                       MENUJU IQAMAH                                  │
│                                                                      │
│                 Persiapkan dan rapatkan shaf                        │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

Countdown adalah focal point terbesar.

## Wireframe — PRAYER

```text
┌──────────────────────────────────────────────────────────────────────┐
│                                                                      │
│                                                                      │
│                         LURUSKAN DAN                                 │
│                         RAPATKAN SHAF                               │
│                                                                      │
│                       صَفُّوا صُفُوفَكُمْ                            │
│                                                                      │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

## Wireframe — INFORMATION

```text
┌──────────────────────────────────────────────────────────────────────┐
│ MASJID AL-IKHLAS                                      10:42          │
│                                                                      │
│                         KAJIAN RUTIN                                 │
│                                                                      │
│                    Sabtu · Ba'da Maghrib                            │
│                         Ust. Fulan                                  │
│                                                                      │
│                 Menjaga Hati di Era Digital                         │
│                                                                      │
│ DZUHUR 12:01 · berikutnya dalam 01:18                               │
└──────────────────────────────────────────────────────────────────────┘
```

Prayer context tetap tersedia tetapi tidak harus dominan.

## Wireframe — DONATION / QRIS

Hanya ketika NORMAL/INFORMATION diizinkan. QR besar, tujuan donasi jelas, prayer context tetap tersedia. Jangan menampilkan nominal palsu atau progress tanpa sumber data nyata.

## Typography baseline 1080p

- Hero clock/countdown: ~160–240px tergantung state.
- Prayer/state heading: ~64–96px.
- Prayer schedule time: ~48–64px.
- Secondary/date: ~28–36px.
- Jangan gunakan body text kecil seperti dashboard desktop.

Nilai final ditentukan lewat visual testing dari jarak simulasi, bukan dianggap fixed contract.

## Layout rules

- Gunakan tabular numerals untuk jam/countdown.
- Maksimal satu visual accent utama.
- Hindari border/card berlebihan.
- Background harus memiliki contrast stabil.
- Foto background membutuhkan overlay yang menjamin readability.
- Animasi state transition 200–500ms; hindari motion dekoratif terus-menerus.
- Ticker tidak boleh bergerak saat ADHAN/IQAMAH/PRAYER.

## Theme contract

Theme dapat mengubah color tokens, background treatment, font pairing yang approved, radius/decorative details. Theme tidak boleh mengubah urutan hierarchy, safe area, state behavior, atau menyembunyikan prayer information.

## Required dev feature

Display development build wajib memiliki state switcher tersembunyi/dev-only untuk preview seluruh state dan clock override. Fitur ini tidak tampil pada production kiosk.