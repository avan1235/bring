package `in`.procyk.bring.db

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import java.util.*

internal object FavoriteElementsTable : UUIDTable(name = "favorite_elements") {
    val name = text("name", eagerLoading = true)
    val byUserId = uuid("by_user_id").index()
    val createdAt = timestamp("created_at")
}

internal class FavoriteElementEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<FavoriteElementEntity>(FavoriteElementsTable)

    var name by FavoriteElementsTable.name
    var byUserId by FavoriteElementsTable.byUserId
    var createdAt by FavoriteElementsTable.createdAt
}
