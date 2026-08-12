# 00 — SSOT Index

Status: **Normative / Source of Truth**

Dokumen ini adalah pintu masuk spesifikasi Masjid Display. Tujuannya menjaga proses coding—termasuk coding oleh AI—tetap konsisten tanpa harus membaca satu dokumen raksasa.

## Aturan prioritas

Jika ada konflik spesifikasi, gunakan urutan berikut:

1. `00-SSOT-INDEX.md` untuk aturan dokumentasi.
2. Dokumen domain yang paling spesifik (`03`–`07`).
3. `01-PRODUCT.md` untuk scope produk.
4. `02-ARCHITECTURE.md` untuk batas teknis.
5. `08-IMPLEMENTATION.md` untuk urutan implementasi.
6. Dokumen legacy hanya sebagai referensi historis.

**Jangan menebak jika SSOT belum menentukan behavior penting.** Tambahkan keputusan ke dokumen yang tepat sebelum implementasi.

## Peta dokumen

### 01 — Product
Baca ketika menentukan fitur, acceptance criteria, prioritas, atau apakah sesuatu masuk MVP.

### 02 — Architecture
Baca sebelum membuat package, service, dependency, deployment, API, storage, atau memilih library.

### 03 — Domain & Data
Baca untuk prayer schedule, koreksi waktu, iqamah, mosque config, announcement, dan model persistence.

### 04 — State Machine
Baca untuk semua behavior display. Ini adalah otoritas utama tentang apa yang tampil pada waktu tertentu.

### 05 — UI TV
Baca ketika mengimplementasikan komponen/display 16:9, typography, safe area, hierarchy, atau responsive TV.

### 06 — UI Admin
Baca ketika mengimplementasikan onboarding, settings, content management, preview, atau kontrol admin.

### 07 — Offline & Sync
Baca untuk cache, koneksi internet, persistence, boot, sync, fallback, dan recovery.

### 08 — Implementation
Baca ketika membuat struktur repo, milestone, test, atau memilih task berikutnya.

## Prinsip lintas dokumen

- TV display adalah surface utama.
- Informasi sholat memiliki prioritas tertinggi.
- State machine mengontrol UI; komponen UI tidak menentukan state bisnis.
- Core prayer operation harus tetap berjalan offline.
- Admin dan Display dipisahkan secara konseptual.
- Theme boleh mengubah estetika, tidak boleh mengubah hierarchy informasi.
- Jangan memasukkan fitur di luar MVP hanya karena mudah dibuat.
- Optimalkan readability dari jarak jauh, bukan density informasi.

## Protokol kerja untuk AI/code generator

Untuk setiap task:

1. Identifikasi domain task.
2. Baca Index + maksimal dokumen domain yang diperlukan.
3. Nyatakan requirement/acceptance criteria yang sedang diimplementasikan.
4. Implementasikan perubahan sekecil mungkin.
5. Tambahkan/update test.
6. Verifikasi tidak ada konflik dengan state machine/offline rules.
7. Update SSOT jika keputusan produk/arsitektur berubah.

Hindari meminta generator membaca seluruh repo dan mengimplementasikan banyak milestone sekaligus. Kerjakan vertical slice kecil yang dapat diverifikasi.