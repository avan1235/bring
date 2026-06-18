package `in`.procyk.savvry.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.twotone.Label
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import savvry.app.generated.resources.*
import `in`.procyk.savvry.ui.SavvryAppTheme
import `in`.procyk.savvry.ui.LocalColors
import `in`.procyk.savvry.ui.LocalContentColor
import `in`.procyk.savvry.ui.components.*
import `in`.procyk.savvry.vm.LoyaltyCardsViewModel
import `in`.procyk.savvry.vm.LoyaltyCardsViewModel.Card
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun LoyaltyCardsScreen(
    padding: PaddingValues,
    vm: LoyaltyCardsViewModel,
) {
    ImportableCollectionScreen(
        testTag = "screen-loyalty-cards",
        padding = padding,
        vm = vm,
        addDialogTitle = Res.string.add_loyalty_card,
        importDialogTitle = Res.string.import_loyalty_card,
        scanButtonTestTag = "button-scan-loyalty-card",
        itemContent = { card -> LoyaltyCardRow(card, vm) },
        overlay = { SelectedCardDialog(vm) },
    )
}

@Composable
private fun LoyaltyCardRow(
    card: Card,
    vm: LoyaltyCardsViewModel,
) {
    val enableEditMode by vm.enableEditMode.collectAsState()
    val selectedColor by vm.cardColor(card).collectAsState()
    val showCardsLabels by vm.showLabels.collectAsState()
    val previousColor = remember { mutableStateOf<Color?>(null) }
    Row(
        modifier = Modifier
            .clickable { vm.selectCard(card) }
            .padding(8.dp)
            .fillMaxWidth()
            .testTag("loyalty-card"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            !showCardsLabels -> {}
            enableEditMode -> IconButton(
                variant = IconButtonVariant.Ghost,
                onClick = { previousColor.value = selectedColor },
            ) {
                Icon(
                    imageVector = if (selectedColor != Color.Unspecified) Icons.AutoMirrored.TwoTone.Label else Icons.AutoMirrored.Outlined.Label,
                    tint = if (selectedColor != Color.Unspecified) selectedColor else LocalContentColor.current,
                )
            }

            else -> Icon(
                imageVector = if (selectedColor != Color.Unspecified) Icons.AutoMirrored.TwoTone.Label else Icons.AutoMirrored.Outlined.Label,
                tint = if (selectedColor != Color.Unspecified) selectedColor else LocalContentColor.current,
                modifier = Modifier.padding(8.dp),
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = card.data.label,
            maxLines = 1,
            style = SavvryAppTheme.typography.h3,
            modifier = Modifier.weight(1f, fill = true),
        )
        SelectColorDialog(
            selectedColor = selectedColor,
            previousColor = previousColor,
            onColorSaved = { vm.onCardColorUpdated(card.data.id, it) },
            onColorReset = { vm.onCardColorUpdated(card.data.id, null) },
        )
    }
}

@Composable
private fun SelectedCardDialog(vm: LoyaltyCardsViewModel) {
    val selectedCard by vm.selectedCard.collectAsState()
    selectedCard?.let { selectedCard ->
        var showRemoveNotification by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        AlertDialogComponent(
            enableDragGesture = true,
            onDismissRequest = { vm.unselectCard() },
            confirmButton = {
                Button(
                    variant = ButtonVariant.PrimaryOutlined,
                    text = stringResource(Res.string.close),
                    onClick = { vm.unselectCard() },
                )
            },
            dismissButton = {
                IconButton(
                    variant = IconButtonVariant.Ghost,
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
                    onLongClick = { vm.removeCard(selectedCard) },
                ) {
                    Icon(Icons.Outlined.DeleteOutline)
                }
                IconButton(
                    variant = IconButtonVariant.Ghost,
                    onClick = { vm.shareCard(selectedCard) },
                ) {
                    Icon(Icons.Outlined.IosShare)
                }
            },
            title = { Text(text = selectedCard.data.label) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    var visibleDetails by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CodeGenerator(
                            code = selectedCard.data.code,
                            color = LocalColors.current.onSurface,
                            width = 256.dp,
                            modifier = Modifier.clickable(
                                interactionSource = null,
                                indication = null,
                            ) { visibleDetails = !visibleDetails },
                        )
                    }
                    AnimatedVisibility(visibleDetails) {
                        Text(
                            text = selectedCard.data.code.text,
                            style = SavvryAppTheme.typography.body1,
                        )
                    }
                    AnimatedVisibility(showRemoveNotification) {
                        Text(
                            text = stringResource(Res.string.long_click_to_remove),
                            style = SavvryAppTheme.typography.body1,
                            color = SavvryAppTheme.colors.textSecondary,
                        )
                    }
                }
            },
        )
    }
}
