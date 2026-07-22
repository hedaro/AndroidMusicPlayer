package com.hedaro.musicplayer.data.model

/**
 * Ordering options for track lists (library & favorites).
 * [MOST_PLAYED] is backed by the per-track play count; [RECENTLY_ADDED] by MediaStore's date-added.
 */
enum class TrackSort {
    TITLE,
    ARTIST,
    RECENTLY_ADDED,
    MOST_PLAYED,
}
