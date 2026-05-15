package com.moneydance.modules.features.mcpserver.tools;

import com.moneydance.apps.md.controller.FeatureModuleContext;
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

        long totalAssets = 0;
        long totalLiabilities = 0;

        List<Account> accounts = new ArrayList<>();
        findAccounts(book.getRootAccount(), targetIds, false, accounts);

        com.infinitekind.moneydance.model.CurrencyType base = book.getCurrencies().getBaseType();
        int conversionDate = mdDate > 0 ? mdDate : DateUtil.getToday();

        for (Account acct : accounts) {
            long balance;
            if (mdDate > 0) {
                balance = AccountUtil.getBalanceAsOfDate(book, acct, mdDate);
            } else {
                balance = acct.getBalance();
            }

            // Convert to base currency value using historical price/rate if date provided
            long valueInBase = com.infinitekind.moneydance.model.CurrencyUtil.convertValue(
                balance, acct.getCurrencyType(), base, conversionDate);

            if (isAsset(acct)) {
                totalAssets += valueInBase;
            } else if (isLiability(acct)) {
                totalLiabilities += valueInBase;
            }
        }

        String resultJson = new JsonObjectBuilder()
            .put("date", mdDate > 0 ? DateUtil.encodeIsoDate(mdDate) : "current")
            .put("total_assets", CurrencyFormatter.toDecimal(totalAssets, base))
            .put("total_liabilities", CurrencyFormatter.toDecimal(totalLiabilities, base))
            .put("net_worth", CurrencyFormatter.toDecimal(totalAssets + totalLiabilities, base)) // Liabilities are negative in MD
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

    private void findAccounts(Account parent, Set<String> targetIds, boolean includeAll, List<Account> results) {
        for (int i = 0; i < parent.getSubAccountCount(); i++) {
            Account acct = parent.getSubAccount(i);
            boolean match = includeAll || targetIds == null || targetIds.isEmpty() || targetIds.contains(acct.getUUID());
            
            if (match) {
                if (com.moneydance.modules.features.mcpserver.utils.AccountHelper.isAsset(acct) || 
                    com.moneydance.modules.features.mcpserver.utils.AccountHelper.isLiability(acct)) {
                    results.add(acct);
                }
            }
            
            // Recurse, passing true if this account or its ancestor was a match
            findAccounts(acct, targetIds, match, results);
        }
    }

    private boolean isAsset(Account acct) {
        return com.moneydance.modules.features.mcpserver.utils.AccountHelper.isAsset(acct);
    }

    private boolean isLiability(Account acct) {
        return com.moneydance.modules.features.mcpserver.utils.AccountHelper.isLiability(acct);
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
