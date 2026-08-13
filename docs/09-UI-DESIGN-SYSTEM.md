# 09 — UI Design System

Status: **Normative / Source of Truth untuk konstruksi visual**

Dokumen ini menerjemahkan referensi visual menjadi aturan yang dapat dipahami coding agent **tanpa vision**. Jangan menyalin nama masjid, foto, logo, copywriting, atau aset dari referensi. Yang diadopsi adalah struktur, hierarchy, density, interaction pattern, dan karakter visual.

## Design direction

### TV
- Readability jarak jauh lebih penting daripada density.
- NORMAL boleh kaya informasi/media.
- Prayer transition adalah full-screen focus state.
- Nuansa Islami hadir melalui geometry/pattern yang halus, bukan ornamen berlebihan.
- Foto adalah content surface, bukan dekorasi yang mengurangi keterbacaan.

### Admin
- Modern Android utility UI.
- Background terang, card sederhana, accent tunggal, whitespace cukup.
- Tidak membawa ornamen Islami TV ke setiap card.
- Pengurus harus dapat menyelesaikan pekerjaan dengan sedikit tap.

## Base tokens

### Color — Light/Admin
```text
color.background        = #F6F7F5
color.surface           = #FFFFFF
color.surfaceMuted      = #EEF1ED
color.textPrimary       = #17201B
color.textSecondary     = #68736D
color.divider           = #DDE2DE
color.accent            = #176B45
color.onAccent          = #FFFFFF
color.warning           = #D99518
color.error             = #B3261E
color.success           = #176B45
```

### Color — TV Focus warm theme
```text
color.tvFocusBg         = #F4EFE4
color.tvFocusSurface    = #FFFDF8
color.tvFocusText       = #171A17
color.tvFocusSecondary  = #62665F
color.tvFocusAccent     = #B9842C
color.tvFocusPattern    = rgba(185,132,44,0.07)
```

Theme boleh mengganti warna tetapi contrast/hierarchy tidak boleh berubah.

### Radius
```text
radius.small   = 8dp
radius.medium  = 14dp
radius.large   = 20dp
radius.pill    = 999dp
```

### Admin spacing
Gunakan grid 4dp. Nilai utama: 8, 12, 16, 20, 24, 32dp. Screen horizontal padding 20dp. Jarak section 28–32dp.

### TV spacing baseline 1920×1080
Gunakan safe inset minimum 48px vertical / 64px horizontal untuk elemen non-edge. Critical text sebaiknya >= 72px dari physical edge.

## Typography

Gunakan font Android/system sans yang sangat terbaca. Hindari decorative font untuk informasi waktu.

### Admin
```text
Display/hero       32sp / 700
Screen title       24sp / 700
Section title      14sp / 700 / optional uppercase tracking
Card title         16sp / 600
Body               14sp / 400
Supporting         12sp / 400
Button             14sp / 600
Numeric important  18–22sp / 700 / tabular numerals
```

### TV 1080p baseline
```text
Hero clock/countdown  120–200px / 700 / tabular
State title            58–76px / 700
Mosque name            38–48px / 700
Prayer label           30–38px / 500–600
Prayer time            40–52px / 700 / tabular
Secondary              24–30px / 400–500
Ticker                  28–34px / 500
```

## TV layout families

NORMAL memiliki dua approved layout families. MVP implementasi dimulai dari `HORIZONTAL_MEDIA`; `SIDEBAR_MEDIA` adalah layout kedua setelah yang pertama stabil.

### A. HORIZONTAL_MEDIA

```text
1920 × 1080
┌──────────────────────────────────────────────────────────────────┐
│  CLOCK      MOSQUE NAME + LOCATION              DATE + HIJRI     │ 170
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│                         MEDIA / PHOTO                            │ 720
│                                                                  │
├──────────────────────────────────────────────────────────────────┤
│ SUBUH │ SYURUQ │ DZUHUR │ ASHAR │ MAGHRIB │ ISYA               │ 190
└──────────────────────────────────────────────────────────────────┘
```

Rules:
- Header height ~15–16% canvas.
- Prayer bar ~17–18% canvas.
- Media fills remaining center.
- Current/next prayer cell highlighted; cell height tidak berubah.
- Prayer label di atas, time besar di bawah.
- Countdown kecil boleh berada di highlighted cell.
- Background media `ContentScale.Crop`; header/prayer surfaces harus menjaga contrast.

### B. SIDEBAR_MEDIA

```text
1920 × 1080
┌────────── 430px ──────────┬─────────────────────────────────────┐
│ CLOCK                     │ MOSQUE NAME            DATE/HIJRI  │ 180
├───────────────────────────┼─────────────────────────────────────┤
│ SUBUH              04:32  │                                     │
├───────────────────────────┤                                     │
│ SYURUQ             05:47  │                                     │
├───────────────────────────┤            MEDIA                    │
│ DZUHUR             11:46  │                                     │
├───────────────────────────┤                                     │
│ ASHAR              15:07  │                                     │
├───────────────────────────┤                                     │
│ MAGHRIB            17:42  │                                     │
├───────────────────────────┤                                     │
│ ISYA               18:54  │                                     │
├───────────────────────────┴─────────────────────────────────────┤
│ ANNOUNCEMENT / INFORMATION BAR                                 │ 100
└──────────────────────────────────────────────────────────────────┘
```

Rules:
- Sidebar 22–24% width.
- Prayer rows fixed/equal height.
- Label left, time right, baseline aligned.
- Active/next row mendapat accent surface dan boleh menampilkan countdown kecil.
- Media mengambil sisa canvas tanpa card inset.
- Bottom information bar full width.

## TV focus state family

APPROACHING, ADHAN, IQAMAH, PRAYER, FRIDAY dan NOTICE menggunakan satu visual grammar.

```text
┌──────────────────────────────────────────────────────────────────┐
│                                                                  │
│                         [ STATE ICON ]                           │
│                                                                  │
│                 ─────  TIME / CONTEXT  ─────                    │
│                                                                  │
│                        PRIMARY MESSAGE                           │
│                                                                  │
│                       SECONDARY MESSAGE                          │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

Visual treatment:
- Warm low-contrast background.
- Sangat halus Islamic geometric pattern maksimal ~7% visual opacity.
- Center surface/architectural frame boleh digunakan tetapi tidak meniru frame referensi secara literal.
- Accent gold/warm hanya sebagai outline/detail, bukan memenuhi layar.
- Negative space besar.
- Tidak ada photo carousel, ticker, QRIS, atau prayer table pada focus state.

### APPROACHING
Hierarchy:
```text
STATE LABEL kecil
MAGHRIB
09:54          <- focal terbesar
Adzan · 17:42
optional one-line reminder
```
Jangan gunakan countdown negatif (`-09:54`).

### ADHAN
```text
[adhan icon]
MAGHRIB
17:42
WAKTU ADZAN
optional short reminder
```

### IQAMAH
```text
[iqamah icon]
IQAMAH MAGHRIB
08:42          <- focal terbesar
Persiapkan dan rapatkan shaf
```
60 detik terakhir boleh meningkatkan emphasis warna/weight tanpa mengubah layout.

### PRAYER
```text
[prayer icon]
SHOLAT MAGHRIB
Luruskan dan rapatkan shaf
optional short verse/reminder
```
Jam/countdown tidak menjadi focal point.

### FRIDAY
Menggunakan grammar PRAYER dengan label `SHOLAT JUMAT` dan context Jumat yang dikonfigurasi.

### NOTICE
Untuk reminder operasional singkat seperti menyenyapkan HP. NOTICE tidak boleh mengambil alih prayer state prioritas lebih tinggi.

## Component anatomy — TV

### `TvHeader`
```text
TvHeader
├── CurrentClock
├── MosqueIdentity
│   ├── MosqueName
│   └── Location
└── DateBlock
    ├── GregorianDate
    └── HijriDate
```

### `PrayerCell`
```text
PrayerCell
├── PrayerName
├── PrayerTime
└── Countdown?   // hanya cell yang relevan
```
Constraints: fixed size, no layout jump ketika active, tabular numeral.

### `PrayerSidebarRow`
```text
PrayerSidebarRow
├── AccentIndicator? / ActiveSurface
├── PrayerName
├── Countdown?
└── PrayerTime
```

### `FocusStateContent`
```text
FocusStateContent
├── StateIcon?
├── ContextTime?
├── PrimaryLabel
├── HeroValue?
└── SecondaryMessage?
```

## Admin information architecture

```text
Beranda
├── TV status + preview
├── quick actions
└── grouped settings

Masjid
├── Informasi Masjid
├── Jadwal Sholat
├── Adzan & Iqamah
└── Jumat

Konten
├── Pengumuman
├── Media
└── QRIS

Display
├── Tampilan
└── Perangkat
```

## Admin onboarding

Empat tahap:

```text
1 Aktivasi Lisensi
2 Hubungkan TV
3 Data Masjid
4 Selesai
```

### Stepper anatomy
```text
TopAppBar
Stepper: ●────○────○────○
Title
Description
PrimaryContent
Spacer/flexible area
PrimaryButton
```

### Step 1 — Offline license
- Input serial number.
- Validasi sepenuhnya lokal.
- Tidak ada loading network/server copy.
- Success lanjut ke pairing.

### Step 2 — Pair TV
- Primary: scan QR dari TV.
- Secondary fallback: pairing code bila didukung protocol.
- Jelaskan HP dan TV harus berada pada LAN yang dapat saling mencapai.
- Jangan meminta IP pada normal flow.

### Step 3 — Mosque data
Fields minimal: nama, location label, coordinates, timezone, prayer method. Lokasi HP dapat membantu mengisi koordinat tetapi user dapat koreksi.

### Step 4 — Review
Tampilkan TV name, mosque identity, timezone, readiness. Primary CTA `Mulai Display`.

## Admin Home wireframe

```text
┌──────────────────────────────┐
│ Masjid Nurul Hikmah          │
│ Sleman, Yogyakarta           │
│                              │
│ ┌──────────────────────────┐ │
│ │      TV PREVIEW          │ │
│ └──────────────────────────┘ │
│ ● TV Utama      Terhubung    │
│                              │
│ PENGATURAN UTAMA             │
│ [ Informasi Masjid       › ] │
│ [ Jadwal Sholat          › ] │
│ [ Adzan & Iqamah         › ] │
│ [ Jumat                  › ] │
│                              │
│ KONTEN                       │
│ [ Pengumuman             › ] │
│ [ Media                  › ] │
│ [ QRIS                   › ] │
│                              │
│ DISPLAY                      │
│ [ Tampilan               › ] │
│ [ Perangkat              › ] │
└──────────────────────────────┘
```

## Admin components

### `SettingsCard`
```text
SettingsCard
├── LeadingIcon (accent)
├── TextColumn
│   ├── Title
│   └── SupportingText?
└── TrailingChevron / Value
```
Height minimum 68dp; radius medium; border/divider subtle; hindari shadow berat.

### `PrayerSettingCard`
```text
PrayerSettingCard
├── PrayerIcon
├── PrayerName
├── CorrectionSummary
├── CalculatedDisplayTime
└── Chevron
```
Tap membuka bottom sheet, bukan screen baru untuk perubahan sederhana.

### Prayer edit bottom sheet
```text
Prayer name
Raw calculated time
Correction:  [−]  +2 menit  [+]
Displayed time: 11:46
Iqamah:     [−]  10 menit  [+]
[ Simpan ]
```

### `DeviceStatusCard`
States:
- connected: green/status text `Terhubung langsung`
- searching
- not found
- credential invalid
- protocol incompatible

Jangan gunakan istilah `internet offline` untuk status TV.

### `MediaGrid`
- 2–3 columns tergantung width.
- Thumbnail 1:1 atau 4:3 konsisten.
- Add media prominent tetapi tidak floating menutupi konten.
- Multi-select via Android Photo Picker.

### `TransferProgress`
```text
Mengirim 8 foto
5 selesai · 1 mengirim · 2 menunggu
[total progress]
filename.jpg              41%
```
File gagal memiliki Retry individual; sukses tidak diulang.

## Admin interaction rules

- Primary action maksimal satu per screen.
- Save config dianggap berhasil setelah TV mengembalikan validation/persist success.
- Setting sederhana gunakan bottom sheet.
- Destructive action membutuhkan confirmation.
- Jangan menyembunyikan status TV ketika action membutuhkan koneksi LAN.
- Loading harus menjelaskan aktivitas: `Mencari TV`, `Mengirim konfigurasi`, `Mengirim 3/8 foto`.

## Reference-content prohibition

Mockup dan production UI wajib memakai branding/data original Masjid Display. Dilarang menyalin dari referensi:
- nama masjid,
- alamat,
- logo/brand,
- foto/video frame,
- nomor kontak,
- serial contoh,
- copy marketing,
- ornamen proprietary secara pixel-identical.

Referensi digunakan untuk memahami pola UI, bukan sebagai asset produk.