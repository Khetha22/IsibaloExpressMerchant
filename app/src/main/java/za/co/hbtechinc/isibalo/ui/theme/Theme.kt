package za.co.hbtechinc.isibalo.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import za.co.hbtechinc.isibalo.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Brand blue used for the top bar and bottom nav in BOTH light and dark mode. */
val NavBlue = Color(0xFF0A4FD8)
val NavMuted = Color(0xFFBCD0FF)

private val LightColors = lightColorScheme(
    primary = NavBlue, onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE6FF), onPrimaryContainer = Color(0xFF001A52),
    secondary = Color(0xFF3B6EF0), onSecondary = Color.White,
    background = Color.White, onBackground = Color(0xFF0A1226),
    surface = Color.White, onSurface = Color(0xFF0A1226),
    surfaceVariant = Color(0xFFEEF3FF), onSurfaceVariant = Color(0xFF44506B),
    outline = Color(0xFFB4C3E6),
    error = Color(0xFFC62828), errorContainer = Color(0xFFFFE3E0), onErrorContainer = Color(0xFF5A0A06)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6C97FF), onPrimary = Color(0xFF00133A),
    primaryContainer = Color(0xFF12307A), onPrimaryContainer = Color(0xFFDCE6FF),
    secondary = Color(0xFF8FADFF), onSecondary = Color(0xFF00133A),
    background = Color(0xFF0A1222), onBackground = Color(0xFFE6ECFA),
    surface = Color(0xFF0F1A30), onSurface = Color(0xFFE6ECFA),
    surfaceVariant = Color(0xFF17243F), onSurfaceVariant = Color(0xFFA9B6D6),
    outline = Color(0xFF34466F),
    error = Color(0xFFFF8A80), errorContainer = Color(0xFF5A1A16), onErrorContainer = Color(0xFFFFDAD6)
)

private val SquareShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp), small = RoundedCornerShape(0.dp), medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp), extraLarge = RoundedCornerShape(0.dp)
)

val Bebas = FontFamily(Font(R.font.bebas_neue))

private val AppTypography = Typography(
    headlineMedium = TextStyle(fontFamily = Bebas, fontSize = 34.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.5.sp),
    titleLarge = TextStyle(fontFamily = Bebas, fontSize = 26.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.5.sp),
    titleMedium = TextStyle(fontFamily = Bebas, fontSize = 20.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.4.sp),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
    labelSmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium)
)

enum class ThemeMode { SYSTEM, LIGHT, DARK }

class ThemeStore(ctx: Context) {
    private val prefs = ctx.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val _mode = MutableStateFlow(
        runCatching { ThemeMode.valueOf(prefs.getString("theme", null) ?: "SYSTEM") }
            .getOrDefault(ThemeMode.SYSTEM)
    )
    val mode: StateFlow<ThemeMode> = _mode.asStateFlow()

    fun set(m: ThemeMode) {
        prefs.edit().putString("theme", m.name).apply()
        _mode.value = m
    }
}

@Composable
fun IsibaloTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        shapes = SquareShapes,
        typography = AppTypography,
        content = content
    )
}
