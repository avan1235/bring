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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.twotone.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import bring.app.generated.resources.*
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import `in`.procyk.bring.ui.BringAppTheme
import `in`.procyk.bring.ui.Theme
import `in`.procyk.bring.ui.components.*
import `in`.procyk.bring.ui.components.liquid.LiquidBottomTab
import `in`.procyk.bring.ui.components.liquid.LiquidBottomTabs
import `in`.procyk.bring.ui.components.snackbar.Snackbar
import `in`.procyk.bring.ui.components.snackbar.SnackbarHost
import `in`.procyk.bring.ui.components.topbar.TopBarDefaults
import `in`.procyk.bring.ui.screen.*
import `in`.procyk.bring.vm.*
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun BringApp(
    platformContext: PlatformContext,
    initListId: String? = null,
) {
    BringAppTheme(platformContext) { context ->
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
                val useBottomNavigation by context.useBottomNavigation.collectAsState()
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
                modifier = Modifier.padding(WindowInsets.navigationBars.asPaddingValues()),
            ) {
                val useBottomNavigation by context.useBottomNavigation.collectAsState()
                val useLiquidGlassNavigation by context.useLiquidGlassNavigation.collectAsState()
                Snackbar(
                    it,
                    modifier = if (useBottomNavigation && useLiquidGlassNavigation) Modifier.padding(bottom = 88.dp) else Modifier,
                )
            }
        },
        bottomBar = {
            val useBottomNavigation by context.useBottomNavigation.collectAsState()
            val useLiquidGlassNavigation by context.useLiquidGlassNavigation.collectAsState()
            if (useBottomNavigation && !useLiquidGlassNavigation) {
                Navigation(context)
            }
        },
        content = { padding ->
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                val backdrop = rememberLayerBackdrop()
                val focusManager = LocalFocusManager.current
                NavHost(
                    navController = context.navController,
                    modifier = Modifier
                        .layerBackdrop(backdrop)
                        .then(
                            Modifier.pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { focusManager.clearFocus() },
                                )
                            }.padding(
                                top = padding.calculateTopPadding(),
                                bottom = padding.calculateBottomPadding(),
                            ),
                        )
                        .fillMaxSize(),
                    startDestination = when (initListId) {
                        null -> Screen.CreateList
                        else -> Screen.EditList(
                            initListId, fetchSuggestionsAndFavoriteElements = true,
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
                        val vm = viewModel(key = "edit-list-${editList.listId}") {
                            EditListScreenViewModel(
                                context = context,
                                listId = listId,
                                fetchSuggestionsAndFavoriteElements = fetchSuggestionsAndFavoriteElements,
                            )
                        }
                        EditListScreen(padding, vm)
                    }
                    composable<Screen.Recipes> {
                        val vm = viewModel { RecipesViewModel(context) }
                        RecipesScreen(padding, vm)
                    }
                    composable<Screen.Recipe> { backStackEntry ->
                        val route = backStackEntry.toRoute<Screen.Recipe>()
                        val vm = viewModel(key = route.recipeId) { RecipeViewModel(context, route.recipeId) }
                        RecipeScreen(padding, vm)
                    }
                    composable<Screen.LoyaltyCards> {
                        val vm = viewModel { LoyaltyCardsViewModel(context) }
                        LoyaltyCardsScreen(padding, vm)
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
                val useBottomNavigation by context.useBottomNavigation.collectAsState()
                val useLiquidGlassNavigation by context.useLiquidGlassNavigation.collectAsState()
                if (useBottomNavigation && useLiquidGlassNavigation) {
                    LiquidNavigation(context, padding, backdrop)
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
                initialOffsetY = { fullWidth -> fullWidth },
            ) togetherWith slideOutHorizontally(
                animationSpec = tween(ScreenChangeAnimationDurationMillis),
                targetOffsetX = { fullWidth -> -fullWidth },
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
private fun BoxScope.LiquidNavigation(
    context: AbstractViewModel.Context,
    paddingValues: PaddingValues,
    backdrop: Backdrop,
) {
    val currentTarget by context.navBarTarget.collectAsState()
    val theme by context.store.updates.mapNotNull { it?.darkMode }.collectAsState(Theme.System)
    val isLightTheme = when (theme) {
        Theme.Light -> true
        Theme.Dark -> false
        Theme.System -> !isSystemInDarkTheme()
    }
    val contentColor = BringAppTheme.colors.onSurface
    val iconColorFilter = ColorFilter.tint(contentColor)
    LiquidBottomTabs(
        selectedTab = { currentTarget },
        onTabSelected = { context.onNavBarTargetSelected(it) },
        fromIndex = { NavBarTarget.entries[it] },
        backdrop = backdrop,
        tabsCount = NavBarTarget.entries.size,
        isLightTheme = isLightTheme,
        modifier = Modifier
            .widthIn(max = NavBarTarget.entries.size * 128.dp)
            .padding(bottom = 24.dp)
            .padding(horizontal = 16.dp)
            .padding(paddingValues)
            .align(Alignment.BottomCenter),
    ) {
        val currentTarget by context.navBarTarget.collectAsState()
        NavBarTarget.entries.forEach { target ->
            val selected = currentTarget == target
            LiquidBottomTab(
                onClick = { context.onNavBarTargetSelected(target) },
                modifier = Modifier.testTag("button-navigate-${target.name.lowercase()}"),
            ) {
                val painter = rememberVectorPainter(image = if (selected) target.filledIcon else target.outlinedIcon)
                Box(
                    Modifier
                        .size(28f.dp)
                        .paint(painter, colorFilter = iconColorFilter),
                )
                BasicText(
                    stringResource(target.label),
                    style = TextStyle(contentColor, 12f.sp),
                )
            }
        }
    }
}

@Composable
private fun Navigation(
    context: AbstractViewModel.Context,
) {
    val useBottomNavigation by context.useBottomNavigation.collectAsState()
    val density = LocalDensity.current
    NavigationBar(
        modifier = when {
            useBottomNavigation -> Modifier
            else -> Modifier.size(300.dp, 48.dp)
        },
        windowInsets = when {
            useBottomNavigation -> NavigationBarDefaults.windowInsets
                .takeIf { it.getBottom(density) > 0 }
                ?: WindowInsets(bottom = 32.dp)

            else -> WindowInsets()
        },
    ) {
        val currentTarget by context.navBarTarget.collectAsState()
        NavBarTarget.entries.forEach { target ->
            val selected = currentTarget == target
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) target.filledIcon else target.twoToneIcon,
                        modifier = Modifier
                            .background(
                                color = if (selected) BringAppTheme.colors.primary.copy(alpha = 0.16f) else Color.Transparent,
                                shape = RoundedCornerShape(16.dp),
                            )
                            .padding(horizontal = 18.dp, vertical = 4.dp),
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
                },
            )
        }
    }
}

private const val ScreenChangeAnimationDurationMillis: Int = 400

private val NavBarTarget.twoToneIcon: ImageVector
    get() = when (this) {
        NavBarTarget.Main -> Icons.TwoTone.Summarize
        NavBarTarget.LoyaltyCards -> Icons.TwoTone.CreditCard
        NavBarTarget.Recipes -> Icons.TwoTone.RestaurantMenu
        NavBarTarget.Favourites -> Icons.TwoTone.Favorite
        NavBarTarget.Settings -> Icons.TwoTone.Settings
    }

private val NavBarTarget.outlinedIcon: ImageVector
    get() = when (this) {
        NavBarTarget.Main -> Icons.Outlined.Summarize
        NavBarTarget.LoyaltyCards -> Icons.Outlined.CreditCard
        NavBarTarget.Recipes -> Icons.Outlined.RestaurantMenu
        NavBarTarget.Favourites -> Icons.Outlined.Favorite
        NavBarTarget.Settings -> Icons.Outlined.Settings
    }

private val NavBarTarget.filledIcon: ImageVector
    get() = when (this) {
        NavBarTarget.Main -> Icons.Filled.Summarize
        NavBarTarget.LoyaltyCards -> Icons.Filled.CreditCard
        NavBarTarget.Recipes -> Icons.Filled.RestaurantMenu
        NavBarTarget.Favourites -> Icons.Filled.Favorite
        NavBarTarget.Settings -> Icons.Filled.Settings
    }

private val NavBarTarget.label: StringResource
    get() = when (this) {
        NavBarTarget.Main -> Res.string.lists
        NavBarTarget.LoyaltyCards -> Res.string.cards
        NavBarTarget.Recipes -> Res.string.recipes
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
    navigatesFrom<Screen.Recipes>() -> 2
    navigatesFrom<Screen.Recipe>() -> 3
    navigatesFrom<Screen.LoyaltyCards>() -> 4
    navigatesFrom<Screen.Favorites>() -> 5
    navigatesFrom<Screen.Settings>() -> 6
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
    data object Recipes : Screen()

    @Serializable
    data class Recipe(val recipeId: String) : Screen()

    @Serializable
    data object LoyaltyCards : Screen()

    @Serializable
    data class EditList(val listId: String, val fetchSuggestionsAndFavoriteElements: Boolean) : Screen()
}