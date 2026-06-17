package `in`.procyk.savvry.vm

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import `in`.procyk.savvry.ui.Theme
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

internal class SettingsViewModel(context: Context) : AbstractViewModel(context) {

    val enableShoppingListEditMode: StateFlow<Boolean> =
        storeFlow.map { it.enableShoppingListEditMode }.state(storeFlow.value.enableShoppingListEditMode)

    val enableRecipesEditMode: StateFlow<Boolean> =
        storeFlow.map { it.enableRecipesEditMode }.state(storeFlow.value.enableRecipesEditMode)

    val showRecipesLabels: StateFlow<Boolean> =
        storeFlow.map { it.showRecipesLabels }.state(storeFlow.value.showRecipesLabels)

    val useRecipesCache: StateFlow<Boolean> =
        storeFlow.map { it.useRecipesCache }.state(storeFlow.value.useRecipesCache)

    val enableCardsEditMode: StateFlow<Boolean> =
        storeFlow.map { it.enableCardsEditMode }.state(storeFlow.value.enableCardsEditMode)

    val showCardsLabels: StateFlow<Boolean> =
        storeFlow.map { it.showCardsLabels }.state(storeFlow.value.showCardsLabels)

    val useCardsCache: StateFlow<Boolean> =
        storeFlow.map { it.useCardsCache }.state(storeFlow.value.useCardsCache)

    val showUncheckedFirst: StateFlow<Boolean> =
        storeFlow.map { it.showUncheckedFirst }.state(storeFlow.value.showUncheckedFirst)

    val showSuggestions: StateFlow<Boolean> =
        storeFlow.map { it.showSuggestions }.state(storeFlow.value.showSuggestions)

    val showFavoriteElements: StateFlow<Boolean> =
        storeFlow.map { it.showFavoriteElements }.state(storeFlow.value.showFavoriteElements)

    val theme: StateFlow<Theme> =
        storeFlow.map { it.darkMode }.state(storeFlow.value.darkMode)

    val themeColor: StateFlow<Color> =
        storeFlow.map { Color(it.themeColor) }.state(Color(storeFlow.value.themeColor))

    val geminiKey: StateFlow<String> =
        storeFlow.map { it.geminiKey }.state(storeFlow.value.geminiKey)

    val useGeminiLists: StateFlow<Boolean> =
        storeFlow.map { it.useGeminiLists }.state(storeFlow.value.useGeminiLists)

    val useGeminiRecipes: StateFlow<Boolean> =
        storeFlow.map { it.useGeminiRecipes }.state(storeFlow.value.useGeminiRecipes)

    val useHaptics: StateFlow<Boolean> =
        storeFlow.map { it.useHaptics }.state(storeFlow.value.useHaptics)

    val recentShoppingListsCount: StateFlow<Int> =
        storeFlow.map { it.recentShoppingListsCount }.state(storeFlow.value.recentShoppingListsCount)

    val useBottomNavigation: StateFlow<Boolean> =
        context.useBottomNavigation

    val useLiquidGlassNavigation: StateFlow<Boolean> =
        context.useLiquidGlassNavigation

    fun onShoppingListEditModeChanged(value: Boolean) {
        launchUpdateConfig { it.copy(enableShoppingListEditMode = value) }
    }

    fun onRecipesEditModeChanged(value: Boolean) {
        launchUpdateConfig { it.copy(enableRecipesEditMode = value) }
    }

    fun onShowRecipesLabelsChanged(value: Boolean) {
        launchUpdateConfig { it.copy(showRecipesLabels = value) }
    }

    fun onUseRecipesCacheChanged(value: Boolean) {
        launchUpdateConfig { it.copy(useRecipesCache = value) }
    }

    fun onCardsEditModeChanged(value: Boolean) {
        launchUpdateConfig { it.copy(enableCardsEditMode = value) }
    }

    fun onShowCardsLabelsChanged(value: Boolean) {
        launchUpdateConfig { it.copy(showCardsLabels = value) }
    }

    fun onUseCardsCacheChanged(value: Boolean) {
        launchUpdateConfig { it.copy(useCardsCache = value) }
    }

    fun onShowUncheckedFirstChanged(value: Boolean) {
        launchUpdateConfig { it.copy(showUncheckedFirst = value) }
    }

    fun onShowSuggestionsChanged(value: Boolean) {
        launchUpdateConfig { it.copy(showSuggestions = value) }
    }

    fun onShowFavoriteElementsChanged(value: Boolean) {
        launchUpdateConfig { it.copy(showFavoriteElements = value) }
    }

    fun onThemeChanged(value: Theme) {
        launchUpdateConfig { it.copy(darkMode = value) }
    }

    fun onThemeColorChanged(value: Color) {
        launchUpdateConfig { it.copy(themeColor = value.toArgb()) }
    }

    fun onGeminiKeyChanged(value: String) {
        launchUpdateConfig { it.copy(geminiKey = value) }
    }

    fun onUseGeminiListsChanged(value: Boolean) {
        launchUpdateConfig { it.copy(useGeminiLists = value) }
    }

    fun onUseGeminiRecipesChanged(value: Boolean) {
        launchUpdateConfig { it.copy(useGeminiRecipes = value) }
    }

    fun onUseHapticsChanged(value: Boolean) {
        launchUpdateConfig { it.copy(useHaptics = value) }
    }

    fun onRecentShoppingListsCountChanged(value: Int) {
        launchUpdateConfig {
            it.copy(
                recentShoppingListsCount = value,
                recentShoppingLists = it.recentShoppingLists.take(value).toSet()
            )
        }
    }

    fun onUseBottomNavigationChanged(value: Boolean) {
        launchUpdateConfig { it.copy(useBottomNavigation = value) }
    }

    fun onUseLiquidGlassNavigation(value: Boolean) {
        launchUpdateConfig { it.copy(useLiquidGlassNavigation = value) }
    }
}