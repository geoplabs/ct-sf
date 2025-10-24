package com.sustainability.evaluate

import com.sustainability.service.CalculationService
import com.sustainability.service.EmissionFactorsService
import com.sustainability.service.GwpService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import java.math.BigDecimal
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VariablesTest {
    // Mocks for the three services
    private val gwpService: GwpService = mock(GwpService::class.java)
    private val calculationService: CalculationService = mock(CalculationService::class.java)
    private val emissionFactorsService: EmissionFactorsService = mock(EmissionFactorsService::class.java)
    private lateinit var calculatorBaseVisitor: CalculatorBaseVisitorImpl

    @BeforeEach
    fun setUp() {
        // Inject mocks into the visitor under test
        calculatorBaseVisitor = CalculatorBaseVisitorImpl(
            gwpService,
            calculationService,
            emissionFactorsService,
        )
    }

    private fun providerForVariablesSuccess(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(
                ":left+:right",
                mutableMapOf(
                    "left" to NumberTypeValue(BigDecimal(2)),
                    "right" to NumberTypeValue(BigDecimal(3)),
                ),
                5,
            ),
            Arguments.of(
                ":left-:right",
                mutableMapOf(
                    "left" to NumberTypeValue(BigDecimal(2)),
                    "right" to NumberTypeValue(BigDecimal(3)),
                ),
                -1,
            ),
            Arguments.of(
                ":left^:right",
                mutableMapOf(
                    "left" to NumberTypeValue(BigDecimal(2)),
                    "right" to NumberTypeValue(BigDecimal(3)),
                ),
                8,
            ),
            Arguments.of(
                ":left/:right",
                mutableMapOf(
                    "left" to NumberTypeValue(BigDecimal(6)),
                    "right" to NumberTypeValue(BigDecimal(3)),
                ),
                2,
            ),
            Arguments.of(
                ":left*:right",
                mutableMapOf(
                    "left" to NumberTypeValue(BigDecimal(2)),
                    "right" to NumberTypeValue(BigDecimal(3)),
                ),
                6,
            ),
            Arguments.of(
                ":left",
                mutableMapOf("left" to NumberTypeValue(BigDecimal(-3))),
                -3,
            ),
            Arguments.of(
                ":left*3",
                mutableMapOf("left" to NumberTypeValue(BigDecimal(-3))),
                -9,
            ),
        )
    }

    @ParameterizedTest
    @MethodSource("providerForVariablesSuccess")
    fun `test evaluateExpression with valid expression with variables and addition`(
        expression: String,
        variables: MutableMap<String, DynamicTypeValue<*>>,
        expected: Int,
    ) {
        val evaluateRequest = EvaluateRequest(expression, variables)
        val response = calculatorBaseVisitor.evaluateExpression(evaluateRequest)
        assertNotNull(response)
        assertEquals(BigDecimal(expected), (response?.result as NumberTypeValue).getValue)
    }

    @Test
    fun `test set variable and use in next expression`() {
        // Create a shared parameters map for both expressions
        val sharedParams = mutableMapOf<String, DynamicTypeValue<*>>()

        // First set a variable
        val setVarRequest = EvaluateRequest("set :myVar = 42", sharedParams)
        val setResponse = calculatorBaseVisitor.evaluateExpression(setVarRequest)
        assertNotNull(setResponse)
        assertEquals(BigDecimal(42), (setResponse?.result as NumberTypeValue).getValue)

        // Then use the variable in an expression with the same parameters map
        val useVarRequest = EvaluateRequest(":myVar * 2", sharedParams)
        val useResponse = calculatorBaseVisitor.evaluateExpression(useVarRequest)
        assertNotNull(useResponse)
        assertEquals(BigDecimal(84), (useResponse?.result as NumberTypeValue).getValue)
    }
}
