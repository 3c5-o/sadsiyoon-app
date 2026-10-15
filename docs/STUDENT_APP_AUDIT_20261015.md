# SADESION — Student App Audit (2026-10-15)

Scope: **Android student application only**. This audit is based on the `main` repository source at commit `ad2a8d13632e73f822172aa9ef59e2dc8a40a03c`. It is not a claim of running the APK on an Android device.

## Verified existing implementation

- Native Android application written in Java; min SDK 26, target SDK 35, version 1.6.0-beta / versionCode 18.
- Portrait student pages with home, teacher directory, batch selector (2026/2027), subjects, chapter/lesson listings, Waziriat, offline practice, library, search, and settings.
- Player 3.0 uses Media3 for direct MP4/HLS, WebView for supported third-party provider embeds, fullscreen/orientation, PiP, bookmarking, local playback positions, favorite/completed state, and timestamped notes.
- Bundled JSON course catalogs under `app/src/main/assets/catalog*`; the catalog URL being present does **not** guarantee playable video or permission to redistribute it.
- Student progress stored on-device in SharedPreferences; export/import JSON backup exists.
- CI static catalog validation and source-link audit run before a debug build. Debug installation is deliberately isolated as `com.sadsiyoon.app.preview`; a stable production-signed build requires separately configured private signing credentials.

## Observed gaps and risks (source review)

### P0 — Release blockers

1. **Local-only architecture is intentionally selected by the owner.** There is no Supabase dependency and no remote login or cross-device sync; student progress stays on the device with optional manual backup. This is not a release blocker for an offline-first student application.
2. **External video playback is not end-to-end verified.** Several indexed provider embeds may require provider subscriptions; the UI already warns about this. Real-device tests must verify source access, sound, video, buffering and back navigation without bypassing subscription protections.
3. **No automated UI/instrumented device test suite.** Source-level student library unit tests were added in 1.6.1, but compilation and JVM tests still cannot establish navigation correctness on a phone.
4. **Production signing remains unconfigured until the owner supplies the stable keystore privately via GitHub Actions secrets.**

### P1 — Student quality

1. Teacher photographs load from external hosts; availability and portrait accuracy are not guaranteed.
2. Large lists use programmatically built views rather than recyclable rows; deep search and chapters need mobile performance checks.
3. Student pages mostly use fixed dp dimensions; test 320dp/360dp/large phones, enlarged fonts, portrait/landscape for the player, gesture navigation and Android 15 insets.
4. Authentication/profile is currently a local nickname only; do not label locally saved progress as cross-device synced.
5. Real Waziriat coverage is limited to verified sources, and offline practice is intentionally a small original question sample. Never label sample questions as ministry questions.
6. Notes, favorites, and progress need stable content IDs when moving to dynamic catalogs; do not invalidate old local preference keys without migration.

### P2 — Next phase

1. Design the full student UI in Figma against the existing Android styles/tokens, then implement native layouts and responsive typography after a real-device screenshot review.
2. Keep bundled JSON catalogs readable offline, with honest labels when a source is not available or cannot be verified.
3. Improve local-only backup/restore and study progress, and test package upgrade behavior before signing for release.
4. Add crash reporting/analytics only if specifically requested and after a clear student privacy plan; connecting PostHog in ChatGPT is **not** Android SDK integration.
5. The admin dashboard, Supabase database, cloud accounts and remote synchronization are **out of scope** until the owner changes that instruction.

## Changes on this branch

- Library shows an in-place **load more** control beyond its former 100-item display cap.
- Removing a favorite or completed item from the library refreshes the displayed list immediately; item buttons have Arabic accessibility descriptions.
- Resetting progress clears the last-resumed lesson pointer as well as saved completion, favorites, positions, and notes.

## Acceptance checks (before merge/release)

- [ ] Android CI compiles the PR and publishes a preview APK artifact.
- [ ] Library with >100 favorites or completed lessons: load more without losing scroll; toggle entry from within library; confirm removal and count update.
- [ ] Mark an unfinished lesson, open it, save playback position; check resume after restarting app.
- [ ] Reset progress: confirm home no longer shows a stale last-watched card and notes/favorites/completion are gone.
- [ ] Home bottom navigation: every tab opens its destination; Android Back returns correctly.
- [ ] Play a *licensed* MP4/HLS test stream with sound and a provider embed, test fullscreen, skip, previous/next, PiP, network interruption, and retry.
- [ ] Reinstall preview build alongside stable build; original app data/signature unchanged.
- [ ] Validate portrait labels / clipping on small device and with increased system font sizes.

**Not yet completed:** real-device playback validation, visual Figma audit of a linked design file, Supabase project creation/integration, telemetry SDK, full exam library, production signing and signed APK publishing.


## 1.6.1 offline-only student follow-up

- Three library filters (favorites, completed, continue-watching). Continue-watching is based on locally saved position for native MP4/HLS, not external embeds.
- Bottom navigation no longer accumulates a cyclic history across top-level tabs.
- Teacher portraits use a bounded download pool and retain their initial-letter placeholder when offline.
- Native player errors/long buffering show an in-player retry action; full playback success is still unverified on a device.
- Four JVM tests protect local library selection rules; debug and signed release CI now run these tests before APK build.
- The project remains **fully independent of Supabase** by request. Do not introduce a cloud dependency without explicit later approval.
