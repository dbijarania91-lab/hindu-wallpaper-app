package com.mahiinfo.hinduwallpaper.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mahiinfo.hinduwallpaper.ui.components.GlassCard
import com.mahiinfo.hinduwallpaper.ui.theme.HinduColors
import com.mahiinfo.hinduwallpaper.viewmodel.MainViewModel

@Composable
fun FavoritesScreen(
    vm: MainViewModel = hiltViewModel(),
    onWallpaperClick: (String) -> Unit
) {
    val favorites by vm.favorites.collectAsState(initial = emptyList())

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(HinduColors.BgGradientStart, HinduColors.BgGradientEnd)))
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("❤️ Favorites", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("${favorites.size} saved", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
            }

            if (favorites.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FavoriteBorder, null, tint = HinduColors.Saffron, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("No favorites yet", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Tap ♥ on any wallpaper", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(favorites, key = { it.id }) { fav ->
                        Box(modifier = Modifier.clip(RoundedCornerShape(14.dp)).aspectRatio(9f / 16f)) {
                            AsyncImage(
                                model = fav.imageUrl,
                                contentDescription = fav.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            IconButton(
                                onClick = { vm.removeFavorite(fav.id) },
                                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(32.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.Favorite, null, tint = HinduColors.Crimson, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MoreScreen() {
    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(HinduColors.BgGradientStart, HinduColors.BgGradientEnd)))
    ) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(16.dp))
            Text("More", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp))
                            .background(Brush.radialGradient(listOf(HinduColors.Saffron, HinduColors.RoyalPurple))),
                        contentAlignment = Alignment.Center
                    ) { Text("🕉", fontSize = 28.sp) }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Hindu Wallpaper", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("by Mahi Info", color = HinduColors.Saffron, fontSize = 13.sp)
                        Text("v1.0.0", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Fixed: removed YouTube/Feedback/Policy icons that may not exist, use safe alternatives
            listOf(
                Triple(Icons.Default.Star, "Rate App", HinduColors.Gold),
                Triple(Icons.Default.Share, "Share App", HinduColors.Saffron),
                Triple(Icons.Default.Info, "About", HinduColors.RoyalPurple),
                Triple(Icons.Default.PrivacyTip, "Privacy Policy", Color.White.copy(alpha = 0.7f)),
                Triple(Icons.Default.PlayCircle, "YouTube Channel", Color(0xFFFF0000)),
            ).forEach { (icon, label, tint) ->
                MoreMenuItem(icon = icon, label = label, tint = tint) { }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun MoreMenuItem(icon: ImageVector, label: String, tint: Color, onClick: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.width(14.dp))
            Text(label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
        }
    }
}
