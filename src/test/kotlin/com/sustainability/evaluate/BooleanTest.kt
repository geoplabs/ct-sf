package com.sustainability.evaluate

import com.sustainability.service.CalculationService
import com.sustainability.service.EmissionFactorsService
import com.sustainability.service.GwpService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import java.math.BigDecimal
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BooleanTest {
    // Mocks for the three services
    private val gwpService: GwpService = mock(GwpService::class.java)
    private val calculationService: CalculationService = mock(CalculationService::class.java)
    private val emissionFactorsService: EmissionFactorsService = mock(EmissionFactorsService::class.java)
    private lateinit var calculatorBaseVisitor: CalculatorBaseVisitorImpl

    @BeforeEach
    fun setUp() {
        calculatorBaseVisitor = CalculatorBaseVisitorImpl(
            gwpService,
            calculationService,
            emissionFactorsService,
        )
    }

    private fun providerForBooleanSuccess(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(
                ":left",
                mutableMapOf("left" to BooleanTypeValue(true)),
                true,
            ),
            Arguments.of(
                ":left<:right",
                mutableMapOf("left" to NumberTypeValue(BigDecimal(2)), "right" to NumberTypeValue(BigDecimal(3))),
                true,
            ),
            Arguments.of(
                ":left<=:right",
                mutableMapOf("left" to NumberTypeValue(BigDecimal(3)), "right" to NumberTypeValue(BigDecimal(3))),
                true,
            ),
            Arguments.of(
                ":left>:right",
                mutableMapOf("left" to NumberTypeValue(BigDecimal(2)), "right" to NumberTypeValue(BigDecimal(3))),
                false,
            ),
            Arguments.of(
                ":left>=:right",
                mutableMapOf("left" to NumberTypeValue(BigDecimal(2)), "right" to NumberTypeValue(BigDecimal(3))),
                false,
            ),
            Arguments.of(
                ":left==:right",
                mutableMapOf("left" to NumberTypeValue(BigDecimal(2)), "right" to NumberTypeValue(BigDecimal(3))),
                false,
            ),
            Arguments.of(
                ":left!=:right",
                mutableMapOf("left" to NumberTypeValue(BigDecimal(2)), "right" to NumberTypeValue(BigDecimal(3))),
                true,
            ),
        )
    }

    @ParameterizedTest
    @MethodSource("providerForBooleanSuccess")
    fun `test evaluateExpression with valid expression with boolean`(
        expression: String,
        variables: MutableMap<String, DynamicTypeValue<*>>,
        expected: Boolean,
    ) {
        val evaluateRequest = EvaluateRequest(expression, variables)
        val response = calculatorBaseVisitor.evaluateExpression(evaluateRequest)
        assertNotNull(response)
        assertEquals(expected, (response?.result as BooleanTypeValue).getValue)
    }
}
