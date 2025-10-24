package com.sustainability.evaluate

import com.sustainability.service.CalculationService
import com.sustainability.service.EmissionFactorsService
import com.sustainability.service.GwpService
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import java.math.RoundingMode
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArithmeticTest {
    // 1) declare mocks
    private val gwpService = mock(GwpService::class.java)
    private val calculationService = mock(CalculationService::class.java)
    private val emissionFactorsService = mock(EmissionFactorsService::class.java)
    private lateinit var calculatorBaseVisitor: CalculatorBaseVisitorImpl

    @BeforeEach
    fun setUp() {
        calculatorBaseVisitor = CalculatorBaseVisitorImpl(
            gwpService,
            calculationService,
            emissionFactorsService,
        )
    }

    private fun providerForSuccess(): Stream<Arguments> {
        return Stream.of(
            Arguments.of("1+2", NumberTypeValue(3)),
            Arguments.of("1-2", NumberTypeValue(-1)),
            Arguments.of("6/2", NumberTypeValue(3)),
            Arguments.of("2*4", NumberTypeValue(8)),
            Arguments.of("2*-4", NumberTypeValue(-8)),
            Arguments.of("2^2", NumberTypeValue(4)),
            Arguments.of("1+2+3+4", NumberTypeValue(10)),
            Arguments.of("1-2-3-4", NumberTypeValue(-8)),
            Arguments.of("16/4/2", NumberTypeValue(2)),
            Arguments.of("2*4*3", NumberTypeValue(24)),
            Arguments.of("-2*4*3", NumberTypeValue(-24)),
            Arguments.of(
                "(1+(3*2)/(4-2)*(8+(4/3.5)))^2",
                NumberTypeValue(808.1836734766978906918666325509548187255859375),
            ),
        )
    }

    @ParameterizedTest
    @MethodSource("providerForSuccess")
    fun `test evaluateExpression with valid expression`(expression: String, expected: DynamicTypeValue<*>) {
        val evaluateRequest = EvaluateRequest(expression, mutableMapOf())
        val response = calculatorBaseVisitor.evaluateExpression(evaluateRequest)
        assertTrue(response?.result is NumberTypeValue)
        val actualValue = (response?.result as NumberTypeValue).getValue().setScale(4, RoundingMode.HALF_UP)
        val expectedValue = (expected as NumberTypeValue).getValue().setScale(4, RoundingMode.HALF_UP)
        assertEquals(expectedValue, actualValue)
    }

    private fun providerForFailed(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(
                "1+2+",
                "Line 1:4 mismatched input '<EOF>' expecting {AS_TIMESTAMP, ASSIGN_TO_GROUP, GET_VALUE, COALESCE, " +
                    "CONCAT, CONVERT, IF, IMPACT, LOOKUP, LOWERCASE, REF, CAML, SET, SPLIT, SWITCH, UPPERCASE, " +
                    "SEARCH, BOOLEAN, NULL, CUSTOM_FUNCTION, TOKEN, QUOTED_STRING, NUMBER, " +
                    "SCIENTIFIC_NUMBER, '(', '-', ' '}",
            ),
            Arguments.of(
                "1+2+++5",
                "Line 1:4 mismatched input '+' expecting {AS_TIMESTAMP, ASSIGN_TO_GROUP, GET_VALUE, COALESCE, " +
                    "CONCAT, CONVERT, IF, IMPACT, LOOKUP, LOWERCASE, REF, CAML, SET, SPLIT, SWITCH, " +
                    "UPPERCASE, SEARCH, BOOLEAN, NULL, CUSTOM_FUNCTION, TOKEN, QUOTED_STRING, NUMBER, " +
                    "SCIENTIFIC_NUMBER, '(', '-', ' '}",
            ),
            Arguments.of(
                "/3",
                "Line 1:0 extraneous input '/' expecting {AS_TIMESTAMP, ASSIGN_TO_GROUP, GET_VALUE, COALESCE, " +
                    "CONCAT, CONVERT, IF, IMPACT, LOOKUP, LOWERCASE, REF, CAML, SET, SPLIT, SWITCH, " +
                    "UPPERCASE, SEARCH, BOOLEAN, NULL, CUSTOM_FUNCTION, TOKEN, QUOTED_STRING, NUMBER, " +
                    "SCIENTIFIC_NUMBER, '(', '-', ' '}",
            ),
            Arguments.of(
                "1+-/2",
                "Line 1:3 extraneous input '/' expecting {AS_TIMESTAMP, ASSIGN_TO_GROUP, GET_VALUE, COALESCE, " +
                    "CONCAT, CONVERT, IF, IMPACT, LOOKUP, LOWERCASE, REF, CAML, SET, SPLIT, SWITCH, " +
                    "UPPERCASE, SEARCH, BOOLEAN, NULL, CUSTOM_FUNCTION, TOKEN, QUOTED_STRING, NUMBER, " +
                    "SCIENTIFIC_NUMBER, '(', '-', ' '}",
            ),
        )
    }

    @ParameterizedTest
    @MethodSource("providerForFailed")
    fun `test evaluateExpression with invalid expression`(expression: String, expected: String) {
        val evaluateRequest = EvaluateRequest(expression, mutableMapOf())
        val exception: Exception = Assertions.assertThrows<ParseCancellationException>(
            ParseCancellationException::class.java,
        ) {
            calculatorBaseVisitor.evaluateExpression(evaluateRequest)
        }
        assertEquals(expected, exception.message)
    }
}
