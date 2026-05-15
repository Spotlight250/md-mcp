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
        return "Queries historical transactions across accounts with optional filtering. Returns details including splits, tags, and clearing status.";
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
                "\"required\":[\"start_date\",\"end_date\"]," +
                "\"description\":\"Returns transactions including: date, amount, payee, account, category, status (Uncleared, Reconciling, Cleared), tags (array), and splits (if multi-split).\"" +
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

        com.infinitekind.moneydance.model.DateRange range = 
            new com.infinitekind.moneydance.model.DateRange(startDate, endDate);
        
        com.infinitekind.moneydance.model.TxnSearch search = 
            com.infinitekind.moneydance.model.TxnUtil.getSearch(range);

        JsonArrayBuilder txnsArray = new JsonArrayBuilder();
        int count = 0;

        TransactionSet txnSet = book.getTransactionSet();
        com.infinitekind.moneydance.model.TxnSet txns = txnSet.getTransactions(search);

        for (AbstractTxn txn : txns) {
            if (count >= MAX_TRANSACTIONS) break;
            
            // Account filter (including descendants)
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

            // Status mapping
            String status = "Uncleared";
            byte s = txn.getStatus();
            if (s == AbstractTxn.STATUS_CLEARED) status = "Cleared";
            else if (s == AbstractTxn.STATUS_RECONCILING) status = "Reconciling";
            txnObj.put("status", status);

            // Tags
            java.util.List<String> keywords = txn.getKeywords();
            if (keywords != null && !keywords.isEmpty()) {
                JsonArrayBuilder tagArray = new JsonArrayBuilder();
                for (String kw : keywords) tagArray.add(kw);
                txnObj.putArray("tags", tagArray);
            }

            // Check number
            String checkNum = txn.getCheckNumber();
            if (checkNum != null && !checkNum.trim().isEmpty()) {
                txnObj.put("check_number", checkNum);
            }

            if (txn instanceof ParentTxn) {
                ParentTxn p = (ParentTxn) txn;
                txnObj.put("payee", p.getDescription());
                txnObj.put("memo", p.getMemo());
                
                if (p.getSplitCount() > 0) {
                    JsonArrayBuilder splitsArray = new JsonArrayBuilder();
                    for (int i = 0; i < p.getSplitCount(); i++) {
                        SplitTxn split = p.getSplit(i);
                        splitsArray.addObject(new JsonObjectBuilder()
                            .put("category", split.getAccount().getFullAccountName())
                            .put("category_id", split.getAccount().getUUID())
                            .put("amount", CurrencyFormatter.toDecimal(split.getValue(), split.getAccount().getCurrencyType()))
                            .put("memo", split.getDescription()));
                    }
                    txnObj.putArray("splits", splitsArray);
                    
                    if (p.getSplitCount() == 1) {
                        txnObj.put("category", p.getSplit(0).getAccount().getFullAccountName());
                        txnObj.put("category_id", p.getSplit(0).getAccount().getUUID());
                    } else {
                        txnObj.put("category", "-- Split --");
                    }
                }
            } else if (txn instanceof SplitTxn) {
                SplitTxn sTxn = (SplitTxn) txn;
                txnObj.put("payee", sTxn.getParentTxn().getDescription());
                txnObj.put("memo", sTxn.getDescription());
                txnObj.put("category", sTxn.getParentTxn().getAccount().getFullAccountName());
                txnObj.put("category_id", sTxn.getParentTxn().getAccount().getUUID());
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

}
