package `in`.procyk.bring.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.NewLabel
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.unit.dp
import bring.composeapp.generated.resources.Res
import bring.composeapp.generated.resources.add_loyalty_card
import bring.composeapp.generated.resources.cancel
import bring.composeapp.generated.resources.close
import bring.composeapp.generated.resources.import_action
import bring.composeapp.generated.resources.import_loyalty_card
import bring.composeapp.generated.resources.scan
import bring.composeapp.generated.resources.select_file
import `in`.procyk.bring.ui.BringAppTheme
import `in`.procyk.bring.ui.LocalColors
import `in`.procyk.bring.ui.components.*
import `in`.procyk.bring.ui.components.card.ElevatedCard
import `in`.procyk.bring.ui.components.progressindicators.CircularProgressIndicator
import `in`.procyk.bring.ui.components.textfield.OutlinedTextField
import `in`.procyk.bring.ui.screen.InputDialogAction.AddFromFile
import `in`.procyk.bring.ui.screen.InputDialogAction.ImportById
import `in`.procyk.bring.vm.LoyaltyCardsViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LoyaltyCardsScreen(
    padding: PaddingValues,
    vm: LoyaltyCardsViewModel
) = AppScreen("screen-loyalty-cards", padding) {
    val onSurfaceColor = LocalColors.current.onSurface
    LaunchedEffect(onSurfaceColor) { vm.updateCodeColor(onSurfaceColor) }

    var userInputDialogAction by remember { mutableStateOf<InputDialogAction?>(null) }
    val cards by vm.cards.collectAsState()
    val selectedCard by vm.selectedCard.collectAsState()
    val isLoadingCards by vm.isLoadingCards.collectAsState()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
        items(cards, key = { it.data.id }) {
            ElevatedCard(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .animateItem(),
                shape = RoundedCornerShape(8.dp),
                onClick = { vm.selectCard(it) }
            ) {
                Text(
                    text = it.data.label,
                    maxLines = 1,
                    style = BringAppTheme.typography.h3,
                    modifier = Modifier.padding(16.dp),
                )
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
                    onClick = { userInputDialogAction = AddFromFile },
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
                    onClick = { userInputDialogAction = ImportById },
                )
            }

        }
    }
    userInputDialogAction?.let { action ->
        AlertDialog(
            onDismissRequest = { userInputDialogAction = null; vm.resetUserInput() },
            onConfirmClick = when (action) {
                ImportById -> vm::addLoyaltyCardByCardId
                AddFromFile -> vm::addLoyaltyCardFromFile
            },
            title = when (action) {
                AddFromFile -> stringResource(Res.string.add_loyalty_card)
                ImportById -> stringResource(Res.string.import_loyalty_card)
            },
            text = {
                val userInput by vm.userInput.collectAsState()
                OutlinedTextField(
                    value = userInput,
                    onValueChange = vm::onUserInputChange,
                    leadingIcon = {
                        Icon(
                            imageVector = when (action) {
                                AddFromFile -> Icons.Outlined.NewLabel
                                ImportById -> Icons.Outlined.EditNote
                            },
                            modifier = Modifier.padding(start = 16.dp),
                        )
                    },
                    singleLine = true
                )
            },
            confirmButtonText = when (action) {
                AddFromFile -> stringResource(Res.string.select_file)
                ImportById -> stringResource(Res.string.import_action)
            },
            dismissButtonText = stringResource(Res.string.cancel),
        )
    }
    selectedCard?.let {
        AlertDialog(
            onDismissRequest = vm::unselectCard,
            onConfirmClick = vm::unselectCard,
            title = it.data.label,
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
                    AnimatedVisibility(visibleDetails) {
                        Text(it.data.code.rawText, style = BringAppTheme.typography.body1)
                    }
                }
            },
            confirmButtonText = stringResource(Res.string.close),
            dismissButtonText = null,
        )
    }
}

private enum class InputDialogAction {
    AddFromFile, ImportById,
}