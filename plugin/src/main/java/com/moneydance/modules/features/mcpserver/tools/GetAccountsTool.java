package com.moneydance.modules.features.mcpserver.tools;

import com.moneydance.apps.md.controller.FeatureModuleContext;
import com.infinitekind.moneydance.model.Account;
import com.infinitekind.moneydance.model.AccountBook;
import com.moneydance.modules.features.mcpserver.json.JsonArrayBuilder;
import com.moneydance.modules.features.mcpserver.json.JsonObjectBuilder;

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
        return "Lists all accounts (bank, credit card, investment, loan, asset) with their balances and status.";
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

        com.infinitekind.moneydance.model.CurrencyType base = book.getCurrencies().getBaseType();
        int today = com.moneydance.modules.features.mcpserver.utils.DateUtil.getToday();

        JsonArrayBuilder accountsArray = new JsonArrayBuilder();
        addAccountsRecursively(book.getRootAccount(), base, today, accountsArray);

        return new JsonObjectBuilder()
            .putArray("content", new JsonArrayBuilder()
                .addObject(new JsonObjectBuilder()
                    .put("type", "text")
                    .put("text", accountsArray.build())))
            .put("isError", false)
            .build();
    }

    private void addAccountsRecursively(Account parent, com.infinitekind.moneydance.model.CurrencyType base, int date, JsonArrayBuilder array) {
        for (int i = 0; i < parent.getSubAccountCount(); i++) {
            Account acct = parent.getSubAccount(i);
            
            if (isBalanceAccount(acct)) {
                long balance = acct.getBalance();
                long balanceBase = com.infinitekind.moneydance.model.CurrencyUtil.convertValue(
                    balance, acct.getCurrencyType(), base, date);

                JsonObjectBuilder acctObj = new JsonObjectBuilder()
                    .put("id", acct.getUUID())
                    .put("name", acct.getFullAccountName())
                    .put("type", acct.getAccountType().name())
                    .put("balance", balance)
                    .put("balance_base", balanceBase)
                    .put("currency", acct.getCurrencyType().getIDString())
                    .put("is_inactive", acct.getAccountIsInactive())
                    .put("is_hidden", acct.getHideOnHomePage());
                
                if (acct.getAccountType() == Account.AccountType.SECURITY) {
                    acctObj.put("shares", balance); // For securities, balance IS the share count
                }
                
                array.addObject(acctObj);
            }
            
            addAccountsRecursively(acct, base, date, array);
        }
    }

    private boolean isBalanceAccount(Account acct) {
        return com.moneydance.modules.features.mcpserver.utils.AccountHelper.isAsset(acct) ||
               com.moneydance.modules.features.mcpserver.utils.AccountHelper.isLiability(acct);
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
