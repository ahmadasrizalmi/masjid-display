# 07 — Local Persistence, Pairing & Transfer

## Core rule

Sistem **tidak memiliki cloud sync**. Semua operasi produk terjadi pada perangkat Android dan LAN lokal.

TV App adalah source of truth runtime. Admin App adalah configurator/client lokal.

## TV persistence

TV menyimpan secara lokal:

- mosque config
- prayer calculation settings
- corrected prayer settings
- iqamah settings
- Friday settings
- announcements
- display/theme config
- paired admin identities/credentials
- media metadata
- media files
- schema version

Data terstruktur: Room/SQLite. File media: Android internal app storage.

## Admin persistence

Admin menyimpan data yang diperlukan untuk UX admin dan hubungan perangkat, termasuk daftar TV paired dan credential lokal. Data Admin bukan dependency agar TV terus beroperasi.

## Boot TV

### Sudah dikonfigurasi
Load Room → hitung jadwal → resolve state → render. Network tidak diperlukan.

### Belum dikonfigurasi
Tampilkan setup/pairing screen. Admin App melakukan konfigurasi awal melalui LAN.

### LAN tidak tersedia setelah setup
Display tetap berjalan normal. Fitur edit/transfer dari HP sementara tidak tersedia sampai LAN tersedia kembali.

## Discovery

TV advertise service lokal via Android NSD/mDNS. Admin App mencari service tersebut dan mencocokkannya dengan trusted device identifier.

IP address bukan identitas permanen dan tidak boleh menjadi satu-satunya key perangkat.

## Pairing

1. TV membuat one-time pairing session.
2. TV menampilkan QR + kode fallback.
3. Admin scan QR.
4. Admin menemukan/menghubungi endpoint lokal TV.
5. Handshake memverifikasi one-time secret + protocol version.
6. TV menerbitkan credential trusted admin.
7. Pairing session ditutup dan secret tidak dapat dipakai ulang.

## Local protocol

Protocol memiliki `protocolVersion` dan message contract eksplisit. Minimal capability:

```text
GetDeviceInfo
GetStatus
GetConfig
UpdateMosqueConfig
UpdatePrayerSettings
UpdateIqamahSettings
UpdateFridaySettings
ListAnnouncements
UpsertAnnouncement
DeleteAnnouncement
ListMedia
UploadMedia
DeleteMedia
UpdateDisplaySettings
```

Mutasi harus mengembalikan hasil sukses/error yang dapat ditampilkan Admin App.

## Media transfer

Flow:

```text
Android Photo Picker
  → Admin validates basic metadata
  → request upload session
  → stream bytes over LAN
  → TV writes temporary file
  → verify size/type/checksum
  → atomic move to final local storage
  → persist Room metadata
  → response success
```

Jika transfer gagal, temporary file dibersihkan. File existing tidak boleh rusak karena upload baru gagal.

## Transfer UX

- Bisa memilih beberapa foto.
- Tampilkan progress per file dan total.
- Retry file gagal tanpa mengulang semua file yang sukses.
- TV tidak perlu membuka file picker.
- Admin tidak perlu mengetik IP address pada flow normal.
- Error harus membedakan: TV tidak ditemukan, beda jaringan/tidak reachable, pairing invalid, storage penuh, file unsupported, transfer terputus.

## File constraints

MVP menerima format gambar yang ditentukan implementasi Android (minimal JPEG/PNG/WebP yang tervalidasi). Tetapkan batas ukuran dan resolusi yang masuk akal sebelum implementasi transfer. TV boleh melakukan decode/resize terkontrol untuk mencegah memory pressure.

## Local configuration update

TV menerima payload → validate → transaction Room → response sukses → runtime observe perubahan via Flow → recalculate bila perubahan memengaruhi prayer schedule.

Payload invalid tidak boleh mengganti konfigurasi valid yang sedang berjalan.

## Recovery

Jika database record invalid/corrupt, jangan menebak konfigurasi. Masuk recovery/setup yang jelas untuk pengurus. Error teknis dicatat ke log aplikasi, bukan ditampilkan sebagai stack trace pada layar jamaah.

## Clock reliability

Display tetap bergantung pada clock perangkat yang benar. TV harus menggunakan timezone masjid dari konfigurasi untuk perhitungan/display. Pengaturan automatic date/time perangkat direkomendasikan, tetapi internet bukan bagian dari protocol aplikasi.