package `in`.procyk.bring.db

import `in`.procyk.bring.Code
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import java.util.*

internal object LoyaltyCardsTable : UUIDTable(name = "loyalty_cards") {
    val label = text("label", eagerLoading = true)
    val byUserId = uuid("by_user_id").index()
    val createdAt = timestamp("created_at")
    val codeFormat = enumeration("code_format", Code.Format::class)
    val codeRawText = text("code_raw_text", eagerLoading = true)
}

internal class LoyaltyCardEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<LoyaltyCardEntity>(LoyaltyCardsTable)

    var label by LoyaltyCardsTable.label
    var byUserId by LoyaltyCardsTable.byUserId
    var createdAt by LoyaltyCardsTable.createdAt
    var codeFormat by LoyaltyCardsTable.codeFormat
    var codeRawText by LoyaltyCardsTable.codeRawText
}
