# Masjid Display — TV UI Preview

> Visual checkpoint untuk implementasi TV presentation berdasarkan SSOT. Dokumen ini menjadi pendamping visual untuk `05-UI-TV.md` dan `09-UI-DESIGN-SYSTEM.md`; bila ada konflik detail implementasi, dokumen SSOT tetap menjadi sumber kebenaran utama.

![Masjid Display TV UI Design System](./assets/tv-ui-design-system.jpg)

## Cakupan visual

Preview di atas merangkum komponen dan composition yang sudah diimplementasikan pada branch development saat checkpoint ini:

- `TvHeader`
- `SidebarHeader`
- `PrayerCell`
- `PrayerBar`
- `PrayerSidebarRow`
- `MediaSurface`
- `InformationBar`
- `NormalHorizontalMediaLayout`
- `NormalSidebarMediaLayout`
- `FocusStateContent`
- `ApproachingScreen`
- `AdhanScreen`
- `IqamahScreen`
- `PrayerScreen`
- `FridayScreen`
- `NoticeScreen`

## Bahasa visual

UI TV memakai hierarki informasi besar dan mudah dibaca dari jarak jauh, tabular numerals untuk seluruh waktu, safe area konsisten, media edge-to-edge pada state NORMAL, serta warm focus palette dengan accent emas untuk state yang membutuhkan perhatian penuh. Highlight tidak boleh menyebabkan layout jump.

## Layout baseline

Target utama adalah `1920 × 1080 (16:9)`.

### NORMAL_HORIZONTAL_MEDIA

```text
┌─────────────────────────────────────────────────────┐
│                    TvHeader 170                     │
├─────────────────────────────────────────────────────┤
│                                                     │
│                  MediaSurface 720                   │
│                                                     │
├─────────────────────────────────────────────────────┤
│                    PrayerBar 190                    │
└─────────────────────────────────────────────────────┘
```

### NORMAL_SIDEBAR_MEDIA

```text
┌────────── 430 ──────────┬───────────────────────────┐
│       SidebarHeader / header area 180               │
├─────────────────────────┼───────────────────────────┤
│ PrayerSidebarRow × 6    │                           │
│ fixed / equal rows      │       MediaSurface        │
│                         │       edge-to-edge        │
├─────────────────────────┴───────────────────────────┤
│                InformationBar 100                   │
└─────────────────────────────────────────────────────┘
```

## Focus states

State `APPROACHING`, `ADHAN`, `IQAMAH`, `PRAYER`, `FRIDAY`, dan `NOTICE` menggunakan grammar visual yang sama melalui `FocusStateContent`. Fokus utama adalah satu pesan penting, negative space besar, tanpa prayer table/media/ticker yang mengganggu. Countdown tidak boleh negatif dan state PRAYER/FRIDAY tidak menjadikan jam sebagai focal point.

## Catatan implementasi

Visual ini adalah mockup original Masjid Display yang dibuat dari pola desain dan SSOT proyek, bukan salinan aset atau identitas produk referensi. Nama masjid, data, layout treatment, palette, dan composition digunakan sebagai dokumentasi internal implementasi proyek.
