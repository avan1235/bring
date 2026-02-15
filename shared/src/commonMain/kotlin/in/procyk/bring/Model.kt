package `in`.procyk.bring

import `in`.procyk.bring.service.LoyaltyCardService
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.CborLabel
import kotlin.time.Instant
import kotlin.uuid.Uuid

const val ShoppingListRpcPath: String = "/shoppingList"
const val FavoriteElementRpcPath: String = "/favoriteElement"
const val LoyaltyCardRpcPath: String = "/loyaltyCard"

val ListIdRegex: Regex = Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")

@Serializable
data class ShoppingListData(
    @CborLabel(0) val id: Uuid,
    @CborLabel(1) val name: String,
    @CborLabel(2) val byUserId: Uuid,
    @Serializable(InstantSerializer::class)
    @CborLabel(3) val createdAt: Instant,
    @CborLabel(4) val items: List<ShoppingListItemData>,
)

@Serializable
data class UserShoppingListSuggestionsData(
    @CborLabel(0) val itemsNames: Set<String>,
)

@Serializable
data class UserFavoriteElementsData(
    @CborLabel(0) val elements: List<FavoriteElement>,
)

@Serializable
data class FavoriteElement(
    @CborLabel(0) val id: Uuid,
    @CborLabel(1) val name: String,
)

@Serializable
data class ShoppingListItemData(
    @CborLabel(0) val id: Uuid,
    @CborLabel(1) val name: String,
    @CborLabel(2) val byUserId: Uuid,
    @Serializable(InstantSerializer::class)
    @CborLabel(3) val createdAt: Instant,
    @CborLabel(4) val order: Double,
    @CborLabel(5) val status: CheckedStatusData,
    @CborLabel(6) val count: Int = 1,
) {
    @Serializable
    sealed class CheckedStatusData {
        @Serializable
        data object Unchecked : CheckedStatusData()

        @Serializable
        data class Checked(
            @Serializable(InstantSerializer::class)
            @CborLabel(0) val changedAt: Instant,
            @CborLabel(1) val byUserId: Uuid,
        ) : CheckedStatusData()

        val isChecked: Boolean
            get() = when (this) {
                is Checked -> true
                is Unchecked -> false
            }
    }
}

@Serializable
data class Code(
    @CborLabel(0) val rawText: String,
    @CborLabel(1) val format: Format,
) {
    @Serializable
    enum class Format {
        QR_CODE, DATA_MATRIX, AZTEC, PDF_417, MAXICODE, CODE_128, CODE_39, CODE_93, EAN_13, EAN_8, UPC_A, UPC_E, ITF, CODABAR, RSS_14, RSS_EXPANDED, UPC_EAN_EXTENSION;
    }
}

@Serializable
data class LoyaltyCardData(
    @CborLabel(0) val id: Uuid,
    @CborLabel(1) val label: String,
    @CborLabel(2) val byUserId: Uuid,
    @Serializable(InstantSerializer::class)
    @CborLabel(3) val createdAt: Instant,
    @CborLabel(4) val code: Code,
)