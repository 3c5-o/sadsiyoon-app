# SADESION V1.5.0 Beta — portrait student UI refresh

## Scope

This change updates the **student Android application only**. An administrator application, remote Supabase data service, new exam archive, or new lecture sources are **not** created by this release.

### Screens
- Splash is a branded, 2-second automatic transition with a short fade/slide, no waiting for a button, and no network calls before the home screen.
- All non-player student screens are portrait-oriented. Lecture playback starts in portrait; explicit fullscreen can rotate the player.
- The new navy/royal-blue/teal palette is centralized in `Ui.java`, the Android theme resources, and consistent study surfaces.
- Home prioritizes progress, resume, quick access to teachers/exams/practice/library, cohort selection, teachers and subject discovery.
- Five bottom destinations: Home, Teachers, Waziriat, Library, Account. Practice remains accessible from Home; existing settings remain under Account.
- Player has clear provider-independent controls surrounding the video, title/details beneath it, and direct controls where Media3 has rights to the stream.

### Preserved
- Catalog assets for 2026/2027, existing teachers and links, existing exam source records and local study data are not changed.
- Existing export/import workflow, favorites, notes and progress retain the same `sadsiyoon_local_v1` preference store.
- No assertion is made that iframe links, premium videos, or direct URLs are playable without their provider's permission or a valid subscription.
- Debug preview ID remains `com.sadsiyoon.app.preview`, isolated from the production/release app. The debug APK is not a stable upgrade to previously differently signed APKs.

### Verification
On PR and on changes to main, CI runs catalog verification, audit, Android Debug compilation and application ID verification. Use the resulting APK artifact from the successful GitHub Actions run and perform a physical-device check of splash, five-tab navigation, player and back behavior before production release.
