package com.moneydance.modules.features.mcpserver.tools;

import com.moneydance.apps.md.controller.FeatureModuleContext;
import com.infinitekind.moneydance.model.*;
import com.moneydance.modules.features.mcpserver.json.JsonArrayBuilder;
import com.moneydance.modules.features.mcpserver.json.JsonObjectBuilder;
import com.moneydance.modules.features.mcpserver.json.JsonParser;
import com.moneydance.modules.features.mcpserver.utils.DateUtil;

import java.util.Iterator;

/**
 * Implementation of the 'get_transactions' tool.
 */
public class GetTransactionsTool implements McpTool {

    private static final int MAX_TRANSACTIONS = 5000;

    @Override
    public String getName() {
        return "get_transactions";
    }

    @Override
    public String getDescription() {
        return "Queries historical transactions across accounts with optional filtering.";
    }

    @Override
    public String getInputSchema() {
        return "{" +
                "\"type\":\"object\"," +
                "\"properties\":{" +
                "\"start_date\":{\"type\":\"string\",\"description\":\"ISO date (YYYY-MM-DD)\"}," +
                "\"end_date\":{\"type\":\"string\",\"description\":\"ISO date (YYYY-MM-DD)\"}," +
                "\"account_id\":{\"type\":\"string\",\"description\":\"Optional account UUID\"}," +
                "\"category_id\":{\"type\":\"string\",\"description\":\"Optional category UUID\"}," +
                "\"payee_match\":{\"type\":\"string\",\"description\":\"Optional substring match for payee\"}" +
                "}," +
                "\"required\":[\"start_date\",\"end_date\"]" +
                "}";
    }

    @Override
    public String execute(String paramsJson, FeatureModuleContext ctx) {
        if (ctx == null) return errorResponse("No Moneydance context");
        AccountBook book = ctx.getCurrentAccountBook();
        if (book == null) return errorResponse("No data file open");

        int startDate = DateUtil.decodeIsoDate(JsonParser.getString(paramsJson, "start_date"));
        int endDate = DateUtil.decodeIsoDate(JsonParser.getString(paramsJson, "end_date"));
        String accountId = JsonParser.getString(paramsJson, "account_id");
        String categoryId = JsonParser.getString(paramsJson, "category_id");
        String payeeMatch = JsonParser.getString(paramsJson, "payee_match");
        if (payeeMatch != null) payeeMatch = payeeMatch.toLowerCase();

        // If accountId is provided, get all its descendants to include in the filter
        java.util.Set<String> accountIds = null;
        if (accountId != null) {
            Account targetAcct = book.getAccountByUUID(accountId);
            if (targetAcct != null) {
                accountIds = com.moneydance.modules.features.mcpserver.utils.AccountHelper.getAccountAndDescendants(targetAcct);
            } else {
                // If ID is invalid, only match the ID itself (which will likely result in 0 matches)
                accountIds = new java.util.HashSet<>();
                accountIds.add(accountId);
            }
        }

        JsonArrayBuilder txnsArray = new JsonArrayBuilder();
        int count = 0;

        TransactionSet txnSet = book.getTransactionSet();
        // Fallback to full scan - TODO: Optimize with indexed lookup if possible in this MD version
        Iterator<AbstractTxn> it = txnSet.iterator();

        while (it.hasNext() && count < MAX_TRANSACTIONS) {
            AbstractTxn txn = it.next();
            
            // Filters
            if (txn.getDateInt() < startDate || txn.getDateInt() > endDate) continue;
            
            if (accountIds != null && !accountIds.contains(txn.getAccount().getUUID())) {
                continue;
            }
            
            // Category filter usually applies to the 'other' side of a ParentTxn
            if (categoryId != null) {
                boolean matchesCategory = false;
                if (txn instanceof ParentTxn) {
                    ParentTxn p = (ParentTxn) txn;
                    for (int i = 0; i < p.getSplitCount(); i++) {
                        if (categoryId.equals(p.getSplit(i).getAccount().getUUID())) {
                            matchesCategory = true;
                            break;
                        }
                    }
                } else if (txn instanceof SplitTxn) {
                    if (categoryId.equals(((SplitTxn) txn).getParentTxn().getAccount().getUUID())) {
                        matchesCategory = true;
                    }
                }
                if (!matchesCategory) continue;
            }

            if (payeeMatch != null) {
                String payee = "";
                if (txn instanceof ParentTxn) payee = ((ParentTxn) txn).getDescription();
                else if (txn instanceof SplitTxn) payee = ((SplitTxn) txn).getParentTxn().getDescription();
                
                if (payee == null || !payee.toLowerCase().contains(payeeMatch)) continue;
            }

            // Build result object
            JsonObjectBuilder txnObj = new JsonObjectBuilder()
                .put("id", txn.getUUID())
                .put("date", DateUtil.encodeIsoDate(txn.getDateInt()))
                .put("amount", CurrencyFormatter.toDecimal(txn.getValue(), txn.getAccount().getCurrencyType()))
                .put("account", txn.getAccount().getFullAccountName())
                .put("account_id", txn.getAccount().getUUID());

            if (txn instanceof ParentTxn) {
                ParentTxn p = (ParentTxn) txn;
                txnObj.put("payee", p.getDescription());
                txnObj.put("memo", p.getMemo());
                // For ParentTxn, the category is the first split's account (simplified)
                if (p.getSplitCount() > 0) {
                    txnObj.put("category", p.getSplit(0).getAccount().getFullAccountName());
                }
            } else if (txn instanceof SplitTxn) {
                SplitTxn s = (SplitTxn) txn;
                txnObj.put("payee", s.getParentTxn().getDescription());
                txnObj.put("memo", s.getDescription());
                txnObj.put("category", s.getParentTxn().getAccount().getFullAccountName());
            }

            txnsArray.addObject(txnObj);
            count++;
        }

        return new JsonObjectBuilder()
            .putArray("content", new JsonArrayBuilder()
                .addObject(new JsonObjectBuilder()
                    .put("type", "text")
                    .put("text", txnsArray.build())))
            .put("isError", false)
            .build();
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
