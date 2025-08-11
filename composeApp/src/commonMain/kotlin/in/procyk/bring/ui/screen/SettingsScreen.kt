package `in`.procyk.bring.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import bring.composeapp.generated.resources.*
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import `in`.procyk.bring.LocalBringStore
import `in`.procyk.bring.ui.BringAppTheme
import `in`.procyk.bring.ui.Theme
import `in`.procyk.bring.ui.components.*
import `in`.procyk.bring.ui.components.card.OutlinedCard
import `in`.procyk.bring.ui.components.textfield.OutlinedTextField
import `in`.procyk.bring.ui.icons.BringIcons
import `in`.procyk.bring.ui.icons.GitHub
import `in`.procyk.bring.ui.icons.Html
import `in`.procyk.bring.ui.icons.LinkedIn
import `in`.procyk.bring.vm.SettingsViewModel
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingsScreen(
    padding: PaddingValues,
    vm: SettingsViewModel,
) = AppScreen("screen-settings", padding) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        SettingsCategoryName(Res.string.shopping_list)
        SettingSwitchRow(Res.string.enable_edit_mode, vm.enableEditMode, vm::onEditModeChanged)
        SettingSwitchRow(Res.string.show_unchecked_first, vm.showUncheckedFirst, vm::onShowUncheckedFirstChanged)
        SettingSwitchRow(Res.string.show_favorite_elements, vm.showFavoriteElements, vm::onShowFavoriteElementsChanged)
        SettingSwitchRow(Res.string.show_suggestions, vm.showSuggestions, vm::onShowSuggestionsChanged)
        SettingSwitchRow(Res.string.use_gemini, vm.useGemini, vm::onUseGeminiChanged)
        AnimatedVisibility(vm.useGemini.value) {
            SettingStringRow(
                Res.string.gemini_key,
                vm.geminiKey,
                vm::onGeminiKeyChanged,
                reference = "https://aistudio.google.com/app/apikey"
            )
        }
        SettingsCategoryName(Res.string.theme)
        SettingSelectionRow(Res.string.dark_mode, vm.theme, vm::onThemeChanged, Theme.entries, optionLabel = {
            when (it) {
                Theme.System -> Res.string.system_theme
                Theme.Light -> Res.string.light_theme
                Theme.Dark -> Res.string.dark_theme
            }
        })
        SettingColorPickerRow(Res.string.theme_color, vm.themeColor, vm::onThemeColorChanged)
        SettingsCategoryName(Res.string.miscellaneous)
        SettingSwitchRow(Res.string.use_haptics, vm.useHaptics, vm::onUseHapticsChanged)
        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(16.dp))
        BottomBanner(
            title = "Find Me On",
            BottomBannerItem("https://github.com/avan1235/", BringIcons.GitHub),
            BottomBannerItem("https://www.linkedin.com/in/maciej-procyk/", BringIcons.LinkedIn),
            BottomBannerItem("https://procyk.in", BringIcons.Html),
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private inline fun <T : Any> SettingSelectionRow(
    label: StringResource,
    selected: StateFlow<T>,
    crossinline onSelectedChange: (T) -> Unit,
    entries: List<T>,
    crossinline optionLabel: (T) -> StringResource,
) {
    OutlinedCard(
        modifier = Modifier.padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Text(
                text = stringResource(label), style = BringAppTheme.typography.h4
            )
        }

        HorizontalDivider()

        val selectedOption by selected.collectAsState()
        Column(
            modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()
        ) {
            entries.forEach { option ->
                Row(
                    modifier = Modifier.padding(start = 8.dp), verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedOption == option,
                        onClick = LocalBringStore.current.onClickWithHaptics(onClick = {
                            onSelectedChange(option)
                        }),
                        content = {
                            Text(
                                text = stringResource(optionLabel(option)),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        })
                }
            }
        }
    }
}

@Composable
private fun SettingsCategoryName(
    label: StringResource,
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(label),
            style = BringAppTheme.typography.h4,
        )
        HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.2f))
    }
}

@Composable
private inline fun SettingSwitchRow(
    label: StringResource,
    isChecked: StateFlow<Boolean>,
    crossinline onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val onClick = LocalBringStore.current.onToggleWithHaptics(onCheckedChange)
        val isChecked by isChecked.collectAsState()
        Text(
            text = stringResource(label),
            modifier = Modifier.clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onClick(!isChecked) },
            ).weight(1f),
        )
        Switch(
            checked = isChecked,
            onCheckedChange = {
                onCheckedChange(it)
            })
    }
}

@Composable
private inline fun SettingStringRow(
    label: StringResource,
    value: StateFlow<String>,
    crossinline onValueChange: (String) -> Unit,
    reference: String? = null,
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val value by value.collectAsState()
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it) },
            singleLine = true,
            placeholder = { Text(stringResource(label)) },
            modifier = Modifier.weight(1f),
        )
        reference?.let {
            val uriHandler = LocalUriHandler.current
            IconButton(
                onClick = { uriHandler.openUri(reference) },
                variant = IconButtonVariant.PrimaryGhost,
            ) {
                Icon(Icons.AutoMirrored.Outlined.OpenInNew)
            }
        }
    }
}

@Composable
private inline fun SettingColorPickerRow(
    label: StringResource,
    selectedColor: StateFlow<Color>,
    crossinline onColorChanged: (Color) -> Unit,
) {
    val selectedColor by selectedColor.collectAsState()
    var previousColor by remember { mutableStateOf<Color?>(null) }

    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        Text(
            text = stringResource(label),
            modifier = Modifier.clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { previousColor = selectedColor }
            )
                .weight(1f),
        )
        IconButton(
            variant = IconButtonVariant.PrimaryGhost,
            onClick = { previousColor = selectedColor },
            interactionSource = interactionSource,
        ) {
            Icon(Icons.Filled.Palette)
        }
    }

    AnimatedVisibility(previousColor != null) {
        val controller = rememberColorPickerController()
        AlertDialog(
            onDismissRequest = {
                previousColor?.let(onColorChanged)
                previousColor = null
            },
            onConfirmClick = {
                previousColor = null
            },
            title = stringResource(Res.string.select_color),
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    HsvColorPicker(
                        modifier = Modifier.fillMaxWidth().height(240.dp).padding(10.dp),
                        controller = controller,
                        initialColor = selectedColor,
                        onColorChanged = { onColorChanged(it.color) }
                    )
                }
            },
            confirmButtonText = stringResource(Res.string.save),
            dismissButtonText = stringResource(Res.string.cancel),
        )
    }
}
