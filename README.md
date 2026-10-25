# Isibalo Express Merchant (hbtechinc.co.za)

Offline-first POS for township shops. Kotlin, Jetpack Compose, Room, MVVM, WorkManager sync, MediaPipe on-device AI.
Package: `za.co.hbtechinc.isibalo`.

## Run
Open in Android Studio (Panda 4 or newer), sync Gradle, run on a device.
Stack: AGP 9.2.1, Gradle 9.4.1, Kotlin 2.4.20, KSP 2.3.12, Compose BOM 2026.09.00, Room 2.8.5, Navigation 2.10.2, Lifecycle 2.11.0, WorkManager 2.12.0, MediaPipe tasks-genai 0.10.35. JDK 17+. Sample stock is seeded on first launch.

## Sync to hbtechinc.co.za
1. Upload `server/isibalo/api/sync.php` to `public_html/isibalo/api/sync.php`.
2. Set DB_* and API_KEY in that file (create an empty MySQL DB in cPanel first).
3. Set the same key as `SYNC_API_KEY` in `app/build.gradle.kts`. URL is `SYNC_URL` there.
The app keeps working offline; unsynced rows go up every 15 min when online, or tap SYNC NOW on the Sales tab. Pushes are idempotent per device.

## Offline AI (MediaPipe)
1. Get `gemma3-1b-it-int4.task` (Hugging Face: litert-community/Gemma3-1B-IT).
2. Assistant tab > IMPORT MODEL, or `adb push gemma3-1b-it-int4.task /sdcard/Android/data/za.co.hbtechinc.isibalo/files/model.task`
3. Use a real device with 4 GB+ RAM.

## Logo
Tally-mark logo (isibalo = count): four white bars and a light-blue express slash on a blue square.
Launcher icon is adaptive (with themed monochrome) plus PNG fallbacks for Android 7.1. Source art is in `logo/`.

## Brand
Wordmark: ISIBALO / EXPRESS MERCHANT in Bebas Neue (SIL OFL, in `app/src/main/res/font`). Used on the splash screen, the top bar and for headings.
