package com.moneydance.modules.features.mcpserver.tools;

import com.moneydance.modules.features.mcpserver.json.JsonArrayBuilder;
import com.moneydance.modules.features.mcpserver.json.JsonObjectBuilder;

public class InvestmentFormatter {
    public static JsonObjectBuilder formatInvestment(String accountId, String accountName, String ticker, 
                                        String securityName, String quantity, double price, 
                                        long totalValue, String currency) {
        return new JsonObjectBuilder()
            .put("account_id", accountId)
            .put("account_name", accountName)
            .put("ticker", ticker)
            .put("security_name", securityName)
            .put("quantity", quantity)
            .put("current_price", price)
            .put("total_value_base", totalValue)
            .put("currency", currency);
    }
    
    public static String formatResponse(JsonArrayBuilder investments) {
        return new JsonObjectBuilder()
            .putArray("content", new JsonArrayBuilder()
                .addObject(new JsonObjectBuilder()
                    .put("type", "text")
                    .put("text", investments.build())))
            .put("isError", false)
            .build();
    }
}
