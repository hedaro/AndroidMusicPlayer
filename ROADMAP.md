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

## Step 3 — Data layer ✅ (on branch feature/step-3-data-layer; pending build verification)
- [x] Domain models: `Track` (incl. `isFavorite: Boolean`, `playCount: Int`), `Playlist`, `Album`, `TrackSort`
- [x] `MediaStoreDataSource` (live `Flow` via ContentObserver; queries on IO)
- [x] Room: `MusicDatabase`, `PlaylistDao`, `PlaylistEntity`, `PlaylistTrackCrossRef`
- [x] Room: `TrackStatsEntity` (`trackId` PK, `isFavorite`, `playCount`) + `TrackStatsDao`
      (`setFavorite`, `incrementPlayCount` as atomic upserts, `observeAll`)
- [x] `MusicRepository` (merge MediaStore tracks with Room stats + sort), `PlaylistRepository`
- [x] Hilt `DatabaseModule` (no `AppModule` needed — data source & repositories use constructor injection)
- [x] Added `kotlinx-coroutines-android` dependency

## Step 4 — Playback layer ✅ (on branch feature/step-4-playback; pending build verification)
- [x] `PlaybackService` (`MediaSessionService` + ExoPlayer; audio focus, pause-on-noisy, notification)
- [x] `PlaybackConnection` (MediaController wrapper → `StateFlow` + transport controls incl. ±5/±10s step)
- [x] `PlaybackState` + `RepeatMode` model
- [x] `Player.Listener` increments play count after ~5s of playback (once per play; → `incrementPlayCount`)
- [x] No Hilt `PlaybackModule` needed — `PlaybackConnection` uses constructor injection

## Step 5 — Ad seam (no SDK) ✅ (on branch feature/step-5-ad-seam; pending build verification)
- [x] `AdProvider` interface + `NoOpAdProvider`
- [x] Hilt `AdModule` binding (`@Binds`)
- [x] Bottom `BannerSlot` wired into the app scaffold (invisible)

## Step 6 — UI layer (split into sub-steps, each its own branch + review/merge)

### Step 6a — Foundations + Library (first sound!) ✅ (branch feature/step-6a-library; pending build verification)
- [x] `PlaybackConnection.shufflePlay(tracks)` — start a list playing in random order
- [x] Navigation graph + routes; audio permission gate
- [x] Library screen: track list, sort menu (incl. **Most played**), tap-to-play, **Shuffle-play** button
- [x] `LibraryViewModel`; `TrackRow` component with favorite toggle

### Step 6b — Now Playing + MiniPlayer ✅ (branch feature/step-6b-now-playing; pending build verification)
- [x] Now Playing screen: transport, seek bar, ±5/±10s step, **shuffle toggle**, repeat cycle, favorite
- [x] MiniPlayer (persistent bottom bar), `NowPlayingViewModel`
- [x] Reusable components (TrackSeekBar, StepControls); play count surfaced on Now Playing

### Step 6c — Playlists + Favorites ✅ (branch feature/step-6c-playlists; pending build verification)
- [x] Playlists list + detail (create, rename, delete, add/remove, reorder via up/down), **Shuffle-play** a playlist
- [x] Favorites view (favorited tracks as a playable queue)
- [x] Playlist/Favorites ViewModels; add-to-playlist flow (from track overflow menu)
- [x] Bottom navigation (Library / Favorites / Playlists)

**Shuffle (decided):** two behaviors — the Now Playing on/off **toggle** (binds to existing
`toggleShuffle()`) and a dedicated **shuffle-play** action on the library & each playlist
(new `shufflePlay()`, added in 6a).

## Step 7 — Housekeeping ✅
- [x] Update `PROJECT_IDEAS.md` (revise ad line; move idea to *In Progress*)

## Verification (per the plan)
- [x] `./gradlew assembleDebug` succeeds
- [x] Runs on device/emulator; requests audio permission; lists local tracks
- [x] Core loop: play → pause → next/prev → scrub → ±5/±10s → shuffle/repeat → background (notification controls) → playlist create/reorder/play

---

## Post-v1 polish ✅ (merged to main)

Each shipped on its own `feature/*` branch, reviewed and merged:

- [x] **Search** — Library, Favorites, and Playlist detail (shared `SearchField`)
- [x] **Favorites sort** + **mini-player progress line** + Library header / splash polish
- [x] **Settings + dark mode** — System / Light / Dark theme toggle, persisted via DataStore;
      splash held until the preference loads (no flicker)
- [x] **Folder / album browsing** — Songs / Albums / Folders tabs + drill-in detail screens
- [x] **Drag-to-reorder** playlist tracks (long-press; `sh.calvin.reorderable`)
- [x] **Release signing** — `signingConfigs.release` from git-ignored `keystore.properties`;
      `MusicPlayer-<buildType>.apk` output naming (see README → *Release build*)

### Backlog / next ideas (see `../PROJECT_IDEAS.md`)
- Custom app icon; theming beyond Material defaults (accent color)
- Sleep timer, gapless/crossfade, equalizer
- Publish prep: `free`/`withAds` flavors, Play Console listing + privacy policy, `.aab`
