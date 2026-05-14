package com.moneydance.modules.features.mcpserver.tools;

import com.moneydance.apps.md.controller.FeatureModuleContext;
import com.infinitekind.moneydance.model.*;
import com.moneydance.modules.features.mcpserver.json.JsonArrayBuilder;
import com.moneydance.modules.features.mcpserver.json.JsonObjectBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the 'get_investments' tool.
 * Lists all securities held within investment accounts.
 */
public class GetInvestmentsTool implements McpTool {

    @Override
    public String getName() {
        return "get_investments";
    }

    @Override
    public String getDescription() {
        return "Lists all securities held within investment accounts with their current quantities and values.";
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

        CurrencyType base = book.getCurrencies().getBaseType();
        int today = com.moneydance.modules.features.mcpserver.utils.DateUtil.getToday();

        JsonArrayBuilder investmentsArray = new JsonArrayBuilder();
        findInvestments(book.getRootAccount(), base, today, investmentsArray);

        return InvestmentFormatter.formatResponse(investmentsArray);
    }

    private void findInvestments(Account parent, CurrencyType base, int date, JsonArrayBuilder array) {
        for (int i = 0; i < parent.getSubAccountCount(); i++) {
            Account acct = parent.getSubAccount(i);
            
            if (acct.getAccountType() == Account.AccountType.SECURITY) {
                long shares = acct.getBalance();
                if (shares != 0) {
                    CurrencyType security = acct.getCurrencyType();
                    double currentPrice = 1.0 / security.getRelativeRate(date);
                    long totalValueBase = CurrencyUtil.convertValue(shares, security, base, date);

                    array.addObject(InvestmentFormatter.formatInvestment(
                        acct.getUUID(),
                        acct.getFullAccountName(),
                        security.getTickerSymbol(),
                        security.getName(),
                        security.formatSemiFancy(shares, '.'),
                        currentPrice,
                        totalValueBase,
                        base.getIDString()));
                }
            }
            
            findInvestments(acct, base, date, array);
        }
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
