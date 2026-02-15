package `in`.procyk.bring.vm

import `in`.procyk.bring.CardData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal class CardsViewModel(context: Context) : AbstractViewModel(context) {

    private val _cards: MutableStateFlow<List<CardData>> = MutableStateFlow(emptyList())
    val cards: StateFlow<List<CardData>> = _cards.asStateFlow()

    init {
        _cards.update {
            listOf(
                CardData(Uuid.random(), "Card 1", Uuid.random(), Clock.System.now(), 1),
                CardData(Uuid.random(), "Card 2", Uuid.random(), Clock.System.now(), 2),
                CardData(Uuid.random(), "Card 3", Uuid.random(), Clock.System.now(), 3)
            )
        }
    }

}