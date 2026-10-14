# V1.3.0 Beta — Phase 1 stability

- Implemented navigation history: Waziriat back button returns to entry page, with state restoration on rotation and saved per-page scroll.
- Isolated parsing errors per JSON file, so one bad file does not halt later teachers.
- Corrected embedded player messaging; improved reader HTTP/network errors; deferred orientation UI changes to Android configuration callback.
- Changed teacher portrait LRU cache limit from entry count to 8 MiB.
- Stable release signing is a separate, secure setup: NEVER commit signing keys or claim the automatically debug-signed artifact upgrades previously installed versions in place.

# Sadsiyoon 1.2.7 Beta — 2025 Mlazemna exam samples

Four real, source-verified 2025 first-round exams: Mathematics and Arabic for sixth scientific and literary.
Select 2025, branch, subject and first round in the Waziriat gallery; opening a card displays the unmodified original Mlazemna webpage and its paper images within an attributed WebView.
An external-browser button is available if the source website restricts in-app viewing.
No third-party images are copied, downloaded, rebranded or watermarks removed. These are live webpage embeds, not re-hosted image files.
Other years, subjects and rounds are untouched. The prior teacher course catalogs are preserved.

# Sadsiyoon 1.2.6 — 2025 exam image-gallery pilot

The new `وزاريات سادسيون` screen is accessible from Home and Tests.
It contains 2025 cohort exam-year filters for science/literary subjects and rounds, a clearly labeled **empty illustration**, links to the corresponding Aselatna sections, and an image viewer that supports multiple page images and displays an in-app translucent "سادسيون" watermark without modifying source pixels.

**Data integrity:** Aselatna's visible preparatory sixth-grade archive checked on October 13, 2026 shows papers through 2021; its 2025 tag visible items concern lower grades. The pilot therefore intentionally contains **zero** 2025 sixth-preparatory scanned papers. No source image was fabricated or misdated; there is nothing to strip/rebrand yet. On receipt of verified licensed 2025 source sheets, populate `app/src/main/assets/exams/2025.json` with paper objects specifying year, branch (scientific/literary), subject, round, title, source, and an array of `https://` image URLs. Verify rights and original sheets before altering old branding.

# Sadsiyoon — teacher portraits 1.2.5

- 14 teacher images are linked to existing catalog names directly from the user-supplied top4top.io URLs.
- 2026 and 2027 show the same portrait for the same canonical teacher name, because the shared `Catalog.IMAGES` array is used in both batches.
- All existing lessons and provider embeds remain unchanged.
- Teacher photos are loaded over HTTPS at runtime and are *not* bundled into the APK. Images can fail to load when the external host is unavailable; the UI keeps an initial-letter placeholder.
- External images are downsampled in memory for list performance.
- **Name review before a production release:** the user-provided label `سجاد العگيلي` was paired provisionally with the existing teacher entry `ساجد العكيلي`, and `هشام الغرباوي` with `هاشم الغرباوي`. Please confirm these are the same teachers.
- Debug build workflow validates catalog and portrait mappings, then publishes APK as a GitHub Actions artifact.

# سادسيون — Sadsiyoon

Android app (Java / AndroidX / Media3) with Arabic RTL UI and 2026/2027 batch navigation.

New in 1.2.4 Beta: indexed 2027 teacher **محمد قاسم** (24 math lectures, 1 chapter), **هشام المعموري** (53 Arabic lectures, 4 chapters), and **سجاد العبيدي** (45 English lectures, 6 chapters). Including the earlier Haider catalog's 61 lessons, 2027 has 183 indexed episode embed links for 4 teachers. The teacher directory advertised 99+ for Sajjad, but only 45 public episode entries were verified in the six pages checked; do not imply completion of unavailable content.

All newly added URLs use public Rumble iframe embeds and may require provider Premium subscription; iframe URL retrieval alone does NOT prove actual playback. No playback restrictions are circumvented.


## 1.2.5 Beta — Teacher portraits / photos

- **2026**: original 19-teacher course indexes remain unchanged.
- **2027**: حيدر عبدالائمة, mathematics — Chapter 1: 28 lessons; Chapter 2: 26 lessons; Chapter 3: 7 lessons; **61 lessons total**. The home card totals are computed from the bundled catalog.
- Chapter three's seven Rumble iframe URLs were observed on the public server 4 of https://v.sea-novels.com/p/2027.html?m=1. All seven show Premium Only Content when opened directly without subscription; iframe visibility is not proof of playback rights or successful playback.
- Chapter two's 26 Rumble iframe URLs were selected from the public "سيرفر 3" option of https://v.sea-novels.com/p/3.html?m=1 and their external video titles matched lecture numbers. A misleading advertisement iframe was rejected for lecture 14 and replaced by its correct verified Rumble embed URL.
- All **26 chapter-two Rumble embeds show Premium Only Content** when opened directly without a qualifying subscription. Their presence in the catalog does **not** mean playback can be guaranteed or access restrictions circumvented.
- Chapter one retains Rumble-first/Bitchute-when-Rumble-absent link choices.
- Video displays in licensed provider iframe controls for embeds; Media3 ExoPlayer controls HLS/MP4 directly when an authorized direct video URL is available. Provider player branding or subscriber checks cannot be removed by app-side UI styling.
- Favorites, saved positions, and notes are stored locally per lecture.

**Important:** Public iframe links are not proof of permission to redistribute third-party lectures in a public app. Obtain authorization from rights holders before distribution.

## Build

On each push to main, GitHub Actions runs `scripts/verify_catalogs.py`, builds `app-debug.apk`, and uploads `sadsiyoon-debug-apk` in artifacts. This is a Debug-signed test APK, not a production-signing certificate.
