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
}