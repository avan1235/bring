package `in`.procyk.savvry.service

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class CreateShoppingListTest : SavvryServerIntegrationTestCase(){

    @Test
    fun `create new list`() = runTest {
        val shoppingListService = shoppingListService()
        val listId = shoppingListService.testCall {
            createNewShoppingList(userId, "Today's shopping list")
        }
    }

    @Test
    fun `get created list data`() = runTest {
        val listName = "Tomorrow's shopping list"

        val shoppingListService = shoppingListService()
        val listId = shoppingListService.testCall {
            createNewShoppingList(userId, listName)
        }
        val data = shoppingListService.testCall {
            getShoppingList(listId).first()
        }

        assertEquals(listName, data.name)
        assertEquals(userId, data.byUserId)
        assertEquals(0, data.items.size)
    }
}