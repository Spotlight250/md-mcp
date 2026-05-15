package com.moneydance.modules.features.mcpserver.tools;

import com.moneydance.modules.features.mcpserver.json.JsonArrayBuilder;
import com.moneydance.modules.features.mcpserver.json.JsonObjectBuilder;

public class SecurityPerformanceFormatter {
    public static JsonObjectBuilder formatTransaction(String date, String description, String amount, String price) {
        return new JsonObjectBuilder()
            .put("date", date)
            .put("description", description)
            .put("amount", amount)
            .put("price", price);
    }
    
    public static String formatResponse(String ticker, String totalQuantity, double currentPrice, 
                                      String currentValue, String costBasis, String unrealizedGain,
                                      String roi, JsonArrayBuilder history) {
        return new JsonObjectBuilder()
            .putArray("content", new JsonArrayBuilder()
                .addObject(new JsonObjectBuilder()
                    .put("type", "text")
                    .put("text", "Performance for " + ticker + ":\n" +
                               "Total Quantity: " + totalQuantity + "\n" +
                               "Current Price: " + currentPrice + "\n" +
                               "Current Value: " + currentValue + "\n" +
                               "Cost Basis: " + costBasis + "\n" +
                               "Unrealized Gain/Loss: " + unrealizedGain + "\n" +
                               "ROI: " + roi + "\n" +
                               "Transaction History: " + history.build())))
            .put("isError", false)
            .build();
    }
}
