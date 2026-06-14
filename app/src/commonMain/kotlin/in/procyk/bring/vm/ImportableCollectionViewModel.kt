package `in`.procyk.bring.vm

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import bring.app.generated.resources.Res
import bring.app.generated.resources.loading_importing
import `in`.procyk.bring.BringStore
import `in`.procyk.bring.Identifiable
import `in`.procyk.bring.Orderable
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import kotlin.uuid.Uuid

internal abstract class ImportableCollectionViewModel<TStored, TData, TItem, TInputContext>(
    context: Context,
) : AbstractViewModel(context) where TStored : Orderable, TStored : Identifiable, TItem : Orderable, TItem : Identifiable {

    sealed class InputDialogAction {
        data object AddFromFile : InputDialogAction()
        data object ImportById : InputDialogAction()
        data class Loading(val description: String) : InputDialogAction()
    }

    protected abstract fun BringStore.storedItems(): List<TStored>

    protected abstract fun BringStore.withStoredItems(items: List<TStored>): BringStore

    protected abstract fun cachedData(stored: TStored): TData?

    protected abstract fun withCachedData(stored: TStored, data: TData?): TStored

    protected abstract fun color(stored: TStored): Int?

    protected abstract fun withColor(stored: TStored, color: Int?): TStored

    protected abstract suspend fun fetchData(stored: TStored): Either<TData, FetchError>

    protected abstract fun buildItem(stored: TStored, data: TData): TItem

    protected open fun postProcessItems(items: List<TItem>): List<TItem> = items

    protected abstract val useCacheStored: BringStore.() -> Boolean

    protected abstract val enableEditModeStored: BringStore.() -> Boolean

    protected abstract val showLabelsStored: BringStore.() -> Boolean

    protected abstract val enabledScanButtonStored: BringStore.() -> Boolean

    protected open val idSeparator: String get() = ";"

    protected abstract fun newStored(id: Uuid, order: Double): TStored

    protected abstract suspend fun createFromFile(input: TInputContext): List<Uuid>

    protected abstract suspend fun removeRemote(item: TItem): Either<Unit, RemoveError>

    protected abstract fun getInputContext(): TInputContext

    protected abstract fun isValidContext(input: TInputContext): Boolean

    protected abstract suspend fun share(ids: String)

    enum class FetchError { Internal, UnknownId }
    enum class RemoveError { Internal, UnknownId }

    private val _dialogAction: MutableStateFlow<InputDialogAction?> = MutableStateFlow(null)
    val dialogAction: StateFlow<InputDialogAction?> = _dialogAction.asStateFlow()

    private val _items: MutableStateFlow<List<TItem>> = MutableStateFlow(emptyList())
    val items: StateFlow<List<TItem>> = _items.asStateFlow()

    private val _userInput: MutableStateFlow<String> = MutableStateFlow("")
    val userInput: StateFlow<String> = _userInput.asStateFlow()

    private val _isErrorUserInput: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isErrorUserInput: StateFlow<Boolean> = _isErrorUserInput.asStateFlow()

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val enableEditMode: StateFlow<Boolean> by lazy {
        storeFlow.map { it.enableEditModeStored() }.state(store.enableEditModeStored())
    }

    val showLabels: StateFlow<Boolean> by lazy {
        storeFlow.map { it.showLabelsStored() }.state(store.showLabelsStored())
    }

    val enabledScanButton: StateFlow<Boolean> by lazy {
        storeFlow.map { it.enabledScanButtonStored() }.state(store.enabledScanButtonStored())
    }

    protected fun updateStoredItemsInBackground() {
        viewModelScope.launch {
            val inMemoryCache = mutableMapOf<Uuid, TItem>()
            context.storeFlow.map { it.storedItems() }.distinctUntilIdsChanged().collectLatest { stored ->
                _isLoading.value = true
                val useCache = store.useCacheStored()
                try {
                    val resolved = stored.mapNotNull { entry ->
                        val key = entry.id
                        inMemoryCache[key] ?: run {
                            val cached = if (useCache) cachedData(entry) else null
                            val data = cached ?: fetchData(entry).fold(
                                ifLeft = { it },
                                ifRight = { err ->
                                    when (err) {
                                        FetchError.Internal -> { /* TODO: handle errors */
                                        }

                                        FetchError.UnknownId -> launchUpdateConfig { st ->
                                            st.withStoredItems(st.storedItems().filter { it.id != entry.id })
                                        }
                                    }
                                    return@run null
                                },
                            )
                            launchUpdateConfig { st ->
                                st.withStoredItems(
                                    st.storedItems().map {
                                        if (it.id == key) withCachedData(it, if (useCache) data else null) else it
                                    },
                                )
                            }
                            buildItem(entry, data)
                        }?.also { inMemoryCache[key] = it }
                    }
                    val sorted = postProcessItems(resolved).sortedBy { it as Orderable }
                    _items.value = sorted
                    resetUserInput()
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun addFromFile() {
        val inputContext = validateUserInputContext(::isValidContext) ?: return
        viewModelScope.launch {
            val ids = createFromFile(inputContext)
            updateConfigWithStoredIds(ids)
        }.invokeOnCompletion { closeDialogAction() }
    }

    fun importByIds() {
        viewModelScope.launch {
            updateDialogActionLoading(getString(Res.string.loading_importing))
            val ids = userInput.value.split(idSeparator)
                .mapNotNull { runCatching { Uuid.parse(it) }.getOrNull() }
            updateConfigWithStoredIds(ids)
        }.invokeOnCompletion { closeDialogAction() }
    }

    fun removeItem(item: TItem) {
        launchUpdateConfig { it.withStoredItems(it.storedItems().filter { stored -> stored.id != item.id }) }
        viewModelScope.launch {
            removeRemote(item).onRight { /* TODO: handle errors */ }
        }
    }

    fun shareItem(item: TItem) {
        viewModelScope.launch { share(item.id.toHexDashString()) }
    }

    fun shareAll() {
        viewModelScope.launch {
            val ids = items.value.joinToString(separator = idSeparator) { it.id.toHexDashString() }
            share(ids)
        }
    }

    fun onUpdatedItemOrder(fromKey: Uuid, toKey: Uuid, current: List<TItem>) {
        onUpdatedItemOrder(fromKey, toKey, current) { from, updatedOrder ->
            _items.update { existing ->
                existing.map { if (it.id == from.id) replaceOrder(it, updatedOrder) else it }.sorted()
            }
            launchUpdateConfig { st ->
                st.withStoredItems(
                    st.storedItems().map {
                        if (it.id == from.id) replaceStoredOrder(it, updatedOrder) else it
                    },
                )
            }
        }
    }

    protected abstract fun replaceOrder(item: TItem, order: Double): TItem

    protected abstract fun replaceStoredOrder(stored: TStored, order: Double): TStored

    fun itemColor(item: TItem): StateFlow<Color> =
        storeFlow
            .map { st -> st.storedItems().find { it.id == item.id }?.let(::color)?.let(::Color) ?: Color.Unspecified }
            .state(Color.Unspecified)

    fun onItemColorUpdated(itemId: Uuid, color: Color?) {
        launchUpdateConfig { st ->
            st.withStoredItems(
                st.storedItems().map {
                    if (it.id == itemId) withColor(it, color?.toArgb()) else it
                },
            )
        }
    }

    fun onUserInputChange(value: String) {
        _userInput.update { value }
        _isErrorUserInput.update { false }
    }

    fun resetUserInput() {
        onUserInputChange("")
    }

    open fun openAddFromFileDialog() {
        _dialogAction.update { InputDialogAction.AddFromFile }
    }

    fun openImportByIdDialog() {
        _dialogAction.update { InputDialogAction.ImportById }
    }

    fun updateDialogActionLoading(description: String) {
        _dialogAction.update { InputDialogAction.Loading(description) }
    }

    fun closeDialogAction() {
        _dialogAction.update { null }
    }

    private fun validateUserInputContext(isValid: (TInputContext) -> Boolean): TInputContext? {
        val input = getInputContext()
        if (!isValid(input)) {
            _isErrorUserInput.update { true }
            return null
        }
        return input
    }

    private fun updateConfigWithStoredIds(ids: List<Uuid>) {
        launchUpdateConfig { config ->
            val existing = config.storedItems()
            val maxOrder = existing.maxOfOrNull { it.order } ?: 0.0
            val existingIds = existing.map { it.id }
            config.withStoredItems(
                existing + ids
                    .filter { it !in existingIds }
                    .mapIndexed { idx, id -> newStored(id, maxOrder + 1.0 + idx) },
            )
        }
    }
}
