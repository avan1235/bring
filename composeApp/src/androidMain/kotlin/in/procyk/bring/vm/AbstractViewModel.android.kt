package `in`.procyk.bring.vm

import android.content.ClipData.newPlainText
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.createChooser
import androidx.compose.ui.platform.ClipEntry
import bring.composeapp.generated.resources.Res
import bring.composeapp.generated.resources.copied_list_id_to_clipboard
import `in`.procyk.bring.vm.AbstractViewModel.Context

actual fun updateListLocationPresentation(listId: String?) {}

internal actual suspend fun onShareList(listId: String, context: Context) {
    val shareIntent = Intent(ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, listId)
    }
    val intent = createChooser(shareIntent, null).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.platformContext.context.startActivity(intent)

    context.clipboard.setClipEntry(ClipEntry(newPlainText(null, listId)))
    context.showSnackbar(Res.string.copied_list_id_to_clipboard)
}