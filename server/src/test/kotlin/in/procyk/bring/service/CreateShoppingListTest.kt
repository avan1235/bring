package `in`.procyk.bring.service

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class CreateShoppingListTest : BringServerIntegrationTestCase(){

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

        assertEquals(data.name, listName)
        assertEquals(data.byUserId, userId)
        assertEquals(data.items.size, 0)
    }
}