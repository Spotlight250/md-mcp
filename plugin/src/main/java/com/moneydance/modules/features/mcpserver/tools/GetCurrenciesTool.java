package com.moneydance.modules.features.mcpserver.tools;

import com.moneydance.apps.md.controller.FeatureModuleContext;
import com.infinitekind.moneydance.model.*;
import com.moneydance.modules.features.mcpserver.json.JsonArrayBuilder;
import com.moneydance.modules.features.mcpserver.json.JsonObjectBuilder;

/**
 * Implementation of the 'get_currencies' tool.
 * Lists available currencies and their exchange rates relative to the base currency.
 */
public class GetCurrenciesTool implements McpTool {

    @Override
    public String getName() {
        return "get_currencies";
    }

    @Override
    public String getDescription() {
        return "Lists available currencies and their exchange rates relative to the base currency.";
    }

    @Override
    public String getInputSchema() {
        return "{\"type\":\"object\",\"properties\":{},\"required\":[]}";
    }

    @Override
    public String execute(String paramsJson, FeatureModuleContext ctx) {
        if (ctx == null) return errorResponse("Moneydance context not available");
        AccountBook book = ctx.getCurrentAccountBook();
        if (book == null) return errorResponse("No data file open");

        CurrencyTable table = book.getCurrencies();
        CurrencyType base = table.getBaseType();
        int today = com.moneydance.modules.features.mcpserver.utils.DateUtil.getToday();

        JsonArrayBuilder currenciesArray = new JsonArrayBuilder();
        for (CurrencyType curr : table.getAllCurrencies()) {
            if (curr.getCurrencyType() == CurrencyType.Type.CURRENCY) {
                double rate = 1.0 / curr.getRelativeRate(today);
                
                currenciesArray.addObject(CurrencyFormatter.formatCurrency(
                    curr.getIDString(), 
                    curr.getName(), 
                    rate, 
                    curr.equals(base)));
            }
        }

        return CurrencyFormatter.formatResponse(currenciesArray);
    }

    private String errorResponse(String message) {
        return new JsonObjectBuilder()
            .putArray("content", new JsonArrayBuilder()
                .addObject(new JsonObjectBuilder()
                    .put("type", "text")
                    .put("text", "Error: " + message)))
            .put("isError", true)
            .build();
    }
}
