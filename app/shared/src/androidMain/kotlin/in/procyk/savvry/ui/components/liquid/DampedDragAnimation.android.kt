package `in`.procyk.savvry.ui.components.liquid

internal actual suspend fun awaitFrame() {
    kotlinx.coroutines.android.awaitFrame()
}