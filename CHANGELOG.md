# Changelog

All notable changes to this project are documented here. The format is based on
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] — 2026-07-27

### Added
- Play queue view with tap-to-jump, drag-reorder, and swipe-to-remove.
- Queue and playback position persist across the app being killed (restored paused).
- Queue entry points from the MiniPlayer and Now Playing.
- Shuffle and loop controls in the queue view.
- Clear queue action with confirmation.
- Animated equalizer indicator on the currently-playing track (freezes when paused, respects reduced motion).
- Play next and Add to queue in every track's overflow menu.
- Overflow menu (incl. Add to playlist) on Album and Folder detail rows.

### Changed
- Play next on an empty queue now starts playing immediately.
- Tapping a track from the same source jumps within the queue instead of rebuilding it.
- Manually starting a song now opens Now Playing (auto-advance and next/prev don't).
- Songs list scrolls to the top when the sort order changes.
- Now Playing and Queue no longer stack on top of each other.
- APK/AAB outputs named `MusicPlayer-<versionName>-<versionCode>-<buildType>` for side-by-side archiving.

### Fixed
- MiniPlayer overlapping the system navigation bar on non-tab screens.
- Queue-view crash when the same track appeared more than once.
