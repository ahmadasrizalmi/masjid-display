# 06 — UI Admin APK

Admin adalah aplikasi Android native untuk konfigurasi TV dan transfer media melalui LAN. Tidak ada login online/web dashboard.

## First launch

```text
┌──────────────────────────────┐
│ Masjid Display Admin         │
│                              │
│ Hubungkan TV masjid          │
│                              │
│ [ Scan QR dari TV ]          │
│                              │
│ TV dan HP harus berada       │
│ pada jaringan lokal yang     │
│ dapat saling terhubung.      │
└──────────────────────────────┘
```

Setelah pairing, device disimpan sebagai trusted TV dan ditemukan kembali otomatis via NSD/mDNS.

## Navigation MVP

```text
Beranda
Jadwal
Konten
Media
Tampilan
Pengaturan
```

## Home

```text
┌──────────────────────────────┐
│ Masjid Al-Ikhlas             │
│ TV Utama            ● Lokal  │
├──────────────────────────────┤
│ Sholat berikutnya            │
│ DZUHUR              12:01    │
│ 01:18 lagi                   │
├──────────────────────────────┤
│ [ Preview Display ]          │
│ [ Kirim Foto ]               │
├──────────────────────────────┤
│ Pengumuman aktif        2    │
│ Penyimpanan TV       1.2 GB  │
└──────────────────────────────┘
```

Status koneksi berarti reachability LAN ke TV paired, bukan internet status.

## Prayer settings

```text
┌──────────────────────────────┐
│ Jadwal Sholat                │
├──────────────────────────────┤
│ Subuh      04:51   offset 0  │
│ Dzuhur     12:01   offset +2 │
│ Ashar      15:23   offset 0  │
│ Maghrib    18:13   offset +1 │
│ Isya       19:25   offset 0  │
├──────────────────────────────┤
│ Menuju iqamah                │
│ Subuh 10m · Dzuhur 10m       │
│ Ashar 10m · Maghrib 7m       │
│ Isya 10m                     │
└──────────────────────────────┘
```

## Media

```text
┌──────────────────────────────┐
│ Foto di TV              [+]  │
├──────────────────────────────┤
│ [img] Kajian-01.jpg          │
│ [img] Pembangunan.jpg        │
│ [img] Kegiatan-anak.jpg      │
├──────────────────────────────┤
│ [ Pilih & Kirim Foto ]       │
└──────────────────────────────┘
```

Android Photo Picker digunakan agar app tidak membutuhkan akses gallery luas yang tidak perlu.

## Transfer progress

```text
┌──────────────────────────────┐
│ Mengirim 8 foto              │
│                              │
│ 5 selesai                    │
│ 2 sedang dikirim             │
│ 1 menunggu                   │
│                              │
│ ████████████░░░  72%         │
│                              │
│ kegiatan-07.jpg       41%    │
└──────────────────────────────┘
```

File gagal memiliki action Retry. File sukses tidak dikirim ulang.

## Content

Announcement CRUD sederhana. QRIS diperlakukan sebagai konfigurasi/media lokal. Save dikirim ke TV dan baru ditampilkan sukses setelah TV memvalidasi/persist.

## Appearance

Admin memilih layout/theme tervalidasi, bukan drag-and-drop bebas. MVP boleh hanya memiliki Focus layout production-quality.

## Preview

Admin dapat meminta/menampilkan preview state NORMAL, APPROACHING, ADHAN, IQAMAH, PRAYER, FRIDAY, INFORMATION. Preview tidak mengubah state runtime ibadah TV yang sebenarnya.

## Connection errors

UI harus membedakan:

- TV belum paired
- TV paired tetapi tidak ditemukan
- TV ditemukan tetapi tidak reachable
- pairing/credential invalid
- protocol version incompatible
- storage TV penuh
- transfer terputus
- payload/config invalid

Jangan menyederhanakan semua error menjadi “tidak ada internet”. Internet tidak relevan untuk komunikasi produk.

## UX rule

Flow normal tidak meminta pengguna mengetahui IP address, port, hostname, database, atau detail jaringan teknis.