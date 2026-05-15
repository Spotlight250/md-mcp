package com.moneydance.modules.features.mcpserver.tools;

import com.moneydance.apps.md.controller.FeatureModuleContext;
import com.infinitekind.moneydance.model.*;
import com.moneydance.modules.features.mcpserver.json.JsonArrayBuilder;
import com.moneydance.modules.features.mcpserver.json.JsonObjectBuilder;
import com.moneydance.modules.features.mcpserver.json.JsonParser;
import com.moneydance.modules.features.mcpserver.utils.DateUtil;
import com.moneydance.modules.features.mcpserver.utils.AccountHelper;

/**
 * Implementation of the 'get_accounts' tool.
 */
public class GetAccountsTool implements McpTool {

    @Override
    public String getName() {
        return "get_accounts";
    }

    @Override
    public String getDescription() {
        return "Lists all accounts (bank, credit card, investment, loan, asset) with their balances and status. Supports historical dates and balance types.";
    }

    @Override
    public String getInputSchema() {
        return "{" +
                "\"type\":\"object\"," +
                "\"properties\":{" +
                "\"as_of_date\":{\"type\":\"string\",\"description\":\"ISO date (YYYY-MM-DD). Defaults to today.\"}," +
                "\"cleared_only\":{\"type\":\"boolean\",\"description\":\"If true, only includes cleared transactions in the balance calculation. Defaults to false.\"}," +
                "\"include_inactive\":{\"type\":\"boolean\",\"description\":\"Whether to include inactive accounts. Defaults to true.\"}," +
                "\"include_hidden\":{\"type\":\"boolean\",\"description\":\"Whether to include hidden accounts. Defaults to true.\"}" +
                "}" +
                "}";
    }

    @Override
    public String execute(String paramsJson, FeatureModuleContext ctx) {
        if (ctx == null) return errorResponse("Moneydance context not available");
        AccountBook book = ctx.getCurrentAccountBook();
        if (book == null) return errorResponse("No data file open");

        // Parse parameters with defaults
        String isoDate = JsonParser.getString(paramsJson, "as_of_date");
        int mdDate = isoDate != null ? DateUtil.decodeIsoDate(isoDate) : DateUtil.getToday();
        
        boolean clearedOnly = false;
        String clearedOnlyStr = JsonParser.getString(paramsJson, "cleared_only");
        if (clearedOnlyStr != null) clearedOnly = Boolean.parseBoolean(clearedOnlyStr);
        
        boolean includeInactive = true;
        String incInactiveStr = JsonParser.getString(paramsJson, "include_inactive");
        if (incInactiveStr != null) includeInactive = Boolean.parseBoolean(incInactiveStr);
        
        boolean includeHidden = true;
        String incHiddenStr = JsonParser.getString(paramsJson, "include_hidden");
        if (incHiddenStr != null) includeHidden = Boolean.parseBoolean(incHiddenStr);

        CurrencyType base = book.getCurrencies().getBaseType();
        JsonArrayBuilder accountsArray = new JsonArrayBuilder();
        AccountIterator it = new AccountIterator(book.getRootAccount());

        while (it.hasNext()) {
            Account acct = it.next();
            
            // Apply filters
            if (!includeInactive && acct.getAccountIsInactive()) continue;
            if (!includeHidden && acct.getHideOnHomePage()) continue;

            if (isBalanceAccount(acct)) {
                long balance;
                long recursiveBalanceBase;
                
                if (isoDate != null || clearedOnly) {
                    balance = getSafeBalance(book, acct, mdDate, clearedOnly);
                    recursiveBalanceBase = calculateRecursiveBalanceBase(book, acct, mdDate, clearedOnly, base);
                } else {
                    balance = acct.getBalance();
                    recursiveBalanceBase = calculateRecursiveBalanceBase(book, acct, DateUtil.getToday(), false, base);
                }

                long balanceBase = CurrencyUtil.convertValue(balance, acct.getCurrencyType(), base, mdDate);
                long recursiveBalance = CurrencyUtil.convertValue(recursiveBalanceBase, base, acct.getCurrencyType(), mdDate);

                JsonObjectBuilder acctObj = new JsonObjectBuilder()
                    .put("id", acct.getUUID())
                    .put("name", acct.getFullAccountName())
                    .put("type", acct.getAccountType().name())
                    .put("balance", CurrencyFormatter.toDecimal(balance, acct.getCurrencyType()))
                    .put("balance_base", CurrencyFormatter.toDecimal(balanceBase, base))
                    .put("total_balance", CurrencyFormatter.toDecimal(recursiveBalance, acct.getCurrencyType()))
                    .put("total_balance_base", CurrencyFormatter.toDecimal(recursiveBalanceBase, base))
                    .put("currency", acct.getCurrencyType().getIDString())
                    .put("is_inactive", acct.getAccountIsInactive())
                    .put("is_hidden", acct.getHideOnHomePage())
                    .put("as_of_date", DateUtil.encodeIsoDate(mdDate))
                    .put("cleared_only", clearedOnly);
                
                if (AccountUtil.getBalanceAsOfDate(book, acct, mdDate, clearedOnly) == Long.MIN_VALUE) {
                    acctObj.put("data_quality", "warning: balance unavailable for this date");
                }
                
                Account parent = acct.getParentAccount();
                if (parent != null && parent.getAccountType() != Account.AccountType.ROOT) {
                    acctObj.put("parent_id", parent.getUUID());
                }
                
                if (acct.getAccountType() == Account.AccountType.SECURITY) {
                    acctObj.put("shares", balance);
                }
                
                accountsArray.addObject(acctObj);
            }
        }

        return new JsonObjectBuilder()
            .putArray("content", new JsonArrayBuilder()
                .addObject(new JsonObjectBuilder()
                    .put("type", "text")
                    .put("text", accountsArray.build())))
            .put("isError", false)
            .build();
    }

    private long getSafeBalance(AccountBook book, Account acct, int date, boolean clearedOnly) {
        long b = AccountUtil.getBalanceAsOfDate(book, acct, date, clearedOnly);
        return (b == Long.MIN_VALUE) ? 0 : b;
    }

    private long calculateRecursiveBalanceBase(AccountBook book, Account root, int date, boolean clearedOnly, CurrencyType base) {
        long balance = getSafeBalance(book, root, date, clearedOnly);
        long totalBase = CurrencyUtil.convertValue(balance, root.getCurrencyType(), base, date);
        
        for (int i = 0; i < root.getSubAccountCount(); i++) {
            totalBase += calculateRecursiveBalanceBase(book, root.getSubAccount(i), date, clearedOnly, base);
        }
        return totalBase;
    }


    private boolean isBalanceAccount(Account acct) {
        return com.moneydance.modules.features.mcpserver.utils.AccountHelper.isAsset(acct) ||
               com.moneydance.modules.features.mcpserver.utils.AccountHelper.isLiability(acct);
    }

}
