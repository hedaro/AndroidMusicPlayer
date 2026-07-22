package com.hedaro.musicplayer.di

import com.hedaro.musicplayer.ads.AdProvider
import com.hedaro.musicplayer.ads.NoOpAdProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the app's [AdProvider] implementation. Today it's [NoOpAdProvider] (ad-free).
 *
 * To enable banner ads for a Play Store release: add an `AdMobAdProvider` and change the single
 * binding below to point at it — nothing else in the app needs to change.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AdModule {

    @Binds
    @Singleton
    abstract fun bindAdProvider(impl: NoOpAdProvider): AdProvider
}
