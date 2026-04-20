package com.mahiinfo.hinduwallpaper.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mahiinfo.hinduwallpaper.data.model.VideoStatus
import com.mahiinfo.hinduwallpaper.service.DownloadService
import com.mahiinfo.hinduwallpaper.ui.components.GlassCard
import com.mahiinfo.hinduwallpaper.ui.components.SectionHeader
import com.mahiinfo.hinduwallpaper.ui.components.StatusCard
import com.mahiinfo.hinduwallpaper.ui.theme.HinduColors
import com.mahiinfo.hinduwallpaper.viewmodel.MainViewModel
import java.io.File

@Composable
fun StatusScreen(
    vm: MainViewModel = hiltViewModel()
) {
    val state by vm.statusListState.collectAsState()
    val context = LocalContext.current
    var selectedStatus by remember { mutableStateOf<VideoStatus?>(null) }

    LaunchedEffect(Unit) { vm.loadStatuses() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(HinduColors.BgGradientStart, HinduColors.BgGradientEnd)
                )
            )
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📱 Status", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = HinduColors.Saffron)
            }

            SectionHeader("Trending Status")

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.items) { status ->
                    StatusCard(
                        thumbnailUrl = status.thumbnailUrl,
                        title = status.title,
                        durationSec = status.durationSec,
                        onClick = { selectedStatus = status }
                    )
                }

                if (state.isLoading) {
                    item {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = HinduColors.Saffron, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }

    // Status Detail Bottom Sheet
    selectedStatus?.let { status ->
        StatusDetailDialog(
            status = status,
            onDismiss = { selectedStatus = null },
            onDownload = {
                DownloadService.start(context, status.videoUrl, "${status.id}.mp4", "video")
                selectedStatus = null
            },
            onShareWhatsApp = { shareToWhatsApp(context, status) },
            onShareInstagram = { shareToInstagram(context, status) }
        )
    }
}

@Composable
private fun StatusDetailDialog(
    status: VideoStatus,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onShareWhatsApp: () -> Unit,
    onShareInstagram: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        GlassCard(cornerRadius = 24.dp) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Thumbnail preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(9f / 16f)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = status.thumbnailUrl,
                        contentDescription = status.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Icon(
                        Icons.Default.PlayCircleFilled,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.align(Alignment.Center).size(56.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(status.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                if (status.titleHindi.isNotBlank()) {
                    Text(status.titleHindi, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                }
                Spacer(Modifier.height(16.dp))

                // Download button
                Button(
                    onClick = onDownload,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HinduColors.Saffron),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Download Video", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(10.dp))

                // Share row
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // WhatsApp
                    Button(
                        onClick = onShareWhatsApp,
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("WhatsApp", fontSize = 12.sp)
                    }
                    // Instagram
                    Button(
                        onClick = onShareInstagram,
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE1306C)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Story", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// ─── Share helpers ────────────────────────────────────────────────────────────

private fun shareToWhatsApp(context: Context, status: VideoStatus) {
    // After download, share the local file
    try {
        val file = File(
            android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_MOVIES),
            "HinduStatus/${status.id}.mp4"
        )
        if (!file.exists()) {
            // Not downloaded yet — trigger download first
            DownloadService.start(context, status.videoUrl, "${status.id}.mp4", "video")
            return
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "video/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share to WhatsApp"))
    } catch (e: Exception) {
        // WhatsApp not installed — open generic share
        genericShare(context, status)
    }
}

private fun shareToInstagram(context: Context, status: VideoStatus) {
    try {
        val file = File(
            android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_MOVIES),
            "HinduStatus/${status.id}.mp4"
        )
        if (!file.exists()) {
            DownloadService.start(context, status.videoUrl, "${status.id}.mp4", "video")
            return
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent("com.instagram.share.ADD_TO_STORY").apply {
            setDataAndType(uri, "video/mp4")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        genericShare(context, status)
    }
}

private fun genericShare(context: Context, status: VideoStatus) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "Download: ${status.videoUrl}\n\nHindu Wallpaper App")
    }
    context.startActivity(Intent.createChooser(intent, "Share"))
}
