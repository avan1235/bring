package `in`.procyk.bring

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.twotone.Favorite
import androidx.compose.material.icons.twotone.Settings
import androidx.compose.material.icons.twotone.Summarize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.ContextClick
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import bring.composeapp.generated.resources.Res
import bring.composeapp.generated.resources.favorites
import bring.composeapp.generated.resources.settings
import bring.composeapp.generated.resources.shopping_list
import `in`.procyk.bring.ui.BringAppTheme
import `in`.procyk.bring.ui.components.*
import `in`.procyk.bring.ui.components.snackbar.Snackbar
import `in`.procyk.bring.ui.components.snackbar.SnackbarHost
import `in`.procyk.bring.ui.foundation.systemBarsForVisualComponents
import `in`.procyk.bring.ui.screen.CreateListScreen
import `in`.procyk.bring.ui.screen.EditListScreen
import `in`.procyk.bring.ui.screen.FavoritesScreen
import `in`.procyk.bring.ui.screen.SettingsScreen
import `in`.procyk.bring.ui.useBottomNavigation
import `in`.procyk.bring.vm.*
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun BringApp(initListId: String? = null) {
    BringAppTheme { context ->
        BringAppInternal(context, initListId)
    }
}

@Composable
internal fun BringAppInternal(
    context: AbstractViewModel.Context,
    initListId: String? = null,
) {
    LaunchedEffect(initListId) {
        context.store.update { it?.copy(lastListId = initListId) }
    }
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.padding(WindowInsets.systemBarsForVisualComponents.asPaddingValues())
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val topBarText by context.topBarText.collectAsState()
                Text(
                    text = topBarText,
                    style = BringAppTheme.typography.h2,
                    color = BringAppTheme.colors.onBackground,
                    modifier = Modifier.weight(weight = 1f).padding(16.dp)
                )
                if (!useBottomNavigation) {
                    Navigation(context)
                }
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = context.snackbarHostState,
                modifier = Modifier.padding(WindowInsets.navigationBars.asPaddingValues())
            ) {
                Snackbar(it)
            }
        },
        bottomBar = {
            if (useBottomNavigation) {
                Navigation(context)
            }
        },
        content = { padding ->
            val focusManager = LocalFocusManager.current
            Box(
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { focusManager.clearFocus() })
                }.padding(padding),
            ) {
                NavHost(
                    navController = context.navController,
                    startDestination = when (initListId) {
                        null -> Screen.CreateList
                        else -> Screen.EditList(
                            initListId, fetchSuggestionsAndFavoriteElements = false
                        )
                    },
                    enterTransition = {
                        slideIntoContainer(
                            towards = towards(),
                            animationSpec = tween(durationMillis = 400, easing = EaseOut),
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            towards = towards(),
                            animationSpec = tween(durationMillis = 400, easing = EaseOut),
                            targetOffset = { fullOffset -> (fullOffset * 0.4f).toInt() },
                        )
                    },
                ) {
                    composable<Screen.CreateList> {
                        val vm = viewModel {
                            CreateListScreenViewModel(
                                context = context,
                            )
                        }
                        CreateListScreen(vm)
                    }
                    composable<Screen.EditList> {
                        val editList = it.toRoute<Screen.EditList>()
                        val listId = editList.listId.let(Uuid::parseHexDash)
                        val fetchSuggestionsAndFavoriteElements = editList.fetchSuggestionsAndFavoriteElements
                        val vm = viewModel {
                            EditListScreenViewModel(
                                context = context,
                                listId = listId,
                                fetchSuggestionsAndFavoriteElements = fetchSuggestionsAndFavoriteElements,
                            )
                        }
                        EditListScreen(vm)
                    }
                    composable<Screen.Favorites> {
                        val vm = viewModel { FavoritesViewModel(context) }
                        FavoritesScreen(vm)
                    }
                    composable<Screen.Settings> {
                        val vm = viewModel { SettingsViewModel(context) }
                        SettingsScreen(vm)
                    }
                }
            }
        },
    )
}

@Composable
private fun Navigation(
    context: AbstractViewModel.Context,
) {
    NavigationBar(
        modifier = when {
            useBottomNavigation -> Modifier
            else -> Modifier.width(144.dp).height(48.dp)
        },
    ) {
        val currentTarget by context.navBarTarget.collectAsState()
        val haptics = LocalHapticFeedback.current
        NavBarTarget.entries.forEach { target ->
            val selected = currentTarget == target
            NavigationBarItem(
                icon = { Icon(if (selected) target.filledIcon else target.outlinedIcon) },
                selected = selected,
                onClick = {
                    context.onNavBarTargetSelected(target)
                    haptics.performHapticFeedback(ContextClick)
                },
                modifier = Modifier.testTag("button-navigate-${target.name.lowercase()}"),
                label = when {
                    !useBottomNavigation -> null
                    else -> {
                        { Text(stringResource(target.label)) }
                    }
                })
        }
    }
}

private val NavBarTarget.outlinedIcon: ImageVector
    get() = when (this) {
        NavBarTarget.Main -> Icons.TwoTone.Summarize
        NavBarTarget.Favourites -> Icons.TwoTone.Favorite
        NavBarTarget.Settings -> Icons.TwoTone.Settings
    }

private val NavBarTarget.filledIcon: ImageVector
    get() = when (this) {
        NavBarTarget.Main -> Icons.Filled.Summarize
        NavBarTarget.Favourites -> Icons.Filled.Favorite
        NavBarTarget.Settings -> Icons.Filled.Settings
    }

private val NavBarTarget.label: StringResource
    get() = when (this) {
        NavBarTarget.Main -> Res.string.shopping_list
        NavBarTarget.Favourites -> Res.string.favorites
        NavBarTarget.Settings -> Res.string.settings
    }

private fun AnimatedContentTransitionScope<NavBackStackEntry>.towards(): SlideDirection {
    val targetRoute = targetState.toScreenOrderOrNull() ?: return SlideDirection.Left
    val initialRoute = initialState.toScreenOrderOrNull() ?: return SlideDirection.Left
    return when {
        targetRoute > initialRoute -> SlideDirection.Left
        else -> SlideDirection.Right
    }
}

private fun NavBackStackEntry.toScreenOrderOrNull(): Int? = when {
    navigatesFrom<Screen.CreateList>() -> 0
    navigatesFrom<Screen.EditList>() -> 1
    navigatesFrom<Screen.Favorites>() -> 2
    navigatesFrom<Screen.Settings>() -> 3
    else -> null
}


@Serializable
internal sealed class Screen {
    @Serializable
    data object CreateList : Screen()

    @Serializable
    data object Settings : Screen()

    @Serializable
    data object Favorites : Screen()

    @Serializable
    data class EditList(val listId: String, val fetchSuggestionsAndFavoriteElements: Boolean) : Screen()
}