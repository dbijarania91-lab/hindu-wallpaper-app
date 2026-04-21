package com.mahiinfo.hinduwallpaper.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mahiinfo.hinduwallpaper.ads.AdMobBanner
import com.mahiinfo.hinduwallpaper.ui.components.*
import com.mahiinfo.hinduwallpaper.ui.theme.HinduColors
import com.mahiinfo.hinduwallpaper.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    vm: MainViewModel = hiltViewModel(),
    onWallpaperClick: (String) -> Unit,
    onStatusClick: (String) -> Unit,
    onSeeAllWallpapers: () -> Unit,
    onSeeAllStatus: () -> Unit
) {
    val state by vm.homeState.collectAsState()
    val adMob = vm.adMobManager

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(HinduColors.BgGradientStart, HinduColors.BgGradientEnd)
                )
            )
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = HinduColors.Saffron
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // ── Header ────────────────────────────────────────────────────
                item {
                    HomeHeader(onSearch = { /* navigate */ })
                }

                // ── Banner Ad ─────────────────────────────────────────────────
                item {
                    if (adMob.isEnabled) {
                        Box(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                            AdMobBanner(adUnitId = adMob.bannerId)
                        }
                    }
                }

                // ── Category Chips ────────────────────────────────────────────
                item {
                    CategoryChips(
                        categories = state.categories.map { it.name },
                        onSelect = { /* filter */ }
                    )
                }

                // ── Trending Wallpapers ───────────────────────────────────────
                item {
                    SectionHeader("🔥 Trending Wallpapers", onSeeAll = onSeeAllWallpapers)
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.trendingWallpapers) { w ->
                            WallpaperCard(
                                imageUrl = w.thumbnailUrl,
                                title = w.title,
                                isLive = w.type == com.mahiinfo.hinduwallpaper.data.model.WallpaperType.LIVE,
                                isTrending = w.isTrending,
                                modifier = Modifier.width(140.dp),
                                onClick = { onWallpaperClick(w.id) }
                            )
                        }
                    }
                }

                // ── Video Status Section ──────────────────────────────────────
                item { Spacer(Modifier.height(16.dp)) }
                item {
                    SectionHeader("📱 WhatsApp Status", onSeeAll = onSeeAllStatus)
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.trendingStatuses) { s ->
                            StatusCard(
                                thumbnailUrl = s.thumbnailUrl,
                                title = s.title,
                                durationSec = s.durationSec,
                                modifier = Modifier.width(110.dp),
                                onClick = { onStatusClick(s.id) }
                            )
                        }
                    }
                }

                // ── More Wallpapers Grid ──────────────────────────────────────
                item { Spacer(Modifier.height(16.dp)) }
                item { SectionHeader("🕉 All Wallpapers") }
                item {
                    WallpaperGrid(
                        wallpapers = state.trendingWallpapers.take(6),
                        onWallpaperClick = onWallpaperClick
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(onSearch: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("🕉 Hindu", color = HinduColors.Saffron, fontSize = 14.sp)
            Text("Wallpaper", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        IconButton(
            onClick = onSearch,
            modifier = Modifier
                .clip(CircleShape)
                .background(HinduColors.GlassLight)
        ) {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
        }
    }
}

@Composable
private fun CategoryChips(categories: List<String>, onSelect: (String) -> Unit) {
    var selected by remember { mutableStateOf(0) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val all = listOf("All") + categories
        all.forEachIndexed { i, cat ->
            val isSelected = i == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) HinduColors.Saffron
                        else HinduColors.GlassLight
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    cat,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun WallpaperGrid(
    wallpapers: List<com.mahiinfo.hinduwallpaper.data.model.Wallpaper>,
    onWallpaperClick: (String) -> Unit
) {
    val chunked = wallpapers.chunked(2)
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        chunked.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { w ->
                    WallpaperCard(
                        imageUrl = w.thumbnailUrl,
                        title = w.title,
                        isLive = w.type == com.mahiinfo.hinduwallpaper.data.model.WallpaperType.LIVE,
                        isTrending = w.isTrending,
                        modifier = Modifier.weight(1f),
                        onClick = { onWallpaperClick(w.id) }
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
