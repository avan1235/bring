package `in`.procyk.bring.vm

import kotlinx.browser.window

actual fun updateListLocationPresentation(listId: String?) {
    window.location.hash = when {
        listId != null -> "#${listId}"
        else -> ""
    }
}
