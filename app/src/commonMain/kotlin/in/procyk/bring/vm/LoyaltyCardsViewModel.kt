package `in`.procyk.bring.vm

import androidx.compose.ui.graphics.Color
import arrow.core.Either
import bring.app.generated.resources.Res
import bring.app.generated.resources.loading_read_from_file
import bring.app.generated.resources.loading_scanning
import `in`.procyk.bring.*
import `in`.procyk.bring.service.LoyaltyCardService
import `in`.procyk.bring.service.LoyaltyCardService.GetLoyaltyCardError
import `in`.procyk.bring.service.LoyaltyCardService.RemoveLoyaltyCardError
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.flow.*
import org.jetbrains.compose.resources.getString
import kotlin.uuid.Uuid

internal class LoyaltyCardsViewModel(
    context: Context,
) : ImportableCollectionViewModel<LoyaltyCard, LoyaltyCardData, LoyaltyCardsViewModel.Card>(context) {

    data class Card(
        val data: LoyaltyCardData,
        override val order: Double,
        val color: Color?,
    ) : Orderable, Identifiable {
        override val id get() = data.id
    }

    private val loyaltyCardService = durableRpcService<LoyaltyCardService>(LoyaltyCardRpcPath)

    private val _selectedCard: MutableStateFlow<Card?> = MutableStateFlow(null)
    val selectedCard: StateFlow<Card?> = _selectedCard.asStateFlow()

    fun selectCard(card: Card) {
        _selectedCard.update { card }
    }

    fun unselectCard() {
        _selectedCard.update { null }
    }

    val cards: StateFlow<List<Card>> get() = items

    fun removeCard(card: Card) {
        unselectCard()
        removeItem(card)
    }

    fun shareCard(card: Card) = shareItem(card)
    fun cardColor(card: Card) = itemColor(card)
    fun onCardColorUpdated(cardId: Uuid, color: Color?) = onItemColorUpdated(cardId, color)

    override fun BringStore.storedItems(): List<LoyaltyCard> = loyaltyCards
    override fun BringStore.withStoredItems(items: List<LoyaltyCard>): BringStore = copy(loyaltyCards = items)
    override fun cachedData(stored: LoyaltyCard): LoyaltyCardData? = stored.cachedData
    override fun withCachedData(stored: LoyaltyCard, data: LoyaltyCardData?): LoyaltyCard = stored.copy(cachedData = data)
    override fun color(stored: LoyaltyCard): Int? = stored.color
    override fun withColor(stored: LoyaltyCard, color: Int?): LoyaltyCard = stored.copy(color = color)

    override val useCacheStored: BringStore.() -> Boolean = { useCardsCache }
    override val enableEditModeStored: BringStore.() -> Boolean = { enableCardsEditMode }
    override val showLabelsStored: BringStore.() -> Boolean = { showCardsLabels }

    override suspend fun fetchData(stored: LoyaltyCard): Either<LoyaltyCardData, FetchError> =
        loyaltyCardService.durableCall { getLoyaltyCard(stored.cardId) }.mapRight { err ->
            when (err) {
                GetLoyaltyCardError.Internal -> FetchError.Internal
                GetLoyaltyCardError.UnknownCardId -> FetchError.UnknownId
            }
        }

    override fun buildItem(stored: LoyaltyCard, data: LoyaltyCardData): Card =
        Card(data = data, order = stored.order, color = stored.color?.let(::Color))

    override fun postProcessItems(items: List<Card>): List<Card> =
        items.asSequence()
            .withIndex()
            .groupBy { it.value.data.label }
            .flatMap { (_, indexed) ->
                when {
                    indexed.size <= 1 -> indexed
                    else -> indexed.mapIndexed { innerIdx, ic ->
                        ic.copy(
                            value = ic.value.copy(
                                data = ic.value.data.copy(label = "${ic.value.data.label} #${innerIdx + 1}")
                            )
                        )
                    }
                }
            }
            .sortedBy { it.index }
            .map { it.value }

    override fun newStored(id: Uuid, order: Double): LoyaltyCard = LoyaltyCard(cardId = id, order = order)

    override suspend fun createFromFile(label: String): List<Uuid> {
        val userId = store.userId
        val file = FileKit.openFilePicker(type = SUPPORTED_IMAGE_FORMATS) ?: return emptyList()
        val image = file.readBytes()
        updateDialogActionLoading(getString(Res.string.loading_scanning))
        return loyaltyCardService
            .durableCall { createLoyaltyCard(label, image, userId) }
            .fold(ifLeft = { listOf(it) }, ifRight = { /* TODO: handle errors */ emptyList() })
    }

    override suspend fun removeRemote(item: Card): Either<Unit, RemoveError> =
        loyaltyCardService.durableCall { removeLoyaltyCard(item.data.id, store.userId) }
            .mapRight { err ->
                when (err) {
                    RemoveLoyaltyCardError.Internal -> RemoveError.Internal
                    RemoveLoyaltyCardError.UnknownCardId -> RemoveError.UnknownId
                }
            }

    override suspend fun share(ids: String) = onShareLoyaltyCard(ids, context)

    override fun replaceOrder(item: Card, order: Double): Card = item.copy(order = order)
    override fun replaceStoredOrder(stored: LoyaltyCard, order: Double): LoyaltyCard = stored.copy(order = order)

    init {
        updateStoredItemsInBackground()
    }
}

private val SUPPORTED_IMAGE_FORMATS = FileKitType.File("png", "PNG", "jpg", "JPG", "jpeg", "JPEG")

/** Bridges `Either<A, E>` to `Either<A, E2>` by mapping the right side. */
private inline fun <A, E, E2> Either<A, E>.mapRight(transform: (E) -> E2): Either<A, E2> =
    fold(ifLeft = { Either.Left(it) }, ifRight = { Either.Right(transform(it)) })
