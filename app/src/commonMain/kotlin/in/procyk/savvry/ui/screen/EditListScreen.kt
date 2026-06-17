@file:OptIn(ExperimentalFoundationApi::class)

package `in`.procyk.savvry.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.twotone.Favorite
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import `in`.procyk.savvry.ShoppingListItemData.CheckedStatusData.Checked
import `in`.procyk.savvry.ShoppingListItemData.CheckedStatusData.Unchecked
import `in`.procyk.savvry.ui.components.*
import `in`.procyk.savvry.ui.components.liquid.LiquidBottomTabsSpacer
import `in`.procyk.savvry.ui.components.progressindicators.LinearProgressIndicator
import `in`.procyk.savvry.vm.EditListScreenViewModel
import kotlinx.coroutines.launch
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.uuid.Uuid

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterialApi::class)
@Composable
internal fun EditListScreen(
    padding: PaddingValues,
    vm: EditListScreenViewModel,
) = AppScreen("screen-edit-list", padding) {
    val listState = rememberLazyListState()
    val items by vm.items.collectAsState()
    val reorderableLazyListState = rememberReorderableLazyListState(listState) { from, to ->
        vm.onUpdatedItemOrder(from.key as Uuid, to.key as Uuid, items)
    }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(listState.firstVisibleItemIndex) {
        vm.onShowFab(show = listState.firstVisibleItemIndex > 0)
    }
    val isRefreshing by vm.isRefreshing.collectAsState()
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = vm::onRefresh,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = listState,
        ) {
            item(key = "input") {
                val enableEditMode by vm.enableEditMode.collectAsState()
                AnimatedVisibility(visible = enableEditMode) {
                    BoxWithConstraints {
                        val individualButtons = maxWidth > 480.dp
                        Row(
                            modifier = Modifier.padding(4.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val newItemName by vm.newItemName.collectAsState()
                            val newItemLoading by vm.newItemLoading.collectAsState()
                            AddItemTextField(
                                value = newItemName,
                                onValueChange = vm::onNewItemNameChange,
                                loading = newItemLoading,
                                onAdd = {
                                    vm.onCreateNewItem()
                                    focusRequester.requestFocus()
                                },
                                onDone = { vm.onCreateNewItem() },
                                textFieldModifier = Modifier
                                    .focusRequester(focusRequester)
                                    .testTag("text-field-add-list-item"),
                                buttonEnabled = !newItemLoading,
                                buttonModifier = Modifier
                                    .testTag("button-add-list-item"),
                            )
                            val useGeminiSettings by vm.useGeminiSettings.collectAsState()
                            if (useGeminiSettings) {
                                IconButton(
                                    onClick = { vm.onCreateNewItem(useGemini = true) },
                                    icon = Icons.Outlined.SmartToy,
                                    variant = IconButtonVariant.PrimaryOutlined,
                                    modifier = Modifier.testTag("button-generate-with-gemini"),
                                )
                            }
                            when {
                                individualButtons -> Row {
                                    ControlButtons(vm)
                                }

                                else -> {
                                    var isExpanded by remember { mutableStateOf(false) }
                                    Box {
                                        IconButton(
                                            onClick = { isExpanded = !isExpanded },
                                            modifier = Modifier.testTag("button-expand-options"),
                                            icon = Icons.Default.MoreVert,
                                        )
                                        DropdownMenu(
                                            modifier = Modifier.padding(horizontal = 8.dp),
                                            expanded = isExpanded,
                                            shape = RoundedCornerShape(16.dp),
                                            onDismissRequest = { isExpanded = false },
                                        ) {
                                            ControlButtons(vm)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item(key = "favorite_elements_and_suggestions") {
                val enableEditMode by vm.enableEditMode.collectAsState()
                val showSuggestions by vm.showSuggestions.collectAsState()
                val showFavoriteElements by vm.showFavoriteElements.collectAsState()
                AnimatedVisibility(visible = enableEditMode && (showSuggestions || showFavoriteElements)) {
                    val suggestedItems by vm.suggestedItems.collectAsState()
                    FlowRowItems(
                        items = suggestedItems,
                        modifier = Modifier.padding(horizontal = 4.dp),
                        name = { it.name },
                        id = { it.id },
                        visible = { !it.used },
                    ) { vm.onCreateNewItemFromSuggestion(it.name) }
                }
            }
            items(items, key = { it.id }) { item ->
                ReorderableItemRow(
                    modifier = Modifier.testTag("list-item"),
                    state = reorderableLazyListState,
                    key = item.id,
                ) {
                    val enableEditMode by vm.enableEditMode.collectAsState()
                    Row(
                        modifier = Modifier.weight(1f).animateItem(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val interactionSource = remember { MutableInteractionSource() }
                        Checkbox(
                            checked = item.status.isChecked,
                            onCheckedChange = { vm.onChecked(item.id, it) },
                            interactionSource = interactionSource,
                            modifier = Modifier.testTag("checkbox-list-item"),
                        )
                        Box(
                            modifier = Modifier.clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = { vm.onChecked(item.id, !item.status.isChecked) },
                            ).weight(1f),
                        ) {
                            AnimatedStrikethroughText(
                                text = item.name + if (item.count > 1) " (${item.count})" else "",
                                isChecked = when (item.status) {
                                    is Checked -> true
                                    is Unchecked -> false
                                },
                            )
                        }
                        AnimatedVisibilityGhostButton(
                            visible = enableEditMode && item.count > 1,
                            icon = Icons.Outlined.ExposureNeg1,
                            testTag = "button-decrease-item-count",
                            onClick = { vm.onDecreaseItemCount(item.id) },
                        )
                        AnimatedVisibilityGhostButton(
                            visible = enableEditMode,
                            onClick = { vm.onIncreaseItemCount(item.id) },
                            testTag = "button-increase-item-count",
                            icon = Icons.Outlined.ExposurePlus1,
                        )
                        AnimatedVisibilityGhostButton(
                            visible = enableEditMode,
                            icon = Icons.Outlined.Close,
                            testTag = "button-remove-item",
                            onClick = { vm.onRemoved(item.id) },
                        )
                    }
                }
            }
            item(key = "edit_list_liquid_spacer") {
                LiquidBottomTabsSpacer(vm)
            }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val progress by vm.itemsProgress.collectAsState()
        LinearProgressIndicator(
            progress = progress, modifier = Modifier.fillMaxWidth(),
        )
    }
    val density = LocalDensity.current
    Column(
        modifier = Modifier.padding(FabPadding).fillMaxSize(),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom,
    ) {
        val scope = rememberCoroutineScope()
        val showFab by vm.showFab.collectAsState()
        val extra = remember(density) { with(density) { FabPadding.roundToPx() } }
        AnimatedVisibility(
            visible = showFab,
            enter = slideInHorizontally(initialOffsetX = { it + extra }),
            exit = slideOutHorizontally(targetOffsetX = { it + extra }),
        ) {
            IconButton(
                variant = IconButtonVariant.PrimaryElevated,
                onClick = {
                    scope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
            ) {
                Icon(Icons.Default.KeyboardArrowUp)
            }
        }
        LiquidBottomTabsSpacer(vm)
    }
}

private val FabPadding = 20.dp

@Composable
private fun ControlButtons(
    vm: EditListScreenViewModel,
) {
    val isFavorite by vm.isFavorite.collectAsState()
    IconButton(
        onClick = vm::onToggleListFavorite,
        icon = if (isFavorite) Icons.Filled.Favorite else Icons.TwoTone.Favorite,
        modifier = Modifier.testTag("button-toggle-favorite"),
    )
    IconButton(
        onClick = vm::onShareList,
        icon = Icons.Outlined.IosShare,
        modifier = Modifier.testTag("button-share-list"),
    )
    IconButton(
        onClick = vm::onExtractUncheckedList,
        icon = Icons.Outlined.Unarchive,
        modifier = Modifier.testTag("button-extract-list"),
    )
}

@Composable
private fun IconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    variant: IconButtonVariant = IconButtonVariant.PrimaryGhost,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        variant = variant,
        modifier = modifier,
    ) {
        Icon(icon)
    }
}