package `in`.procyk.bring.vm

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.viewModelScope
import `in`.procyk.bring.LoyaltyCardData
import `in`.procyk.bring.LoyaltyCardRpcPath
import `in`.procyk.bring.service.LoyaltyCardService
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.Uuid

internal class LoyaltyCardsViewModel(context: Context) : AbstractViewModel(context) {

    data class Card(
        val data: LoyaltyCardData,
        val code: ByteArray
    ) {
        override fun hashCode(): Int = data.id.hashCode()
        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other == null || this::class != other::class -> false
            else -> (other as? Card)?.let { it.data.id == data.id } == true
        }
    }

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
    val isLoadingCards: StateFlow<Boolean> = _isLoadingCards.debounce {
        if (!it) 200.milliseconds else Duration.ZERO
    }.state(false)

    init {
        viewModelScope.launch {
            val cache = mutableMapOf<Pair<Uuid, Color>, Card>()
            combine(
                context.storeFlow.map { it.loyaltyCardsIds },
                codeColor.filterNotNull(),
            ) { a, b -> a to b }.distinctUntilChanged().collectLatest { (cardIds, color) ->
                _isLoadingCards.value = true
                try {
                    _cards.value = cardIds.mapNotNull { cardId ->
                        val key = cardId to color
                        cache[key] ?: run {
                            val card = loyaltyCardService.durableCall { getLoyaltyCard(cardId) }
                                .leftOrNull()
                                ?: return@run null
                            val code = loyaltyCardService.durableCall {
                                generateCode(
                                    code = card.code,
                                    color = color.toArgb(),
                                    width = 800,
                                    height = 400
                                )
                            }
                                .leftOrNull()
                                ?: return@run null
                            Card(card, code)
                        }?.also { cache[key] = it }
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
                        .sortedBy { it.index }
                        .map { it.value }
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
            val file = FileKit.openFilePicker(type = FileKitType.File("png", "PNG")) ?: return@launch
            val image = file.readBytes()
            loyaltyCardService
                .durableCall { createLoyaltyCard(label, image, userId) }
                .fold(
                    ifLeft = { cardId -> updateConfig { it.copy(loyaltyCardsIds = it.loyaltyCardsIds + cardId) } },
                    ifRight = { /* TODO: handle errors */ }
                )
        }
    }

    fun addLoyaltyCardByCardId() {
        val cardIds = userInput.value
        val cardUuids = cardIds.split(CARD_ID_SEPARATOR).mapNotNull { runCatching { Uuid.parse(it) }.getOrNull() }
        updateConfig { it.copy(loyaltyCardsIds = it.loyaltyCardsIds + cardUuids) }
    }

    fun resetUserInput() {
        onUserInputChange("")
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
            val cardIds = cards.value.joinToString(separator = CARD_ID_SEPARATOR) { it.data.id.toHexDashString() }
            onShareLoyaltyCard(cardIds, context)
        }
    }
}

private const val CARD_ID_SEPARATOR = ";"