package `in`.procyk.bring.vm

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import `in`.procyk.bring.ui.Theme
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

internal class SettingsViewModel(context: Context) : AbstractViewModel(context) {

    val enableEditMode: StateFlow<Boolean> =
        storeFlow.map { it.enableEditMode }.state(storeFlow.value.enableEditMode)

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

    val useGemini: StateFlow<Boolean> =
        storeFlow.map { it.useGemini }.state(storeFlow.value.useGemini)

    val useHaptics: StateFlow<Boolean> =
        storeFlow.map { it.useHaptics }.state(storeFlow.value.useHaptics)

    val useBottomNavigation: StateFlow<Boolean> =
        context.useBottomNavigation

    fun onEditModeChanged(value: Boolean) {
        updateConfig { it.copy(enableEditMode = value) }
    }

    fun onShowUncheckedFirstChanged(value: Boolean) {
        updateConfig { it.copy(showUncheckedFirst = value) }
    }

    fun onShowSuggestionsChanged(value: Boolean) {
        updateConfig { it.copy(showSuggestions = value) }
    }

    fun onShowFavoriteElementsChanged(value: Boolean) {
        updateConfig { it.copy(showFavoriteElements = value) }
    }

    fun onThemeChanged(value: Theme) {
        updateConfig { it.copy(darkMode = value) }
    }

    fun onThemeColorChanged(value: Color) {
        updateConfig { it.copy(themeColor = value.toArgb()) }
    }

    fun onGeminiKeyChanged(value: String) {
        updateConfig { it.copy(geminiKey = value) }
    }

    fun onUseGeminiChanged(value: Boolean) {
        updateConfig { it.copy(useGemini = value) }
    }

    fun onUseHapticsChanged(value: Boolean) {
        updateConfig { it.copy(useHaptics = value) }
    }

    fun onUseBottomNavigationChanged(value: Boolean) {
        updateConfig { it.copy(useBottomNavigation = value) }
    }
}