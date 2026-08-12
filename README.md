# Masjid Display

Digital prayer-time signage designed for mosque TVs.

> Product principle: **this is a mosque display first, not a dashboard stretched to a TV.**

## Product goal

A mosque administrator should be able to connect an Android TV/STB/mini-PC, choose the mosque/location, configure prayer and iqamah offsets, and get a readable, calm, reliable display that keeps working when the internet is unavailable.

## MVP

- Daily prayer schedule based on mosque location
- Per-prayer manual time correction
- Gregorian + Hijri date
- Current time and next-prayer countdown
- Per-prayer iqamah countdown
- Adhan, iqamah, prayer, and Friday modes
- Announcements / running information
- Optional QRIS donation panel
- Mosque identity: name, logo, location
- Offline-first local cache
- TV-safe 16:9 layouts
- Mobile-friendly administrator UI

## Display states

The display is state-driven rather than a collection of unrelated templates:

`NORMAL -> APPROACHING_PRAYER -> ADHAN -> IQAMAH_COUNTDOWN -> PRAYER -> NORMAL`

Additional states: `FRIDAY`, `INFORMATION`, `OFFLINE`, and `ERROR`.

See [`docs/UI-WIREFRAME.md`](docs/UI-WIREFRAME.md) for the implementation-facing wireframe and [`docs/PRODUCT-SPEC.md`](docs/PRODUCT-SPEC.md) for behavior and MVP scope.

## Design rules

1. Readable from the back of a mosque.
2. Prayer context always outranks promotional/informational content.
3. One dominant piece of information per state.
4. No navbar, mouse-oriented controls, or dashboard chrome on the TV surface.
5. Avoid dense cards. Use whitespace deliberately.
6. Important prayer transitions must work without internet.
7. Themes change appearance; they must not break information hierarchy.

## Planned surfaces

- **Display App** — fullscreen TV experience.
- **Admin App** — setup and content management from phone/desktop.

## Status

Product definition + low-fidelity UI wireframe.