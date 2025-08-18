package `in`.procyk.bring.db

import `in`.procyk.bring.ShoppingListItemData.CheckedStatusData.Checked
import `in`.procyk.bring.ShoppingListItemData.CheckedStatusData.Unchecked
import io.ktor.serialization.kotlinx.cbor.*
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import java.util.*

internal object ShoppingListsTable : UUIDTable(name = "shopping_lists") {
    val name = text("name", eagerLoading = true)
    val byUserId = uuid("by_user_id")
    val createdAt = timestamp("created_at")
}

internal class ShoppingListEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ShoppingListEntity>(ShoppingListsTable)

    var name by ShoppingListsTable.name
    var byUserId by ShoppingListsTable.byUserId
    var createdAt by ShoppingListsTable.createdAt

    val items by ShoppingListItemEntity referrersOn ShoppingListItemsTable.listId
}

internal object ShoppingListItemsTable : UUIDTable(name = "shopping_list_items") {
    val name = text("name", eagerLoading = true)
    val listId = reference("list_id", ShoppingListsTable, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val byUserId = uuid("by_user_id")
    val createdAt = timestamp("created_at")
    val order = double("order").index("order_index")
    val status = blob("status").nullable()
    val count = integer("count").nullable()
}

internal class ShoppingListItemEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ShoppingListItemEntity>(ShoppingListItemsTable)

    var name by ShoppingListItemsTable.name
    var listId by ShoppingListItemsTable.listId
    var byUserId by ShoppingListItemsTable.byUserId
    var createdAt by ShoppingListItemsTable.createdAt
    var order by ShoppingListItemsTable.order
    var status by ShoppingListItemsTable.status.transform(
        wrap = { status ->
            status?.bytes?.let { DefaultCbor.decodeFromByteArray<Checked>(it) } ?: Unchecked
        },
        unwrap = { status ->
            when (status) {
                is Checked -> DefaultCbor.encodeToByteArray<Checked>(status).let(::ExposedBlob)
                Unchecked -> null
            }
        }
    )
    var count by ShoppingListItemsTable.count.transform(
        wrap = { it ?: 1 },
        unwrap = { count ->
            when {
                count <= 1 -> null
                else -> count
            }
        }
    )
}
