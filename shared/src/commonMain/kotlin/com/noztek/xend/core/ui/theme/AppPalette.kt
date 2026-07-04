package com.noztek.xend.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class XendPalette(
    val background: Color,
    val backgroundGlow: Color,
    val surface: Color,
    val surfaceSoft: Color,
    val surfaceRaised: Color,
    val ink: Color,
    val mutedInk: Color,
    val softInk: Color,
    val warmInk: Color,
    val outline: Color,
    val primary: Color,
    val primaryBright: Color,
    val primarySoft: Color,
    val heroText: Color,
    val progressTrack: Color,
    val progressFill: Color,
    val lavender: Color,
    val lavenderSoft: Color,
    val peachSoft: Color,
    val orange: Color,
    val orangeSoft: Color,
)

val lightAppPalette = XendPalette(
    background = Color(0xFFFFFAF7),
    backgroundGlow = Color(0xFFFFF3F6),
    surface = Color(0xFFFFFFFF),
    surfaceSoft = Color(0xFFFFF5F7),
    surfaceRaised = Color(0xFFFFF7F8),
    ink = Color(0xFF171725),
    mutedInk = Color(0xFF7D8091),
    softInk = Color(0xFF8E90A1),
    warmInk = Color(0xFFF08CA3),
    outline = Color(0xFFF6EBEE),
    primary = Color(0xFFF56C91),
    primaryBright = Color(0xFFFF7FA0),
    primarySoft = Color(0xFFFFEEF4),
    heroText = Color.White,
    progressTrack = Color(0xFFFBE1E8),
    progressFill = Color(0xFFF56C91),
    lavender = Color(0xFF8D72F7),
    lavenderSoft = Color(0xFFF3EEFF),
    peachSoft = Color(0xFFFFF1ED),
    orange = Color(0xFFF5A33D),
    orangeSoft = Color(0xFFFFF5E7),
)

val darkAppPalette = XendPalette(
    background = Color(0xFF1F0F10),
    backgroundGlow = Color(0xFF281718),
    surface = Color(0xFF2D1B1C),
    surfaceSoft = Color(0xFF382526),
    surfaceRaised = Color(0xFF443031),
    ink = Color(0xFFFBDBDC),
    mutedInk = Color(0xFFE5BDBF),
    softInk = Color(0xFFCFA8AA),
    warmInk = Color(0xFFFFB2B8),
    outline = Color(0xFF5C3F41),
    primary = Color(0xFFFFB2B8),
    primaryBright = Color(0xFFFF7FA0),
    primarySoft = Color(0xFF3D1B25),
    heroText = Color.White,
    progressTrack = Color(0xFF5C3F41),
    progressFill = Color(0xFFFFB2B8),
    lavender = Color(0xFFB6A5FF),
    lavenderSoft = Color(0xFF2E243F),
    peachSoft = Color(0xFF3A2622),
    orange = Color(0xFFFFB86B),
    orangeSoft = Color(0xFF3A2C16),
)

val LocalXendPalette = staticCompositionLocalOf { lightAppPalette }

object XendTheme {
    val palette: XendPalette
        @Composable get() = LocalXendPalette.current
}
