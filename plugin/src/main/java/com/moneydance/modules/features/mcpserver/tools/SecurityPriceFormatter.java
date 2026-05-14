package com.moneydance.modules.features.mcpserver.tools;

import com.moneydance.modules.features.mcpserver.json.JsonArrayBuilder;
import com.moneydance.modules.features.mcpserver.json.JsonObjectBuilder;

public class SecurityPriceFormatter {
    public static JsonObjectBuilder formatPrice(String date, double price) {
        return new JsonObjectBuilder()
            .put("date", date)
            .put("price", price);
    }
    
    public static String formatResponse(String ticker, JsonArrayBuilder prices) {
        return new JsonObjectBuilder()
            .putArray("content", new JsonArrayBuilder()
                .addObject(new JsonObjectBuilder()
                    .put("type", "text")
                    .put("text", "Price history for " + ticker + ": " + prices.build())))
            .put("isError", false)
            .build();
    }
}
