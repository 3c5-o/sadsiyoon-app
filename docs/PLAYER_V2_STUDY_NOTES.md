# SADESION V1.5.1 Beta — Player and study notes

The student app remains portrait-oriented, with optional landscape fullscreen for video.

## Player
- Native HLS/MP4 uses Media3 without the duplicate stock on-video toolbar.
- Previous/next lecture, native play/pause, fullscreen, and the tools menu live in the video frame.
- The on-video tools menu contains speed, picture-in-picture, reload, favorite, completed and seek (for native playback only).
- A timeline with time counter and seeking is available for direct video streams; provider embeds keep their own timeline and playback controls.
- Unsupported provider controls are hidden rather than promising functionality or circumventing paywalls.
- The tools menu is collapsed by default so videos remain visible.

## Study notes
- Each lesson can have up to 60 independent note cards.
- Each card has an editable title and summary/body, with explicit save and confirmation before deletion.
- Notes live in the existing local preference store with `note_cards_t..._c..._l...` keys.
- On first opening a lesson, any old single-note content is copied into a note titled «ملاحظة سابقة». The old key is never deleted by this migration.
- Local backup export/import preserves and validates both old and structured notes.
- No cloud syncing of notes is implemented; the JSON export is required to carry notes to a separate installation.

## Materials
The previous "الملفات" tab is now "الملازم". It provides a teacher-specific, clearly empty state. Actual PDFs require later content ingestion/authorization and admin linking; no PDFs are fabricated.

## Verification
Run the CI course-catalog audit and Gradle `assembleDebug`. Test both Media3 playback and third-party embed overlays, step navigation, fullscreen/PiP, note add/edit/delete, legacy migration and JSON backup/restore on an Android device.
