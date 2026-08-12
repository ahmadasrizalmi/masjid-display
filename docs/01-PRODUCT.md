# 01 — Product Blueprint

## Product statement

Masjid Display adalah sistem digital signage masjid **local-only** yang terdiri dari APK Android TV dan APK Android Admin. TV menampilkan jadwal sholat dan menjalankan transisi ibadah otomatis; HP pengurus mengatur konfigurasi dan mengirim media langsung melalui LAN.

## Masalah yang diselesaikan

- Display masjid sering terlalu padat dan sulit dibaca.
- Setup/kontrol TV sering membutuhkan remote/keyboard yang merepotkan.
- Transfer foto ke TV pada banyak solusi sejenis terlalu rumit atau tidak stabil.
- Sistem masjid harus tetap bekerja tanpa bergantung server/internet.

## Persona

### Jamaah
Membutuhkan waktu, sholat berikutnya, countdown, jadwal, dan state adzan/iqamah yang terbaca cepat dari jarak jauh.

### Pengurus masjid
Membutuhkan setup lewat HP, koreksi jadwal, iqamah, Jumat, announcement, tampilan, dan transfer foto yang sederhana.

## Product surfaces

### TV App
Android TV/STB APK, fullscreen 16:9, autonomous, unattended.

### Admin App
Android phone APK, mobile-first native UI. Pair ke TV melalui LAN dan mengontrol konfigurasi/media tanpa server.

## Golden path

Install TV → QR pairing → scan dari Admin App → konfigurasi masjid → kirim ke TV → TV berjalan mandiri → pengurus dapat reconnect otomatis di LAN untuk perubahan berikutnya.

## MVP wajib

- Identitas masjid, koordinat, timezone, logo.
- Jadwal Subuh, Syuruq informasional, Dzuhur, Ashar, Maghrib, Isya.
- Koreksi waktu per sholat.
- Durasi iqamah per sholat.
- Masehi + Hijriah.
- Current time + next prayer countdown.
- Automatic state transitions.
- Friday mode.
- Announcement.
- QRIS opsional.
- Minimum satu layout TV production-quality.
- Admin APK.
- QR pairing.
- Auto discovery/reconnect TV di LAN.
- Local configuration protocol.
- Multi-photo transfer HP → TV dengan progress/retry.
- Local Room/SQLite persistence.

## Non-goals MVP

- Web admin.
- Cloud/backend/server.
- Remote control dari luar LAN.
- Online account/login.
- Multi-mosque organization management.
- Analytics kompleks.
- Payment processing.
- Live streaming.
- Marketplace template.
- AI content generation.
- Video editor.

## UX principles

1. Jamaah menangkap informasi utama <= 3 detik.
2. Satu display state memiliki satu focal point.
3. Jadwal tidak kalah oleh announcement/donasi.
4. Countdown berbasis absolute target time.
5. Pengurus tidak perlu mengetik IP pada flow normal.
6. Pairing pertama harus dapat dilakukan dengan scan QR.
7. Transfer foto harus terasa seperti mengirim file ke perangkat dekat: pilih → kirim → progress → selesai.
8. TV tidak membutuhkan HP setelah konfigurasi tersimpan.

## Success criteria

- TV reboot dan kembali menampilkan jadwal dari data lokal.
- State berubah pada boundary waktu yang benar.
- Admin dapat pair tanpa mengetik IP.
- IP DHCP berubah tetapi paired TV dapat ditemukan kembali.
- Config yang dikirim Admin tervalidasi dan tersimpan di TV.
- Beberapa foto dapat dikirim melalui LAN dengan retry file gagal.
- Internet mati/tidak tersedia tidak memengaruhi operasi display.
- UI kritis terbaca pada target Android TV 720p/1080p/4K.