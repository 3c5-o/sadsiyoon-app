# SADESION administration preview — 0.2.0 installation diagnosis

## What changed
- The administration application now ships the original SADESION management artwork as a compressed WebP icon in `admin/src/main/res/drawable-nodpi/sadesion_admin_brand.webp`, referenced for both normal and round launcher icons.
- The new **debug-only** Android package is `com.sadsiyoon.admin.preview2`. It can install alongside the older `com.sadsiyoon.admin.preview` without colliding with its **different** temporary CI signing certificate.
- The new preview version is `0.2.0-beta-preview` with versionCode 2.
- GitHub Actions now validates the new package ID, minimum Android API 26, V2 signature verification with `apksigner`, ZIP archive integrity, and SHA-256 checksum of both preview APKs.
- No production-signing key is used or published.

## Why the older test APK sometimes refused to install
Previous CI runs generated different Android debug signing certificates. Android rejects an in-place update under the same application ID with a different certificate, usually `INSTALL_FAILED_UPDATE_INCOMPATIBLE`. An integrity check of the prior `admin-debug.apk` confirmed it contains an APK Signature Scheme v2 block and its ZIP structure is intact; **this does not prove every phone can install it**. Other causes can be unsupported Android version, storage, package installer restrictions or Play Protect classification.

## Correct beta installation
1. Download the **.apk**, not an unextracted GitHub Artifact `.zip`. A GitHub Artifact must be extracted to obtain the APK.
2. Android 8.0 (API 26) or newer is required. The admin application is an Android phone app in portrait orientation.
3. Install `com.sadsiyoon.admin.preview2` as a **new app**, distinct from any earlier preview; no uninstall of existing app should be needed for this specific collision.
4. Preserve prior local state and credentials; the new app has a separate sandbox. Do not migrate private app data by manually modifying packages.
5. For *future* previews with this same ID, a stable **development-only** signing certificate is needed to upgrade in-place. Until then, use clean installations of preview builds as required; production signing will be configured separately when authorized.
6. If installation still fails, capture the **exact error** from the Android installer or use `adb install <file.apk>` on a trusted development computer and provide the resulting `INSTALL_FAILED_...` diagnostic. Do not turn off Play Protect to install an app it flags as harmful.

## Data backend requirement
APK installation works independently of Supabase schema. **Login and content editing will not succeed** until the owner applies `supabase/migrations/20261015_sadesion_content_admin.sql` in the SADESION project's SQL Editor and securely provisions the existing owner user as described in `docs/ADMIN_SETUP.md`. Changing package ID, icon or signing metadata does not create these tables.

## Release gate
This is an **installability-focused debug update**. A green GitHub build verifies compilation and APK structural checks, not real-device installation, Google Play Protect approval, or end-to-end admin workflows. No claim of complete resolution is made without the user's device test.
