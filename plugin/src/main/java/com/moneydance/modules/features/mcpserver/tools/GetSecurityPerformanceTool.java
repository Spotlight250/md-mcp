package com.moneydance.modules.features.mcpserver.tools;

import com.moneydance.apps.md.controller.FeatureModuleContext;
import com.infinitekind.moneydance.model.*;
import com.moneydance.modules.features.mcpserver.json.JsonArrayBuilder;
import com.moneydance.modules.features.mcpserver.json.JsonObjectBuilder;
import com.moneydance.modules.features.mcpserver.json.JsonParser;
import com.moneydance.modules.features.mcpserver.utils.DateUtil;

/**
 * Implementation of the 'get_security_performance' tool.
 * Provides a detailed view of a security's performance by combining holdings and transactions.
 */
public class GetSecurityPerformanceTool implements McpTool {

    @Override
    public String getName() {
        return "get_security_performance";
    }

    @Override
    public String getDescription() {
        return "Deep-dive analysis of a security's actual performance. Includes transaction history and current valuation.";
    }

    @Override
    public String getInputSchema() {
        return "{" +
                "\"type\":\"object\"," +
                "\"properties\":{" +
                "\"ticker\":{\"type\":\"string\",\"description\":\"Ticker symbol of the security\"}," +
                "\"security_id\":{\"type\":\"string\",\"description\":\"Optional UUID of the security\"}," +
                "\"account_id\":{\"type\":\"string\",\"description\":\"Optional account UUID to filter performance\"}" +
                "}," +
                "\"required\":[\"ticker\"]" +
                "}";
    }

    @Override
    public String execute(String paramsJson, FeatureModuleContext ctx) {
        if (ctx == null) return errorResponse("Moneydance context not available");
        AccountBook book = ctx.getCurrentAccountBook();
        if (book == null) return errorResponse("No data file open");

        String ticker = JsonParser.getString(paramsJson, "ticker");
        String securityId = JsonParser.getString(paramsJson, "security_id");
        String accountId = JsonParser.getString(paramsJson, "account_id");

        CurrencyType security = findSecurity(book, ticker, securityId);
        if (security == null) return errorResponse("Security not found");

        CurrencyType base = book.getCurrencies().getBaseType();
        int today = DateUtil.getToday();

        // 1. Transaction History
        JsonArrayBuilder historyArray = new JsonArrayBuilder();
        TransactionSet txns = book.getTransactionSet();
        java.util.Iterator<AbstractTxn> it = txns.iterator();
        long totalShares = 0;
        
        while (it.hasNext()) {
            AbstractTxn txn = it.next();
            if (security.equals(txn.getAccount().getCurrencyType())) {
                if (accountId != null && !txn.getAccount().getUUID().equals(accountId)) continue;
                
                totalShares += txn.getValue();
                double price = 1.0 / security.getRelativeRate(txn.getDateInt());
                
                historyArray.addObject(SecurityPerformanceFormatter.formatTransaction(
                    DateUtil.encodeIsoDate(txn.getDateInt()),
                    txn.getDescription(),
                    security.formatSemiFancy(txn.getValue(), '.'),
                    String.valueOf(price)));
            }
        }

        double currentPrice = 1.0 / security.getRelativeRate(today);
        long currentTotalValue = CurrencyUtil.convertValue(totalShares, security, base, today);

        return SecurityPerformanceFormatter.formatResponse(
            ticker,
            security.formatSemiFancy(totalShares, '.'),
            currentPrice,
            base.formatSemiFancy(currentTotalValue, '.'),
            historyArray);
    }

    private void addHoldings(Account parent, CurrencyType security, CurrencyType base, int date, JsonArrayBuilder array) {
        for (int i = 0; i < parent.getSubAccountCount(); i++) {
            Account acct = parent.getSubAccount(i);
            if (acct.getAccountType() == Account.AccountType.SECURITY && acct.getCurrencyType().equals(security)) {
                long shares = acct.getBalance();
                if (shares != 0) {
                    double currentPrice = 1.0 / security.getRelativeRate(date);
                    long totalValueBase = CurrencyUtil.convertValue(shares, security, base, date);

                    array.addObject(new JsonObjectBuilder()
                        .put("account_name", acct.getParentAccount().getFullAccountName())
                        .put("shares", security.formatSemiFancy(shares, '.'))
                        .put("current_price", currentPrice)
                        .put("total_value_base", CurrencyFormatter.toDecimal(totalValueBase, base)));
                }
            }
            addHoldings(acct, security, base, date, array);
        }
    }

    private CurrencyType findSecurity(AccountBook book, String ticker, String id) {
        CurrencyTable table = book.getCurrencies();
        if (id != null) {
            return table.getCurrencyByIDString(id);
        }
        for (CurrencyType curr : table.getAllCurrencies()) {
            if (ticker.equalsIgnoreCase(curr.getTickerSymbol())) {
                return curr;
            }
        }
        return null;
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
