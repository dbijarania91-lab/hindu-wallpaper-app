package com.mahiinfo.hinduwallpaper

import android.app.Application
import androidx.room.Room
import com.mahiinfo.hinduwallpaper.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@HiltAndroidApp
class HinduWallpaperApp : Application()

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase =
        Room.databaseBuilder(app, AppDatabase::class.java, "hindu_wallpaper.db")
            .fallbackToDestructiveMigration()
            .build()
}
