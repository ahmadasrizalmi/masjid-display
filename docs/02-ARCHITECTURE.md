# 02 — Architecture & Tech Stack

## Architecture decision

MVP menggunakan **TypeScript monorepo** dengan dua aplikasi web yang berbagi domain package. Display dibuat sebagai PWA/fullscreen web app sehingga dapat dijalankan pada Chromium kiosk, Android TV browser/wrapper, STB, mini-PC, atau Raspberry Pi-class device tanpa menggandakan business logic.

## Tech stack

### Runtime & language
- Node.js LTS
- TypeScript strict mode
- pnpm workspaces

### Frontend
- React
- Vite
- React Router untuk Admin; Display sebisa mungkin single-route
- Tailwind CSS untuk design tokens/utilities
- CSS variables untuk theme runtime
- Lucide untuk icon admin; icon pada TV diminimalkan

### State & validation
- Zustand untuk client/app state sederhana
- Zod untuk runtime schema/config validation
- Domain state machine ditulis sebagai pure TypeScript reducer/transition engine; hindari mengikat business rules ke React component lifecycle

### Data
- IndexedDB via Dexie untuk persistence lokal Display
- LocalStorage hanya untuk preference kecil non-kritis
- Backend fase berikutnya: Supabase (PostgreSQL, Auth, Storage, Realtime) agar admin remote dapat sinkron ke device

### Prayer calculation
- Adhan.js sebagai kandidat calculation engine lokal.
- Semua output calculation melewati domain adapter milik aplikasi agar library dapat diganti tanpa mengubah UI.
- Timezone harus eksplisit menggunakan IANA timezone.

### Date/time
- Luxon untuk timezone-aware date/time operations.
- Countdown dihitung dari `targetTimestamp - now`, bukan decrement interval state.

### Testing
- Vitest untuk unit/domain tests
- React Testing Library untuk component behavior
- Playwright untuk end-to-end dan visual state flows

### Quality
- ESLint
- Prettier
- TypeScript `strict: true`
- GitHub Actions: lint + typecheck + test + build

## Monorepo target

```text
apps/
  display/       # fullscreen TV app
  admin/         # mobile-first admin
packages/
  domain/        # state machine, prayer rules, types
  ui/            # shared primitive/token; jangan paksa layout TV=Admin
  config/        # schemas/default config
  prayer/        # calculation adapter
  storage/       # local persistence/sync contracts
docs/            # SSOT
```

## Dependency direction

`apps -> packages`

`domain` tidak boleh mengimpor React, browser storage, Supabase, atau UI.

`prayer` menghasilkan domain-compatible schedule.

`storage` mengimplementasikan interface persistence/sync; domain tidak mengetahui IndexedDB/Supabase.

## Display runtime

1. Boot shell.
2. Load last-known validated config dari IndexedDB.
3. Resolve timezone/current date.
4. Load/calculate schedule.
5. Start state engine.
6. Render state.
7. Jika online, sync config/content di background.
8. Persist data valid baru secara atomik.

## Deployment target

### Development
Browser desktop dengan viewport preset 1920x1080 dan dev state switcher.

### Production MVP
PWA hosted HTTPS + fullscreen/kiosk launcher pada device. Cache app shell dengan service worker. Device dapat reboot dan kembali ke display tanpa interaksi rutin.

### Future native wrapper
Jika Android TV membutuhkan autostart/device APIs, bungkus Display dengan Capacitor/native WebView tanpa memindahkan domain logic.

## Security boundaries

- Display device mendapatkan identifier/token terbatas; bukan admin credential.
- Admin authentication hanya berada pada Admin App.
- QRIS MVP adalah media/identifier konfigurasi, bukan pemrosesan transaksi.
- Config dari remote harus lolos Zod validation sebelum mengganti last-known-good config.

## Architectural non-goals

- Microservices pada MVP.
- Redux/global event bus besar.
- Business logic di component React.
- Cloud dependency untuk menentukan state sholat.
- Mengambil prayer time API setiap menit.