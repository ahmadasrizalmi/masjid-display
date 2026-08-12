# 01 — Product Blueprint

## Product statement

Masjid Display adalah sistem digital signage khusus masjid yang menampilkan waktu, jadwal sholat, transisi adzan/iqamah/sholat, Jumat, dan informasi masjid secara otomatis pada TV.

## Masalah yang diselesaikan

Display masjid sering terlalu padat, sulit dibaca dari jauh, membutuhkan internet terus-menerus, atau menggunakan template generik. Produk ini mengutamakan konteks ibadah dan keterbacaan.

## Persona utama

### Jamaah
Membutuhkan waktu sekarang, jadwal sholat, sholat berikutnya, dan informasi transisi yang terbaca dalam beberapa detik dari jarak jauh.

### Pengurus masjid
Membutuhkan setup sederhana, koreksi jadwal, iqamah, Jumat, pengumuman, identitas masjid, dan preview tanpa mengoperasikan TV secara langsung.

## Surfaces

### Display App
Fullscreen 16:9, unattended, berjalan sepanjang hari, tidak memiliki navigation chrome.

### Admin App
Mobile-first web UI untuk konfigurasi, konten, jadwal, preview, dan status perangkat.

## MVP — wajib

- Mosque setup: nama, lokasi, timezone, koordinat, logo opsional.
- Prayer times: Subuh, Syuruq opsional, Dzuhur, Ashar, Maghrib, Isya.
- Per-prayer correction offset.
- Per-prayer iqamah duration.
- Gregorian + Hijri date.
- Current time.
- Next prayer + countdown.
- Automatic display state transitions.
- Friday mode.
- Announcement ticker/card.
- QRIS donation card opsional.
- Offline cache dan deterministic boot.
- Minimum satu layout TV yang production-quality.
- Admin settings + live preview dasar.

## Bukan MVP

- Multi-mosque organization management.
- Analytics kompleks.
- Donation payment processing.
- Live streaming.
- Marketplace template.
- Social media integrations.
- AI content generation.
- Video editor.

## UX principles

1. Jamaah harus menangkap informasi utama dalam <= 3 detik.
2. Satu state memiliki satu focal point.
3. Jadwal tidak boleh kalah secara visual oleh announcement/donasi.
4. Countdown harus menggunakan waktu absolut, bukan decrement counter yang mudah drift.
5. Admin harus dapat preview state tanpa menunggu waktu sholat asli.
6. Semua core state harus dapat diuji menggunakan dev state switcher.

## Success criteria MVP

- Boot tanpa internet tetap menghasilkan jadwal valid dari data/cache yang tersedia.
- State berubah otomatis pada boundary waktu yang benar.
- Tidak ada teks kritis di luar TV safe area.
- Semua sholat dapat memiliki koreksi dan iqamah berbeda.
- Admin dapat menyelesaikan konfigurasi awal tanpa keyboard/mouse pada TV.
- Informasi kritis tetap terbaca pada 720p, 1080p, dan 4K scaling.