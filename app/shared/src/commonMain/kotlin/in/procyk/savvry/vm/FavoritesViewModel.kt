package `in`.procyk.savvry.vm

import androidx.lifecycle.viewModelScope
import arrow.core.Either
import savvry.app.generated.resources.Res
import savvry.app.generated.resources.error_updating_favorites
import `in`.procyk.savvry.FavoriteElement
import `in`.procyk.savvry.FavoriteElementRpcPath
import `in`.procyk.savvry.FavoriteShoppingList
import `in`.procyk.savvry.RecentShoppingList
import `in`.procyk.savvry.SavedShoppingList
import `in`.procyk.savvry.UserFavoriteElementsData
import `in`.procyk.savvry.service.FavoriteElementService
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

    val recentLists: StateFlow<List<RecentShoppingList>> =
        storeFlow.map { it.recentShoppingLists.toList() }.state(emptyList())

    private val _newFavoriteElementName: MutableStateFlow<String> = MutableStateFlow("")
    val newFavoriteElementName: StateFlow<String> = _newFavoriteElementName.asStateFlow()


    fun onNavigateToSavedShoppingList(list: SavedShoppingList) {
        context.navigateEditList(list.listId, fetchSuggestions = false)
    }

    fun onNewFavoriteElementNameChange(value: String) {
        _newFavoriteElementName.update { value }
    }

    fun onSavedShoppingListRemoved(list: SavedShoppingList) {
        launchUpdateConfig {
            when (list) {
                is FavoriteShoppingList -> it.copy(favoriteShoppingLists = it.favoriteShoppingLists - list)
                is RecentShoppingList -> it.copy(recentShoppingLists = it.recentShoppingLists - list)
            }
        }
    }

    fun onSavedShoppingListShared(list: SavedShoppingList) {
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
            ifRight = { context.showSnackbar(Res.string.error_updating_favorites) },
        )
    }
}