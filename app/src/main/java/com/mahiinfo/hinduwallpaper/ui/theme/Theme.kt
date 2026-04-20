package com.mahiinfo.hinduwallpaper.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mahiinfo.hinduwallpaper.R

// ─── Saffron & Deep Purple palette (Hindu aesthetic) ─────────────────────────

object HinduColors {
    val Saffron = Color(0xFFFF6B00)
    val SaffronLight = Color(0xFFFF9A3D)
    val SaffronDeep = Color(0xFFE55A00)
    val Gold = Color(0xFFFFD700)
    val GoldDeep = Color(0xFFB8860B)
    val DeepPurple = Color(0xFF2D0A4E)
    val RoyalPurple = Color(0xFF5B1FA8)
    val Crimson = Color(0xFFDC143C)
    val Lotus = Color(0xFFFF69B4)

    // Glassmorphism surfaces
    val GlassLight = Color(0x26FFFFFF)     // white/15%
    val GlassMedium = Color(0x40FFFFFF)    // white/25%
    val GlassBorder = Color(0x33FFFFFF)    // white/20%
    val GlassDark = Color(0x1AFFFFFF)      // white/10%

    // Backgrounds
    val BgDark = Color(0xFF0A0012)
    val BgGradientStart = Color(0xFF150025)
    val BgGradientEnd = Color(0xFF0D0018)
}

private val DarkColorScheme = darkColorScheme(
    primary = HinduColors.Saffron,
    onPrimary = Color.White,
    primaryContainer = HinduColors.SaffronDeep,
    secondary = HinduColors.Gold,
    onSecondary = Color.Black,
    tertiary = HinduColors.RoyalPurple,
    background = HinduColors.BgDark,
    surface = HinduColors.GlassLight,
    onBackground = Color.White,
    onSurface = Color.White,
    error = HinduColors.Crimson,
    outline = HinduColors.GlassBorder
)

val PujaFont = FontFamily.Default   // Replace with: FontFamily(Font(R.font.puja_regular))
val DevanagariFont = FontFamily.Default

val HinduTypography = Typography(
    displayLarge = TextStyle(fontFamily = PujaFont, fontWeight = FontWeight.Bold, fontSize = 32.sp),
    displayMedium = TextStyle(fontFamily = PujaFont, fontWeight = FontWeight.Bold, fontSize = 28.sp),
    titleLarge = TextStyle(fontFamily = PujaFont, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = PujaFont, fontWeight = FontWeight.Medium, fontSize = 18.sp),
    bodyLarge = TextStyle(fontFamily = DevanagariFont, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = DevanagariFont, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelSmall = TextStyle(fontFamily = PujaFont, fontWeight = FontWeight.Medium, fontSize = 11.sp)
)

@Composable
fun HinduWallpaperTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = HinduTypography,
        content = content
    )
}
