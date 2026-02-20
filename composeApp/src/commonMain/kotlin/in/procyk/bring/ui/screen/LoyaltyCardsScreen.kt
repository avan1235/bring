package `in`.procyk.bring.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.twotone.Label
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.NewLabel
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import bring.composeapp.generated.resources.*
import `in`.procyk.bring.ui.BringAppTheme
import `in`.procyk.bring.ui.LocalColors
import `in`.procyk.bring.ui.LocalContentColor
import `in`.procyk.bring.ui.components.*
import `in`.procyk.bring.ui.components.card.ElevatedCard
import `in`.procyk.bring.ui.components.progressindicators.CircularProgressIndicator
import `in`.procyk.bring.ui.components.textfield.OutlinedTextField
import `in`.procyk.bring.ui.screen.InputDialogAction.AddFromFile
import `in`.procyk.bring.ui.screen.InputDialogAction.ImportById
import `in`.procyk.bring.vm.LoyaltyCardsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

@Composable
internal fun LoyaltyCardsScreen(
    padding: PaddingValues,
    vm: LoyaltyCardsViewModel
) = AppScreen("screen-loyalty-cards", padding) {
    val onSurfaceColor = LocalColors.current.onSurface
    LaunchedEffect(onSurfaceColor) { vm.updateCodeColor(onSurfaceColor) }

    var dialogAction by remember { mutableStateOf<InputDialogAction?>(null) }
    val cards by vm.cards.collectAsState()
    val selectedCard by vm.selectedCard.collectAsState()
    val isLoadingCards by vm.isLoadingCards.collectAsState()
    val listState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(listState) { from, to ->
        vm.onUpdatedItemOrder(from.key as Uuid, to.key as Uuid, cards)
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        state = listState,
    ) {
        if (isLoadingCards) item(key = "loyalty-cards-loading-indicator") {
            Row(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .animateItem(),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
        }
        items(cards, key = { it.data.id }) { card ->
            ReorderableItemRow(
                state = reorderableLazyListState,
                key = card.data.id,
                modifier = Modifier
                    .fillParentMaxWidth()
                    .animateItem()
                    .testTag("loyalty-card")
            ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    onClick = { vm.selectCard(card) }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val selectedColor by vm.cardColor(card).collectAsState()
                        val previousColor = remember { mutableStateOf<Color?>(null) }
                        IconButton(
                            variant = IconButtonVariant.Ghost,
                            onClick = { previousColor.value = selectedColor },
                        ) {
                            Icon(
                                imageVector = if (selectedColor != Color.Unspecified) Icons.AutoMirrored.TwoTone.Label else Icons.AutoMirrored.Outlined.Label,
                                tint = if (selectedColor != Color.Unspecified) selectedColor else LocalContentColor.current
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = card.data.label,
                            maxLines = 1,
                            style = BringAppTheme.typography.h3,
                            modifier = Modifier.weight(1f, fill = true)
                        )
                        IconButton(
                            variant = IconButtonVariant.Ghost,
                            onClick = { vm.shareCard(card) },
                        ) {
                            Icon(Icons.Outlined.Share)
                        }
                        SelectColorDialog(
                            selectedColor = selectedColor,
                            previousColor = previousColor,
                            onColorSaved = {
                                vm.onCardColorUpdated(card.data.id, it)
                            },
                            onColorReset = {
                                vm.onCardColorUpdated(card.data.id, null)
                            }
                        )
                    }
                }
            }
        }
        item(key = "loyalty-cards-actions") {
            Row(
                modifier = Modifier.fillParentMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(
                    content = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        ) {
                            Icon(Icons.Outlined.Share)
                        }
                    },
                    variant = IconButtonVariant.PrimaryOutlined,
                    onClick = vm::shareAllCards,
                )
                IconButton(
                    content = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        ) {
                            Icon(Icons.Outlined.QrCodeScanner)
                            Text(stringResource(Res.string.scan))
                        }
                    },
                    variant = IconButtonVariant.Primary,
                    onClick = { dialogAction = AddFromFile },
                    modifier = Modifier.testTag("button-scan-loyalty-card"),
                )
                IconButton(
                    content = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        ) {
                            Icon(Icons.Outlined.EditNote)
                        }
                    },
                    variant = IconButtonVariant.PrimaryOutlined,
                    onClick = { dialogAction = ImportById },
                )
            }

        }
    }
    dialogAction?.let { currentAction ->
        AlertDialogComponent(
            onDismissRequest = { dialogAction = null; vm.resetUserInput() },
            confirmButton = {
                Button(
                    variant = ButtonVariant.PrimaryOutlined,
                    text = when (currentAction) {
                        AddFromFile -> stringResource(Res.string.select_file)
                        ImportById -> stringResource(Res.string.import_action)
                    },
                    onClick = when (currentAction) {
                        ImportById -> fun() = vm.addLoyaltyCardByCardId { dialogAction = null }
                        AddFromFile -> fun() = vm.addLoyaltyCardFromFile { dialogAction = null }
                    })
            },
            dismissButton = {
                Button(
                    variant = ButtonVariant.Ghost,
                    text = stringResource(Res.string.cancel),
                    onClick = { dialogAction = null; vm.resetUserInput() }
                )
            },
            icon = when (currentAction) {
                AddFromFile -> {
                    { Icon(Icons.Outlined.QrCodeScanner) }
                }

                ImportById -> {
                    { Icon(Icons.Outlined.EditNote) }
                }
            },
            title = {
                Text(
                    text = when (currentAction) {
                        AddFromFile -> stringResource(Res.string.add_loyalty_card)
                        ImportById -> stringResource(Res.string.import_loyalty_card)
                    }
                )
            },
            text = {
                val userInput by vm.userInput.collectAsState()
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
                )
            },
        )
    }
    selectedCard?.let {
        var showRemoveNotification by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        AlertDialogComponent(
            onDismissRequest = { -> vm.unselectCard() },
            confirmButton = {
                Button(
                    variant = ButtonVariant.PrimaryOutlined,
                    text = stringResource(Res.string.close),
                    onClick = { vm.unselectCard() })
            },
            dismissButton = {
                Button(
                    variant = ButtonVariant.Ghost,
                    text = stringResource(Res.string.remove),
                    onClick = {
                        scope.launch {
                            showRemoveNotification = true
                            try {
                                delay(1.seconds)
                            } finally {
                                showRemoveNotification = false
                            }
                        }
                    },
                    onLongClick = { vm.removeCard(it) }
                )
            },
            title = { Text(text = it.data.label) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var visibleDetails by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            bitmap = it.code.decodeToImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.clickable(
                                interactionSource = null,
                                indication = null,
                            ) { visibleDetails = !visibleDetails }
                                .requiredWidth(256.dp)
                        )
                    }
                    AnimatedVisibility(showRemoveNotification) {
                        Text(
                            text = stringResource(Res.string.long_click_to_remove),
                            style = BringAppTheme.typography.body1,
                            color = BringAppTheme.colors.textSecondary,
                        )
                    }
                    AnimatedVisibility(visibleDetails) {
                        Text(
                            text = it.data.code.rawText,
                            style = BringAppTheme.typography.body1,
                        )
                    }
                }
            },
        )
    }
}

private enum class InputDialogAction {
    AddFromFile, ImportById,
}