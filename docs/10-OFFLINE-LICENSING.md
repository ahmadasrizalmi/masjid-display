# 10 — Offline Licensing

Status: **Normative**

## Product decision

Masjid Display menggunakan **lisensi lifetime sekali bayar dengan validasi serial sepenuhnya offline**.

Asri Digital adalah tempat penjualan/generasi/distribusi serial. Asri Digital **bukan runtime license server** dan APK tidak melakukan validasi online.

## Required behavior

```text
User input serial
      ↓
Offline validator
      ↓
valid? ── no → tampilkan error
  │
 yes
  ↓
persist activation locally
      ↓
continue onboarding
```

Tidak ada:
- API activation call,
- account login,
- cek status pembayaran dari APK,
- cek jumlah aktivasi,
- device binding,
- periodic license check,
- expiration,
- remote revocation.

## Serial implementation principle

Jangan menyimpan daftar seluruh serial valid dalam APK. Gunakan serial yang dapat diverifikasi secara lokal, misalnya payload/signature/checksum scheme yang hanya membutuhkan verifier material di aplikasi.

Detail algoritma/secret generation **bukan dokumentasi UI** dan tidak boleh diekspos pada layar pengguna atau log production.

## Activation persistence

Setelah serial valid:
- simpan activation state lokal secara durable;
- restart/reboot tidak meminta serial kembali;
- upgrade database/app mempertahankan activation jika storage aplikasi tidak dihapus;
- uninstall/clear app data boleh memerlukan input serial kembali.

## UX

```text
AKTIVASI MASJID DISPLAY

Masukkan serial number yang Anda dapatkan
saat membeli Masjid Display.

[ XXXX - XXXX - XXXX - XXXX ]

[ Aktifkan ]

Beli sekali • Tanpa biaya bulanan
Validasi dilakukan langsung di perangkat
```

Error copy:
- `Serial number tidak valid. Periksa kembali kode yang dimasukkan.`
- Jangan tampilkan error jaringan karena validasi tidak menggunakan internet.

## Commercial philosophy

Lisensi bukan DRM agresif. Satu serial secara teknis dapat digunakan ulang jika mekanisme offline memungkinkan. Produk tidak menambah telemetry/device binding hanya untuk mencegah hal tersebut.

Pengguna yang mampu diharapkan membeli lisensi resmi untuk mendukung kebutuhan pengembang dan keberlanjutan aplikasi. Filosofi lengkap untuk pengguna ada di `LICENSE-APP.md`.

## Architecture boundary

Licensing hanya menjadi gate onboarding/use entitlement. Setelah valid, license module tidak ikut menentukan prayer schedule, display state, LAN pairing, media transfer, atau runtime TV.

Jangan membuat dependency dari `core:domain` prayer/display ke implementation licensing.