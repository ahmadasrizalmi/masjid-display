# 00 — SSOT Index

Status: **Normative / Source of Truth**

Dokumen ini adalah pintu masuk spesifikasi Masjid Display.

## Keputusan arsitektur yang dikunci

MVP adalah **dua APK Android native yang berkomunikasi lokal**:

- TV App = autonomous runtime/source of truth operasional.
- Admin App = configurator + media sender.
- Komunikasi = Wi-Fi/LAN lokal.
- Persistence TV = Room/SQLite + local media storage.

**Dilarang menambahkan web app, PWA, backend internet, cloud database/storage, online account, remote cloud sync, Supabase, Cloudflare, atau infrastructure alternatif ke implementasi MVP.**

Jika kebutuhan itu muncul di masa depan, SSOT harus direvisi secara eksplisit terlebih dahulu. Jangan membuat abstraction/fallback spekulatif untuk cloud.

## Aturan prioritas

Jika ada konflik:

1. `00-SSOT-INDEX.md`
2. Dokumen domain paling spesifik (`03`–`07`)
3. `01-PRODUCT.md`
4. `02-ARCHITECTURE.md`
5. `08-IMPLEMENTATION.md`

Tidak ada dokumen legacy yang dianggap authoritative.

## Peta dokumen

- `01-PRODUCT.md` — fitur, persona, MVP, non-goals.
- `02-ARCHITECTURE.md` — Kotlin/Android modules, persistence, LAN protocol, security.
- `03-DOMAIN-DATA.md` — prayer schedule, config, iqamah, announcement, model data.
- `04-STATE-MACHINE.md` — otoritas behavior temporal display.
- `05-UI-TV.md` — wireframe 16:9 dan visual hierarchy.
- `06-UI-ADMIN.md` — Admin APK, pairing, settings, media UX.
- `07-OFFLINE-SYNC.md` — local persistence, discovery, pairing, protocol, transfer file.
- `08-IMPLEMENTATION.md` — phase build, test, definition of done.

## Prinsip lintas dokumen

- TV harus tetap berfungsi setelah HP admin disconnect.
- Internet bukan bagian dari runtime produk.
- Informasi sholat memiliki prioritas visual tertinggi.
- State machine mengontrol UI; Composable tidak menentukan business state sendiri.
- File media dikirim langsung HP → TV melalui LAN.
- IP address tidak boleh menjadi identitas permanen perangkat.
- Pairing harus aman dan mudah bagi pengurus non-teknis.
- Theme tidak boleh mengubah hierarchy/state behavior.
- Optimalkan readability TV dari jarak jauh.

## Protokol AI/code generator

Untuk setiap task:

1. Identifikasi phase dan module.
2. Baca Index + dokumen domain yang diperlukan saja.
3. Nyatakan acceptance criteria.
4. Implement perubahan kecil dan terverifikasi.
5. Tambah/update test.
6. Verifikasi tidak ada dependency cloud/web/backend yang masuk.
7. Update SSOT terlebih dahulu jika keputusan produk/arsitektur berubah.

Hindari generate seluruh aplikasi sekaligus.