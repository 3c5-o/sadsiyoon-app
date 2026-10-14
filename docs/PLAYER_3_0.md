# SADESION Player 3.0 — V1.6.0 Beta

## Native direct HLS / MP4 player

The lecture player follows the reference structure rather than permanently obstructing video:
- A transparent tap target **only for native video** toggles the student controller.
- Top row: back, title, lock, fullscreen and tools.
- Center row: rewind 10 seconds, play/pause, forward 10 seconds.
- Bottom row: elapsed / total time (explicit LTR to avoid RTL reversal) and seek slider.
- The controls automatically hide after 3 seconds **while video is playing**. A pause leaves them visible.
- Changing the seek position defers hiding until dragging ends. Settings menu gives up to 5 seconds.
- Lock mode displays an unlock control when the video is tapped.
- Landscape fullscreen hides system bars and lower application panels; returning to portrait restores the normal content.
- PiP hides application controller; the Android player owns picture-in-picture rendering.
- Player automatically pauses when the activity is stopped outside PiP; progress is still stored.

## Embedded third-party players

Embedded providers retain their original player controls, without a fake app play/pause or seek overlay. The touch listener does **not** consume gestures, so providers remain interactive. A temporary top menu can show study-level controls and lesson navigation; provider fullscreen remains supported using WebChromeClient. This doesn't bypass any premium requirements or imply third-party playback has been verified.

## Notes

Each lecture still has separate titled notes. Optionally insert the current time while creating/editing notes on native videos, and tap a note's timestamp to return to that moment. The data is saved as optional `positionMs`, compatible with existing notes and the same JSON backup checks. Legacy notes are not removed.

## No new material sources

Teacher catalogs, links, exam papers, original media and the future teacher-specific `الملازم` tab remain intact. The materials tab intentionally shows an empty state until verified PDFs are provided.

## Testing before release

CI validates course catalogs and builds a debug APK on the PR and main branch. It verifies Java compilation and package ID only. Phone QA still needs to cover: after 3s controls hidden / tap reveal / double tap hide, seek scrubbing, lock/unlock, 16:9 and landscape, PiP, embedded WebView interaction, back navigation, note title/summary/timestamp, backup export/import and switching lectures.

The preview package is `com.sadsiyoon.app.preview`, not a signed in-place update of production.
