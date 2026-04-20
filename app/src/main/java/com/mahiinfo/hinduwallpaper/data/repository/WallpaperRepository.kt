package com.mahiinfo.hinduwallpaper.data.repository

import com.mahiinfo.hinduwallpaper.data.local.AppDatabase
import com.mahiinfo.hinduwallpaper.data.model.*
import com.mahiinfo.hinduwallpaper.data.remote.WallpaperRemoteDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Throwable? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

@Singleton
class WallpaperRepository @Inject constructor(
    private val remote: WallpaperRemoteDataSource,
    private val db: AppDatabase
) {
    suspend fun getTrendingWallpapers(): Result<List<Wallpaper>> = safeCall { remote.getTrendingWallpapers() }
    suspend fun getWallpapersByCategory(cat: String, type: WallpaperType? = null, page: Int = 0) =
        safeCall { remote.getWallpapersByCategory(cat, type, page) }
    suspend fun getAllWallpapers(type: WallpaperType? = null, page: Int = 0) =
        safeCall { remote.getAllWallpapers(type, page) }
    suspend fun searchWallpapers(query: String) = safeCall { remote.searchWallpapers(query) }
    suspend fun incrementDownload(id: String) = safeCall { remote.incrementDownload(id) }

    suspend fun getTrendingStatuses() = safeCall { remote.getTrendingStatuses() }
    suspend fun getAllStatuses(category: String? = null, page: Int = 0) =
        safeCall { remote.getAllStatuses(category, page) }

    suspend fun getCategories(type: String = "wallpaper") = safeCall { remote.getCategories(type) }
    suspend fun getAppConfig() = safeCall { remote.getAppConfig() }

    // Favorites
    fun getFavorites(): Flow<List<FavoriteEntity>> = db.favoriteDao().getAllFavorites()
    suspend fun addFavorite(entity: FavoriteEntity) = db.favoriteDao().insert(entity)
    suspend fun removeFavorite(id: String) = db.favoriteDao().delete(id)
    fun isFavorite(id: String): Flow<Boolean> = db.favoriteDao().isFavorite(id)

    // Downloads
    fun getDownloads(): Flow<List<DownloadEntity>> = db.downloadDao().getAllDownloads()
    suspend fun saveDownload(entity: DownloadEntity) = db.downloadDao().insert(entity)

    private suspend fun <T> safeCall(block: suspend () -> T): Result<T> = try {
        Result.Success(block())
    } catch (e: Exception) {
        Result.Error(e.message ?: "Unknown error", e)
    }
}
