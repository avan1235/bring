package `in`.procyk.bring.ui.components.liquid

internal actual suspend fun awaitFrame() {
    kotlinx.coroutines.android.awaitFrame()
}