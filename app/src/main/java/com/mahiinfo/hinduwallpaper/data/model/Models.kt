package com.mahiinfo.hinduwallpaper.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─── Supabase Tables ──────────────────────────────────────────────────────────

@Serializable
data class Wallpaper(
    val id: String,
    val title: String,
    @SerialName("title_hi") val titleHindi: String = "",
    val category: String,
    val type: WallpaperType,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("thumbnail_url") val thumbnailUrl: String,
    @SerialName("live_url") val liveUrl: String? = null,
    val resolution: String = "HD",        // HD, FHD, 4K
    val tags: List<String> = emptyList(),
    @SerialName("download_count") val downloadCount: Int = 0,
    @SerialName("view_count") val viewCount: Int = 0,
    @SerialName("like_count") val likeCount: Int = 0,
    @SerialName("is_trending") val isTrending: Boolean = false,
    @SerialName("is_premium") val isPremium: Boolean = false,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("storage_path") val storagePath: String = "",
    @SerialName("file_size_kb") val fileSizeKb: Long = 0L,
    @SerialName("color_palette") val colorPalette: List<String> = emptyList()
)

@Serializable
enum class WallpaperType {
    @SerialName("image") IMAGE,
    @SerialName("live") LIVE,
    @SerialName("gif") GIF
}

@Serializable
data class VideoStatus(
    val id: String,
    val title: String,
    @SerialName("title_hi") val titleHindi: String = "",
    val category: String,
    @SerialName("video_url") val videoUrl: String,
    @SerialName("thumbnail_url") val thumbnailUrl: String,
    @SerialName("duration_sec") val durationSec: Int = 30,
    @SerialName("resolution") val resolution: String = "1080p",
    @SerialName("download_count") val downloadCount: Int = 0,
    @SerialName("is_trending") val isTrending: Boolean = false,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("file_size_kb") val fileSizeKb: Long = 0L,
    val tags: List<String> = emptyList()
)

@Serializable
data class Category(
    val id: String,
    val name: String,
    @SerialName("name_hi") val nameHindi: String,
    @SerialName("icon_url") val iconUrl: String = "",
    @SerialName("banner_url") val bannerUrl: String = "",
    @SerialName("item_count") val itemCount: Int = 0,
    val type: String = "wallpaper"     // wallpaper | status
)

@Serializable
data class AppConfig(
    val id: Int = 1,
    // AdMob IDs — all managed from Supabase admin panel
    @SerialName("admob_banner_id") val admobBannerId: String = "",
    @SerialName("admob_interstitial_id") val admobInterstitialId: String = "",
    @SerialName("admob_rewarded_id") val admobRewardedId: String = "",
    @SerialName("admob_native_id") val admobNativeId: String = "",
    @SerialName("admob_open_app_id") val admobOpenAppId: String = "",
    @SerialName("ads_enabled") val adsEnabled: Boolean = true,
    @SerialName("interstitial_interval") val interstitialInterval: Int = 3,
    @SerialName("rewarded_for_premium") val rewardedForPremium: Boolean = false,
    @SerialName("app_version") val appVersion: String = "1.0.0",
    @SerialName("force_update") val forceUpdate: Boolean = false,
    @SerialName("maintenance_mode") val maintenanceMode: Boolean = false,
    @SerialName("privacy_policy_url") val privacyPolicyUrl: String = "",
    @SerialName("rate_us_url") val rateUsUrl: String = "",
    @SerialName("whatsapp_channel") val whatsappChannel: String = "",
    @SerialName("telegram_channel") val telegramChannel: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)

// ─── Local Room Entity ────────────────────────────────────────────────────────

@androidx.room.Entity(tableName = "favorites")
data class FavoriteEntity(
    @androidx.room.PrimaryKey val id: String,
    val type: String,       // wallpaper | status
    val imageUrl: String,
    val title: String,
    val addedAt: Long = System.currentTimeMillis()
)

@androidx.room.Entity(tableName = "downloads")
data class DownloadEntity(
    @androidx.room.PrimaryKey val id: String,
    val type: String,
    val localPath: String,
    val originalUrl: String,
    val title: String,
    val downloadedAt: Long = System.currentTimeMillis()
)
