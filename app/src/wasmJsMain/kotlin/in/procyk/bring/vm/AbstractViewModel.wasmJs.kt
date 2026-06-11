@file:OptIn(ExperimentalWasmJsInterop::class)

package `in`.procyk.bring.vm

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry.Companion.withPlainText
import bring.app.generated.resources.Res
import bring.app.generated.resources.copied_card_id_to_clipboard
import bring.app.generated.resources.copied_list_url_to_clipboard
import `in`.procyk.bring.vm.AbstractViewModel.Context
import kotlinx.browser.window
import kotlin.js.ExperimentalWasmJsInterop


@OptIn(ExperimentalComposeUiApi::class)
internal actual suspend fun onShareList(listId: String, context: Context) {
    val url = "${window.location.origin}/#${listId}"
    val clipEntry = withPlainText(url)
    context.clipboard.setClipEntry(clipEntry)
    context.showSnackbar(Res.string.copied_list_url_to_clipboard)
    if (jsIsShareSupported()) {
        jsShareUrl(url, null, onSuccess = {}, onDismissed = {}, onError = {})
    }
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

@JsFun("function() { return ('share' in navigator); }")
private external fun jsIsShareSupported(): Boolean

@JsFun("""
function(url, title, onSuccess, onDismissed, onError) {
    var data = { url: url };
    if (title) data.title = title;
    navigator.share(data).then(function() {
        onSuccess();
    }).catch(function(e) {
        if (e && e.name === 'AbortError') { onDismissed(); } else { onError(); }
    });
}
""")
private external fun jsShareUrl(
    url: String,
    title: JsString?,
    onSuccess: () -> Unit,
    onDismissed: () -> Unit,
    onError: () -> Unit,
)
