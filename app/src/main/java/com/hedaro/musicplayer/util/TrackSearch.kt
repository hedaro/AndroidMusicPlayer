package com.hedaro.musicplayer.util

import com.hedaro.musicplayer.data.model.Track

/** Case-insensitive match of a search query against a track's title, artist, or album. */
fun Track.matchesQuery(query: String): Boolean =
    title.contains(query, ignoreCase = true) ||
        artist.contains(query, ignoreCase = true) ||
        album.contains(query, ignoreCase = true)
