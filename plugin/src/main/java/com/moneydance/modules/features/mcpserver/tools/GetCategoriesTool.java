package com.moneydance.modules.features.mcpserver.tools;

import com.moneydance.apps.md.controller.FeatureModuleContext;
import com.infinitekind.moneydance.model.Account;
import com.infinitekind.moneydance.model.AccountBook;
import com.moneydance.modules.features.mcpserver.json.JsonArrayBuilder;
import com.moneydance.modules.features.mcpserver.json.JsonObjectBuilder;

/**
 * Implementation of the 'get_categories' tool.
 */
public class GetCategoriesTool implements McpTool {

    @Override
    public String getName() {
        return "get_categories";
    }

    @Override
    public String getDescription() {
        return "Lists all income and expense categories.";
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

        JsonArrayBuilder categoriesArray = new JsonArrayBuilder();
        com.infinitekind.moneydance.model.AccountIterator it = 
            new com.infinitekind.moneydance.model.AccountIterator(book.getRootAccount());

        while (it.hasNext()) {
            Account acct = it.next();
            if (isCategory(acct)) {
                JsonObjectBuilder catObj = new JsonObjectBuilder()
                    .put("id", acct.getUUID())
                    .put("name", acct.getFullAccountName())
                    .put("type", acct.getAccountType().name())
                    .put("is_inactive", acct.getAccountIsInactive());
                
                Account parentAcct = acct.getParentAccount();
                if (parentAcct != null && parentAcct.getAccountType() != Account.AccountType.ROOT) {
                    catObj.put("parent_id", parentAcct.getUUID());
                }
                
                categoriesArray.addObject(catObj);
            }
        }

        return new JsonObjectBuilder()
            .putArray("content", new JsonArrayBuilder()
                .addObject(new JsonObjectBuilder()
                    .put("type", "text")
                    .put("text", categoriesArray.build())))
            .put("isError", false)
            .build();
    }


    private boolean isCategory(Account acct) {
        Account.AccountType type = acct.getAccountType();
        return type == Account.AccountType.INCOME || type == Account.AccountType.EXPENSE;
    }

}
