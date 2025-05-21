package `in`.procyk.bring.vm

import androidx.lifecycle.viewModelScope
import bring.composeapp.generated.resources.Res
import bring.composeapp.generated.resources.error_fetching_favorite_elements
import bring.composeapp.generated.resources.error_fetching_suggestions
import bring.composeapp.generated.resources.list_not_found
import `in`.procyk.bring.*
import `in`.procyk.bring.ShoppingListItemData.CheckedStatusData.Checked
import `in`.procyk.bring.ShoppingListItemData.CheckedStatusData.Unchecked
import `in`.procyk.bring.service.FavoriteElementService
import `in`.procyk.bring.service.ShoppingListService
import `in`.procyk.bring.service.ShoppingListService.GetShoppingListError
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

internal class EditListScreenViewModel(
    context: Context,
    private val listId: Uuid,
    fetchSuggestionsAndFavoriteElements: Boolean,
) : AbstractViewModel(context) {

    private val shoppingListService = durableRpcService<ShoppingListService>(ShoppingListRpcPath)
    private val favoriteElementService = durableRpcService<FavoriteElementService>(FavoriteElementRpcPath)

    data class SuggestedItem(val name: String, val used: Boolean, val id: Uuid)

    private val _suggestedItems = MutableStateFlow<List<SuggestedItem>>(emptyList())
    val suggestedItems: StateFlow<List<SuggestedItem>> = _suggestedItems.asStateFlow()

    init {
        if (fetchSuggestionsAndFavoriteElements) viewModelScope.launch {
            val userId = store.userId

            val suggestions = when {
                !store.showSuggestions -> CompletableDeferred(value = emptyList())
                else -> CompletableDeferred<Collection<String>>().also { deferred ->
                    shoppingListService.durableCall {
                        getUserShoppingListSuggestions(userId).fold(
                            ifLeft = { deferred.complete(it.itemsNames) },
                            ifRight = {
                                context.showSnackbar(Res.string.error_fetching_suggestions)
                                deferred.complete(emptyList())
                            },
                        )
                    }
                }
            }


            val favoriteElements = when {
                !store.showFavoriteElements -> CompletableDeferred(value = emptyList())
                else -> CompletableDeferred<Collection<String>>().also { deferred ->
                    favoriteElementService.durableCall {
                        getFavoriteElements(userId).fold(
                            ifLeft = { deferred.complete(it.elements.map { it.name }) },
                            ifRight = {
                                context.showSnackbar(Res.string.error_fetching_favorite_elements)
                                deferred.complete(emptyList())
                            },
                        )
                    }
                }
            }

            _suggestedItems.value = buildSet {
                addAll(favoriteElements.await())
                addAll(suggestions.await())
            }.map { SuggestedItem(it, used = false, id = Uuid.random()) }
        }

        shoppingListService.durableLaunch {
            getShoppingList(listId).collectLatest {
                it.fold(
                    ifLeft = {
                        val updatedList = it.copy(items = it.items.sortedWith(ListItemsComparator))
                        fetchedList.update { updatedList }
                    },
                    ifRight = {
                        when (it) {
                            GetShoppingListError.Internal -> {}
                            GetShoppingListError.UnknownListId -> {
                                context.showSnackbar(Res.string.list_not_found)
                                context.navigateCreateList(cleanLastListId = true)
                            }
                        }
                    },
                )
            }
        }
    }

    private val fetchedList = MutableStateFlow<ShoppingListData?>(null)
    private val localListItems = MutableStateFlow<List<ShoppingListItemData>>(emptyList())

    init {
        viewModelScope.launch {
            var lastElementsIds = setOf<Uuid>()
            fetchedList.mapNotNull { it?.items }.distinctUntilChanged().debounce {
                val elementsIds = it.mapTo(HashSet()) { it.id }
                val reorderedOnly = lastElementsIds == elementsIds
                lastElementsIds = elementsIds
                if (reorderedOnly) 1.seconds else 0.seconds
            }.collectLatest { localListItems.value = it }
        }
        viewModelScope.launch {
            fetchedList.mapNotNull { it?.name }.distinctUntilChanged().collectLatest {
                context.updateListName(it)
            }
        }
    }

    val showSuggestions: StateFlow<Boolean> = storeFlow.map { it.showSuggestions }.state(store.showSuggestions)

    val showFavoriteElements: StateFlow<Boolean> =
        storeFlow.map { it.showFavoriteElements }.state(store.showFavoriteElements)

    val items: StateFlow<List<ShoppingListItemData>> =
        combine(localListItems, storeFlow.map { it.showUncheckedFirst }) { items, showUncheckedFirst ->
            when {
                showUncheckedFirst -> items.filter { !it.status.isChecked } + items.filter { it.status.isChecked }
                else -> items
            }
        }.state(emptyList())

    val itemsProgress: StateFlow<Float> = items.map { items ->
        items.takeIf { it.isNotEmpty() }?.let { it.count { it.status.isChecked }.toFloat() / it.size.toFloat() } ?: 0f
    }.state(0f)

    private val _newItemName: MutableStateFlow<String> = MutableStateFlow("")
    val newItemName: StateFlow<String> = _newItemName.asStateFlow()

    val showInputField: StateFlow<Boolean> = storeFlow.map { it.enableEditMode }.state(store.enableEditMode)

    private val _showFab: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val showFab: StateFlow<Boolean> =
        combine(_showFab, showInputField) { state, setting -> state && setting }.state(false)

    val isFavorite: StateFlow<Boolean> =
        combine(storeFlow.map { it.favoriteShoppingLists }, fetchedList) { favorites, list ->
            favorites.any { it -> it.listId == list?.id }
        }.state(false)

    fun onNewItemNameChange(name: String) {
        _newItemName.update { name }
    }

    fun onCreateNewItem() {
        val name = _newItemName.value
        _newItemName.update { "" }
        shoppingListService.durableCall {
            addEntryToShoppingList(store.userId, listId, name)
        }
    }

    fun onCreateNewItemFromSuggestion(name: String) {
        _suggestedItems.update { items -> items.map { if (it.name == name) it.copy(used = true) else it } }
        shoppingListService.durableCall {
            addEntryToShoppingList(store.userId, listId, name)
        }
    }

    fun onToggleListFavorite() {
        val list = fetchedList.value ?: return
        val favoriteShoppingList = FavoriteShoppingList(list.name, list.id)
        updateConfig {
            it.run {
                when {
                    favoriteShoppingList !in favoriteShoppingLists -> copy(favoriteShoppingLists = favoriteShoppingLists + favoriteShoppingList)
                    else -> copy(favoriteShoppingLists = favoriteShoppingLists - favoriteShoppingList)
                }
            }
        }
    }

    fun onShareList() {
        viewModelScope.launch {
            onShareList(listId.toHexDashString(), context)
        }
    }

    fun onChecked(itemId: Uuid, checked: Boolean) {
        val localStatus = if (checked) Checked(Clock.System.now(), store.userId) else Unchecked
        localListItems.update { list ->
            list.map { if (it.id == itemId) it.copy(status = localStatus) else it }.sortedWith(ListItemsComparator)
        }
        when {
            checked -> shoppingListService.durableCall { markItemAsChecked(store.userId, itemId) }
            else -> shoppingListService.durableCall { markItemAsUnchecked(itemId) }
        }
    }

    fun onRemoved(itemId: Uuid) {
        localListItems.update { list ->
            list.filter { it.id != itemId }
        }
        shoppingListService.durableCall {
            removeShoppingListItem(itemId)
        }
    }

    fun onUpdatedItemOrder(fromKey: Uuid, toKey: Uuid, items: List<ShoppingListItemData>) {
        val (fromIndex, from) = items.withIndex().firstOrNull { (_, item) -> item.id == fromKey } ?: return
        val (toIndex, to) = items.withIndex().firstOrNull { (_, item) -> item.id == toKey } ?: return
        val relativeOrder = when {
            toIndex < fromIndex -> items.getOrNull(toIndex - 1)?.order ?: (to.order + 1.0)
            toIndex > fromIndex -> items.getOrNull(toIndex + 1)?.order ?: (to.order - 1.0)
            else -> return
        }
        val updatedOrder = (to.order + relativeOrder) / 2.0
        localListItems.update { list ->
            list.map { if (it.id == from.id) it.copy(order = updatedOrder) else it }.sortedWith(ListItemsComparator)
        }
        shoppingListService.durableCall {
            updateItemOrder(from.id, updatedOrder)
        }
    }

    fun onShowFab(show: Boolean) {
        _showFab.update { show }
    }
}

private val ListItemsComparator: Comparator<ShoppingListItemData> =
    compareByDescending(ShoppingListItemData::order).thenByDescending(ShoppingListItemData::createdAt)
