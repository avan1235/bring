package `in`.procyk.bring.ui

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.navigation.compose.rememberNavController
import com.materialkolor.rememberDynamicColorScheme
import `in`.procyk.bring.BringStore
import `in`.procyk.bring.LocalBringStore
import `in`.procyk.bring.bringCodec
import `in`.procyk.bring.ui.components.snackbar.rememberSnackbarHost
import `in`.procyk.bring.ui.foundation.ripple
import `in`.procyk.bring.vm.AbstractViewModel.Context
import io.github.xxfast.kstore.storeOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

object BringAppTheme {
    val colors: Colors
        @ReadOnlyComposable @Composable get() = LocalColors.current

    val typography: Typography
        @ReadOnlyComposable @Composable get() = LocalTypography.current
}

@Composable
internal inline fun BringAppTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    crossinline content: @Composable (Context) -> Unit,
) {
    val codec = bringCodec<BringStore>()
    val store = remember { storeOf(codec, default = BringStore.Default) }
    val snackbarHostState = rememberSnackbarHost()
    val navController = rememberNavController()
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val context = remember(navController, snackbarHostState, store, scope) {
        Context(navController, snackbarHostState, store, clipboard, scope)
    }

    val theme by store.updates.mapNotNull { it?.darkMode }.collectAsState(Theme.System)
    val themeColor by store.updates.mapNotNull { it?.themeColor }.map { Color(it) }
        .collectAsState(Color(BringStore.Default.themeColor))
    val colorScheme = rememberDynamicColorScheme(
        seedColor = themeColor, isDark = when (theme) {
            Theme.System -> isDarkTheme
            Theme.Light -> false
            Theme.Dark -> true
        }
    )

    val colors = remember(colorScheme) { colorScheme.toColors() }
    val rippleIndication = ripple()
    val selectionColors = rememberTextSelectionColors(colors)
    val typography = provideTypography()
    val currStore by store.updates.collectAsState(BringStore.Default)

    MaterialTheme(
        colorScheme = colorScheme,
    ) {
        CompositionLocalProvider(
            LocalColors provides colors,
            LocalTypography provides typography,
            LocalIndication provides rippleIndication,
            LocalTextSelectionColors provides selectionColors,
            LocalContentColor provides colors.contentColorFor(colors.background),
            LocalTextStyle provides typography.body1,
            LocalBringStore provides (currStore ?: BringStore.Default),
            content = { content(context) },
        )
    }
}

@Composable
internal fun contentColorFor(color: Color): Color {
    return BringAppTheme.colors.contentColorFor(color)
}

@Composable
internal fun rememberTextSelectionColors(colorScheme: Colors): TextSelectionColors {
    val primaryColor = colorScheme.primary
    return remember(primaryColor) {
        TextSelectionColors(
            handleColor = primaryColor,
            backgroundColor = primaryColor.copy(alpha = TextSelectionBackgroundOpacity),
        )
    }
}

internal const val TextSelectionBackgroundOpacity = 0.4f
