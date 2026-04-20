package com.mahiinfo.hinduwallpaper.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mahiinfo.hinduwallpaper.data.model.WallpaperType
import com.mahiinfo.hinduwallpaper.ui.components.WallpaperCard
import com.mahiinfo.hinduwallpaper.ui.theme.HinduColors
import com.mahiinfo.hinduwallpaper.viewmodel.MainViewModel

@Composable
fun WallpaperListScreen(
    vm: MainViewModel = hiltViewModel(),
    onWallpaperClick: (String) -> Unit
) {
    val state by vm.wallpaperListState.collectAsState()
    val gridState = rememberLazyGridState()
    var selectedFilter by remember { mutableStateOf<WallpaperType?>(null) }

    // Infinite scroll trigger
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= state.items.size - 6 && !state.isLoading && state.hasMore
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) vm.loadWallpapers(selectedFilter)
    }
    LaunchedEffect(selectedFilter) {
        vm.loadWallpapers(selectedFilter)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(HinduColors.BgGradientStart, HinduColors.BgGradientEnd)))
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🕉 Wallpapers", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            // Filter Tabs
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(HinduColors.GlassDark),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterTab("All", Icons.Default.GridView, selectedFilter == null) {
                    selectedFilter = null
                }
                FilterTab("Image", Icons.Default.Image, selectedFilter == WallpaperType.IMAGE) {
                    selectedFilter = WallpaperType.IMAGE
                }
                FilterTab("Live", Icons.Default.Movie, selectedFilter == WallpaperType.LIVE) {
                    selectedFilter = WallpaperType.LIVE
                }
            }

            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.items, key = { it.id }) { w ->
                    WallpaperCard(
                        imageUrl = w.thumbnailUrl,
                        title = w.title,
                        isLive = w.type == WallpaperType.LIVE,
                        isTrending = w.isTrending,
                        onClick = { onWallpaperClick(w.id) }
                    )
                }

                if (state.isLoading) {
                    item {
                        Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = HinduColors.Saffron, modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterTab(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) HinduColors.Saffron.copy(alpha = 0.2f) else Color.Transparent)
    ) {
        Icon(
            icon, contentDescription = null,
            tint = if (selected) HinduColors.Saffron else Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            label,
            color = if (selected) HinduColors.Saffron else Color.White.copy(alpha = 0.5f),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}
