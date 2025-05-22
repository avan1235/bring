package `in`.procyk.bring.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import bring.composeapp.generated.resources.*
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import `in`.procyk.bring.ui.BringAppTheme
import `in`.procyk.bring.ui.Theme
import `in`.procyk.bring.ui.components.*
import `in`.procyk.bring.ui.components.card.OutlinedCard
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
    vm: SettingsViewModel,
) = AppScreen {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        SettingSwitchRow(Res.string.enable_edit_mode, vm.enableEditMode, vm::onEditModeChanged)
        SettingSwitchRow(Res.string.show_unchecked_first, vm.showUncheckedFirst, vm::onShowUncheckedFirstChanged)
        SettingSwitchRow(Res.string.show_favorite_elements, vm.showFavoriteElements, vm::onShowFavoriteElementsChanged)
        SettingSwitchRow(Res.string.show_suggestions, vm.showSuggestions, vm::onShowSuggestionsChanged)
        SettingSelectionRow(Res.string.dark_mode, vm.theme, vm::onThemeChanged, Theme.entries, optionLabel = {
            when (it) {
                Theme.System -> Res.string.system_theme
                Theme.Light -> Res.string.light_theme
                Theme.Dark -> Res.string.dark_theme
            }
        })
        SettingColorPickerRow(Res.string.theme_color, vm.themeColor, vm::onThemeColorChanged)
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
        modifier = Modifier.padding(horizontal = 16.dp),
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
                        onClick = { onSelectedChange(option) },
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
        val isChecked by isChecked.collectAsState()
        Text(
            text = stringResource(label),
            modifier = Modifier.clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onCheckedChange(!isChecked) }).weight(1f),
        )
        Switch(
            checked = isChecked, onCheckedChange = { onCheckedChange(it) })
    }
}

@Composable
private inline fun SettingColorPickerRow(
    label: StringResource,
    selectedColor: StateFlow<Color>,
    crossinline onColorChanged: (Color) -> Unit,
) {
    val selectedColor by selectedColor.collectAsState()
    var openedDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        Text(
            text = stringResource(label),
            modifier = Modifier.clickable(
                interactionSource = interactionSource, indication = null, onClick = { openedDialog = true })
                .weight(1f),
        )
        ColorBox(
            color = selectedColor, onClick = { openedDialog = true })
    }

    AnimatedVisibility(openedDialog) {
        val controller = rememberColorPickerController()
        AlertDialog(
            onDismissRequest = {
                openedDialog = false
            },
            onConfirmClick = {
                onColorChanged(controller.selectedColor.value)
                openedDialog = false
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
                    )
                    Spacer(Modifier.height(16.dp))

                    ColorBox(
                        color = controller.selectedColor.value,
                    )
                }
            },
            confirmButtonText = stringResource(Res.string.save),
            dismissButtonText = stringResource(Res.string.cancel),
        )
    }
}

@Composable
private fun ColorBox(
    color: Color,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).run {
            if (onClick != null) clickable(onClick = onClick) else this
        }.border(1.dp, BringAppTheme.colors.outline, RoundedCornerShape(8.dp))
            .background(color, RoundedCornerShape(8.dp)).size(36.dp)
    )
}
