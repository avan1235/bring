package `in`.procyk.bring.code

import `in`.procyk.bring.Code
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.random.Random
import kotlin.streams.asStream
import kotlin.test.assertEquals

class CodeBitsSerializationTest {

    @ParameterizedTest(name = "{index} width={0} height={1} data={2}")
    @MethodSource("randomBitsSource")
    fun `random bits deserialize to same data`(
        width: Int,
        height: Int,
        data: BooleanArray,
    ) {
        val expected = Code.Bits(width, height, data)
        val actual = expected.serialize().deserialize()
        assertEquals(expected, actual)
    }

    companion object {

        private const val MAX_WIDTH = 1_000
        private const val MAX_HEIGHT = 1_000
        private const val TEST_COUNT = 10_000

        @JvmStatic
        fun randomBitsSource(): Stream<Arguments> = generateSequence {
            val width = (1..MAX_WIDTH).random()
            val height = (1..MAX_HEIGHT).random()
            val data = BooleanArray(width * height) { Random.nextBoolean() }
            Arguments.of(width, height, data)
        }.take(TEST_COUNT).asStream()
    }
}