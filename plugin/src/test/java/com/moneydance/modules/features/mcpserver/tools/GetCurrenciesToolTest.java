package com.moneydance.modules.features.mcpserver.tools;

import com.moneydance.modules.features.mcpserver.json.JsonParser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GetCurrenciesToolTest {

    @Test
    void testFormatCurrency() {
        String json = CurrencyFormatter.formatCurrency("GBP", "British Pound", 1.25, true).build();
        assertEquals("GBP", JsonParser.getString(json, "id"));
        assertEquals("1.25", JsonParser.getValue(json, "rate_to_base"));
        assertEquals("true", JsonParser.getValue(json, "is_base"));
    }

    @Test
    void testMetadata() {
        GetCurrenciesTool tool = new GetCurrenciesTool();
        assertEquals("get_currencies", tool.getName());
        assertTrue(tool.getDescription().contains("exchange rates"));
    }
}
