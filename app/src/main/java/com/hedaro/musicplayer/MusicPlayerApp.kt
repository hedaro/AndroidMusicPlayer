package com.hedaro.musicplayer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point. Annotated with [HiltAndroidApp] so Hilt can generate the
 * dependency-injection container used across the app (repositories, database,
 * playback connection, etc.).
 */
@HiltAndroidApp
class MusicPlayerApp : Application()
