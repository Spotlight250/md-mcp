package com.moneydance.modules.features.mcpserver.resources;

import com.moneydance.apps.md.controller.FeatureModuleContext;
import com.infinitekind.moneydance.model.*;
import com.moneydance.modules.features.mcpserver.json.JsonArrayBuilder;
import com.moneydance.modules.features.mcpserver.json.JsonObjectBuilder;

/**
 * Exposes all securities and their current metadata as a JSON resource.
 */
public class SecurityMasterResource implements McpResource {

    @Override
    public String getUri() {
        return "mcp://moneydance/securities/master";
    }

    @Override
    public String getName() {
        return "Security Master";
    }

    @Override
    public String getDescription() {
        return "A complete list of all securities with tickers, names, and current prices.";
    }

    @Override
    public String getMimeType() {
        return "application/json";
    }

    @Override
    public String read(FeatureModuleContext context) {
        CurrencyTable table = context.getCurrentAccountBook().getCurrencies();
        int today = com.moneydance.modules.features.mcpserver.utils.DateUtil.getToday();
        JsonArrayBuilder securities = new JsonArrayBuilder();
        
        for (CurrencyType curr : table.getAllCurrencies()) {
            if (curr.getCurrencyType() == CurrencyType.Type.SECURITY) {
                double price = 1.0 / curr.getRelativeRate(today);
                
                securities.addObject(new JsonObjectBuilder()
                    .put("id", curr.getIDString())
                    .put("ticker", curr.getTickerSymbol())
                    .put("name", curr.getName())
                    .put("price", String.valueOf(price)));
            }
        }
        
        return new JsonObjectBuilder()
            .putArray("securities", securities)
            .build();
    }
}
