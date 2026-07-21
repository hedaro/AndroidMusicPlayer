# Music Player

A clean, ad-free Android music player for the local device library — built for personal daily use.

## Status

🚧 In development. See [`ROADMAP.md`](ROADMAP.md) for the step-by-step build plan and progress.

## Features (target)

- Playback: play / pause / stop / next / previous
- Shuffle and loop (single track & whole playlist)
- Seek bar with elapsed / remaining time + scrubbing
- Touch-friendly step controls: ±5s / ±10s
- Playlists: create, edit, reorder
- Track metadata: title, artist, album, art, duration
- Local library scanning from device storage
- **Ad-free.** Structured to allow strictly non-intrusive banner ads *if* published to the Play Store — no ad SDK ships today (see the `ads/` package).

## Stack

| Concern | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Playback | Media3 (ExoPlayer + `MediaSessionService`) |
| Library scan | `MediaStore.Audio` |
| Persistence | Room |
| Album art | Coil |
| DI | Hilt |
| Build | Gradle (Kotlin DSL) + version catalog |

- **minSdk:** 26 (Android 8.0) · **targetSdk:** 35
- Package: `com.hedaro.musicplayer`

## Getting started

1. Open the project folder in **Android Studio** (latest stable).
2. On first sync, Android Studio downloads Gradle 8.11.1 and generates the wrapper JAR
   (`gradle/wrapper/gradle-wrapper.jar`). If you build from the command line before opening in
   Android Studio, generate it once with a locally installed Gradle: `gradle wrapper`.
3. Let Gradle sync finish, then **Run** on a device/emulator (API 26+).
4. On first launch the app requests audio permission and scans your local library.

## Project structure

```
app/src/main/java/com/hedaro/musicplayer/
├── data/        # models, MediaStore data source, Room DB, repositories
├── playback/    # Media3 service + MediaController connection
├── ui/          # theme, navigation, screens (library / nowplaying / playlists), components
├── ads/         # ad seam: AdProvider interface + NoOpAdProvider (no SDK)
├── di/          # Hilt modules
└── util/        # helpers (time formatting, permissions, constants)
```

## Ads policy

v1 is genuinely ad-free — no ad SDK, no network/analytics dependency. The `ads/` package defines an
`AdProvider` interface bound to a `NoOpAdProvider`, and screens render a single bottom `BannerSlot` that
is invisible today. Enabling non-intrusive banner ads later (for a Play Store release) means adding the
AdMob dependency and an `AdMobAdProvider`, then flipping the Hilt binding — no screen changes required.
