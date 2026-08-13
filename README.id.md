# Masjid Display

Sistem display jadwal sholat dan informasi masjid berbasis **Android native, local-only, tanpa cloud**.

> Prinsip utama: **TV adalah otak sistem. HP admin adalah remote/configurator. Setelah setup, TV harus dapat berjalan mandiri.**

## Produk

Masjid Display terdiri dari dua APK:

- **Masjid Display TV** — dipasang di Android TV/STB, menampilkan jadwal dan menjalankan seluruh state sholat.
- **Masjid Display Admin** — dipasang di HP Android pengurus untuk konfigurasi dan transfer media langsung ke TV melalui Wi-Fi/LAN lokal.

Tidak ada akun online, web admin, backend internet, database cloud, atau cloud storage untuk operasi aplikasi.

## Lisensi: Sekali Bayar, Offline Selamanya

Masjid Display dibuat dengan harapan dapat memberi manfaat bagi masjid dan jamaah serta menjadi bagian dari amal yang terus mengalir. Pada saat yang sama, pengembangan aplikasi membutuhkan waktu, tenaga, perangkat, dan biaya hidup. Karena itu aplikasi menggunakan **lisensi sekali bayar**, tanpa langganan bulanan.

Serial number resmi didistribusikan melalui Asri Digital, tetapi **validasi serial dilakukan sepenuhnya di perangkat**. Aplikasi tidak bergantung pada server lisensi dan tidak membutuhkan internet untuk aktivasi maupun penggunaan sehari-hari.

Kami mengharapkan pengguna yang mampu memperoleh lisensi secara resmi sebagai bentuk dukungan terhadap keberlanjutan pengembangan. Sistem ini sengaja tidak menggunakan DRM agresif. Apabila aplikasi pada akhirnya digunakan lebih luas karena serial dibagikan atau mekanisme lisensinya dilewati, kami tetap berharap manfaat yang diterima masjid dan jamaah menjadi kebaikan serta amal jariyah. Rezeki dapat datang melalui banyak jalan; lisensi ini adalah salah satu ikhtiar agar pengembangan dapat terus berjalan sambil memenuhi kebutuhan sehari-hari.

Ketentuan dan filosofi lengkap: [`LICENSE-APP.md`](LICENSE-APP.md).

## Visual UI Blueprint

Visual concept digunakan sebagai companion untuk wireframe tekstual. Implementasi harus memperlakukan visual sebagai **arah desain**, sedangkan behavior, hierarchy, dan state tetap mengikuti dokumen SSOT.

Visual mencakup state TV utama serta flow Admin seperti Jadwal, Pengumuman, Media, Photo Picker, transfer progress, daftar perangkat, QR pairing, dan Pengaturan.

### Aturan membaca visual

1. Visual bukan izin untuk menambah state baru.
2. `docs/04-STATE-MACHINE.md` tetap otoritas behavior.
3. `docs/05-UI-TV.md` tetap otoritas hierarchy/safe-area TV.
4. `docs/06-UI-ADMIN.md` tetap otoritas flow Admin APK.
5. Teks kecil/detail hasil mockup boleh disempurnakan saat implementasi; struktur dan focal point yang disetujui harus dipertahankan.
6. Jangan menambahkan konsep cloud/online dari interpretasi visual.

## Pengalaman utama

1. Install/buka TV App.
2. Aktivasi menggunakan serial number valid secara offline.
3. TV menampilkan QR pairing.
4. Buka Admin App di HP pada jaringan lokal yang sama.
5. Scan QR dan pair.
6. Atur identitas masjid, lokasi, jadwal, koreksi, iqamah, Jumat, tampilan, dan pengumuman.
7. Pilih foto dari HP dan kirim langsung ke TV melalui LAN.
8. TV menyimpan semuanya lokal dan terus berjalan walau HP tidak terhubung.

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
| `LICENSE-APP.md` | Filosofi dan ketentuan lisensi aplikasi |

## MVP

- Jadwal sholat lokal berdasarkan lokasi
- Koreksi per sholat
- Countdown sholat dan iqamah
- Masehi + Hijriah
- State Adzan/Iqamah/Sholat/Jumat
- Pengumuman dan QRIS opsional
- Android TV fullscreen 16:9
- Admin APK Android
- Offline lifetime serial activation
- Pairing QR dan auto-discovery TV di LAN
- Transfer beberapa foto HP → TV
- Local SQLite/Room persistence
- Tidak membutuhkan internet untuk operasi produk

## Aturan coding

Mulai dari `docs/00-SSOT-INDEX.md`. Baca hanya dokumen domain yang relevan dengan task. Jangan menambahkan cloud/backend/web stack atau arsitektur alternatif tanpa keputusan SSOT baru.