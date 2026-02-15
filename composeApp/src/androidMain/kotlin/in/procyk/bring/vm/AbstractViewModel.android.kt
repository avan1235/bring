package `in`.procyk.bring.vm

import android.content.ClipData.newPlainText
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.createChooser
import androidx.compose.ui.platform.ClipEntry
import bring.composeapp.generated.resources.Res
import bring.composeapp.generated.resources.copied_card_id_to_clipboard
import bring.composeapp.generated.resources.copied_list_id_to_clipboard
import `in`.procyk.bring.vm.AbstractViewModel.Context
import org.jetbrains.compose.resources.StringResource

actual fun updateListLocationPresentation(listId: String?) {}

internal actual suspend fun onShareList(listId: String, context: Context) =
    onShare(listId, context, Res.string.copied_list_id_to_clipboard)

internal actual suspend fun onShareLoyaltyCard(cardId: String, context: Context) =
    onShare(cardId, context, Res.string.copied_card_id_to_clipboard)

private suspend fun onShare(value: String, context: Context, message: StringResource) {
    val shareIntent = Intent(ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, value)
    }
    val intent = createChooser(shareIntent, null).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.platformContext.context.startActivity(intent)

    context.clipboard.setClipEntry(ClipEntry(newPlainText(null, value)))
    context.showSnackbar(message)
}