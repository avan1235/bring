package `in`.procyk.bring

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
    @CborLabel(0) val text: String,
    @CborLabel(1) val format: Format,
    @CborLabel(2) val bits: Bits,
) {
    @Serializable
    enum class Format {
        QR_CODE, DATA_MATRIX, AZTEC, PDF_417, MAXICODE, CODE_128, CODE_39, CODE_93, EAN_13, EAN_8, UPC_A, UPC_E, ITF, CODABAR, RSS_14, RSS_EXPANDED, UPC_EAN_EXTENSION;
    }

    @Serializable
    data class Bits(
        @CborLabel(0) val width: Int,
        @CborLabel(1) val height: Int,
        @CborLabel(2) val data: BooleanArray,
    ) {
        constructor(width: Int, height: Int, data: (Int, Int) -> Boolean) :
                this(width, height, BooleanArray(width * height).apply {
                    for (y in 0..<height) for (x in 0..<width) this[y * width + x] = data(x, y)
                })

        operator fun get(x: Int, y: Int): Boolean = data[y * width + x]

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Bits

            if (width != other.width) return false
            if (height != other.height) return false
            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = width
            result = 31 * result + height
            result = 31 * result + data.contentHashCode()
            return result
        }
    }

    operator fun get(x: Int, y: Int): Boolean = bits[x, y]
}

@Serializable
data class LoyaltyCardData(
    @CborLabel(0) val id: Uuid,
    @CborLabel(1) val label: String,
    @CborLabel(2) val code: Code,
)