package `in`.procyk.bring

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import `in`.procyk.bring.ui.Theme
import io.github.xxfast.kstore.Codec
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.CborLabel
import kotlin.uuid.Uuid

@Serializable
data class BringStore(
    @CborLabel(0) val userId: Uuid = Uuid.random(),
    @CborLabel(1) val enableEditMode: Boolean = true,
    @CborLabel(2) val showUncheckedFirst: Boolean = false,
    @CborLabel(3) val showSuggestions: Boolean = true,
    @CborLabel(4) val lastListId: String? = null,
    @CborLabel(5) val favoriteShoppingLists: Set<FavoriteShoppingList> = emptySet(),
    @CborLabel(7) val showFavoriteElements: Boolean = true,
    @CborLabel(8) val themeColor: Int = Color.White.toArgb(),
    @CborLabel(9) val darkMode: Theme = Theme.System,
) {
    companion object {
        val Default: BringStore = BringStore()
    }
}

@Serializable
data class FavoriteShoppingList(
    @CborLabel(0) val listName: String,
    @CborLabel(1) val listId: Uuid,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as FavoriteShoppingList

        return listId == other.listId
    }

    override fun hashCode(): Int =
        listId.hashCode()
}

@Composable
expect inline fun <reified T : @Serializable Any> bringCodec(): Codec<T>
