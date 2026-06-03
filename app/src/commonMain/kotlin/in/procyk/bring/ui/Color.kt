package `in`.procyk.bring.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.materialkolor.dynamicColorScheme
import `in`.procyk.bring.BringStore
import kotlinx.serialization.Serializable

@Immutable
data class Colors(
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val error: Color,
    val onError: Color,
    val success: Color,
    val onSuccess: Color,
    val disabled: Color,
    val onDisabled: Color,
    val surface: Color,
    val onSurface: Color,
    val background: Color,
    val onBackground: Color,
    val outline: Color,
    val transparent: Color = Color.Transparent,
    val white: Color = Color.White,
    val black: Color = Color.Black,
    val text: Color,
    val textSecondary: Color,
    val textDisabled: Color,
    val scrim: Color,
    val elevation: Color = Color.Black,
)

internal fun ColorScheme.toColors(): Colors = let { colorScheme ->
    Colors(
        primary = colorScheme.primary,
        onPrimary = colorScheme.onPrimary,
        secondary = colorScheme.secondary,
        onSecondary = colorScheme.onSecondary,
        tertiary = colorScheme.tertiary,
        onTertiary = colorScheme.onTertiary,
        background = colorScheme.surfaceVariant,
        onBackground = colorScheme.onSurfaceVariant,
        surface = colorScheme.surface,
        onSurface = colorScheme.onSurface,
        error = colorScheme.error,
        onError = colorScheme.onError,
        outline = colorScheme.outline,
        scrim = colorScheme.scrim,
        text = colorScheme.primary,
        textSecondary = colorScheme.secondary,
        textDisabled = colorScheme.tertiary,
        success = colorScheme.primary,
        onSuccess = colorScheme.onPrimary,
        disabled = colorScheme.secondary,
        onDisabled = colorScheme.onSecondary,
    )
}

@Serializable
enum class Theme {
    System, Light, Dark
}

val LocalColors = staticCompositionLocalOf<Colors> {
    dynamicColorScheme(
        seedColor = Color(BringStore.Default.themeColor),
        isDark = true
    ).toColors()
}
val LocalContentColor = compositionLocalOf { Color.Black }

fun Colors.contentColorFor(backgroundColor: Color): Color {
    return when (backgroundColor) {
        primary -> onPrimary
        secondary -> onSecondary
        tertiary -> onTertiary
        surface -> onSurface
        error -> onError
        success -> onSuccess
        disabled -> onDisabled
        background -> onBackground
        else -> Color.Unspecified
    }
}
