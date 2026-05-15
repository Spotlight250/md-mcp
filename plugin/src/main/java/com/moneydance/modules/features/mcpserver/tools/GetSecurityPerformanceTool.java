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
        return "Deep-dive analysis of a security's actual performance. Includes cost basis, unrealized gain/loss, ROI, and transaction history.";
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
                "\"required\":[\"ticker\"]," +
                "\"description\":\"Returns performance metrics: cost basis, unrealized gain/loss, and annualized ROI where available.\"" +
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

        CurrencyType security = com.moneydance.modules.features.mcpserver.utils.SecurityHelper.findSecurity(book, ticker, securityId);
        if (security == null) return errorResponse("Security not found");

        CurrencyType base = book.getCurrencies().getBaseType();
        int today = DateUtil.getToday();

        // 1. Find all accounts holding this security
        java.util.List<Account> securityAccounts = new java.util.ArrayList<>();
        if (accountId != null) {
            Account acct = book.getAccountByUUID(accountId);
            if (acct != null && security.equals(acct.getCurrencyType())) {
                securityAccounts.add(acct);
            }
        } else {
            // Scan all accounts for this security
            java.util.List<Account> allAccounts = book.getRootAccount().getSubAccounts();
            findSecurityAccounts(book.getRootAccount(), security, securityAccounts);
        }

        long totalShares = 0;
        long totalCostBasisBase = 0;
        long totalValueBase = 0;

        JsonArrayBuilder historyArray = new JsonArrayBuilder();

        for (Account acct : securityAccounts) {
            long shares = acct.getBalance();
            totalShares += shares;
            
            // Get native cost basis
            long basis = InvestUtil.getCostBasis(acct);
            // Convert basis (in parent currency) to base currency
            long basisBase = CurrencyUtil.convertValue(basis, acct.getParentAccount().getCurrencyType(), base, today);
            totalCostBasisBase += basisBase;

            // Current value in base
            long valBase = CurrencyUtil.convertValue(shares, security, base, today);
            totalValueBase += valBase;

            // Collect transactions for this account
            TransactionSet txnSet = book.getTransactionSet();
            TxnSet txns = txnSet.getTransactionsForAccount(acct);
            for (AbstractTxn txn : txns) {
                double price = 1.0 / security.getRelativeRate(txn.getDateInt());
                historyArray.addObject(SecurityPerformanceFormatter.formatTransaction(
                    DateUtil.encodeIsoDate(txn.getDateInt()),
                    txn.getDescription(),
                    security.formatSemiFancy(txn.getValue(), '.'),
                    String.valueOf(price)));
            }
        }

        double currentPrice = 1.0 / security.getRelativeRate(today);
        long unrealizedGain = totalValueBase - totalCostBasisBase;
        
        // Simple ROI calculation: (Gain / Basis) * 100
        // Note: This is a simplified ROI. For accurate ROI, we'd need cash flow analysis.
        String roiStr = "N/A";
        if (totalCostBasisBase != 0) {
            double roi = ((double) unrealizedGain / (double) totalCostBasisBase) * 100.0;
            roiStr = String.format("%.2f%%", roi);
        }

        return SecurityPerformanceFormatter.formatResponse(
            ticker,
            security.formatSemiFancy(totalShares, '.'),
            currentPrice,
            base.formatSemiFancy(totalValueBase, '.'),
            base.formatSemiFancy(totalCostBasisBase, '.'),
            base.formatSemiFancy(unrealizedGain, '.'),
            roiStr,
            historyArray);
    }

    private void findSecurityAccounts(Account parent, CurrencyType security, java.util.List<Account> results) {
        for (Account sub : parent.getSubAccounts()) {
            if (security.equals(sub.getCurrencyType())) {
                results.add(sub);
            }
            findSecurityAccounts(sub, security, results);
        }
    }



}
