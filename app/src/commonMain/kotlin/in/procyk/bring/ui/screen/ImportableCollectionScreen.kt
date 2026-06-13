package `in`.procyk.bring.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.NewLabel
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import bring.app.generated.resources.*
import `in`.procyk.bring.Identifiable
import `in`.procyk.bring.Orderable
import `in`.procyk.bring.ui.components.*
import `in`.procyk.bring.ui.components.card.ElevatedCard
import `in`.procyk.bring.ui.components.liquid.LiquidBottomTabsSpacer
import `in`.procyk.bring.ui.components.progressindicators.CircularProgressIndicator
import `in`.procyk.bring.ui.components.textfield.OutlinedTextField
import `in`.procyk.bring.vm.ImportableCollectionViewModel
import `in`.procyk.bring.vm.ImportableCollectionViewModel.InputDialogAction.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.uuid.Uuid

@Composable
internal fun <V, I> ImportableCollectionScreen(
    testTag: String,
    padding: PaddingValues,
    vm: V,
    addDialogTitle: StringResource,
    importDialogTitle: StringResource,
    scanButtonTestTag: String? = null,
    overlay: @Composable () -> Unit = {},
    itemContent: @Composable (item: I) -> Unit,
) where V : ImportableCollectionViewModel<*, *, I, *>,
        I : Orderable,
        I : Identifiable = AppScreen(testTag, padding) {
    val dialogAction by vm.dialogAction.collectAsState()
    val items by vm.items.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val enableEditMode by vm.enableEditMode.collectAsState()
    val listState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(listState) { from, to ->
        vm.onUpdatedItemOrder(from.key as Uuid, to.key as Uuid, items)
    }

    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val isTransitionFinished = lifecycleState == Lifecycle.State.RESUMED
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        state = listState,
    ) {
        if (isLoading || !isTransitionFinished) item(key = "$testTag-loading-indicator") {
            Row(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .fillParentMaxWidth()
                    .animateItem(),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
        }
        if (isTransitionFinished) items(items, key = { it.id }) { item ->
            ReorderableItemRow(
                state = reorderableLazyListState,
                key = item.id,
                enabled = enableEditMode,
                modifier = Modifier
                    .padding(start = if (enableEditMode) 4.dp else 16.dp)
                    .fillParentMaxWidth()
                    .animateItem(),
            ) {
                if (enableEditMode) Spacer(Modifier.width(4.dp))
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    itemContent(item)
                }
            }
        }
        if (enableEditMode) item(key = "$testTag-actions") {
            Row(
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        top = if (isLoading || items.isNotEmpty()) 8.dp else 0.dp,
                    )
                    .fillParentMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                ActionButton(
                    icon = Icons.Outlined.IosShare,
                    variant = IconButtonVariant.PrimaryGhost,
                    onClick = vm::shareAll,
                )
                ActionButton(
                    icon = Icons.Outlined.QrCodeScanner,
                    text = stringResource(Res.string.scan),
                    variant = IconButtonVariant.Primary,
                    onClick = vm::openAddFromFileDialog,
                    testTag = scanButtonTestTag,
                )
                ActionButton(
                    icon = Icons.Outlined.EditNote,
                    variant = IconButtonVariant.PrimaryGhost,
                    onClick = vm::openImportByIdDialog,
                )
            }
        }
        item(key = "$testTag-liquid-spacer") {
            LiquidBottomTabsSpacer(vm)
        }
    }
    dialogAction?.let { currentAction ->
        AlertDialogComponent(
            onDismissRequest = { vm.run { closeDialogAction(); resetUserInput() } },
            confirmButton = {
                if (currentAction is Loading) return@AlertDialogComponent
                Button(
                    variant = ButtonVariant.PrimaryOutlined,
                    text = when (currentAction) {
                        AddFromFile -> stringResource(Res.string.select_file)
                        ImportById -> stringResource(Res.string.import_action)
                    },
                    onClick = when (currentAction) {
                        ImportById -> vm::importByIds
                        AddFromFile -> vm::addFromFile
                    },
                )
            },
            dismissButton = composeIfNotNull(currentAction.takeIf { it !is Loading }) {
                Button(
                    variant = ButtonVariant.Ghost,
                    text = stringResource(Res.string.cancel),
                    onClick = { vm.run { closeDialogAction(); resetUserInput() } },
                )
            },
            icon = when (currentAction) {
                is AddFromFile -> {
                    { Icon(Icons.Outlined.QrCodeScanner) }
                }

                is ImportById -> {
                    { Icon(Icons.Outlined.EditNote) }
                }

                is Loading -> null
            },
            title = {
                if (currentAction is Loading) return@AlertDialogComponent
                Text(
                    text = when (currentAction) {
                        AddFromFile -> stringResource(addDialogTitle)
                        ImportById -> stringResource(importDialogTitle)
                    },
                )
            },
            text = {
                if (currentAction is Loading) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                        Text(currentAction.description)
                    }
                } else {
                    val userInput by vm.userInput.collectAsState()
                    val isErrorUserInput by vm.isErrorUserInput.collectAsState()
                    OutlinedTextField(
                        value = userInput,
                        onValueChange = vm::onUserInputChange,
                        leadingIcon = {
                            Icon(
                                imageVector = when (currentAction) {
                                    AddFromFile -> Icons.Outlined.NewLabel
                                    ImportById -> Icons.Outlined.EditNote
                                },
                                modifier = Modifier.padding(start = 16.dp),
                            )
                        },
                        singleLine = true,
                        isError = isErrorUserInput,
                    )
                }
            },
        )
    }
    overlay()
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    variant: IconButtonVariant,
    onClick: () -> Unit,
    text: String? = null,
    testTag: String? = null,
) {
    IconButton(
        content = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp),
            ) {
                Icon(icon)
                if (text != null) Text(text)
            }
        },
        variant = variant,
        onClick = onClick,
        modifier = if (testTag != null) Modifier.testTag(testTag) else Modifier,
    )
}

private inline fun <T : Any> composeIfNotNull(
    value: T?,
    crossinline f: @Composable () -> Unit,
): @Composable () -> Unit =
    if (value == null) {
        {}
    } else {
        { f() }
    }
