package com.mahiinfo.hinduwallpaper.ui.screens

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mahiinfo.hinduwallpaper.data.model.Wallpaper
import com.mahiinfo.hinduwallpaper.service.DownloadService
import com.mahiinfo.hinduwallpaper.ui.components.GlassCard
import com.mahiinfo.hinduwallpaper.ui.theme.HinduColors
import com.mahiinfo.hinduwallpaper.viewmodel.MainViewModel

@Composable
fun WallpaperDetailScreen(
    wallpaper: Wallpaper,
    vm: MainViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isFavorite by vm.isFavorite(wallpaper.id).collectAsState(initial = false)
    val applying by vm.applyingWallpaper.collectAsState()
    var showApplyDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Full-screen wallpaper preview
        AsyncImage(
            model = wallpaper.imageUrl,
            contentDescription = wallpaper.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent),
                        endY = 400f
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                        startY = 800f
                    )
                )
        )

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GlassIconButton(icon = Icons.Default.ArrowBack, onClick = onBack)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassIconButton(
                    icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    tint = if (isFavorite) HinduColors.Crimson else Color.White,
                    onClick = {
                        if (isFavorite) vm.removeFavorite(wallpaper.id)
                        else vm.toggleFavorite(wallpaper)
                    }
                )
                GlassIconButton(icon = Icons.Default.Share, onClick = {
                    shareWallpaper(context, wallpaper.imageUrl, wallpaper.title)
                })
            }
        }

        // Bottom actions panel
        GlassCard(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            cornerRadius = 24.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = wallpaper.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (wallpaper.titleHindi.isNotBlank()) {
                    Text(text = wallpaper.titleHindi, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                }
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Chip("HD") { }
                    Chip(wallpaper.category) { }
                    if (wallpaper.type.name == "LIVE") Chip("Live", HinduColors.Saffron) { }
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Apply Wallpaper
                    Button(
                        onClick = { showApplyDialog = true },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HinduColors.Saffron),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !applying
                    ) {
                        if (applying) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Default.Wallpaper, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Apply", fontWeight = FontWeight.Bold)
                        }
                    }
                    // Download
                    OutlinedButton(
                        onClick = {
                            DownloadService.start(
                                context,
                                wallpaper.imageUrl,
                                "${wallpaper.id}.jpg",
                                "image"
                            )
                            vm.incrementDownload(wallpaper.id)
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Save")
                    }
                }
            }
        }
    }

    // Apply to Home / Lock / Both dialog
    if (showApplyDialog) {
        ApplyWallpaperDialog(
            onDismiss = { showApplyDialog = false },
            onApplyHome = {
                showApplyDialog = false
                vm.applyWallpaper(wallpaper.imageUrl, WallpaperManager.FLAG_SYSTEM)
            },
            onApplyLock = {
                showApplyDialog = false
                vm.applyWallpaper(wallpaper.imageUrl, WallpaperManager.FLAG_LOCK)
            },
            onApplyBoth = {
                showApplyDialog = false
                vm.applyWallpaper(wallpaper.imageUrl, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
            }
        )
    }
}

@Composable
private fun ApplyWallpaperDialog(
    onDismiss: () -> Unit,
    onApplyHome: () -> Unit,
    onApplyLock: () -> Unit,
    onApplyBoth: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        GlassCard(cornerRadius = 24.dp) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Set as Wallpaper", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                ApplyOption("🏠 Home Screen", onClick = onApplyHome)
                Spacer(Modifier.height(8.dp))
                ApplyOption("🔒 Lock Screen", onClick = onApplyLock)
                Spacer(Modifier.height(8.dp))
                ApplyOption("📱 Both Screens", onClick = onApplyBoth, primary = true)
            }
        }
    }
}

@Composable
private fun ApplyOption(text: String, onClick: () -> Unit, primary: Boolean = false) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (primary) HinduColors.Saffron else HinduColors.GlassLight
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(text, color = Color.White, fontWeight = if (primary) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun GlassIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(HinduColors.GlassMedium)
    ) {
        Icon(icon, contentDescription = null, tint = tint)
    }
}

@Composable
private fun Chip(text: String, color: Color = HinduColors.GlassLight, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, color = Color.White, fontSize = 11.sp)
    }
}

private fun shareWallpaper(context: Context, url: String, title: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "Check out this beautiful Hindu wallpaper: $url\n\nDownload Hindu Wallpaper app!")
        putExtra(Intent.EXTRA_SUBJECT, title)
    }
    context.startActivity(Intent.createChooser(intent, "Share Wallpaper"))
}
