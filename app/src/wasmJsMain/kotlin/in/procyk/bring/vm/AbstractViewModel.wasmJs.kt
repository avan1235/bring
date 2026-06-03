package `in`.procyk.bring.vm

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry.Companion.withPlainText
import bring.app.generated.resources.Res
import bring.app.generated.resources.copied_card_id_to_clipboard
import bring.app.generated.resources.copied_list_url_to_clipboard
import `in`.procyk.bring.vm.AbstractViewModel.Context
import kotlinx.browser.window


@OptIn(ExperimentalComposeUiApi::class)
internal actual suspend fun onShareList(listId: String, context: Context) {
    val clipEntry = withPlainText("${window.location.origin}/#${listId}")
    context.clipboard.setClipEntry(clipEntry)
    context.showSnackbar(Res.string.copied_list_url_to_clipboard)
}

@OptIn(ExperimentalComposeUiApi::class)
internal actual suspend fun onShareLoyaltyCard(cardId: String, context: Context) {
    val clipEntry = withPlainText(cardId)
    context.clipboard.setClipEntry(clipEntry)
    context.showSnackbar(Res.string.copied_card_id_to_clipboard)
}

actual fun updateListLocationPresentation(listId: String?) {
    window.location.hash = when {
        listId != null -> "#${listId}"
        else -> ""
    }
}