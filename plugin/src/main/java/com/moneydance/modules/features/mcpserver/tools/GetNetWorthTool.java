package com.moneydance.modules.features.mcpserver.tools;

import com.moneydance.apps.md.controller.FeatureModuleContext;
import com.moneydance.modules.features.mcpserver.McpLogger;
import com.infinitekind.moneydance.model.Account;
import com.infinitekind.moneydance.model.AccountBook;
import com.infinitekind.moneydance.model.AccountUtil;
import com.moneydance.modules.features.mcpserver.json.JsonArrayBuilder;
import com.moneydance.modules.features.mcpserver.json.JsonObjectBuilder;
import com.moneydance.modules.features.mcpserver.json.JsonParser;
import com.moneydance.modules.features.mcpserver.utils.DateUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;

/**
 * Implementation of the 'get_net_worth' tool.
 */
public class GetNetWorthTool implements McpTool {

    @Override
    public String getName() {
        return "get_net_worth";
    }

    @Override
    public String getDescription() {
        return "Returns total assets, liabilities, and net worth. Can be queried for a specific date or specific accounts.";
    }

    @Override
    public String getInputSchema() {
        return "{" +
                "\"type\":\"object\"," +
                "\"properties\":{" +
                "\"as_of_date\":{\"type\":\"string\",\"description\":\"ISO date (YYYY-MM-DD)\"}," +
                "\"account_ids\":{\"type\":\"array\",\"items\":{\"type\":\"string\"},\"description\":\"Optional list of account UUIDs\"}" +
                "}" +
                "}";
    }

    @Override
    public String execute(String paramsJson, FeatureModuleContext ctx) {
        if (ctx == null) return errorResponse("No Moneydance context");
        AccountBook book = ctx.getCurrentAccountBook();
        if (book == null) return errorResponse("No data file open");

        String isoDate = JsonParser.getString(paramsJson, "as_of_date");
        int mdDate = isoDate != null ? DateUtil.decodeIsoDate(isoDate) : 0;
        
        String accountIdsJson = JsonParser.getValue(paramsJson, "account_ids");
        Set<String> targetIds = parseIdList(accountIdsJson);

        com.infinitekind.moneydance.model.CurrencyType base = book.getCurrencies().getBaseType();
        long totalAssets = 0;
        long totalLiabilities = 0;

        // Traverse using AccountIterator
        int conversionDate = mdDate > 0 ? mdDate : DateUtil.getToday();
        com.infinitekind.moneydance.model.AccountIterator it = 
            new com.infinitekind.moneydance.model.AccountIterator(book.getRootAccount());
        
        while (it.hasNext()) {
            Account acct = it.next();
            McpLogger.log("Calculating balance for: " + acct.getFullAccountName());
            
            // Skip if we have a target list and this account isn't in it
            if (targetIds != null && !targetIds.isEmpty() && !targetIds.contains(acct.getUUID())) {
                continue;
            }

            // Filter using standard SDK-aligned logic
            if (com.moneydance.modules.features.mcpserver.utils.AccountHelper.shouldIncludeInNetWorth(acct)) {
                long balance = (mdDate > 0) ? 
                    com.infinitekind.moneydance.model.AccountUtil.getBalanceAsOfDate(book, acct, mdDate) : 
                    acct.getBalance();
                
                // Sanitize sentinel error values from SDK
                if (balance == Long.MIN_VALUE) {
                    McpLogger.log("Warning: Skipping " + acct.getFullAccountName() + " because balance is unavailable for " + (mdDate > 0 ? mdDate : "current date"));
                    balance = 0;
                }
                
                com.infinitekind.moneydance.model.CurrencyType acctCurrency = acct.getCurrencyType();
                if (acctCurrency != null) {
                    long valueInBase = com.infinitekind.moneydance.model.CurrencyUtil.convertValue(
                        balance, acctCurrency, base, conversionDate);
                    
                    if (com.moneydance.modules.features.mcpserver.utils.AccountHelper.isAsset(acct)) {
                        totalAssets += valueInBase;
                    } else if (com.moneydance.modules.features.mcpserver.utils.AccountHelper.isLiability(acct)) {
                        totalLiabilities += valueInBase;
                    }
                }
            }
        }

        String resultJson = new JsonObjectBuilder()
            .put("date", mdDate > 0 ? DateUtil.encodeIsoDate(mdDate) : "current")
            .put("total_assets", CurrencyFormatter.toDecimal(totalAssets, base))
            .put("total_liabilities", CurrencyFormatter.toDecimal(totalLiabilities, base))
            .put("net_worth", CurrencyFormatter.toDecimal(totalAssets + totalLiabilities, base))
            .put("currency", base.getIDString())
            .build();

        return new JsonObjectBuilder()
            .putArray("content", new JsonArrayBuilder()
                .addObject(new JsonObjectBuilder()
                    .put("type", "text")
                    .put("text", resultJson)))
            .put("isError", false)
            .build();
    }


    private Set<String> parseIdList(String jsonArray) {
        if (jsonArray == null || jsonArray.trim().isEmpty()) return null;
        Object parsed = JsonParser.parse(jsonArray);
        if (!(parsed instanceof List)) return null;
        
        Set<String> ids = new HashSet<>();
        for (Object item : (List<?>) parsed) {
            ids.add(String.valueOf(item));
        }
        return ids;
    }

}
