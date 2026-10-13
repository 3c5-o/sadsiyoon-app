# سادسيون — Sadsiyoon

Android app (Java / AndroidX / Media3) with Arabic RTL UI and 2026/2027 batch navigation.

## 1.2.2 Beta — Chapter two, 2027

- **2026**: original 19-teacher course indexes remain unchanged.
- **2027**: حيدر عبدالائمة, mathematics — Chapter 1: 28 lectures; Chapter 2: 26 lectures, **54 indexed lectures total**. The home card totals are computed from the bundled catalog.
- Chapter two's 26 Rumble iframe URLs were selected from the public "سيرفر 3" option of https://v.sea-novels.com/p/3.html?m=1 and their external video titles matched lecture numbers. A misleading advertisement iframe was rejected for lecture 14 and replaced by its correct verified Rumble embed URL.
- All **26 chapter-two Rumble embeds show Premium Only Content** when opened directly without a qualifying subscription. Their presence in the catalog does **not** mean playback can be guaranteed or access restrictions circumvented.
- Chapter one retains Rumble-first/Bitchute-when-Rumble-absent link choices.
- Video displays in licensed provider iframe controls for embeds; Media3 ExoPlayer controls HLS/MP4 directly when an authorized direct video URL is available. Provider player branding or subscriber checks cannot be removed by app-side UI styling.
- Favorites, saved positions, and notes are stored locally per lecture.

**Important:** Public iframe links are not proof of permission to redistribute third-party lectures in a public app. Obtain authorization from rights holders before distribution.

## Build

On each push to main, GitHub Actions runs `scripts/verify_catalogs.py`, builds `app-debug.apk`, and uploads `sadsiyoon-debug-apk` in artifacts. This is a Debug-signed test APK, not a production-signing certificate.
