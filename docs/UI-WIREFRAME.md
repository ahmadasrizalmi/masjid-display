# UI Wireframe — Masjid Display

Implementation-facing low-fidelity wireframe for a 16:9 mosque TV and mobile admin.

## 1. TV canvas

Reference canvas: **1920 x 1080 (16:9)**.

Keep critical content inside a 5% safe area (~96px horizontal / ~54px vertical at 1080p). Design must remain readable at 1280x720.

Hierarchy:

1. Current/prayer-state information
2. Next prayer / iqamah context
3. Daily prayer schedule
4. Mosque/date identity
5. Announcements and optional content

Do not put essential information in the bottom ticker.

---

## 2. Normal state

```text
┌──────────────────────────────────────────────────────────────────────────┐
│ [LOGO]  MASJID AL-IKHLAS                     KAMIS · 13 AGUSTUS 2026     │
│         Sleman, Yogyakarta                       29 SAFAR 1448 H          │
│                                                                          │
│                          SHOLAT BERIKUTNYA                                │
│                               DZUHUR                                     │
│                                                                          │
│                              10:42:38                                    │
│                                                                          │
│                           dalam 01:18:22                                 │
│                                                                          │
│    SUBUH         DZUHUR         ASHAR        MAGHRIB         ISYA         │
│    04:51         12:01          15:23         18:13          19:25        │
│                    ▲                                                     │
│                                                                          │
├──────────────────────────────────────────────────────────────────────────┤
│  ● Kajian ba'da Maghrib · Tema: Adab Bertetangga                        │
└──────────────────────────────────────────────────────────────────────────┘
```

### Rules

- Current clock is the strongest element in normal mode.
- Next prayer is highlighted without turning the schedule into five cards.
- Prayer names use uppercase/semibold; prayer times use tabular numerals.
- Ticker is one line maximum.
- Avoid continuously moving backgrounds behind time information.

---

## 3. Approaching prayer

Trigger default: configurable, suggested 10 minutes before adhan.

```text
┌──────────────────────────────────────────────────────────────────────────┐
│                              DZUHUR                                      │
│                                                                          │
│                              11:56                                      │
│                                                                          │
│                         WAKTU SHOLAT DALAM                               │
│                              04:38                                      │
│                                                                          │
│                    Bersiap untuk menunaikan sholat                       │
│                                                                          │
│              SUBUH 04:51   DZUHUR 12:01   ASHAR 15:23                   │
└──────────────────────────────────────────────────────────────────────────┘
```

Announcements disappear. Prayer transition owns the screen.

---

## 4. Adhan state

```text
┌──────────────────────────────────────────────────────────────────────────┐
│                                                                          │
│                              DZUHUR                                      │
│                                                                          │
│                              12:01                                      │
│                                                                          │
│                           WAKTU ADZAN                                    │
│                                                                          │
│                        حَيَّ عَلَى الصَّلَاةِ                              │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

No ticker, QRIS, event promotion, slideshow, or unrelated content.

---

## 5. Iqamah countdown

```text
┌──────────────────────────────────────────────────────────────────────────┐
│                          IQAMAH DZUHUR                                   │
│                                                                          │
│                              08:42                                      │
│                                                                          │
│                       MENUJU PELAKSANAAN                                 │
│                             SHOLAT                                      │
│                                                                          │
│                     Siapkan dan rapatkan shaf                            │
└──────────────────────────────────────────────────────────────────────────┘
```

Countdown is the dominant element. Last 30 seconds may simplify further; do not use stressful flashing effects.

---

## 6. Prayer state

At iqamah zero:

```text
┌──────────────────────────────────────────────────────────────────────────┐
│                                                                          │
│                                                                          │
│                         LURUSKAN DAN                                     │
│                         RAPATKAN SHAF                                    │
│                                                                          │
│                    أَقِيمُوا صُفُوفَكُمْ وَتَرَاصُّوا                       │
│                                                                          │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

After a configurable short period, dim/minimize the screen for the configured prayer duration. Avoid visually distracting animation while congregational prayer is in progress.

---

## 7. Information state

Information is temporal content, not permanent dashboard clutter.

```text
┌──────────────────────────────────────────────────────────────────────────┐
│ [MASJID AL-IKHLAS]                                      18:03            │
│                                                                          │
│                           KAJIAN RUTIN                                   │
│                                                                          │
│                       Sabtu · Ba'da Maghrib                              │
│                           Ust. Fulan                                     │
│                                                                          │
│                    “Menjaga Hati di Era Digital”                         │
│                                                                          │
│              Selanjutnya: MAGHRIB · 18:13 · dalam 10 menit              │
└──────────────────────────────────────────────────────────────────────────┘
```

The next-prayer context remains visible unless prayer-transition mode takes over.

---

## 8. Donation / QRIS information

```text
┌──────────────────────────────────────────────────────────────────────────┐
│                                                                          │
│                       PEMBANGUNAN MASJID                                 │
│                                                                          │
│               Dukungan jamaah dapat disalurkan melalui                  │
│                                                                          │
│                       ┌──────────────┐                                   │
│                       │              │                                   │
│                       │     QRIS     │                                   │
│                       │              │                                   │
│                       └──────────────┘                                   │
│                                                                          │
│                     Jazakumullahu khairan                                │
└──────────────────────────────────────────────────────────────────────────┘
```

QR content is optional and never shown during adhan/iqamah/prayer state.

---

## 9. Friday mode

Friday gets its own schedule behavior rather than pretending it is a normal Dhuhr.

Normal Friday display can show:

- Friday prayer time
- Khatib / imam / muadzin when configured
- Countdown to khutbah/Jumu'ah
- Friday-specific announcement

When khutbah/prayer begins, informational/promotional content disappears.

---

## 10. Offline state

Offline is **not** a full-screen error when cached schedule/configuration exists.

```text
Normal display remains operational
                                      [small] OFFLINE
```

Use cached prayer schedule, mosque configuration, theme and announcements. Sync again silently when connectivity returns.

Only show a blocking setup/error screen if the device has never received enough configuration to calculate/display a valid schedule.

---

# Admin mobile wireframe

Admin is a separate product surface. It may use conventional navigation/cards because it is interactive.

## Home

```text
┌──────────────────────────────┐
│ Masjid Al-Ikhlas        ⚙    │
│ Display online · synced      │
├──────────────────────────────┤
│                              │
│       10:42                  │
│   Dzuhur · 12:01             │
│                              │
├──────────────────────────────┤
│ Jadwal Sholat                │
│ Pengaturan Iqamah            │
│ Pengumuman                   │
│ Tampilan                     │
│ Jumat                        │
│ Perangkat                    │
└──────────────────────────────┘
```

## Prayer settings

```text
┌──────────────────────────────┐
│ ← Jadwal Sholat              │
├──────────────────────────────┤
│ Lokasi                       │
│ Sleman, Yogyakarta           │
│                              │
│ Koreksi waktu                │
│ Subuh       [ - ]  0  [ + ] │
│ Dzuhur      [ - ] +2  [ + ] │
│ Ashar       [ - ]  0  [ + ] │
│ Maghrib     [ - ] -1  [ + ] │
│ Isya        [ - ]  0  [ + ] │
│                              │
│ Durasi menuju iqamah         │
│ Subuh              10 menit  │
│ Dzuhur             10 menit  │
│ Ashar              10 menit  │
│ Maghrib             7 menit  │
│ Isya               10 menit  │
│                              │
│          [ Simpan ]          │
└──────────────────────────────┘
```

## Appearance

Choose **layout + theme**, not dozens of unrelated templates.

```text
Layout
(●) Focus
( ) Sidebar
( ) Cinematic

Theme
[ Emerald ] [ Midnight ] [ Warm ] [ Light ]

Background
(●) Solid
( ) Gradient
( ) Mosque photo

[ Preview TV ]
```

---

# Design tokens / implementation guidance

## Typography

Use a highly legible sans-serif for Latin text and a dedicated Arabic-capable font for Arabic content. Use tabular numerals for clock/countdowns.

Approximate 1080p starting scale (validate on a real TV):

| Role | Suggested size |
|---|---:|
| Hero clock/countdown | 160–220 px |
| State/prayer name | 52–72 px |
| Prayer schedule time | 44–56 px |
| Prayer schedule label | 24–32 px |
| Header/date | 24–32 px |
| Ticker | 28–36 px |

Do not blindly scale CSS pixels across platforms; these are visual targets for the 1920x1080 reference canvas.

## Spacing

Base grid: 8px. Prefer large gaps (24/32/48/64/96) on TV. Do not solve whitespace by adding more content.

## Motion

- State transition: subtle fade/crossfade ~300–600ms.
- Information slides: calm transitions.
- No flashing countdown.
- No decorative motion during adhan, iqamah or prayer.
- Respect low-end Android/STB performance.

## Contrast

All critical time/prayer text must maintain strong contrast over the selected background. If using photography, add a deterministic scrim/overlay; never rely on the photo itself being dark enough.

---

# Component map

TV primitives:

- `MosqueIdentity`
- `DateBlock`
- `HeroClock`
- `PrayerStateLabel`
- `Countdown`
- `PrayerSchedule`
- `PrayerScheduleItem`
- `AnnouncementTicker`
- `InformationSlide`
- `DonationSlide`
- `OfflineBadge`

State-level views:

- `NormalView`
- `ApproachingPrayerView`
- `AdhanView`
- `IqamahView`
- `PrayerView`
- `FridayView`
- `InformationView`
- `SetupErrorView`

Keep state logic outside visual components so theme/layout changes do not alter prayer behavior.