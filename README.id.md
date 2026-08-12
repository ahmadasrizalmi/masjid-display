# Masjid Display

Sistem display jadwal sholat dan informasi masjid berbasis **Android native, local-only, tanpa cloud**.

> Prinsip utama: **TV adalah otak sistem. HP admin adalah remote/configurator. Setelah setup, TV harus dapat berjalan mandiri.**

## Produk

Masjid Display terdiri dari dua APK:

- **Masjid Display TV** — dipasang di Android TV/STB, menampilkan jadwal dan menjalankan seluruh state sholat.
- **Masjid Display Admin** — dipasang di HP Android pengurus untuk konfigurasi dan transfer media langsung ke TV melalui Wi-Fi/LAN lokal.

Tidak ada akun online, web admin, backend internet, database cloud, atau cloud storage.

## Pengalaman utama

1. Install/buka TV App.
2. TV menampilkan QR pairing.
3. Buka Admin App di HP pada jaringan lokal yang sama.
4. Scan QR dan pair.
5. Atur identitas masjid, lokasi, jadwal, koreksi, iqamah, Jumat, tampilan, dan pengumuman.
6. Pilih foto dari HP dan kirim langsung ke TV melalui LAN.
7. TV menyimpan semuanya lokal dan terus berjalan walau HP tidak terhubung.

## Tech stack

- Kotlin
- Android SDK
- Jetpack Compose / Compose TV
- Gradle Kotlin DSL multi-module
- Room + SQLite
- DataStore
- Kotlin Coroutines + Flow
- Android NSD/mDNS
- QR pairing
- Local HTTP protocol + kotlinx.serialization
- Android internal storage untuk media
- Android Photo Picker
- JUnit + Compose/instrumented tests

## SSOT

| Dokumen | Fungsi |
|---|---|
| `docs/00-SSOT-INDEX.md` | Titik masuk dan aturan SSOT |
| `docs/01-PRODUCT.md` | Scope, persona, MVP, non-goals |
| `docs/02-ARCHITECTURE.md` | Arsitektur Android native dan tech stack |
| `docs/03-DOMAIN-DATA.md` | Domain/data dan aturan jadwal |
| `docs/04-STATE-MACHINE.md` | State display dan transisi |
| `docs/05-UI-TV.md` | Wireframe/aturan UI TV |
| `docs/06-UI-ADMIN.md` | Wireframe/flow Admin APK |
| `docs/07-OFFLINE-SYNC.md` | Persistence lokal, pairing, protocol, transfer media |
| `docs/08-IMPLEMENTATION.md` | Urutan implementasi dan DoD |

## MVP

- Jadwal sholat lokal berdasarkan lokasi
- Koreksi per sholat
- Countdown sholat dan iqamah
- Masehi + Hijriah
- State Adzan/Iqamah/Sholat/Jumat
- Pengumuman
- QRIS opsional
- Android TV fullscreen 16:9
- Admin APK Android
- Pairing QR
- Auto-discovery TV di LAN
- Transfer beberapa foto HP → TV
- Local SQLite/Room persistence
- Tidak membutuhkan internet untuk operasi produk

## Aturan coding

Mulai dari `docs/00-SSOT-INDEX.md`. Baca hanya dokumen domain yang relevan dengan task. Jangan menambahkan cloud/backend/web stack atau arsitektur alternatif tanpa keputusan SSOT baru.