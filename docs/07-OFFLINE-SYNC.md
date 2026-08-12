# 07 — Offline & Sync

## Core rule

Internet adalah enhancement untuk administrasi/sinkronisasi. Internet **bukan dependency untuk menentukan kapan sholat/adzan/iqamah state terjadi**.

## Last-known-good model

Display menyimpan:

- validated mosque config
- prayer calculation settings
- schedule cache
- active announcements/content metadata
- branding assets yang diperlukan
- schema versions
- last successful sync timestamp

Remote payload baru hanya mengganti data aktif setelah validation sukses.

## Boot matrix

### Online + local cache
Render cache segera, sync background, lalu swap data secara aman jika remote valid.

### Offline + local cache
Render cache dan hitung schedule lokal. Tampilkan offline indicator kecil.

### Online + no cache
Pair/setup atau fetch initial config; setelah valid, persist lalu start display.

### Offline + no cache
Tampilkan setup/recovery error. Jangan membuat jadwal berdasarkan lokasi asumsi.

## Service worker

Cache static app shell dan immutable assets. Jangan menganggap service worker sebagai database konfigurasi.

## IndexedDB

Gunakan transactional persistence untuk config/schedule/content metadata. Simpan schema version dan migration.

## Sync strategy MVP

- Sync saat boot jika online.
- Periodic background refresh dengan interval konservatif.
- Sync setelah reconnect.
- Admin-triggered change dapat menggunakan realtime/polling di fase backend, tetapi Display tetap memvalidasi payload.

## Conflict policy

Server adalah source of truth untuk admin-managed configuration setelah pairing. Display mempertahankan last-known-good local copy sebagai runtime fallback.

Local runtime state seperti current countdown tidak di-upload sebagai configuration.

## Assets

Logo/QR/background yang diperlukan harus dicache setelah config diterima. Jika asset baru gagal diunduh, jangan hapus asset lama yang masih valid.

## Connectivity indicator

Indicator offline kecil pada NORMAL/INFORMATION. Jangan menampilkan banner besar selama prayer states selama core data masih valid.

## Recovery

Jika persisted data corrupt:

1. Reject record invalid.
2. Coba last-known-good/version sebelumnya bila tersedia.
3. Jika tidak ada config valid, masuk ERROR/setup recovery.
4. Log diagnostic tanpa menampilkan detail teknis ke jamaah.

## Clock reliability

Offline operation tetap bergantung pada clock device yang benar. Deployment guide harus menganjurkan automatic date/time + timezone/NTP. Aplikasi tidak boleh diam-diam mengganti timezone masjid dengan timezone device.