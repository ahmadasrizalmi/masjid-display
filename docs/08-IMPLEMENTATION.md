# 08 — Implementation Plan

## Rule of execution

Jangan generate seluruh aplikasi dalam satu prompt/commit. Implementasi dilakukan per vertical slice dan setiap slice harus runnable/testable.

## Phase 0 — Foundation

- pnpm workspace
- TypeScript strict
- apps/display
- apps/admin
- packages/domain/config/prayer/storage/ui
- lint/typecheck/test/build scripts
- CI

**Done:** clean install + lint + typecheck + test + build berhasil.

## Phase 1 — Domain clock & prayer schedule

- canonical types
- prayer adapter
- correction offsets
- timezone handling
- daily schedule
- unit tests

**Done:** fixture location/date menghasilkan schedule deterministic dan corrected schedule teruji.

## Phase 2 — State engine

- `resolveDisplayState`
- approaching threshold
- adhan duration
- iqamah target
- prayer duration
- Friday override skeleton
- boundary tests

**Done:** seluruh transition memiliki test detik sebelum/saat/setelah boundary.

## Phase 3 — TV Focus UI

- 1920×1080 design tokens
- Normal
- Approaching
- Adhan
- Iqamah
- Prayer
- Information
- offline indicator
- dev state/clock switcher

**Done:** seluruh state dapat dipreview tanpa backend dan lolos screenshot review 720p/1080p/4K viewport.

## Phase 4 — Local persistence/offline

- Dexie schema
- config validation
- last-known-good
- schedule cache
- service worker/app shell
- boot scenarios

**Done:** reload offline mempertahankan display valid.

## Phase 5 — Admin local MVP

- onboarding
- mosque/location settings
- offsets
- iqamah settings
- content
- appearance
- state preview

**Done:** admin config dapat menghasilkan config schema valid dan preview display.

## Phase 6 — Remote backend

- Supabase project/schema
- admin auth
- mosque/device records
- config/content sync
- asset storage
- device pairing/token

**Done:** perubahan admin dapat diterima display, divalidasi, dipersist, dan survive offline.

## Phase 7 — Hardening

- E2E
- visual regression critical states
- error recovery
- kiosk/autostart deployment notes
- accessibility/contrast
- performance/memory soak

## Suggested first file boundaries

```text
packages/domain/src/types.ts
packages/domain/src/state/resolveDisplayState.ts
packages/domain/src/state/resolveDisplayState.test.ts
packages/prayer/src/calculateSchedule.ts
packages/config/src/schema.ts
apps/display/src/screens/*
apps/display/src/dev/StateSwitcher.tsx
```

## AI generation protocol

Setiap generation task harus menyebut:

- file/directory scope
- dokumen SSOT yang wajib dibaca
- acceptance criteria
- tests yang harus dibuat/diupdate
- hal yang tidak boleh diubah

Contoh task yang baik:

> Implement Phase 2 state resolver only. Read docs/00-SSOT-INDEX.md, docs/03-DOMAIN-DATA.md, docs/04-STATE-MACHINE.md. Do not build UI. Add boundary unit tests for Dhuhr flow.

Contoh task buruk:

> Build the whole Masjid Display app according to docs.

## Definition of Done umum

Sebuah task belum selesai jika:

- hanya UI tanpa domain behavior yang diperlukan,
- ada type error/lint failure,
- business rule baru tidak ditest,
- behavior menyimpang dari SSOT,
- offline-critical logic membutuhkan network tanpa alasan,
- TODO menggantikan acceptance criteria utama.

## Change management

Jika keputusan berubah:

1. Update dokumen domain SSOT terkait.
2. Update test expectation.
3. Implement code.
4. Catat breaking/config migration jika ada.

Dengan urutan ini dokumentasi tetap menjadi blueprint aktual, bukan dokumentasi yang ditulis setelah code.