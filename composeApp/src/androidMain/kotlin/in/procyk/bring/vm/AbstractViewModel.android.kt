package `in`.procyk.bring.vm

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry
import bring.composeapp.generated.resources.Res
import bring.composeapp.generated.resources.copied_list_id_to_clipboard

actual fun updateListLocationPresentation(listId: String?) {}

actual suspend fun onShareList(listId: String, context: AbstractViewModel.Context) {
    context.clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(null, listId)))
    context.showSnackbar(Res.string.copied_list_id_to_clipboard)
}