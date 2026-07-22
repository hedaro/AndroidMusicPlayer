package com.hedaro.musicplayer.util

import android.Manifest
import android.os.Build

/**
 * The runtime permission needed to scan the local audio library:
 * granular `READ_MEDIA_AUDIO` on Android 13+ (API 33), `READ_EXTERNAL_STORAGE` below.
 */
val AUDIO_PERMISSION: String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        @Suppress("DEPRECATION")
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
