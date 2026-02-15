package `in`.procyk.bring.vm

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import bring.composeapp.generated.resources.Res
import bring.composeapp.generated.resources.copied_card_id_to_clipboard
import bring.composeapp.generated.resources.copied_list_id_to_clipboard
import `in`.procyk.bring.vm.AbstractViewModel.Context
import org.jetbrains.compose.resources.StringResource
import java.awt.datatransfer.StringSelection

actual fun updateListLocationPresentation(listId: String?) {}

internal actual suspend fun onShareList(listId: String, context: Context) =
    onShare(listId, context, Res.string.copied_list_id_to_clipboard)

internal actual suspend fun onShareLoyaltyCard(cardId: String, context: Context) =
    onShare(cardId, context, Res.string.copied_card_id_to_clipboard)

@OptIn(ExperimentalComposeUiApi::class)
private suspend fun onShare(value: String, context: Context, message: StringResource) {
    context.clipboard.setClipEntry(ClipEntry(StringSelection(value)))
    context.showSnackbar(message)
}