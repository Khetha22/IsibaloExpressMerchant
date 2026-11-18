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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import za.co.hbtechinc.isibalo.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Brand blue matching isibalo_express_pos_preview.html (#0D47A1). */
val NavBlue = Color(0xFF0D47A1)
val NavMuted = Color(0xFFBBDEFB)

private val LightColors = lightColorScheme(
    primary = NavBlue, onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF), onPrimaryContainer = Color(0xFF001B3D),
    secondary = Color(0xFF2563EB), onSecondary = Color.White,
    background = Color(0xFFF4F7FC), onBackground = Color(0xFF0A1226),
    surface = Color.White, onSurface = Color(0xFF0A1226),
    surfaceVariant = Color(0xFFEEF3FF), onSurfaceVariant = Color(0xFF44506B),
    outline = Color(0xFFB4C3E6),
    error = Color(0xFFC62828), errorContainer = Color(0xFFFFE3E0), onErrorContainer = Color(0xFF5A0A06)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6C97FF), onPrimary = Color(0xFF00133A),
    primaryContainer = Color(0xFF12307A), onPrimaryContainer = Color(0xFFDCE6FF),
    secondary = Color(0xFF3B82F6), onSecondary = Color(0xFF00133A),
    background = Color(0xFF0B0F19), onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF111827), onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1E293B), onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155),
    error = Color(0xFFFF8A80), errorContainer = Color(0xFF5A1A16), onErrorContainer = Color(0xFFFFDAD6)
)

/** Material 3 Expressive Shape Scale */
private val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
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
        shapes = ExpressiveShapes,
        typography = AppTypography,
        content = content
    )
}
