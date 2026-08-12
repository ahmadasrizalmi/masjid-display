# 08 — Implementation Plan

## Rule of execution

Jangan generate seluruh aplikasi dalam satu task. Implementasi per vertical slice, runnable dan testable. Tidak boleh menambahkan backend/cloud/web stack.

## Phase 0 — Android Foundation

- Gradle Kotlin DSL multi-module project
- `app-tv`
- `app-admin`
- `core:domain`
- `core:prayer`
- `core:database`
- `core:protocol`
- `core:network-local`
- `core:media`
- `core:designsystem`
- Kotlin/Compose baseline
- lint/test/build CI

**Done:** kedua APK debug dapat dibuild; unit test berjalan.

## Phase 1 — Prayer Domain

- canonical Kotlin models
- prayer calculation lokal
- correction offsets
- timezone
- daily schedule
- iqamah target calculation
- unit tests

**Done:** fixture location/date menghasilkan schedule deterministic tanpa network.

## Phase 2 — Display State Engine

- pure Kotlin state resolver
- approaching threshold
- adhan duration
- iqamah target
- prayer duration
- Friday override
- boundary tests

**Done:** transition diuji sebelum/saat/setelah boundary.

## Phase 3 — Android TV Focus UI

- fullscreen Compose TV
- Normal
- Approaching
- Adhan
- Iqamah
- Prayer
- Friday
- Information
- dev-only clock/state preview

**Done:** seluruh state dapat dipreview pada emulator/perangkat tanpa Admin App.

## Phase 4 — TV Room Persistence

- Room entities/DAO
- migrations
- config repository
- media metadata
- boot configured/unconfigured
- runtime observe config

**Done:** restart app/device mempertahankan konfigurasi dan display dapat berjalan tanpa network.

## Phase 5 — Local Discovery & Secure Pairing

- TV NSD advertise
- Admin NSD discovery
- pairing QR
- one-time secret
- trusted device credential
- protocol version negotiation
- reconnect setelah IP DHCP berubah

**Done:** HP dapat pair dan reconnect ke TV tanpa mengetik IP.

## Phase 6 — Admin APK

- paired device home
- mosque/location
- prayer correction
- iqamah
- Friday
- announcements
- display appearance
- status
- preview

**Done:** perubahan dari Admin dikirim via LAN, divalidasi TV, tersimpan Room, dan langsung memengaruhi runtime yang relevan.

## Phase 7 — Local Media Transfer

- Android Photo Picker
- multi-select
- upload session
- streaming LAN
- temporary file
- checksum/validation
- atomic save
- progress
- retry per-file
- delete media
- storage-full handling

**Done:** beberapa foto dapat dikirim HP → TV tanpa internet dan kegagalan satu file tidak merusak file lain.

## Phase 8 — Hardening

- pairing/security tests
- database migration tests
- UI tests
- transfer interruption tests
- large image/memory tests
- process death/restart
- TV reboot/autostart behavior
- accessibility/contrast
- performance soak

## Target source boundaries

```text
app-tv/src/main/...
app-admin/src/main/...
core/domain/src/main/...
core/prayer/src/main/...
core/database/src/main/...
core/protocol/src/main/...
core/network-local/src/main/...
core/media/src/main/...
core/designsystem/src/main/...
```

## AI generation protocol

Setiap task harus menyebut:

- phase
- file/module scope
- dokumen SSOT yang wajib dibaca
- acceptance criteria
- test yang wajib dibuat/diupdate
- hal yang dilarang diubah

Contoh:

> Implement Phase 2 state resolver only in core:domain. Read docs/00-SSOT-INDEX.md, docs/03-DOMAIN-DATA.md, docs/04-STATE-MACHINE.md. Pure Kotlin only. No Android UI, network, database, backend, or cloud dependencies. Add boundary unit tests.

## Definition of Done

Task belum selesai jika:

- business rule baru tanpa test,
- build/test gagal,
- logic domain ditempel di Composable,
- TV membutuhkan HP agar state prayer terus berjalan,
- IP address di-hardcode,
- media/config dikirim melalui internet,
- dependency backend/cloud/web ditambahkan,
- behavior bertentangan dengan SSOT,
- TODO menggantikan acceptance criteria utama.

## Change management

Keputusan arsitektur harus diubah di SSOT terlebih dahulu, baru code/test. Jangan meninggalkan arsitektur lama sebagai fallback atau komentar alternatif karena dapat membingungkan code generator.