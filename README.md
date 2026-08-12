# Masjid Display

Native Android prayer-time signage for mosque TVs with a companion Android admin app.

> Product principle: **the TV is autonomous; the admin phone is a local remote/configurator.**

## Architecture

Masjid Display is local-only:

- Android TV/STB App — Kotlin + Jetpack Compose, Room/SQLite, local media storage.
- Android Admin App — Kotlin + Jetpack Compose.
- Phone ↔ TV communication — local Wi-Fi/LAN using discovery, QR pairing, and a versioned local protocol.
- Media transfer — direct from Android Photo Picker on the phone to TV local storage.

There is no web admin, internet backend, cloud database/storage, or online account in the MVP.

## Core behavior

The TV calculates prayer schedules locally, persists configuration locally, runs the prayer display state machine itself, and continues operating without the admin phone or network connectivity after setup.

`NORMAL -> APPROACHING_PRAYER -> ADHAN -> IQAMAH_COUNTDOWN -> PRAYER -> NORMAL`

Additional display behavior includes Friday and information modes.

## Documentation

Start with [`docs/00-SSOT-INDEX.md`](docs/00-SSOT-INDEX.md). It routes implementation tasks to smaller normative documents so developers and code generators do not need to consume one oversized specification.

Indonesian overview: [`README.id.md`](README.id.md).

## Implementation rule

Do not introduce cloud/backend/web architecture or speculative fallback infrastructure. Architecture changes must first be approved and reflected in the SSOT.