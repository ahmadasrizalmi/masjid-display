# 00 — SSOT Index

Status: **Normative / Source of Truth**

Dokumen ini adalah pintu masuk spesifikasi Masjid Display.

## Keputusan arsitektur yang dikunci

MVP adalah **dua APK Android native yang berkomunikasi lokal**:

- TV App = autonomous runtime/source of truth operasional.
- Admin App = configurator + media sender.
- Komunikasi = Wi-Fi/LAN lokal.
- Persistence TV = Room/SQLite + local media storage.
- Lisensi = lifetime serial, validasi sepenuhnya offline.

Tidak ada web app/PWA/backend/cloud untuk **runtime aplikasi**. Asri Digital hanya menjadi kanal penjualan/generasi/distribusi serial; APK tidak melakukan validasi lisensi online.

Dilarang menambahkan Supabase, Cloudflare runtime, cloud database/storage, online account, remote cloud sync, license API call, telemetry licensing, atau infrastructure alternatif ke APK MVP tanpa revisi SSOT eksplisit.

## Aturan prioritas

Jika ada konflik:

1. `00-SSOT-INDEX.md`
2. Dokumen domain paling spesifik (`03`–`07`, `09`, `10`)
3. `01-PRODUCT.md`
4. `02-ARCHITECTURE.md`
5. `08-IMPLEMENTATION.md`
6. README

Tidak ada dokumen legacy yang authoritative.

## Peta dokumen

- `01-PRODUCT.md` — fitur, persona, MVP, non-goals.
- `02-ARCHITECTURE.md` — Kotlin/Android modules, persistence, LAN protocol, security.
- `03-DOMAIN-DATA.md` — prayer schedule, config, iqamah, announcement, model data.
- `04-STATE-MACHINE.md` — otoritas behavior temporal display.
- `05-UI-TV.md` — wireframe dasar TV dan visual hierarchy.
- `06-UI-ADMIN.md` — flow dasar Admin APK.
- `07-OFFLINE-SYNC.md` — local persistence, discovery, pairing, protocol, transfer file.
- `08-IMPLEMENTATION.md` — phase build, test, definition of done.
- `09-UI-DESIGN-SYSTEM.md` — **otoritas konstruksi visual detail**: tokens, dimensions, component anatomy, wireframe, behavior UI.
- `10-OFFLINE-LICENSING.md` — lifetime serial dan offline validation.
- `LICENSE-APP.md` — filosofi lisensi yang dibaca pengguna.

Jika `05/06` dan `09` berbeda dalam detail visual, gunakan `09`. Jika visual bertentangan dengan behavior state, `04` menang untuk behavior.

## Prinsip lintas dokumen

- TV tetap berfungsi setelah HP admin disconnect.
- Internet bukan bagian dari runtime display, pairing, transfer, atau license validation.
- Informasi sholat memiliki prioritas visual tertinggi.
- State machine mengontrol UI; Composable tidak menentukan business state sendiri.
- File media dikirim langsung HP → TV melalui LAN.
- IP address bukan identitas permanen perangkat.
- Pairing aman dan mudah bagi pengurus non-teknis.
- Theme tidak mengubah hierarchy/state behavior.
- TV dioptimalkan untuk readability jarak jauh.
- Reference image tidak pernah menjadi asset production; hanya pola desain yang diekstrak.

## Protokol AI/code generator

Untuk setiap task:

1. Identifikasi phase dan module.
2. Baca Index + dokumen domain yang diperlukan.
3. Untuk pekerjaan UI, **wajib baca `09-UI-DESIGN-SYSTEM.md`** selain dokumen screen terkait.
4. Nyatakan acceptance criteria.
5. Implement perubahan kecil dan terverifikasi.
6. Tambah/update test.
7. Verifikasi tidak ada dependency cloud/web/backend yang masuk.
8. Jangan mengarang visual jika dimensinya sudah didefinisikan di `09`.
9. Update SSOT terlebih dahulu jika keputusan produk/arsitektur berubah.

Hindari generate seluruh aplikasi sekaligus.