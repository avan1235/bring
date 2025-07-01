package `in`.procyk.bring.extract

import `in`.procyk.bring.db.ShoppingListEntity
import `in`.procyk.bring.db.ShoppingListItemEntity
import `in`.procyk.bring.db.ShoppingListItemsTable
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

internal data object ExistingShoppingListIngredientsExtractor : IngredientsExtractor {

    override suspend fun supports(input: String): Boolean =
        runCatching { Uuid.parseHexDash(input) }.isSuccess

    override suspend fun extractIngredients(input: String): List<Ingredient> {
        return newSuspendedTransaction {
            val id = Uuid.parseHexDash(input).toJavaUuid()
            ShoppingListItemEntity.find {
                ShoppingListItemsTable.listId eq id
            }.map { Ingredient(it.name)  }
        }
    }

    override suspend fun extractTitle(input: String): String? {
        return newSuspendedTransaction {
            val id = Uuid.parseHexDash(input).toJavaUuid()
            ShoppingListEntity.findById(id)?.name
        }
    }
}