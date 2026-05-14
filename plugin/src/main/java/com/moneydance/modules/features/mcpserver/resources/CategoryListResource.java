package com.moneydance.modules.features.mcpserver.resources;

import com.moneydance.apps.md.controller.FeatureModuleContext;
import com.infinitekind.moneydance.model.Account;
import com.infinitekind.moneydance.model.AccountBook;
import com.moneydance.modules.features.mcpserver.json.JsonArrayBuilder;
import com.moneydance.modules.features.mcpserver.json.JsonObjectBuilder;

/**
 * Exposes the income and expense category tree as a JSON resource.
 */
public class CategoryListResource implements McpResource {

    @Override
    public String getUri() {
        return "mcp://moneydance/categories/full";
    }

    @Override
    public String getName() {
        return "Category List";
    }

    @Override
    public String getDescription() {
        return "A full list of income and expense categories, preserving the parent-child structure.";
    }

    @Override
    public String getMimeType() {
        return "application/json";
    }

    @Override
    public String read(FeatureModuleContext context) {
        AccountBook book = context.getCurrentAccountBook();
        Account root = book.getRootAccount();
        
        JsonArrayBuilder categories = new JsonArrayBuilder();
        for (Account sub : root.getSubAccounts()) {
            if (sub.getAccountType() == Account.AccountType.INCOME || 
                sub.getAccountType() == Account.AccountType.EXPENSE) {
                categories.addObject(formatCategory(sub));
            }
        }
        
        return new JsonObjectBuilder()
            .putArray("categories", categories)
            .build();
    }

    private JsonObjectBuilder formatCategory(Account acct) {
        JsonObjectBuilder json = new JsonObjectBuilder()
            .put("id", acct.getUUID())
            .put("name", acct.getAccountName())
            .put("type", acct.getAccountType().name());

        if (acct.getSubAccountCount() > 0) {
            JsonArrayBuilder children = new JsonArrayBuilder();
            for (Account sub : acct.getSubAccounts()) {
                children.addObject(formatCategory(sub));
            }
            json.putArray("children", children);
        }
        return json;
    }
}
