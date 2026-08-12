# Masjid Display

Aplikasi display jadwal sholat dan informasi masjid untuk TV 16:9.

> Prinsip utama: **ini adalah layar operasional masjid, bukan dashboard web yang diperbesar ke TV.**

## Tujuan

Pengurus masjid cukup menghubungkan Android TV/STB/mini-PC ke TV, memilih lokasi masjid, mengatur koreksi jadwal dan iqamah, lalu display berjalan otomatis serta tetap aman ketika internet terputus.

## Dokumen SSOT

Dokumentasi sengaja dipecah agar implementasi oleh developer maupun AI tidak bergantung pada satu file sangat panjang.

| Dokumen | Fungsi |
|---|---|
| `docs/00-SSOT-INDEX.md` | Titik masuk dan aturan membaca spesifikasi |
| `docs/01-PRODUCT.md` | Scope, persona, requirement, non-goals |
| `docs/02-ARCHITECTURE.md` | Arsitektur dan tech stack |
| `docs/03-DOMAIN-DATA.md` | Model data dan aturan waktu sholat |
| `docs/04-STATE-MACHINE.md` | State display dan transisi |
| `docs/05-UI-TV.md` | Wireframe dan aturan UI TV |
| `docs/06-UI-ADMIN.md` | Wireframe admin dan flow konfigurasi |
| `docs/07-OFFLINE-SYNC.md` | Offline-first, cache, sync, recovery |
| `docs/08-IMPLEMENTATION.md` | Struktur kode, urutan build, definition of done |

## MVP

- Jadwal sholat berdasarkan lokasi
- Koreksi waktu per sholat
- Jam, tanggal Masehi dan Hijriah
- Countdown sholat berikutnya
- Countdown iqamah per sholat
- Mode adzan, iqamah, sholat, Jumat, dan informasi
- Pengumuman/running information
- QRIS opsional
- Identitas masjid
- Offline-first
- Display 16:9 TV-safe
- Admin mobile-friendly

## Cara menggunakan dokumentasi saat coding

1. Mulai dari `docs/00-SSOT-INDEX.md`.
2. Baca hanya dokumen yang relevan dengan task yang sedang dikerjakan.
3. Jangan membuat behavior baru yang bertentangan dengan SSOT.
4. Jika implementasi membutuhkan keputusan baru, dokumentasikan keputusan terlebih dahulu.
5. UI harus mengikuti state machine; jangan membuat state visual independen yang tidak terdefinisi.

Dokumen lama `PRODUCT-SPEC.md` dan `UI-WIREFRAME.md` tetap menjadi referensi historis sampai seluruh informasi pentingnya termigrasi ke struktur SSOT.