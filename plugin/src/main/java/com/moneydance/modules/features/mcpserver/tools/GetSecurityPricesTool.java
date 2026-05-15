package com.moneydance.modules.features.mcpserver.tools;

import com.moneydance.apps.md.controller.FeatureModuleContext;
import com.infinitekind.moneydance.model.*;
import com.moneydance.modules.features.mcpserver.json.JsonArrayBuilder;
import com.moneydance.modules.features.mcpserver.json.JsonObjectBuilder;
import com.moneydance.modules.features.mcpserver.json.JsonParser;
import com.moneydance.modules.features.mcpserver.utils.DateUtil;

import java.util.List;

/**
 * Implementation of the 'get_security_prices' tool.
 * Retrieves historical price data for a security.
 */
public class GetSecurityPricesTool implements McpTool {

    @Override
    public String getName() {
        return "get_security_prices";
    }

    @Override
    public String getDescription() {
        return "Retrieves historical price data for a security to enable performance analysis.";
    }

    @Override
    public String getInputSchema() {
        return "{" +
                "\"type\":\"object\"," +
                "\"properties\":{" +
                "\"ticker\":{\"type\":\"string\",\"description\":\"Ticker symbol of the security\"}," +
                "\"security_id\":{\"type\":\"string\",\"description\":\"Optional UUID of the security\"}," +
                "\"start_date\":{\"type\":\"string\",\"description\":\"ISO date (YYYY-MM-DD)\"}," +
                "\"end_date\":{\"type\":\"string\",\"description\":\"ISO date (YYYY-MM-DD)\"}" +
                "}," +
                "\"required\":[\"start_date\",\"end_date\"]" +
                "}";
    }

    @Override
    public String execute(String paramsJson, FeatureModuleContext ctx) {
        if (ctx == null) return errorResponse("Moneydance context not available");
        AccountBook book = ctx.getCurrentAccountBook();
        if (book == null) return errorResponse("No data file open");

        String ticker = JsonParser.getString(paramsJson, "ticker");
        String securityId = JsonParser.getString(paramsJson, "security_id");
        String startDateStr = JsonParser.getString(paramsJson, "start_date");
        String endDateStr = JsonParser.getString(paramsJson, "end_date");

        if (ticker == null && securityId == null) {
            return errorResponse("Either 'ticker' or 'security_id' must be provided");
        }

        CurrencyType security = com.moneydance.modules.features.mcpserver.utils.SecurityHelper.findSecurity(book, ticker, securityId);
        if (security == null) return errorResponse("Security not found");

        int startDate = DateUtil.decodeIsoDate(startDateStr);
        int endDate = DateUtil.decodeIsoDate(endDateStr);

        JsonArrayBuilder pricesArray = new JsonArrayBuilder();
        for (CurrencySnapshot snapshot : security.getSnapshots()) {
            if (snapshot.getDateInt() >= startDate && snapshot.getDateInt() <= endDate) {
                double price = 1.0 / snapshot.getRate();
                pricesArray.addObject(SecurityPriceFormatter.formatPrice(
                    String.valueOf(snapshot.getDateInt()),
                    price));
            }
        }

        return SecurityPriceFormatter.formatResponse(ticker, pricesArray);
    }


}
