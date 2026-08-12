# 02 — Architecture & Tech Stack

## Keputusan arsitektur final MVP

Masjid Display adalah **sistem Android native, local-only, tanpa backend dan tanpa cloud**. Terdapat dua APK:

1. **TV App** — otak sistem dan runtime display pada Android TV/STB Android.
2. **Admin App** — remote/configurator pada HP Android yang berkomunikasi langsung dengan TV melalui jaringan lokal.

Tidak ada web admin, server, akun online, database cloud, remote API, atau storage cloud pada arsitektur MVP.

## Prinsip authority

TV App adalah authoritative runtime. Setelah konfigurasi diterima dan disimpan TV, HP admin boleh disconnect/mati/dibawa pulang dan TV harus tetap berjalan penuh.

Admin App hanya mengedit konfigurasi, mengirim command/config/media, melihat status, dan preview.

## Tech stack

### Platform
- Kotlin
- Android SDK
- Gradle Kotlin DSL
- Multi-module Android project

### UI
- Jetpack Compose
- Android TV/Compose TV patterns untuk TV App
- Material 3 Compose untuk Admin App
- Shared design tokens boleh berada di core, tetapi layout TV dan HP tidak dipaksa berbagi komponen yang sama

### Persistence
- Room di atas SQLite untuk data terstruktur
- DataStore untuk preference kecil
- Android internal app storage untuk foto/logo/QR/background

### Async/runtime
- Kotlin Coroutines
- Flow/StateFlow
- WorkManager hanya untuk pekerjaan lokal terjadwal yang memang perlu survive process restart

### Local communication
- Android NSD / mDNS untuk discovery TV pada LAN
- QR pairing untuk bootstrap trusted relationship
- HTTP lokal pada TV sebagai transport aplikasi MVP, bind hanya ke interface lokal yang diperlukan
- JSON serialization menggunakan kotlinx.serialization
- Protocol versioning wajib
- Pairing token/session credential tidak boleh dikirim ulang sebagai plaintext UI

### Media
- Android Photo Picker pada Admin App
- Stream file langsung HP → TV melalui LAN
- TV memvalidasi metadata, ukuran, tipe, checksum, lalu menyimpan ke internal storage
- Metadata media disimpan Room
- Tidak ada upload internet

### Prayer & time
- Prayer calculation adalah Kotlin module lokal tanpa network dependency
- Gunakan `java.time`/desugaring untuk date/time dan timezone
- Timezone masjid eksplisit
- Countdown = target absolute time - current time

### Testing
- JUnit untuk domain/unit tests
- kotlinx-coroutines-test
- Room in-memory tests
- Compose UI tests
- Android instrumented tests untuk pairing/transfer kritis

## Struktur project target

```text
app-tv/
app-admin/
core/
  domain/
  prayer/
  database/
  protocol/
  network-local/
  media/
  designsystem/
docs/
```

## Dependency direction

- `app-tv` dan `app-admin` bergantung pada module `core` yang diperlukan.
- `core:domain` tidak bergantung pada Android UI.
- `core:protocol` mendefinisikan message/DTO/version contract HP ↔ TV.
- `core:network-local` mengimplementasikan discovery, pairing, transport.
- `core:database` berisi Room entities/DAO/migration.
- `core:prayer` tidak membutuhkan network.

## TV runtime

1. Start Android TV app.
2. Load konfigurasi lokal dari Room.
3. Resolve waktu/tanggal/timezone masjid.
4. Hitung/load jadwal sholat lokal.
5. Jalankan display state resolver.
6. Render fullscreen.
7. Advertise local service untuk Admin App bila LAN tersedia.
8. Terima config/media hanya dari admin yang sudah paired.

Tidak adanya LAN/internet tidak boleh menghentikan display setelah setup selesai.

## Pairing model

TV menampilkan QR pairing berisi data bootstrap minimum seperti device identifier, local endpoint hint, protocol version, dan one-time pairing secret. Admin App scan QR, menemukan/menghubungi TV di LAN, melakukan handshake, lalu menyimpan trusted device relationship.

Setelah pairing pertama, Admin App menggunakan NSD/mDNS untuk menemukan kembali TV meski alamat IP DHCP berubah.

## Security boundary

- Hanya perangkat paired yang boleh mengubah config/media.
- Pairing secret harus one-time/berumur pendek.
- Endpoint mutasi membutuhkan credential hasil pairing.
- Validate seluruh payload sebelum persist.
- Batasi MIME type dan ukuran media.
- Nama file dari client tidak boleh dipakai langsung sebagai filesystem path.
- TV tidak membuka administrative endpoint ke internet.

## Deployment

TV App didistribusikan sebagai APK Android TV/STB. Admin App sebagai APK Android phone. Keduanya harus usable tanpa akun dan tanpa layanan server.

## Non-goals arsitektur

- Web/PWA
- React/TypeScript runtime
- Supabase
- Cloudflare
- Backend/server internet
- Cloud database/storage
- Remote control dari luar LAN
- Login/account online
- Sinkronisasi internet
- Ketergantungan prayer-time API

Jika requirement masa depan membutuhkan cloud, itu adalah proyek/keputusan arsitektur baru dan **tidak boleh dipersiapkan secara spekulatif dalam MVP ini**.