package `in`.procyk.bring.vm

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import bring.composeapp.generated.resources.Res
import bring.composeapp.generated.resources.copied_list_url_to_clipboard
import `in`.procyk.bring.vm.AbstractViewModel.Context
import kotlinx.browser.window

actual fun updateListLocationPresentation(listId: String?) {
    window.location.hash = when {
        listId != null -> "#${listId}"
        else -> ""
    }
}

@OptIn(ExperimentalComposeUiApi::class)
actual suspend fun onShareList(listId: String, context: Context) {
    context.clipboard.setClipEntry(ClipEntry.withPlainText("${window.location.origin}/#${listId}"))
    context.showSnackbar(Res.string.copied_list_url_to_clipboard)
}