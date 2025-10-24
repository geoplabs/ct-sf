package com.sustainability.evaluate

import com.sustainability.service.CalculationService
import com.sustainability.service.EmissionFactorsService
import com.sustainability.service.GwpService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import java.math.BigDecimal
import java.math.RoundingMode

class CalculatorBaseVisitorImplTest {
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

    @Test
    fun `test visitScientificAtom with valid value`() {
        val evaluateRequest = EvaluateRequest(
            ":left",
            mutableMapOf("left" to NumberTypeValue(BigDecimal(3.0E-3))),
        )
        val response = calculatorBaseVisitor.evaluateExpression(evaluateRequest)
        assertNotNull(response)
        val actualValue = (response?.result as NumberTypeValue).getValue().setScale(4, RoundingMode.HALF_UP)
        assertEquals(BigDecimal("0.0030"), actualValue)
    }

    @Test
    fun `test visitVariableAtom with valid value`() {
        val evaluateRequest = EvaluateRequest(
            ":left",
            mutableMapOf("left" to NumberTypeValue(BigDecimal(3))),
        )
        val response = calculatorBaseVisitor.evaluateExpression(evaluateRequest)
        assertNotNull(response)
        assertEquals(BigDecimal(3), (response?.result as NumberTypeValue).getValue)
    }

    @Test
    fun `test visitBracesAtom with valid value`() {
        val evaluateRequest = EvaluateRequest(
            ":left",
            mutableMapOf("left" to StringTypeValue("{3}")),
        )
        val response = calculatorBaseVisitor.evaluateExpression(evaluateRequest)
        assertNotNull(response)
        assertEquals("{3}", (response?.result as StringTypeValue).getValue)
    }

    @Test
    fun `test visitTokenAtom with valid value`() {
        val evaluateRequest = EvaluateRequest(
            ":one",
            mutableMapOf("one" to StringTypeValue("ONE")),
        )
        val response = calculatorBaseVisitor.evaluateExpression(evaluateRequest)
        assertNotNull(response)
        assertEquals("ONE", (response?.result as StringTypeValue).getValue)
    }

    @Test
    fun `test visitQuotedStringAtom with valid value`() {
        val evaluateRequest = EvaluateRequest(
            ":one:two:three",
            mutableMapOf("one:two:three" to StringTypeValue("EMBEDDED COLONS")),
        )
        val response = calculatorBaseVisitor.evaluateExpression(evaluateRequest)
        assertNotNull(response)
        assertEquals("EMBEDDED COLONS", (response?.result as StringTypeValue).getValue)
    }

    @Test
    fun `test visitSetVariableExpression with valid value`() {
        val evaluateRequest = EvaluateRequest(
            "set :a = 10",
            mutableMapOf(),
        )
        val response = calculatorBaseVisitor.evaluateExpression(evaluateRequest)
        assertNotNull(response)
        assertEquals(BigDecimal(10), (response?.result as NumberTypeValue).getValue)
    }
}
