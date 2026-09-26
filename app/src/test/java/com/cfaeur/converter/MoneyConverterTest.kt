package com.cfaeur.converter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class MoneyConverterTest {
    @Test fun officialParityAndRounding() {
        assertEquals(BigDecimal("1.52"), MoneyConverter.convert(BigDecimal("1000"), Direction.CFA_TO_EUR))
        assertEquals(BigDecimal("656"), MoneyConverter.convert(BigDecimal("1"), Direction.EUR_TO_CFA))
        assertEquals(BigDecimal("984"), MoneyConverter.convert(BigDecimal("1.50"), Direction.EUR_TO_CFA))
    }

    @Test fun amountsHaveTheExpectedPrecision() {
        assertEquals(BigDecimal("1.25"), MoneyConverter.parse("1,25", Direction.EUR_TO_CFA))
        assertNull(MoneyConverter.parse("1,259", Direction.EUR_TO_CFA))
        assertNull(MoneyConverter.parse("1,5", Direction.CFA_TO_EUR))
        assertNull(MoneyConverter.parse("-5", Direction.CFA_TO_EUR))
    }

    @Test fun widgetKeysAndSwap() {
        val francs = WidgetValue().press("1").press("00")
        assertEquals("100", francs.input)
        assertEquals("0.15", francs.output.toPlainString())
        assertEquals("10", francs.press("DELETE").input)
        assertEquals("0", francs.press("CLEAR").input)

        val euros = francs.press("SWAP")
        assertEquals(Direction.EUR_TO_CFA, euros.direction)
        assertEquals("0.15", euros.input)
        assertEquals("98", euros.output.toPlainString())
        assertEquals(euros, euros.press("DECIMAL"))
        assertEquals("0.16", euros.press("DELETE").press("6").input)
    }

    @Test fun keypadFormattingAndLimits() {
        assertEquals("1.234,50", MoneyConverter.formatInput("1234.50"))
        assertEquals("1.234,50", MoneyConverter.format(BigDecimal("1234.5"), "EUR"))
        assertEquals("0", WidgetValue().press("00").input)
        assertEquals("0", WidgetValue().press("DECIMAL").input)
        assertEquals("0.", WidgetValue(direction = Direction.EUR_TO_CFA).press("DECIMAL").input)
        assertEquals("0.12", WidgetValue(direction = Direction.EUR_TO_CFA).press("DECIMAL").press("1").press("2").input)
        assertEquals("0.12", WidgetValue(direction = Direction.EUR_TO_CFA).press("DECIMAL").press("1").press("2").press("3").input)
        val largeEuros = WidgetValue("999999999999", Direction.EUR_TO_CFA)
        val largeFrancs = largeEuros.press("SWAP")
        assertEquals(Direction.CFA_TO_EUR, largeFrancs.direction)
        assertEquals(largeEuros.output, largeFrancs.amount)
    }
}
