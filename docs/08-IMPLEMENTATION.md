# 08 — Implementation Plan

## Rule of execution

Implementasi **component-first dan vertical-slice**, runnable/testable. Jangan generate seluruh aplikasi sekaligus. Untuk UI wajib mengikuti `09-UI-DESIGN-SYSTEM.md` sehingga model tanpa vision dapat membangun hasil yang konsisten.

## Phase 0 — Android Foundation

- Gradle Kotlin DSL multi-module
- `app-tv`, `app-admin`
- `core:domain`, `core:prayer`, `core:database`, `core:protocol`, `core:network-local`, `core:media`, `core:designsystem`, `core:license`
- Kotlin/Compose baseline
- lint/test/build CI

**Done:** kedua APK debug build; unit test baseline berjalan.

## Phase 1 — Design System Components

Implement **tanpa business logic**:

### Shared tokens
- color tokens
- typography
- spacing
- radius
- icon sizing

### TV primitives
1. `TvHeader`
2. `PrayerCell`
3. `PrayerBar`
4. `PrayerSidebarRow`
5. `MediaSurface`
6. `InformationBar`
7. `FocusStateContent`

### Admin primitives
1. `AdminTopBar`
2. `SetupStepper`
3. `SettingsCard`
4. `PrayerSettingCard`
5. `DeviceStatusCard`
6. `PrimaryButton`
7. `MediaGrid`
8. `TransferProgress`

**Done:** Compose previews/sample screens menunjukkan dimensions/hierarchy sesuai doc 09; tidak ada network/database/domain dependency.

## Phase 2 — Prayer Domain

- canonical Kotlin models
- local prayer calculation
- offsets
- timezone
- daily schedule
- iqamah target
- tests

**Done:** fixture deterministic tanpa network.

## Phase 3 — Display State Engine

- pure Kotlin resolver
- approaching
- adhan
- iqamah
- prayer
- Friday
- notice priority
- boundary tests

**Done:** transition diuji sebelum/saat/setelah boundary.

## Phase 4 — TV Screens

Urutan implementasi:
1. `NORMAL_HORIZONTAL_MEDIA`
2. APPROACHING
3. ADHAN
4. IQAMAH
5. PRAYER
6. FRIDAY
7. INFORMATION/NOTICE
8. `NORMAL_SIDEBAR_MEDIA`
9. dev-only state/clock switcher

**Done:** semua state previewable pada 1280×720, 1920×1080, 3840×2160; tidak ada layout jump pada prayer highlight.

## Phase 5 — TV Room Persistence

- Room entities/DAO/migrations
- config repository
- media metadata
- boot configured/unconfigured
- runtime Flow observation

**Done:** restart mempertahankan config; display berjalan tanpa network.

## Phase 6 — Offline Licensing

- `core:license` offline serial validator
- activation persistence
- Admin activation screen
- invalid/valid tests
- no network permission/dependency required by validator

**Done:** serial valid dapat mengaktifkan onboarding dalam airplane/no-network condition.

## Phase 7 — Local Discovery & Pairing

- TV NSD advertise
- Admin NSD discovery
- QR pairing
- one-time pairing secret
- trusted credential
- protocol negotiation
- DHCP/IP change reconnect

**Done:** pair/reconnect tanpa input IP.

## Phase 8 — Admin Screens

Urutan:
1. Activation
2. Pair TV
3. Mosque setup
4. Setup review
5. Home
6. Prayer settings + edit bottom sheet
7. Adhan/Iqamah
8. Friday
9. Announcements
10. Display appearance
11. Device status

**Done:** config dikirim LAN, divalidasi TV, persist Room, runtime bereaksi.

## Phase 9 — Local Media Transfer

- Photo Picker
- media grid
- multi-select
- upload session
- stream LAN
- temp file/checksum
- atomic save
- progress/retry
- delete/storage-full

**Done:** multi-photo transfer tanpa internet; individual retry.

## Phase 10 — Hardening

- security/pairing tests
- DB migration tests
- Compose UI tests
- transfer interruption
- image memory pressure
- process death/restart
- TV reboot/autostart
- accessibility/contrast
- performance soak

## Component task protocol

Satu coding task idealnya hanya satu komponen atau satu behavior. Contoh pertama:

> Implement `core:designsystem` tokens and TV `TvHeader` only. Read docs/00-SSOT-INDEX.md and docs/09-UI-DESIGN-SYSTEM.md. Add Compose previews at 1080p-equivalent constraints. Do not implement prayer calculation, database, network, license, or full screen.

Task berikutnya baru `PrayerCell`, lalu `PrayerBar`, dst.

## Definition of Done

Task belum selesai jika:
- dimensions/hierarchy menyimpang dari doc 09 tanpa SSOT update,
- business rule baru tanpa test,
- build/test gagal,
- domain logic ditempel di Composable,
- TV membutuhkan HP agar prayer runtime berjalan,
- IP di-hardcode,
- media/config dikirim via internet,
- license validator melakukan network call,
- backend/cloud dependency ditambahkan,
- reference branding/assets disalin,
- TODO menggantikan acceptance criteria utama.

## Change management

Ubah SSOT → test expectation → code. Jangan meninggalkan arsitektur/desain lama sebagai fallback komentar yang dapat membingungkan generator.