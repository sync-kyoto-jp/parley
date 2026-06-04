package jp.kyoto.sync.honnyaku.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = ParleyIndigo,
    onPrimary = Color.White,
    secondary = ParleyCyan,
    onSecondary = Color(0xFF06121A),
    tertiary = ParleyEmerald,
    onTertiary = Color(0xFF062014),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceMuted,
    outline = Color(0xFF34374A),
)

private val LightColors = lightColorScheme(
    primary = ParleyIndigo,
    onPrimary = Color.White,
    secondary = Color(0xFF0E7C97),
    onSecondary = Color.White,
    tertiary = Color(0xFF0B8A5B),
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceMuted,
    outline = Color(0xFFC9CBDA),
)

/** ブランドのグラデーション（アイコン/ボタン/ヘッダで再利用） */
object ParleyBrand {
    val gradient: Brush = Brush.linearGradient(listOf(ParleyIndigo, ParleySky, ParleyCyan))
    val activeGradient: Brush = Brush.linearGradient(listOf(ParleyEmerald, ParleyCyan))
}

@Composable
fun ParleyTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}
