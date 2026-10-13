# سادسيون — Sadsiyoon

Android app (Java / AndroidX / Media3) with Arabic RTL UI and 2026/2027 batch navigation.

New in 1.2.4 Beta: indexed 2027 teacher **محمد قاسم** (24 math lectures, 1 chapter), **هشام المعموري** (53 Arabic lectures, 4 chapters), and **سجاد العبيدي** (45 English lectures, 6 chapters). Including the earlier Haider catalog's 61 lessons, 2027 has 183 indexed episode embed links for 4 teachers. The teacher directory advertised 99+ for Sajjad, but only 45 public episode entries were verified in the six pages checked; do not imply completion of unavailable content.

All newly added URLs use public Rumble iframe embeds and may require provider Premium subscription; iframe URL retrieval alone does NOT prove actual playback. No playback restrictions are circumvented.


## 1.2.4 Beta — Four teachers indexed for 2027

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
