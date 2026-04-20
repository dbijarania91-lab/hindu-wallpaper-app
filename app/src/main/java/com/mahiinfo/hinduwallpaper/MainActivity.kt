package com.mahiinfo.hinduwallpaper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.mahiinfo.hinduwallpaper.ui.screens.*
import com.mahiinfo.hinduwallpaper.ui.theme.HinduColors
import com.mahiinfo.hinduwallpaper.ui.theme.HinduWallpaperTheme
import dagger.hilt.android.AndroidEntryPoint

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Wallpapers : Screen("wallpapers", "Wallpaper", Icons.Default.Wallpaper)
    object Status : Screen("status", "Status", Icons.Default.VideoLibrary)
    object Favorites : Screen("favorites", "Saved", Icons.Default.FavoriteBorder)
    object More : Screen("more", "More", Icons.Default.GridView)
}

val bottomNavItems = listOf(Screen.Home, Screen.Wallpapers, Screen.Status, Screen.Favorites, Screen.More)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HinduWallpaperTheme {
                HinduWallpaperApp()
            }
        }
    }
}

@Composable
fun HinduWallpaperApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                GlassBottomNav(
                    items = bottomNavItems,
                    currentRoute = currentRoute,
                    onItemClick = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        containerColor = HinduColors.BgDark
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onWallpaperClick = { id -> navController.navigate("wallpaper_detail/$id") },
                    onStatusClick = { id -> navController.navigate("status_detail/$id") },
                    onSeeAllWallpapers = { navController.navigate(Screen.Wallpapers.route) },
                    onSeeAllStatus = { navController.navigate(Screen.Status.route) }
                )
            }
            composable(Screen.Wallpapers.route) {
                WallpaperListScreen(
                    onWallpaperClick = { id -> navController.navigate("wallpaper_detail/$id") }
                )
            }
            composable(Screen.Status.route) { StatusScreen() }
            composable(Screen.Favorites.route) { FavoritesScreen(
                onWallpaperClick = { id -> navController.navigate("wallpaper_detail/$id") }
            ) }
            composable(Screen.More.route) { MoreScreen() }

            composable("wallpaper_detail/{id}") { backStack ->
                val id = backStack.arguments?.getString("id") ?: return@composable
                // In real app: load wallpaper by ID from VM, pass to detail screen
                // WallpaperDetailScreen(wallpaper = ..., onBack = navController::popBackStack)
            }
        }
    }
}

@Composable
fun GlassBottomNav(
    items: List<Screen>,
    currentRoute: String?,
    onItemClick: (Screen) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f))
                )
            )
    ) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(24.dp)),
            containerColor = HinduColors.GlassMedium,
            contentColor = Color.White,
            tonalElevation = 0.dp
        ) {
            items.forEach { screen ->
                val selected = currentRoute == screen.route
                NavigationBarItem(
                    selected = selected,
                    onClick = { onItemClick(screen) },
                    icon = {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.label,
                            modifier = if (selected) Modifier.size(26.dp) else Modifier.size(22.dp)
                        )
                    },
                    label = { Text(screen.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = HinduColors.Saffron,
                        selectedTextColor = HinduColors.Saffron,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f),
                        indicatorColor = HinduColors.Saffron.copy(alpha = 0.15f)
                    )
                )
            }
        }
    }
}
