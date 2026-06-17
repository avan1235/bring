package `in`.procyk.savvry.service

import arrow.core.Either
import arrow.core.Either.Left
import `in`.procyk.savvry.DurableRpcService
import `in`.procyk.savvry.ShoppingListData
import `in`.procyk.savvry.ShoppingListItemData
import `in`.procyk.savvry.service.ShoppingListService.GetShoppingListError
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.uuid.Uuid

internal class EditShoppingListTest : SavvryServerIntegrationTestCase() {

    private inline fun runTestWithShoppingList(
        crossinline testBody: suspend TestScope.(
            shoppingListService: DurableRpcService<ShoppingListService>,
            listId: Uuid,
        ) -> Unit,
    ) = runTest {
        val shoppingListService = shoppingListService()
        val listId = shoppingListService.testCall {
            createNewShoppingList(userId, "Non Empty shopping list")
        }
        testBody(shoppingListService, listId)
    }

    @Test
    fun `add item to created list`() = runTestWithShoppingList { shoppingListService, listId ->
        val input = "Test item"

        val data = shoppingListService.testCall {
            getShoppingList(listId).first()
        }
        assertEquals(0, data.items.size)

        shoppingListService.testCall {
            addEntryToShoppingList(userId, listId, input)
        }

        val updatedData = shoppingListService.testCall {
            getShoppingList(listId).first { it.leftOrNull()?.items?.isNotEmpty() == true }
        }

        assertEquals(1, updatedData.items.size)
        val addedItem = updatedData.items.first()

        assertEquals(userId, addedItem.byUserId)
        assertEquals(input, addedItem.name)
        assertEquals(ShoppingListItemData.CheckedStatusData.Unchecked, addedItem.status)
    }

    @Test
    fun `added item updates list flow`() = runTestWithShoppingList { shoppingListService, listId ->
        val input = "Test item"
        val updates = mutableListOf<Either<ShoppingListData, GetShoppingListError>>()

        val data = shoppingListService.testLaunch {
            getShoppingList(listId)
        }

        val updatesCollection = launch {
            data.collect {
                updates.add(it)
                it.onLeft {
                    if (it.items.isNotEmpty()) cancel("received updated list with items")
                }
            }
        }

        shoppingListService.testCall {
            addEntryToShoppingList(userId, listId, input)
        }

        updatesCollection.join()

        updates.forEach { assertIs<Left<ShoppingListData>>(it) }
        assertEquals(listOf(input), updates.last().leftOrNull()?.items?.map { it.name })
        assertEquals(listOf(false), updates.last().leftOrNull()?.items?.map { it.status.isChecked })
    }
}
