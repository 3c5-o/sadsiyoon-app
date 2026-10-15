# SADESION — secure Android release signing

## What this setup does

- Builds a **release** APK with the production package `com.sadsiyoon.app`, signed by an owner-controlled keystore.
- Validates the APK signature with Android SDK `apksigner`, checks the v2 signing scheme (Android 8+) and confirms the release is not debuggable.
- Publishes the signed APK plus SHA-256 checksum and **public** signing-certificate fingerprint.
- Optionally checks the certificate against a pinned fingerprint so a mistaken key change blocks a new release.
- Rejects changed manifest permissions or automatic extraction of local notes in regular CI.

**This is not a Google Play Protect approval or bypass.** A locally signed APK can still trigger warnings from unknown-app distribution or a genuinely harmful classification. If Play Protect flags this app incorrectly, diagnose the exact warning and use Google's official appeal process.

## Signing prerequisites (not stored in this repository)

The current debug APK is `com.sadsiyoon.app.preview` and uses a debug signing key. It is **not** a production release and its certificate may change between build environments.

**Important:** If a production APK of `com.sadsiyoon.app` has ever been installed and must receive in-place updates, locate its ORIGINAL signing key before doing anything else. A newly generated key cannot update an installation signed with the previous key. Export notes and favorites from the old installation before a planned migration.

Keep the production keystore offline and private. Do not upload it to a public repository, paste it into chat, or embed it in the APK. Prefer creating it locally using Android Studio's **Build > Generate Signed Bundle / APK** or the JDK `keytool` on a trusted device.

Store these values under **GitHub repository > Settings > Secrets and variables > Actions > Secrets**:

- `SADSIYOON_KEYSTORE_BASE64` — one-line base64 of the private `.jks` keystore
- `SADSIYOON_KEYSTORE_PASSWORD`
- `SADSIYOON_KEY_ALIAS`
- `SADSIYOON_KEY_PASSWORD`

On the same GitHub page, after the first approved release, add a normal **Variable** (not a secret) named `SADSIYOON_EXPECTED_CERT_SHA256` containing the certificate SHA-256 shown in the signing report. This certificate fingerprint is public data; it is NOT the keystore or a password. Future CI runs compare it against the actual signing certificate and reject changes.

## Create and verify a signed release

1. Open **GitHub Actions > Manual Signed Sadsiyoon Release > Run workflow** on the `main` branch. The workflow requires all four private secrets.
2. Wait for the source catalog audit, privacy check, JVM unit tests and Android release build.
3. Download the workflow artifact named `sadsiyoon-stable-signed-release`. It contains `app-release.apk`, its `app-release.apk.sha256`, and `signing-report.txt`.
4. Keep the certificate fingerprint and `sha256` in the public release record; verify the downloaded APK checksum before installation.
5. Test installation and upgrade on an Android 8+ device. Do not claim Play Protect approval merely because the build or signature verification succeeded.
6. For every later release, preserve the SAME owner-controlled signing key and increment `versionCode`.

The workflow deletes the temporary keystore from the runner at the end; GitHub's runner is ephemeral, but the owner's separate, secure keystore backup remains essential.

## Local-only privacy and playback

The student's favorites, progress and private notes remain local to the app; automatic OS backups and device transfers are disabled. The app's own voluntary JSON export/import still functions. The manifest only requests `INTERNET` and `ACCESS_NETWORK_STATE`, rejects cleartext traffic, and keeps non-launcher Android activities non-exported. WebViews request safe-browsing checks and do not silently open arbitrary top-level pages inside the embedded lecture player.

Only link to video sources for which the app has permission. These protections do not guarantee third-party videos will play.

## If Play Protect warns

- Get the precise warning text or screenshot and check whether it concerns unknown installation sources, an unrecognized developer, or a harmful-app detection. These are not the same problem.
- Verify the `package`, signature and checksum against the release report.
- Investigate unwanted behavior or dependencies if Google identifies a real problem. If it is a false positive, request official review via [Google's App verification page](https://support.google.com/googleplay/android-developer/answer/2992033).
- Do NOT disable or impersonate Play Protect, insert fake `com.android.stamp` metadata, alter Google package identities, or borrow another app's signing certificate.
