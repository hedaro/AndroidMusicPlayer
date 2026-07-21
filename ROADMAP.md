# Build Roadmap

Step-by-step plan for the Music Player. We build, tune, and polish each step before moving on.
(The authoritative planning doc lives in the Claude plan file; this checklist mirrors it next to the code.)

## Step 1 — Gradle scaffold ✅
- [x] `settings.gradle.kts`, root & module `build.gradle.kts`
- [x] `gradle/libs.versions.toml` version catalog
- [x] `gradle.properties`, `.gitignore`, wrapper config + scripts
- [x] `AndroidManifest.xml` (permissions + `PlaybackService` declaration)
- [x] `res/` strings, theme, adaptive launcher icon
- [x] `README.md`, `ROADMAP.md`

## Step 2 — App shell ✅
- [x] `MusicPlayerApp` (`@HiltAndroidApp`)
- [x] `MainActivity` (single-activity Compose host)
- [x] `ui/theme/` (Color, Type, Theme)
- [x] Compiles and launches to an empty scaffold

## Step 3 — Data layer
- [ ] Domain models: `Track`, `Playlist`, `Album`
- [ ] `MediaStoreDataSource` (query device audio + metadata)
- [ ] Room: `MusicDatabase`, `PlaylistDao`, `PlaylistEntity`, `PlaylistTrackCrossRef`
- [ ] `MusicRepository`, `PlaylistRepository`
- [ ] Hilt `AppModule`, `DatabaseModule`

## Step 4 — Playback layer
- [ ] `PlaybackService` (`MediaSessionService` + ExoPlayer)
- [ ] `PlaybackConnection` (MediaController wrapper → `StateFlow`)
- [ ] `PlaybackState` model
- [ ] Hilt `PlaybackModule`

## Step 5 — Ad seam (no SDK)
- [ ] `AdProvider` interface + `NoOpAdProvider`
- [ ] Hilt `AdModule` binding
- [ ] Bottom `BannerSlot` wired into the app scaffold (invisible)

## Step 6 — UI layer
- [ ] Theme + navigation graph
- [ ] Library screen (list, permission gate, scan)
- [ ] Now Playing (transport, seek, ±5/±10s step, shuffle, repeat)
- [ ] Playlists + Playlist detail (create, edit, reorder)
- [ ] Reusable components (MiniPlayer, TrackRow, SeekBar, StepButtons)

## Step 7 — Housekeeping ✅
- [x] Update `PROJECT_IDEAS.md` (revise ad line; move idea to *In Progress*)

## Verification (per the plan)
- [ ] `./gradlew assembleDebug` succeeds
- [ ] Runs on device/emulator; requests audio permission; lists local tracks
- [ ] Core loop: play → pause → next/prev → scrub → ±5/±10s → shuffle/repeat → background (notification controls) → playlist create/reorder/play
