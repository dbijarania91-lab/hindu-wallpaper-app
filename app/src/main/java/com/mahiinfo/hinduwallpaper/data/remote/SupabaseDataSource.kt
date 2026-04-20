package com.mahiinfo.hinduwallpaper.data.remote

import com.mahiinfo.hinduwallpaper.BuildConfig
import com.mahiinfo.hinduwallpaper.data.model.*
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseClient @Inject constructor() {
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
        install(Storage)
    }
}

@Singleton
class WallpaperRemoteDataSource @Inject constructor(
    private val supabase: SupabaseClient
) {
    private val db get() = supabase.client.postgrest

    suspend fun getTrendingWallpapers(limit: Int = 20): List<Wallpaper> =
        withContext(Dispatchers.IO) {
            db.from("wallpapers")
                .select(Columns.ALL) {
                    filter { eq("is_trending", true) }
                    order("view_count", Order.DESCENDING)
                    limit(limit.toLong())
                }.decodeList()
        }

    suspend fun getWallpapersByCategory(
        category: String,
        type: WallpaperType? = null,
        page: Int = 0,
        pageSize: Int = 20
    ): List<Wallpaper> = withContext(Dispatchers.IO) {
        db.from("wallpapers")
            .select(Columns.ALL) {
                filter {
                    eq("category", category)
                    if (type != null) eq("type", type.name.lowercase())
                }
                order("created_at", Order.DESCENDING)
                range(from = (page * pageSize).toLong(), to = ((page + 1) * pageSize - 1).toLong())
            }.decodeList()
    }

    suspend fun getAllWallpapers(
        type: WallpaperType? = null,
        page: Int = 0,
        pageSize: Int = 20
    ): List<Wallpaper> = withContext(Dispatchers.IO) {
        db.from("wallpapers")
            .select(Columns.ALL) {
                if (type != null) filter { eq("type", type.name.lowercase()) }
                order("created_at", Order.DESCENDING)
                range(from = (page * pageSize).toLong(), to = ((page + 1) * pageSize - 1).toLong())
            }.decodeList()
    }

    suspend fun searchWallpapers(query: String): List<Wallpaper> = withContext(Dispatchers.IO) {
        db.from("wallpapers")
            .select(Columns.ALL) {
                filter { ilike("title", "%$query%") }
                limit(30)
            }.decodeList()
    }

    suspend fun incrementDownload(id: String) = withContext(Dispatchers.IO) {
        db.rpc("increment_download", mapOf("wallpaper_id" to id))
    }

    suspend fun getTrendingStatuses(limit: Int = 20): List<VideoStatus> =
        withContext(Dispatchers.IO) {
            db.from("video_statuses")
                .select(Columns.ALL) {
                    filter { eq("is_trending", true) }
                    order("download_count", Order.DESCENDING)
                    limit(limit.toLong())
                }.decodeList()
        }

    suspend fun getAllStatuses(category: String? = null, page: Int = 0): List<VideoStatus> =
        withContext(Dispatchers.IO) {
            db.from("video_statuses")
                .select(Columns.ALL) {
                    if (!category.isNullOrBlank()) filter { eq("category", category) }
                    order("created_at", Order.DESCENDING)
                    range(from = (page * 20).toLong(), to = ((page + 1) * 20 - 1).toLong())
                }.decodeList()
        }

    suspend fun getCategories(type: String = "wallpaper"): List<Category> =
        withContext(Dispatchers.IO) {
            db.from("categories")
                .select(Columns.ALL) {
                    filter { eq("type", type) }
                    order("sort_order", Order.ASCENDING)
                }.decodeList()
        }

    suspend fun getAppConfig(): AppConfig = withContext(Dispatchers.IO) {
        db.from("app_config")
            .select(Columns.ALL) { limit(1) }
            .decodeSingle()
    }

    suspend fun getSignedUrl(bucket: String, path: String): String = withContext(Dispatchers.IO) {
        supabase.client.storage.from(bucket).createSignedUrl(path, 3600L)
    }
}
