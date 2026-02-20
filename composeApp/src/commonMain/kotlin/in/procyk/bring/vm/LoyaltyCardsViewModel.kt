package `in`.procyk.bring.vm

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.viewModelScope
import `in`.procyk.bring.LoyaltyCard
import `in`.procyk.bring.LoyaltyCardData
import `in`.procyk.bring.LoyaltyCardRpcPath
import `in`.procyk.bring.service.LoyaltyCardService
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

internal class LoyaltyCardsViewModel(context: Context) : AbstractViewModel(context) {

    data class Card(
        val data: LoyaltyCardData,
        val code: ByteArray,
        val order: Double,
        val color: Color?,
    )

    private val loyaltyCardService = durableRpcService<LoyaltyCardService>(LoyaltyCardRpcPath)

    private val _codeColor: MutableStateFlow<Color?> = MutableStateFlow(null)
    val codeColor: StateFlow<Color?> = _codeColor.asStateFlow()

    private val _cards: MutableStateFlow<List<Card>> = MutableStateFlow(emptyList())
    val cards: StateFlow<List<Card>> = _cards.asStateFlow()

    private val _userInput: MutableStateFlow<String> = MutableStateFlow("")
    val userInput: StateFlow<String> = _userInput.asStateFlow()

    private val _selectedCard: MutableStateFlow<Card?> = MutableStateFlow(null)
    val selectedCard: StateFlow<Card?> = _selectedCard.asStateFlow()

    private val _isLoadingCards: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoadingCards: StateFlow<Boolean> = _isLoadingCards.asStateFlow()

    init {
        viewModelScope.launch {
            val cache = mutableMapOf<Pair<LoyaltyCard, Color>, Card>()
            combine(
                context.storeFlow.map { it.loyaltyCards },
                codeColor.filterNotNull(),
            ) { a, b -> a to b }.distinctUntilChanged().collectLatest { (cards, color) ->
                _isLoadingCards.value = true
                try {
                    val sortedCards = cards.mapNotNull { card ->
                        val key = card to color
                        cache[key] ?: run {
                            val cardData =
                                loyaltyCardService.durableCall { getLoyaltyCard(card.cardId) }
                                    .leftOrNull()
                                    ?: return@run null
                            val code = loyaltyCardService.durableCall {
                                generateCode(
                                    code = cardData.code,
                                    color = color.toArgb(),
                                    width = 800,
                                    height = 400
                                )
                            }
                                .leftOrNull()
                                ?: return@run null
                            Card(
                                data = cardData,
                                code = code,
                                order = card.order,
                                color = card.color?.let(::Color)
                            )
                        }?.also { cache[key] = it }
                    }
                        .withIndex()
                        .groupBy { it.value.data.label }
                        .flatMap { (_, indexedCards) ->
                            when {
                                indexedCards.size <= 1 -> indexedCards
                                else -> indexedCards.mapIndexed { innerIdx, indexedCard ->
                                    indexedCard.copy(
                                        value = indexedCard.value.copy(
                                            data = indexedCard.value.data.copy(
                                                label = "${indexedCard.value.data.label} #${innerIdx + 1}"
                                            )
                                        )
                                    )
                                }
                            }
                        }
                        .sortedByDescending { it.value.order }
                        .map { it.value }
                    _cards.value = sortedCards
                    resetUserInput()
                } finally {
                    _isLoadingCards.value = false
                }
            }
        }
    }

    fun addLoyaltyCardFromFile(afterAdded: () -> Unit = {}) {
        val userId = store.userId
        val label = userInput.value
        viewModelScope.launch {
            val file =
                FileKit.openFilePicker(type = FileKitType.File("png", "PNG")) ?: return@launch
            val image = file.readBytes()
            loyaltyCardService
                .durableCall { createLoyaltyCard(label, image, userId) }
                .fold(
                    ifLeft = { updateConfigWithLoyaltyCardIds(listOf(it)) },
                    ifRight = { /* TODO: handle errors */ }
                )
            afterAdded()
        }
    }

    fun addLoyaltyCardByCardId(afterAdded: () -> Unit = {}) {
        val cardIds = userInput.value
        val cardUuids = cardIds.split(CARD_ID_SEPARATOR)
            .mapNotNull { runCatching { Uuid.parse(it) }.getOrNull() }
        updateConfigWithLoyaltyCardIds(cardUuids)
        afterAdded()
    }

    private fun updateConfigWithLoyaltyCardIds(cardIds: List<Uuid>) {
        updateConfig { config ->
            val maxOrder = config.loyaltyCards.maxOfOrNull { it.order } ?: 0.0
            val prevCardIds = config.loyaltyCards.map { it.cardId }
            config.copy(
                loyaltyCards = config.loyaltyCards + cardIds
                    .filter { it !in prevCardIds }
                    .mapIndexed { idx, cardId -> LoyaltyCard(cardId, maxOrder + 1.0 + idx) }
            )
        }
    }

    fun resetUserInput() {
        onUserInputChange("")
    }

    fun removeCard(card: Card) {
        unselectCard()
        updateConfig { it.copy(loyaltyCards = it.loyaltyCards.filter { it.cardId != card.data.id }) }
    }

    fun onUserInputChange(value: String) {
        _userInput.update { value }
    }

    fun unselectCard() {
        _selectedCard.update { null }
    }

    fun selectCard(card: Card) {
        _selectedCard.update { card }
    }

    fun updateCodeColor(color: Color?) {
        _codeColor.update { color }
    }

    fun shareCard(card: Card) {
        viewModelScope.launch {
            onShareLoyaltyCard(card.data.id.toHexDashString(), context)
        }
    }

    fun shareAllCards() {
        viewModelScope.launch {
            val cardIds =
                cards.value.joinToString(separator = CARD_ID_SEPARATOR) { it.data.id.toHexDashString() }
            onShareLoyaltyCard(cardIds, context)
        }
    }

    fun onUpdatedItemOrder(fromKey: Uuid, toKey: Uuid, cards: List<Card>) {
        val (fromIndex, from) = cards.withIndex()
            .firstOrNull { (_, item) -> item.data.id == fromKey } ?: return
        val (toIndex, to) = cards.withIndex().firstOrNull { (_, item) -> item.data.id == toKey }
            ?: return
        val relativeOrder = when {
            toIndex < fromIndex -> cards.getOrNull(toIndex - 1)?.order ?: (to.order + 1.0)
            toIndex > fromIndex -> cards.getOrNull(toIndex + 1)?.order ?: (to.order - 1.0)
            else -> return
        }
        val updatedOrder = (to.order + relativeOrder) / 2.0
        _cards.update { cards ->
            cards.map { if (it.data.id == from.data.id) it.copy(order = updatedOrder) else it }
                .sortedByDescending { it.order }
        }
        updateConfig {
            it.copy(loyaltyCards = it.loyaltyCards.map { if (it.cardId == from.data.id) it.copy(order = updatedOrder) else it })
        }
    }

    fun cardColor(card: Card): StateFlow<Color> =
        storeFlow
            .map { it.loyaltyCards.find { it.cardId == card.data.id }?.color?.let(::Color) ?: Color.Unspecified }
            .state(Color.Unspecified)

    fun onCardColorUpdated(cardId: Uuid, color: Color?) {
        updateConfig { it.copy(loyaltyCards = it.loyaltyCards.map { if (it.cardId == cardId) it.copy(color = color?.toArgb()) else it }) }
    }
}

private const val CARD_ID_SEPARATOR = ";"