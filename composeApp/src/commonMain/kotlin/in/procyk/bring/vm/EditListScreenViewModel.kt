package `in`.procyk.bring.vm

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.structure.StructuredOutput.Manual
import ai.koog.prompt.structure.StructuredOutputConfig
import ai.koog.prompt.structure.executeStructured
import androidx.lifecycle.viewModelScope
import bring.composeapp.generated.resources.Res
import bring.composeapp.generated.resources.error_fetching_favorite_elements
import bring.composeapp.generated.resources.error_fetching_suggestions
import bring.composeapp.generated.resources.list_not_found
import `in`.procyk.bring.*
import `in`.procyk.bring.ShoppingListItemData.CheckedStatusData.Checked
import `in`.procyk.bring.ShoppingListItemData.CheckedStatusData.Unchecked
import `in`.procyk.bring.ai.MarkdownWrappedJsonStructuredData.Companion.createMarkdownWrappedJsonStructure
import `in`.procyk.bring.service.FavoriteElementService
import `in`.procyk.bring.service.ShoppingListService
import `in`.procyk.bring.service.ShoppingListService.GetShoppingListError
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.time.Clock
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

    val useGeminiSettings = storeFlow.map { it.useGemini }.distinctUntilChanged().state(store.useGemini)

    private val _useGemini = MutableStateFlow(true)
    val useGemini = _useGemini.asStateFlow()

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

    val enableEditMode: StateFlow<Boolean> = storeFlow.map { it.enableEditMode }.state(store.enableEditMode)

    private val _showFab: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val showFab: StateFlow<Boolean> =
        combine(_showFab, enableEditMode) { state, setting -> state && setting }.state(false)

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
        viewModelScope.launch {
            val items = when {
                name.isNotBlank()
                        && useGeminiSettings.value
                        && useGemini.value
                        && store.geminiKey.isNotEmpty() -> getSuggestionsFromGemini(store.geminiKey, name)
                    ?.items
                    ?.map { it.name to it.count }
                    ?.takeUnless { it.isEmpty() }
                    ?: listOf(name to 1)

                else -> listOf(name to 1)
            }
            items.forEach { (item, count) ->
                shoppingListService.durableCall {
                    addEntryToShoppingList(store.userId, listId, item, count)
                }
            }
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

    fun onToggleUseGemini() {
        _useGemini.update { !it }
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

    fun onIncreaseItemCount(itemId: Uuid) {
        localListItems.update { list ->
            list.map { if (it.id == itemId) it.copy(count = it.count + 1) else it }
        }
        shoppingListService.durableCall {
            increaseItemCount(itemId)
        }
    }

    fun onDecreaseItemCount(itemId: Uuid) {
        localListItems.update { list ->
            list.map { if (it.id == itemId) it.copy(count = max(it.count - 1, 1)) else it }
        }
        shoppingListService.durableCall {
            decreaseItemCount(itemId)
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

private suspend fun getSuggestionsFromGemini(
    apiKey: String,
    description: String,
): SuggestedShoppingList? {
    val promptExecutor = simpleGoogleAIExecutor(apiKey)
    val structuredResponse = promptExecutor.executeStructured(
        prompt = prompt("shopping-list-suggestion") {
            system(
                """
                You are shopping list planner assistant.
                Write down a shopping list items from the provided input.
                Provide a list of elements that might be available at local shops, being some concrete, one by one listed products, to be bought at shop.
                Don't answer with general things to buy, only concrete ones matter, but not specify concrete producer.
                Use the same language to describe the list elements as he list description is written in.
                """.trimIndent()
            )
            user(description)
        },
        model = GoogleModels.Gemini2_0Flash,
        config = StructuredOutputConfig(
            default = Manual(shoppingListStructure),
        )
    )
    return structuredResponse.getOrNull()?.structure
}

@Serializable
@SerialName("ShoppingList")
@LLMDescription("Suggested shopping list containing items with their count")
private data class SuggestedShoppingList(
    @property:LLMDescription("List of suggested items")
    val items: List<SuggestedShoppingListItem>,
)

@Serializable
@SerialName("ShoppingListItem")
@LLMDescription("Shopping list item with its count")
private data class SuggestedShoppingListItem(
    @property:LLMDescription("Shopping list item count")
    val count: Int,
    @property:LLMDescription("Shopping list item name")
    val name: String,
)

private val examples = listOf(
    SuggestedShoppingList(
        items = listOf(
            SuggestedShoppingListItem(1, "bread"),
            SuggestedShoppingListItem(2, "milk"),
            SuggestedShoppingListItem(10, "eggs"),
            SuggestedShoppingListItem(1, "cheese"),
        )
    ),
    SuggestedShoppingList(
        items = listOf(
            SuggestedShoppingListItem(1, "cup"),
            SuggestedShoppingListItem(5, "water filter"),
            SuggestedShoppingListItem(3, "tea"),
        )
    )
)

private val shoppingListStructure =
    createMarkdownWrappedJsonStructure<SuggestedShoppingList>(examples = examples)

