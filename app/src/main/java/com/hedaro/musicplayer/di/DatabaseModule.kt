package com.hedaro.musicplayer.di

import android.content.Context
import androidx.room.Room
import com.hedaro.musicplayer.data.local.db.MusicDatabase
import com.hedaro.musicplayer.data.local.db.dao.PlaylistDao
import com.hedaro.musicplayer.data.local.db.dao.TrackStatsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides the Room database and its DAOs to the Hilt graph.
 * Installed in [SingletonComponent] so a single database instance lives for the whole app.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMusicDatabase(@ApplicationContext context: Context): MusicDatabase =
        Room.databaseBuilder(context, MusicDatabase::class.java, "music.db").build()

    @Provides
    fun providePlaylistDao(database: MusicDatabase): PlaylistDao = database.playlistDao()

    @Provides
    fun provideTrackStatsDao(database: MusicDatabase): TrackStatsDao = database.trackStatsDao()
}
