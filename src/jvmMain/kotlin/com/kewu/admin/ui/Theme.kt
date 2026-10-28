package com.kewu.admin.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val KewuBlue = Color(0xFF1450C8)
val KewuDeepBlue = Color(0xFF0B1E4B)
val KewuGreen = Color(0xFF19C77A)
val AdminBackground = Color(0xFFF4F7FB)
val AdminSurface = Color(0xFFFFFFFF)
val AdminInk = Color(0xFF14213D)
val AdminMuted = Color(0xFF64748B)
val AdminBorder = Color(0xFFE2E8F0)
val AdminRed = Color(0xFFD92D20)
val AdminAmber = Color(0xFFB54708)

private val LightColors = lightColorScheme(
    primary = KewuBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8EFFF),
    onPrimaryContainer = KewuDeepBlue,
    secondary = KewuGreen,
    onSecondary = Color.White,
    background = AdminBackground,
    onBackground = AdminInk,
    surface = AdminSurface,
    onSurface = AdminInk,
    surfaceVariant = Color(0xFFF8FAFC),
    onSurfaceVariant = AdminMuted,
    outline = AdminBorder,
    outlineVariant = AdminBorder,
    error = AdminRed,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8EB2FF),
    secondary = Color(0xFF55DDA2),
    background = Color(0xFF0D1526),
    surface = Color(0xFF142038),
    onBackground = Color(0xFFE7EDF8),
    onSurface = Color(0xFFE7EDF8),
    surfaceVariant = Color(0xFF1B2942),
    onSurfaceVariant = Color(0xFFAAB8CC),
    outline = Color(0xFF34445E),
    outlineVariant = Color(0xFF34445E),
)

@Composable
fun KewuAdminTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content,
    )
}
