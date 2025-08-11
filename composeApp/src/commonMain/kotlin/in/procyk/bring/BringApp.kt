package `in`.procyk.bring

import androidx.compose.animation.*
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
import androidx.compose.animation.core.Spring.StiffnessLow
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.twotone.Favorite
import androidx.compose.material.icons.twotone.Settings
import androidx.compose.material.icons.twotone.Summarize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
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
import `in`.procyk.bring.ui.components.topbar.TopBarDefaults
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
                modifier = Modifier
                    .windowInsetsPadding(TopBarDefaults.windowInsets)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val topBarText by context.topBarText.collectAsState()
                AnimatedTopBar(topBarText, modifier = Modifier.weight(1f))
                if (!useBottomNavigation) {
                    Navigation(context)
                    Spacer(Modifier.width(16.dp))
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
            NavHost(
                navController = context.navController,
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { focusManager.clearFocus() })
                }.padding(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding(),
                ),
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
                    )
                },
            ) {
                composable<Screen.CreateList> {
                    val vm = viewModel {
                        CreateListScreenViewModel(
                            context = context,
                        )
                    }
                    CreateListScreen(padding, vm)
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
                    EditListScreen(padding, vm)
                }
                composable<Screen.Favorites> {
                    val vm = viewModel { FavoritesViewModel(context) }
                    FavoritesScreen(padding, vm)
                }
                composable<Screen.Settings> {
                    val vm = viewModel { SettingsViewModel(context) }
                    SettingsScreen(padding, vm)
                }
            }
        },
    )
}

@Composable
private fun AnimatedTopBar(
    text: String,
    modifier: Modifier = Modifier,
) {
    var counter by remember { mutableStateOf(0L) }
    var textWithCounter by remember { mutableStateOf(text to counter) }
    LaunchedEffect(text) {
        textWithCounter = text to counter
        counter += 1
    }
    AnimatedContent(
        targetState = textWithCounter,
        transitionSpec = {
            slideInVertically(
                animationSpec = spring(DampingRatioMediumBouncy, StiffnessLow),
                initialOffsetY = { fullWidth -> fullWidth }
            ) togetherWith slideOutHorizontally(
                animationSpec = tween(ScreenChangeAnimationDurationMillis),
                targetOffsetX = { fullWidth -> -fullWidth }
            )
        },
        contentAlignment = Alignment.CenterStart,
        modifier = modifier,
    ) { (currentText, _) ->
        Text(
            text = currentText,
            modifier = Modifier.padding(16.dp),
            style = BringAppTheme.typography.h2,
            color = BringAppTheme.colors.onBackground,
        )
    }
}

@Composable
private fun Navigation(
    context: AbstractViewModel.Context,
) {
    NavigationBar(
        modifier = when {
            useBottomNavigation -> Modifier
            else -> Modifier.size(224.dp, 48.dp)
        },
        windowInsets = when {
            useBottomNavigation -> NavigationBarDefaults.windowInsets
            else -> WindowInsets(0)
        },
    ) {
        val currentTarget by context.navBarTarget.collectAsState()
        NavBarTarget.entries.forEach { target ->
            val selected = currentTarget == target
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) target.filledIcon else target.outlinedIcon,
                        modifier = Modifier
                            .background(
                                color = if (selected) BringAppTheme.colors.primary.copy(alpha = 0.16f) else Color.Transparent,
                                shape = RoundedCornerShape(16.dp),
                            )
                            .padding(horizontal = 24.dp, vertical = 4.dp),
                    )
                },
                selected = selected,
                onClick = {
                    context.onNavBarTargetSelected(target)
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

private const val ScreenChangeAnimationDurationMillis: Int = 400

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