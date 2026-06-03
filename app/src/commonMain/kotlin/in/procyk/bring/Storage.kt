package `in`.procyk.bring

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.ContextClick
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.ToggleOff
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.ToggleOn
import androidx.compose.ui.platform.LocalHapticFeedback
import `in`.procyk.bring.ui.Theme
import `in`.procyk.bring.ui.defaultUseBottomNavigation
import io.github.xxfast.kstore.Codec
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.CborLabel
import kotlin.uuid.Uuid

@Serializable
data class BringStore(
    @CborLabel(0) val userId: Uuid = Uuid.random(),
    @CborLabel(1) val enableShoppingListEditMode: Boolean = true,
    @CborLabel(2) val showUncheckedFirst: Boolean = false,
    @CborLabel(3) val showSuggestions: Boolean = true,
    @CborLabel(4) val lastListId: String? = null,
    @CborLabel(5) val favoriteShoppingLists: Set<FavoriteShoppingList> = emptySet(),
    @CborLabel(7) val showFavoriteElements: Boolean = true,
    @CborLabel(8) val themeColor: Int = Color.White.toArgb(),
    @CborLabel(9) val darkMode: Theme = Theme.System,
    @CborLabel(14) val geminiKey: String = "",
    @CborLabel(10) val useGemini: Boolean = false,
    @CborLabel(11) val useHaptics: Boolean = true,
    @CborLabel(12) val useBottomNavigation: Boolean = defaultUseBottomNavigation,
    @CborLabel(17) val loyaltyCards: List<LoyaltyCard> = emptyList(),
    @CborLabel(18) val enableCardsEditMode: Boolean = true,
    @CborLabel(19) val showCardsLabels: Boolean = true,
    @CborLabel(20) val useCardsCache: Boolean = true,
    @CborLabel(21) val recentShoppingLists: Set<RecentShoppingList> = emptySet(),
    @CborLabel(22) val recentShoppingListsCount: Int = 5,
) {
    companion object {
        val Default: BringStore = BringStore()
    }

    @Composable
    inline fun onClickWithHaptics(
        crossinline onClick: () -> Unit,
        type: HapticFeedbackType = ContextClick,
    ): () -> Unit {
        val hapticFeedback = LocalHapticFeedback.current
        return when {
            useHaptics -> fun() {
                onClick()
                hapticFeedback.performHapticFeedback(type)
            }

            else -> fun() { onClick() }
        }
    }

    @Composable
    inline fun onToggleWithHaptics(
        crossinline onToggle: (Boolean) -> Unit,
    ): (Boolean) -> Unit {
        val hapticFeedback = LocalHapticFeedback.current
        return when {
            useHaptics -> fun(value: Boolean) {
                onToggle(value)
                hapticFeedback.performHapticFeedback(if (value) ToggleOn else ToggleOff)
            }

            else -> fun(value: Boolean) { onToggle(value) }
        }
    }
}

val LocalBringStore: ProvidableCompositionLocal<BringStore> =
    staticCompositionLocalOf { BringStore.Default }

sealed class SavedShoppingList {
    abstract val listName: String
    abstract val listId: Uuid

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SavedShoppingList

        return listId == other.listId
    }

    override fun hashCode(): Int =
        listId.hashCode()
}

@Serializable
data class FavoriteShoppingList(
    @CborLabel(0) override val listName: String,
    @CborLabel(1) override val listId: Uuid,
) : SavedShoppingList()

@Serializable
data class RecentShoppingList(
    @CborLabel(0) override val listName: String,
    @CborLabel(1) override val listId: Uuid,
) : SavedShoppingList()

@Serializable
data class LoyaltyCard(
    @CborLabel(0) val cardId: Uuid,
    @CborLabel(1) override val order: Double,
    @CborLabel(2) val color: Int? = null,
    @CborLabel(3) val cachedData: LoyaltyCardData? = null,
) : Orderable, Identifiable {
    override val id get() = cardId
}

@Composable
internal expect inline fun <reified T : @Serializable Any> bringCodec(): Codec<T>
