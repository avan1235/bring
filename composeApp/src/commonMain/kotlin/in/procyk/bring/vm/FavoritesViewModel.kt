package `in`.procyk.bring.vm

import androidx.lifecycle.viewModelScope
import arrow.core.Either
import `in`.procyk.bring.FavoriteElement
import `in`.procyk.bring.FavoriteElementRpcPath
import `in`.procyk.bring.FavoriteShoppingList
import `in`.procyk.bring.UserFavoriteElementsData
import `in`.procyk.bring.service.FavoriteElementService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

internal class FavoritesViewModel(context: Context) : AbstractViewModel(context) {

    private val favoriteElementService = durableRpcService<FavoriteElementService>(FavoriteElementRpcPath)

    private val _favoriteElements = MutableStateFlow<List<FavoriteElement>>(emptyList())
    val favoriteElements: StateFlow<List<FavoriteElement>> = _favoriteElements.asStateFlow()

    init {
        viewModelScope.launch {
            favoriteElementService.durableCall {
                getFavoriteElements(store.userId).updateFavoriteElements()
            }
        }
    }

    val favoriteLists: StateFlow<List<FavoriteShoppingList>> =
        storeFlow.map { it.favoriteShoppingLists.toList() }.state(emptyList())

    private val _newFavoriteElementName: MutableStateFlow<String> = MutableStateFlow("")
    val newFavoriteElementName: StateFlow<String> = _newFavoriteElementName.asStateFlow()


    fun onNavigateToFavoriteList(list: FavoriteShoppingList) {
        context.navigateEditList(list.listId, fetchSuggestions = false)
    }

    fun onNewFavoriteElementNameChange(value: String) {
        _newFavoriteElementName.update { value }
    }

    fun onFavoriteListRemoved(list: FavoriteShoppingList) {
        updateConfig { it.copy(favoriteShoppingLists = it.favoriteShoppingLists - list) }
    }

    fun onFavoriteListShared(list: FavoriteShoppingList) {
        viewModelScope.launch {
            onShareList(list.listId.toHexDashString(), context)
        }
    }

    fun onFavoriteElementCreated() {
        val name = _newFavoriteElementName.value
        _newFavoriteElementName.value = ""
        viewModelScope.launch {
            favoriteElementService.durableCall {
                addFavoriteElement(name, store.userId).updateFavoriteElements()
            }
        }
    }

    fun onFavoriteElementRemoved(elementId: Uuid) {
        viewModelScope.launch {
            favoriteElementService.durableCall {
                removeFavoriteElement(elementId).updateFavoriteElements()
            }
        }
    }

    private fun <E> Either<UserFavoriteElementsData, E>.updateFavoriteElements() {
        fold(
            ifLeft = { _favoriteElements.value = it.elements },
            ifRight = { /* TODO: handle errors */ },
        )
    }
}