# Android release-signing procedure (V1.4.0)

**This repository contains no production signing key.** The current debug APKs may carry different signatures; do NOT promise in-place upgrades from historical debug builds.

To publish a stable upgradeable Android APK:

1. Generate an Android signing keystore once on a trusted developer machine, or choose the previously adopted production key if one already exists. Back it up securely offline. Do not send its password or binary contents into chat or Git commits.
2. Open GitHub repository > Settings > Secrets and variables > Actions, and set these four encrypted repository secrets:
   - `SADSIYOON_KEYSTORE_BASE64`: base64-encoded keystore bytes (one line)
   - `SADSIYOON_KEYSTORE_PASSWORD`: keystore password
   - `SADSIYOON_KEY_ALIAS`: key alias
   - `SADSIYOON_KEY_PASSWORD`: alias password
3. Run Actions > `Manual Signed Sadsiyoon Release` > Run workflow. It verifies catalogs, builds the signed APK and validates its signature.
4. Keep the **same signing key** for every future release and increment `versionCode`. Before rolling out, test that an APK update installs over a device already running a release signed with that SAME certificate.

Never commit or publicly share .jks/.keystore files, passwords or the base64 secret. Existing debug users may have to migrate their local data (Settings > Export), remove the old app, install the signed release, and restore the backup. Preserve the same applicationId `com.sadsiyoon.app`.

The automated debug workflow remains usable independently and has NOT been switched to production signing.
