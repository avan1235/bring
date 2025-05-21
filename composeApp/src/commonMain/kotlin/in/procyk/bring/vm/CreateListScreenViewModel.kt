package `in`.procyk.bring.vm

import bring.composeapp.generated.resources.Res
import bring.composeapp.generated.resources.list_not_found
import `in`.procyk.bring.ListIdRegex
import `in`.procyk.bring.ShoppingListRpcPath
import `in`.procyk.bring.service.ShoppingListService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.*
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlin.uuid.Uuid

internal class CreateListScreenViewModel(
    context: Context,
) : AbstractViewModel(context) {

    private val shoppingListService = durableRpcService<ShoppingListService>(ShoppingListRpcPath)

    private val _name: MutableStateFlow<String> = MutableStateFlow(suggestListName())
    val name = _name.asStateFlow()

    private val _listId: MutableStateFlow<String> = MutableStateFlow("")
    val listId = _listId.asStateFlow()
    val isListIdValid = _listId
        .map { it.isBlank() || ListIdRegex.find(it) != null }
        .state(false)
    val isJoinShoppingListEnabled = _listId
        .map { ListIdRegex.find(it) != null }
        .state(false)

    fun onNameChange(name: String) {
        _name.update { name }
    }

    fun onListIdChange(listId: String) {
        _listId.update { listId }
    }

    fun onCreateShoppingList() {
        val name = name.value
        val userId = store.userId
        shoppingListService.durableCall {
            createNewShoppingList(userId, name).fold(
                ifLeft = { this@CreateListScreenViewModel.context.navigateEditList(it, fetchSuggestions = true) },
                ifRight = { /* TODO: handle errors */ }
            )
        }
    }

    fun onJoinShoppingList() {
        val listId = ListIdRegex.find(_listId.value)?.value
            ?: return context.showSnackbar(Res.string.list_not_found)
        context.navigateEditList(listId, fetchSuggestions = true)
    }
}

private fun suggestListName(): String {
    val now = Clock.System.now()
    val dateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
    return dateTime.date.format(suggestListNameDateFormat)
}

private val suggestListNameDateFormat = LocalDate.Format {
    dayOfMonth()
    char('.')
    monthNumber(padding = Padding.ZERO)
    char('.')
    year()
}