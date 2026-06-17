package `in`.procyk.savvry.ui

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
import `in`.procyk.savvry.SavvryStore
import `in`.procyk.savvry.LocalUseHaptics
import `in`.procyk.savvry.savvryCodec
import `in`.procyk.savvry.ui.components.snackbar.rememberSnackbarHost
import `in`.procyk.savvry.ui.foundation.ripple
import `in`.procyk.savvry.vm.AbstractViewModel.Context
import `in`.procyk.savvry.vm.PlatformContext
import io.github.xxfast.kstore.storeOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

object SavvryAppTheme {
    val colors: Colors
        @ReadOnlyComposable @Composable get() = LocalColors.current

    val typography: Typography
        @ReadOnlyComposable @Composable get() = LocalTypography.current
}

@Composable
internal inline fun SavvryAppTheme(
    platformContext: PlatformContext,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    crossinline content: @Composable (Context) -> Unit,
) {
    val codec = savvryCodec<SavvryStore>()
    val store = remember { storeOf(codec, default = SavvryStore.Default) }
    val snackbarHostState = rememberSnackbarHost()
    val navController = rememberNavController()
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val context = remember(navController, snackbarHostState, store, scope) {
        Context(navController, snackbarHostState, store, clipboard, scope, platformContext)
    }

    val theme by store.updates.mapNotNull { it?.darkMode }.collectAsState(Theme.System)
    val themeColor by store.updates.mapNotNull { it?.themeColor }.map { Color(it) }
        .collectAsState(Color(SavvryStore.Default.themeColor))
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
    val currUseHaptics by store.updates.mapNotNull { it?.useHaptics }.collectAsState(SavvryStore.Default.useHaptics)

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
            LocalUseHaptics provides currUseHaptics,
            content = { content(context) },
        )
    }
}

@Composable
internal fun contentColorFor(color: Color): Color {
    return SavvryAppTheme.colors.contentColorFor(color)
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
