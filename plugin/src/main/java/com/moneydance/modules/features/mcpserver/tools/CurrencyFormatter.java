package com.moneydance.modules.features.mcpserver.tools;

import com.moneydance.modules.features.mcpserver.json.JsonArrayBuilder;
import com.moneydance.modules.features.mcpserver.json.JsonObjectBuilder;

public class CurrencyFormatter {
    public static JsonObjectBuilder formatCurrency(String id, String name, double rateToBase, boolean isBase) {
        return new JsonObjectBuilder()
            .put("id", id)
            .put("name", name)
            .put("code", id)
            .put("rate_to_base", rateToBase)
            .put("is_base", isBase);
    }
    
    public static String formatResponse(JsonArrayBuilder currencies) {
        return new JsonObjectBuilder()
            .putArray("content", new JsonArrayBuilder()
                .addObject(new JsonObjectBuilder()
                    .put("type", "text")
                    .put("text", currencies.build())))
            .put("isError", false)
            .build();
    }
}
