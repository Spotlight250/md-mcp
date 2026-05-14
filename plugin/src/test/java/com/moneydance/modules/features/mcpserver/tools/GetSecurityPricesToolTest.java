package com.moneydance.modules.features.mcpserver.tools;

import com.moneydance.modules.features.mcpserver.json.JsonParser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GetSecurityPricesToolTest {

    @Test
    void testFormatPrice() {
        String json = SecurityPriceFormatter.formatPrice("20230101", 150.25).build();
        assertEquals("20230101", JsonParser.getString(json, "date"));
        assertEquals("150.25", JsonParser.getValue(json, "price"));
    }

    @Test
    void testMetadata() {
        GetSecurityPricesTool tool = new GetSecurityPricesTool();
        assertEquals("get_security_prices", tool.getName());
    }
}
