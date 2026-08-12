# Product Specification — MVP

## Product promise

**Connect the player to a TV, configure the mosque once, and get a calm, accurate prayer display that continues to work offline.**

## Users

### Viewer / jamaah

Needs to understand the current time, upcoming prayer, today's prayer times, and mosque information at a glance from several meters away.

### Mosque administrator

Needs to configure prayer calculations/corrections, iqamah timing, Friday settings, announcements, appearance and connected display devices without touching the TV every day.

## Surfaces

### Display App

Fullscreen, non-interactive during ordinary operation. Target: Android TV/STB first, architecture should not prevent browser/mini-PC deployment later.

### Admin App

Phone-first configuration interface. Cloud sync is useful, but the display must retain enough local state to operate through an internet outage.

## State machine

```text
                     ┌──────────── INFORMATION ────────────┐
                     │                                     │
                     ▼                                     │
NORMAL ──> APPROACHING_PRAYER ──> ADHAN ──> IQAMAH ──> PRAYER
  ▲                                                        │
  └────────────────────────────────────────────────────────┘

FRIDAY replaces the normal Dhuhr transition where applicable.
OFFLINE is a connectivity condition layered over operational states.
ERROR is reserved for conditions where valid operation is impossible.
```

## State priority

Highest wins:

1. Blocking configuration/error
2. Prayer in progress
3. Iqamah countdown
4. Adhan
5. Approaching prayer
6. Friday-specific context
7. Normal
8. Information/announcement rotation

An information slide must never delay or obscure a prayer transition.

## Core data

### Mosque

- name
- logo
- city/area
- latitude / longitude
- timezone
- calculation method
- madhab/asr method where applicable
- Hijri adjustment

### Prayer configuration

For each prayer:

- calculated adhan time
- manual minute correction
- iqamah delay
- optional prayer-mode duration
- enabled/visible state

Friday:

- Jumu'ah time
- optional khutbah time
- imam
- khatib
- muadzin

### Content

Announcement:

- title/text
- start/end publication time
- priority
- enabled

Information slide:

- type: announcement/event/donation/image
- duration
- schedule
- payload

## Offline-first behavior

The player persists locally:

- mosque configuration
- prayer calculation configuration
- sufficient prayer schedule horizon
- iqamah settings
- theme/layout configuration
- currently valid announcements/content
- last successful sync timestamp

The clock and prayer state machine must not depend on a live API request.

When offline:

- continue normal operation
- expose only a subtle offline indicator
- queue/retry sync
- never replace a valid prayer screen with a giant network error

## Time correctness

Time is safety-critical to the product experience. Implementation must explicitly handle:

- configured IANA timezone
- device clock drift strategy
- date rollover
- Hijri date adjustment
- prayer offsets
- schedule regeneration
- restart/reboot during an active prayer state

On app boot, derive the correct state from current local mosque time rather than always starting at NORMAL.

## Content scheduling

Information content may rotate only during safe NORMAL windows. Suggested defaults:

- normal screen: 45–90 sec
- information slide: 10–15 sec
- return to normal after each slide
- suspend information rotation before approaching-prayer threshold

Exact values are administrator-configurable later; MVP may ship with sensible defaults.

## Theme system

Do not create dozens of hard-coded templates.

Composition:

`Layout + Theme + Background + Typography options`

MVP layouts:

1. **Focus** — central clock/countdown; default.
2. **Sidebar** — mosque identity/schedule aside with large primary area.
3. **Cinematic** — minimal overlay suitable for a mosque photo/background.

MVP themes:

- Emerald
- Midnight
- Warm
- Light

All layouts must implement the same state contract.

## MVP acceptance criteria

A first usable release should pass these scenarios:

1. Fresh device can be paired/configured without keyboard/mouse dependence during daily use.
2. Correct daily prayer schedule appears after configuration.
3. Administrator can correct each prayer by minutes.
4. Display automatically transitions into approaching, adhan, iqamah and prayer states.
5. Restarting during iqamah restores the correct countdown/state.
6. Internet can be disconnected for a day without losing prayer-time operation.
7. Announcements never cover adhan/iqamah/prayer states.
8. UI remains readable at 720p and 1080p on a TV viewed from distance.
9. Friday behavior can differ from ordinary Dhuhr.
10. Theme changes do not change prayer timing/state logic.

## Not MVP

- Advanced donation accounting
- Live-stream production
- Complex multi-screen video walls
- Analytics dashboard
- Large template marketplace
- Social media integrations
- AI-generated religious content
- Full mosque management/finance suite

These can be evaluated after the display core is reliable.

## Suggested implementation sequence

1. Time/prayer domain model + deterministic state machine
2. Static Focus layout matching wireframe
3. Prayer calculation + manual corrections
4. Iqamah and Friday behavior
5. Local persistence/offline boot
6. Admin configuration UI
7. Device pairing/sync
8. Announcement rotation
9. Theme/layout variants
10. Hardening on real Android TV/STB hardware
