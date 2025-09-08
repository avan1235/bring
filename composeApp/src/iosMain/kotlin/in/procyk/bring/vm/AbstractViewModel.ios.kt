package `in`.procyk.bring.vm

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import bring.composeapp.generated.resources.Res
import bring.composeapp.generated.resources.copied_list_id_to_clipboard
import `in`.procyk.bring.vm.AbstractViewModel.Context
import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSString
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

actual fun updateListLocationPresentation(listId: String?) {}

@OptIn(ExperimentalComposeUiApi::class, BetaInteropApi::class)
actual suspend fun onShareList(listId: String, context: Context) {
    val activityItems = listOf(
        NSString.create(string = "bring://$listId")
    )
    val activityViewController =
        UIActivityViewController(activityItems = activityItems, applicationActivities = null)

    val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
    rootViewController?.presentViewController(activityViewController, animated = true, completion = null)

    context.clipboard.setClipEntry(ClipEntry.withPlainText(listId))
    context.showSnackbar(Res.string.copied_list_id_to_clipboard)
}