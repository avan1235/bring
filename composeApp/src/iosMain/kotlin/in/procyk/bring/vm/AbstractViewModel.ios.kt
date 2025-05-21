package `in`.procyk.bring.vm

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import bring.composeapp.generated.resources.Res
import bring.composeapp.generated.resources.copied_list_id_to_clipboard
import `in`.procyk.bring.vm.AbstractViewModel.Context

actual fun updateListLocationPresentation(listId: String?) {}

@OptIn(ExperimentalComposeUiApi::class)
actual suspend fun onShareList(listId: String, context: Context) {
    context.clipboard.setClipEntry(ClipEntry.withPlainText(listId))
    context.showSnackbar(Res.string.copied_list_id_to_clipboard)
}