package `in`.procyk.bring.vm

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.viewModelScope
import `in`.procyk.bring.*
import `in`.procyk.bring.service.LoyaltyCardService
import `in`.procyk.bring.service.LoyaltyCardService.GetLoyaltyCardError
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

internal class LoyaltyCardsViewModel(context: Context) : AbstractViewModel(context) {

    enum class InputDialogAction {
        AddFromFile, ImportById, Loading,
    }

    data class Card(
        val data: LoyaltyCardData,
        override val order: Double,
        val color: Color?,
    ) : Orderable, Identifiable {
        override val id get() = data.id
    }

    private val loyaltyCardService = durableRpcService<LoyaltyCardService>(LoyaltyCardRpcPath)

    private val _dialogAction: MutableStateFlow<InputDialogAction?> = MutableStateFlow(null)
    val dialogAction: StateFlow<InputDialogAction?> = _dialogAction.asStateFlow()

    private val _cards: MutableStateFlow<List<Card>> = MutableStateFlow(emptyList())
    val cards: StateFlow<List<Card>> = _cards.asStateFlow()

    private val _userInput: MutableStateFlow<String> = MutableStateFlow("")
    val userInput: StateFlow<String> = _userInput.asStateFlow()

    private val _selectedCard: MutableStateFlow<Card?> = MutableStateFlow(null)
    val selectedCard: StateFlow<Card?> = _selectedCard.asStateFlow()

    private val _isLoadingCards: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoadingCards: StateFlow<Boolean> = _isLoadingCards.asStateFlow()

    val enableEditMode: StateFlow<Boolean> = storeFlow.map { it.enableCardsEditMode }.state(store.enableCardsEditMode)

    val showCardsLabels: StateFlow<Boolean> = storeFlow.map { it.showCardsLabels }.state(store.showCardsLabels)

    init {
        viewModelScope.launch {
            val inMemoryCache = mutableMapOf<Uuid, Card>()
            context.storeFlow.map { it.loyaltyCards }.distinctUntilIdsChanged().collectLatest { cards ->
                _isLoadingCards.value = true
                val useCardsCache = store.useCardsCache
                try {
                    val sortedCards = cards.mapNotNull { card ->
                        val key = card.cardId
                        inMemoryCache[key] ?: run {
                            val cachedCardData = if (useCardsCache) card.cachedData else null
                            val cardData =
                                cachedCardData ?: loyaltyCardService.durableCall { getLoyaltyCard(card.cardId) }
                                    .fold(
                                        ifLeft = { it },
                                        ifRight = {
                                            when (it) {
                                                GetLoyaltyCardError.Internal -> {
                                                    /* TODO: handle errors */
                                                }

                                                GetLoyaltyCardError.UnknownCardId -> updateConfig { store ->
                                                    store.copy(loyaltyCards = store.loyaltyCards.filter { it.cardId != card.cardId })
                                                }
                                            }
                                            return@run null
                                        }
                                    )
                            updateConfig { store ->
                                store.copy(loyaltyCards = store.loyaltyCards.map {
                                    if (it.cardId == key) it.copy(cachedData = if (useCardsCache) cardData else null) else it
                                })
                            }
                            Card(
                                data = cardData,
                                order = card.order,
                                color = card.color?.let(::Color)
                            )
                        }?.also { inMemoryCache[key] = it }
                    }
                        .asSequence()
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
                        .sortedBy { it.value }
                        .map { it.value }
                    _cards.value = sortedCards
                    resetUserInput()
                } finally {
                    _isLoadingCards.value = false
                }
            }
        }
    }

    fun addLoyaltyCardFromFile() {
        val userId = store.userId
        val label = userInput.value
        viewModelScope.launch {
            val file =
                FileKit.openFilePicker(type = SUPPORTED_IMAGE_FORMATS) ?: return@launch
            val image = file.readBytes()
            startDialogActionLoading()
            loyaltyCardService
                .durableCall { createLoyaltyCard(label, image, userId) }
                .fold(
                    ifLeft = { updateConfigWithLoyaltyCardIds(listOf(it)) },
                    ifRight = { /* TODO: handle errors */ }
                )
        }.invokeOnCompletion { closeDialogAction() }
    }

    fun addLoyaltyCardByCardId() {
        try {
            startDialogActionLoading()
            val cardIds = userInput.value
            val cardUuids = cardIds.split(CARD_ID_SEPARATOR)
                .mapNotNull { runCatching { Uuid.parse(it) }.getOrNull() }
            updateConfigWithLoyaltyCardIds(cardUuids)
        } finally {
            closeDialogAction()
        }
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
        viewModelScope.launch {
            loyaltyCardService
                .durableCall { removeLoyaltyCard(card.data.id, store.userId) }
                .onRight { /* TODO: handle errors */ }
        }
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
        onUpdatedItemOrder(fromKey, toKey, cards) { from, updatedOrder ->
            _cards.update { cards ->
                cards.map { if (it.id == from.id) it.copy(order = updatedOrder) else it }.sorted()
            }
            updateConfig {
                it.copy(loyaltyCards = it.loyaltyCards.map { if (it.cardId == from.data.id) it.copy(order = updatedOrder) else it })
            }
        }
    }

    fun cardColor(card: Card): StateFlow<Color> =
        storeFlow
            .map { it.loyaltyCards.find { it.cardId == card.data.id }?.color?.let(::Color) ?: Color.Unspecified }
            .state(Color.Unspecified)

    fun onCardColorUpdated(cardId: Uuid, color: Color?) {
        updateConfig { it.copy(loyaltyCards = it.loyaltyCards.map { if (it.cardId == cardId) it.copy(color = color?.toArgb()) else it }) }
    }

    fun openAddFromFileDialog() {
        _dialogAction.update { InputDialogAction.AddFromFile }
    }

    fun openImportByIdDialog() {
        _dialogAction.update { InputDialogAction.ImportById }
    }

    fun startDialogActionLoading() {
        _dialogAction.update { InputDialogAction.Loading }
    }

    fun closeDialogAction() {
        _dialogAction.update { null }
    }
}

private const val CARD_ID_SEPARATOR = ";"

private val SUPPORTED_IMAGE_FORMATS = FileKitType.File("png", "PNG", "jpg", "JPG", "jpeg", "JPEG")