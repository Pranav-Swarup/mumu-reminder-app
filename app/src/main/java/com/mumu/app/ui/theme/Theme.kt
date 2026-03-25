package com.mumu.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Pastel Palette ───
val SoftBlack = Color(0xFF121212)
val CardDark = Color(0xFF1E1E1E)
val SurfaceDark = Color(0xFF181818)

val Lavender = Color(0xFFCDB4DB)
val Peach = Color(0xFFFFC8A2)
val Mint = Color(0xFFBDE0C4)
val SoftPink = Color(0xFFFFAFCC)
val SoftBlue = Color(0xFFA2D2FF)
val SoftYellow = Color(0xFFFDFFB6)

val LavenderDim = Color(0xFF3D2E4A)
val PeachDim = Color(0xFF4A3228)
val MintDim = Color(0xFF2A3E30)
val PinkDim = Color(0xFF4A2838)

val OffWhite = Color(0xFFEDEDED)
val MutedGray = Color(0xFF8A8A8A)
val DimGray = Color(0xFF555555)
val UrgentRed = Color(0xFFFF6B6B)

// Card color palette (for notes and tags)
val PastelColors = listOf(Lavender, Peach, Mint, SoftPink, SoftBlue, SoftYellow)
val DimColors = listOf(LavenderDim, PeachDim, MintDim, PinkDim)

// ─── Typography ───
// Using default sans-serif (will use system's rounded sans on most Android devices)
val MuMuFontFamily = FontFamily.Default

val MuMuTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = MuMuFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        letterSpacing = (-0.5).sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = MuMuFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        letterSpacing = 0.sp,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = MuMuFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        letterSpacing = 0.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = MuMuFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        letterSpacing = 0.1.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = MuMuFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        letterSpacing = 0.15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = MuMuFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        letterSpacing = 0.15.sp,
        lineHeight = 19.sp
    ),
    labelLarge = TextStyle(
        fontFamily = MuMuFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        letterSpacing = 0.5.sp,
        lineHeight = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = MuMuFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        letterSpacing = 0.4.sp,
        lineHeight = 15.sp
    )
)

// ─── Color Scheme ───
private val MuMuDarkColorScheme = darkColorScheme(
    primary = Lavender,
    onPrimary = SoftBlack,
    primaryContainer = LavenderDim,
    onPrimaryContainer = Lavender,
    secondary = Peach,
    onSecondary = SoftBlack,
    secondaryContainer = PeachDim,
    onSecondaryContainer = Peach,
    tertiary = Mint,
    onTertiary = SoftBlack,
    tertiaryContainer = MintDim,
    onTertiaryContainer = Mint,
    error = UrgentRed,
    onError = SoftBlack,
    background = SoftBlack,
    onBackground = OffWhite,
    surface = SurfaceDark,
    onSurface = OffWhite,
    surfaceVariant = CardDark,
    onSurfaceVariant = MutedGray,
    outline = DimGray
)

// ─── Shapes ───
val MuMuShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MuMuTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MuMuDarkColorScheme,
        typography = MuMuTypography,
        shapes = MuMuShapes,
        content = content
    )
}
