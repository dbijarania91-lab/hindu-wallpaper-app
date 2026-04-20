package com.mahiinfo.hinduwallpaper.viewmodel

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahiinfo.hinduwallpaper.ads.AdMobManager
import com.mahiinfo.hinduwallpaper.data.model.*
import com.mahiinfo.hinduwallpaper.data.repository.Result
import com.mahiinfo.hinduwallpaper.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.net.URL
import javax.inject.Inject

data class HomeUiState(
    val trendingWallpapers: List<Wallpaper> = emptyList(),
    val trendingStatuses: List<VideoStatus> = emptyList(),
    val categories: List<Category> = emptyList(),
    val statusCategories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class WallpaperListState(
    val items: List<Wallpaper> = emptyList(),
    val isLoading: Boolean = false,
    val hasMore: Boolean = true,
    val page: Int = 0
)

data class StatusListState(
    val items: List<VideoStatus> = emptyList(),
    val isLoading: Boolean = false,
    val hasMore: Boolean = true,
    val page: Int = 0
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repo: WallpaperRepository,
    val adMobManager: AdMobManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _homeState = MutableStateFlow(HomeUiState())
    val homeState = _homeState.asStateFlow()

    private val _wallpaperListState = MutableStateFlow(WallpaperListState())
    val wallpaperListState = _wallpaperListState.asStateFlow()

    private val _statusListState = MutableStateFlow(StatusListState())
    val statusListState = _statusListState.asStateFlow()

    private val _selectedWallpaper = MutableStateFlow<Wallpaper?>(null)
    val selectedWallpaper = _selectedWallpaper.asStateFlow()

    private val _applyingWallpaper = MutableStateFlow(false)
    val applyingWallpaper = _applyingWallpaper.asStateFlow()

    val favorites = repo.getFavorites()
    val downloads = repo.getDownloads()

    init { loadHome() }

    fun loadHome() = viewModelScope.launch {
        _homeState.update { it.copy(isLoading = true) }
        try {
            val trending = (repo.getTrendingWallpapers() as? Result.Success)?.data ?: emptyList()
            val statuses = (repo.getTrendingStatuses() as? Result.Success)?.data ?: emptyList()
            val cats = (repo.getCategories("wallpaper") as? Result.Success)?.data ?: emptyList()
            val sCats = (repo.getCategories("status") as? Result.Success)?.data ?: emptyList()
            val config = (repo.getAppConfig() as? Result.Success)?.data
            if (config != null) adMobManager.initialize(config)
            _homeState.update {
                it.copy(
                    trendingWallpapers = trending,
                    trendingStatuses = statuses,
                    categories = cats,
                    statusCategories = sCats,
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            _homeState.update { it.copy(isLoading = false, error = e.message) }
        }
    }

    fun loadWallpapers(type: WallpaperType? = null, category: String? = null) = viewModelScope.launch {
        if (_wallpaperListState.value.isLoading) return@launch
        _wallpaperListState.update { it.copy(isLoading = true) }
        val page = _wallpaperListState.value.page
        val result = if (category != null) repo.getWallpapersByCategory(category, type, page)
        else repo.getAllWallpapers(type, page)
        when (result) {
            is Result.Success -> _wallpaperListState.update {
                it.copy(
                    items = it.items + result.data,
                    isLoading = false,
                    page = it.page + 1,
                    hasMore = result.data.size == 20
                )
            }
            is Result.Error -> _wallpaperListState.update { it.copy(isLoading = false) }
            else -> {}
        }
    }

    fun loadStatuses(category: String? = null) = viewModelScope.launch {
        if (_statusListState.value.isLoading) return@launch
        _statusListState.update { it.copy(isLoading = true) }
        val page = _statusListState.value.page
        when (val result = repo.getAllStatuses(category, page)) {
            is Result.Success -> _statusListState.update {
                it.copy(
                    items = it.items + result.data,
                    isLoading = false,
                    page = it.page + 1,
                    hasMore = result.data.size == 20
                )
            }
            else -> _statusListState.update { it.copy(isLoading = false) }
        }
    }

    fun selectWallpaper(w: Wallpaper) { _selectedWallpaper.value = w }

    // ─── Apply Wallpaper ─────────────────────────────────────────────────────

    fun applyWallpaper(imageUrl: String, which: Int = WallpaperManager.FLAG_SYSTEM) =
        viewModelScope.launch {
            _applyingWallpaper.value = true
            try {
                val inputStream = URL(imageUrl).openStream()
                val bmp = BitmapFactory.decodeStream(inputStream)
                val wm = WallpaperManager.getInstance(context)
                wm.setBitmap(bmp, null, true, which)
            } catch (_: Exception) { }
            _applyingWallpaper.value = false
        }

    // ─── Toggle Favorite ─────────────────────────────────────────────────────

    fun toggleFavorite(wallpaper: Wallpaper) = viewModelScope.launch {
        val fav = FavoriteEntity(
            id = wallpaper.id, type = "wallpaper",
            imageUrl = wallpaper.thumbnailUrl, title = wallpaper.title
        )
        // isFavorite check done in composable; here just toggle
        repo.addFavorite(fav)
    }

    fun removeFavorite(id: String) = viewModelScope.launch { repo.removeFavorite(id) }

    fun isFavorite(id: String): Flow<Boolean> = repo.isFavorite(id)

    fun incrementDownload(id: String) = viewModelScope.launch { repo.incrementDownload(id) }
}
