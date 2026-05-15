package com.moneydance.modules.features.mcpserver.tools;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CurrencyFormatterTest {

    @Test
    void testToDecimalNullType() {
        // Fallback case: value / 100.0
        assertEquals(1.0, CurrencyFormatter.toDecimal(100L, null), 0.001);
        assertEquals(12.34, CurrencyFormatter.toDecimal(1234L, null), 0.001);
        assertEquals(0.0, CurrencyFormatter.toDecimal(0L, null), 0.001);
        assertEquals(-1.5, CurrencyFormatter.toDecimal(-150L, null), 0.001);
    }
}
