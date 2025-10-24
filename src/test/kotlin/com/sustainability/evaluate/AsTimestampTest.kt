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
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AsTimestampTest {
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

    private fun provideTimestampTestCases(): Stream<Arguments> {
        val value = "1/21/22 13:40:13"
        val pattern = "M/d/yy HH:mm:ss"
        return Stream.of(
            Arguments.of(
                "AS_TIMESTAMP('1/21/22','M/d/yy',timezone='America/Denver')",
                NumberTypeValue(1642748400000).getValue().toLong(), // "2022-01-21 07:00:00.000000000"
            ),
            Arguments.of(
                "AS_TIMESTAMP('1/21/22 13:40:13','M/d/yy HH:mm:ss',timezone='UTC')",
                NumberTypeValue(1642772413000L).getValue().toLong(), // "2022-01-21 13:40:13.000000000"
            ),
            Arguments.of(
                "AS_TIMESTAMP('1/21/22 13:40:13 PST','M/d/yy HH:mm:ss zzz',timezone='America/Denver')",
                NumberTypeValue(1642797613000L).getValue().toLong(), // "2022-01-21 13:40:13.000000000"
            ),
            Arguments.of(
                "AS_TIMESTAMP('2022-03-12T13:12:11','yyyy-MM-dd\\'T\\'HH:mm:ss',timezone='America/Denver')",
                NumberTypeValue(1647115931000L).getValue().toLong(),
            ),
            Arguments.of(
                "AS_TIMESTAMP('2022-07-25T12:53:54.097+00:00','yyyy-MM-dd\\'T\\'HH:mm:ss.SSSXXX')",
                1658753634097L, // Actual value from implementation - UTC timestamp
            ),
            Arguments.of(
                "AS_TIMESTAMP('2022-07-25T18:23:54.097+05:30','yyyy-MM-dd\\'T\\'HH:mm:ss.SSSXXX')",
                1658753634097L, // Actual value from implementation - UTC timestamp
            ),
            Arguments.of(
                "AS_TIMESTAMP('2022-07-25T05:53:54.097-07:00','yyyy-MM-dd\\'T\\'HH:mm:ss.SSSXXX')",
                1658753634097L, // Actual value from implementation - UTC timestamp
            ),
            Arguments.of(
                "AS_TIMESTAMP('1/21/22 13:40:13 PST','M/d/yy HH:mm:ss zzz',timezone='America/Denver'," +
                    "roundDownTo='day')",
                NumberTypeValue(1642748400000L).getValue().toLong(), // "2022-01-21 00:00:00.000000000 UTC-7 (MST)"
            ),
            Arguments.of(
                "AS_TIMESTAMP('1/21/22 13:40:13 PST','M/d/yy HH:mm:ss zzz',timezone='America/Denver'," +
                    "locale='en-US',roundDownTo='week')",
                NumberTypeValue(1642316400000L).getValue().toLong(), // "2022-01-16 00:00:00.000000000 UTC-7 (MST)"
            ),
            Arguments.of(
                "AS_TIMESTAMP('2/21/22 13:40:13 PST','M/d/yy HH:mm:ss zzz',timezone='America/Denver'," +
                    "roundDownTo='month')",
                NumberTypeValue(1643698800000L).getValue().toLong(), // "2022-02-01 00:00:00.000000000 UTC-7 (MST)"
            ),
            Arguments.of(
                "AS_TIMESTAMP('2/21/22 13:40:13 PST','M/d/yy HH:mm:ss zzz',timezone='America/Denver'," +
                    "roundDownTo='year')",
                NumberTypeValue(1641020400000L).getValue().toLong(), // "2022-01-01 00:00:00.000000000 UTC-7 (MST)"
            ),
            Arguments.of(
                "AS_TIMESTAMP('8/21/22 13:40:13 PST','M/d/yy HH:mm:ss zzz',timezone='America/Denver'," +
                    "roundDownTo='quarter')",
                NumberTypeValue(1656655200000L).getValue().toLong(), // "2022-07-01 00:00:00.000000000 UTC-7 (MST)"
            ),
            Arguments.of(
                "AS_TIMESTAMP('11/21/22 13:40:13 PST','M/d/yy HH:mm:ss zzz',timezone='America/Denver'," +
                    "roundDownTo='quarter')",
                1664604000000L, // Updated for proper quarter start in Mountain time
            ),
            Arguments.of(
                String.format("AS_TIMESTAMP('%s','%s',roundDownTo='week')", value, pattern),
                getBeginningOfWeekOfLocaleRegion(
                    value,
                    pattern,
                ), // Match to beginning of week of locale timezone/region
            ),
        )
    }

    @ParameterizedTest
    @MethodSource("provideTimestampTestCases")
    fun `test timeZone with valid value`(expression: String, expected: Long) {
        val evaluateRequest = EvaluateRequest(
            expression,
            mutableMapOf(),
        )
        val response = calculatorBaseVisitor.evaluateExpression(evaluateRequest)
        assertNotNull(response)
        assertEquals(expected, (response?.result?.getValue as java.math.BigDecimal).toLong())
    }

    companion object {
        fun getBeginningOfWeekOfLocaleRegion(value: String, pattern: String): Any? {
            val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
            val dateTime = LocalDateTime.parse(value, formatter).atZone(ZoneId.systemDefault())
            val beginningOfWeek =
                dateTime.toLocalDate().atStartOfDay().with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1)
            return NumberTypeValue(beginningOfWeek.toEpochSecond(dateTime.offset) * 1000).getValue().toLong()
        }
    }
}
