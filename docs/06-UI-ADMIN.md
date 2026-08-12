# 06 — UI Admin

Admin adalah mobile-first control surface. Admin tidak meniru UI TV.

## Navigation MVP

```text
Beranda
Jadwal
Konten
Tampilan
Pengaturan
```

## Onboarding flow

```text
Welcome
  ↓
Identitas Masjid
  ↓
Lokasi + Timezone
  ↓
Metode Jadwal
  ↓
Koreksi Waktu
  ↓
Durasi Iqamah
  ↓
Preview TV
  ↓
Simpan / Hubungkan Display
```

## Wireframe — Home

```text
┌──────────────────────────────┐
│ Masjid Al-Ikhlas        ●    │
│ Display online               │
├──────────────────────────────┤
│ Sholat berikutnya            │
│ DZUHUR              12:01    │
│ 01:18 lagi                   │
├──────────────────────────────┤
│ [ Preview Display ]          │
├──────────────────────────────┤
│ Pengumuman aktif        2    │
│ Terakhir sinkron    10:41    │
└──────────────────────────────┘
```

## Wireframe — Prayer settings

```text
┌──────────────────────────────┐
│ Jadwal Sholat                │
│ Kamis, 13 Agustus            │
├──────────────────────────────┤
│ Subuh      04:51   offset 0  │
│ Dzuhur     12:01   offset +2 │
│ Ashar      15:23   offset 0  │
│ Maghrib    18:13   offset +1 │
│ Isya       19:25   offset 0  │
├──────────────────────────────┤
│ Durasi menuju iqamah         │
│ Subuh  10m                   │
│ Dzuhur 10m                   │
│ Ashar  10m                   │
│ Maghrib 7m                   │
│ Isya   10m                   │
└──────────────────────────────┘
```

## Wireframe — Content

```text
┌──────────────────────────────┐
│ Konten                  [+]  │
├──────────────────────────────┤
│ ● Kajian rutin               │
│   aktif sampai Sabtu         │
├──────────────────────────────┤
│ ● Informasi parkir           │
│   selalu aktif               │
├──────────────────────────────┤
│ ○ QRIS pembangunan           │
│   nonaktif                   │
└──────────────────────────────┘
```

## Wireframe — Display appearance

Admin memilih layout/theme yang sudah tervalidasi, bukan drag-and-drop bebas.

```text
┌──────────────────────────────┐
│ Tampilan                     │
├──────────────────────────────┤
│ Layout                       │
│ [ Focus ✓ ] [ Sidebar ]      │
│                              │
│ Theme                        │
│ [ Midnight ] [ Emerald ]     │
│                              │
│ [ Preview semua state ]      │
└──────────────────────────────┘
```

MVP dapat mengirim hanya `Focus` sebagai production layout; opsi lain tidak boleh menjadi blocker.

## Preview

Preview harus dapat memaksa state NORMAL, APPROACHING, ADHAN, IQAMAH, PRAYER, FRIDAY, INFORMATION tanpa mengubah clock/config produksi.

## Save behavior

- Form divalidasi sebelum persist.
- Perubahan lokal admin memberi status Saving/Saved/Error.
- Remote config tidak dianggap aktif di display sampai display menerima dan memvalidasinya.
- Jangan menggunakan optimistic state untuk perubahan yang dapat merusak prayer schedule tanpa recovery.