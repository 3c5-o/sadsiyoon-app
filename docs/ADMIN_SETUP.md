# SADESION Android administration + student catalogue (beta)

## Project connection

- Supabase project: SADESION (the existing project).
- API: project-specific HTTPS URL and **publishable** key in Android BuildConfig (these are public client values).
- No service-role key or admin password is shipped in either APK.
- Builds are `com.sadsiyoon.admin.preview` and `com.sadsiyoon.app.preview` on DEBUG builds. Android always digitally signs installable APKs; these use development/debug signatures only, **not production signatures**.

## Required one-time database setup

Supabase's connected schema-edit operation could not be completed. The database was not changed. To activate this build:

1. Visit your *SADESION* project on Supabase and open **SQL Editor**.
2. Review/apply `supabase/migrations/20261015_sadesion_content_admin.sql`.
3. Privately, in SQL Editor, grant the owner role to the ALREADY EXISTING, email-confirmed user. Replace the email placeholder in the following example in SQL Editor **only**, not in Android files or public GitHub source:

```sql
insert into public.sadesion_admins(user_id,role,active)
select id,'owner',true from auth.users
where lower(email)=lower('YOUR_EXISTING_OWNER_EMAIL')
  and email_confirmed_at is not null
on conflict(user_id) do update set role='owner',active=true;
```

Do **not** create an admin user inside the Android app. Supabase Auth checks the email/password, then the `sadesion_is_admin()` function checks the confirmed user's UID against the private server-side allowlist. Unauthorized users cannot read private drafts or write content even if they modify the APK or REST requests.

4. Confirm project Auth has a password for the existing owner account. Do not share passwords, publish server secrets or upload signing keys to chat/GitHub.
5. Launch the admin preview APK, sign in and create a draft record (e.g. cohort 2027, subject الرياضيات, teacher and chapter). After checking it, publish records by setting status to `published`.
6. Open the *student preview* home screen > **محتوى سادسيون المنشور**. It reads published items only and caches the last successfully fetched list locally; the bundled old 2026/2027 catalog remains accessible in the original Teachers area.

## Admin features

Native RTL portrait dashboard, content totals and draft/published/hidden counts; manage cohorts, subjects, teachers, chapters, lessons, resources, exam references, quizzes, and announcements. Each entry supports title, description, cohort, subject, secure media/image/source URLs, JSON metadata, ordered positioning, parent selection, content status, edit/delete, and an HTTPS source preview. Search, refresh, recent audit log, private password login, owner verification, and logout are available.

**Current limits:** Links and basic metadata are managed; no file upload into Supabase Storage, push notification delivery, bulk import, automatic provider playback verification, or full classroom attendance. A successful published video URL does not prove authorized playback. Quizzes can store options in JSON metadata as `{"options":["A","B","C"],"answer":1,"explanation":"..."}`. No server-side grading is implemented.

## Privacy and production release

Do not publish or distribute either preview as a signed production version. Once Android UI, content permissions, tests and Play Protect outcomes are validated, produce separately controlled, **stable production certificates**; never attempt to imitate Google's certificate/attestation metadata. The student app keeps local favorites and legacy catalog; remote content is a new optional path.

## Permissions smoke test

- New/unapproved Auth account: sign-in may succeed, but administration must be denied.
- Confirmed owner: list, create and update a draft; published item visible on student preview, draft invisible.
- Anonymous REST access: can read published only; PATCH/POST/DELETE must be forbidden.
- Offline student mode: previous published cache and bundled catalog still available.
- Logout clears private session; changing password or revoking owner membership blocks future writes.
- Confirm APK application IDs: `com.sadsiyoon.app.preview`, `com.sadsiyoon.admin.preview`.
