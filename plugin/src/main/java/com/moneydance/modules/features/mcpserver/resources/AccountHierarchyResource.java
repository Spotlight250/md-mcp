package com.moneydance.modules.features.mcpserver.resources;

import com.moneydance.apps.md.controller.FeatureModuleContext;
import com.infinitekind.moneydance.model.Account;
import com.infinitekind.moneydance.model.AccountBook;
import com.moneydance.modules.features.mcpserver.json.JsonArrayBuilder;
import com.moneydance.modules.features.mcpserver.json.JsonObjectBuilder;

/**
 * Exposes the full account hierarchy as a JSON resource.
 */
public class AccountHierarchyResource implements McpResource {

    @Override
    public String getUri() {
        return "mcp://moneydance/accounts/hierarchy";
    }

    @Override
    public String getName() {
        return "Account Hierarchy";
    }

    @Override
    public String getDescription() {
        return "A hierarchical list of all accounts including bank, investment, and credit accounts.";
    }

    @Override
    public String getMimeType() {
        return "application/json";
    }

    @Override
    public String read(FeatureModuleContext context) {
        AccountBook book = context.getCurrentAccountBook();
        Account root = book.getRootAccount();
        
        JsonObjectBuilder rootJson = formatAccount(root);
        return rootJson.build();
    }

    private JsonObjectBuilder formatAccount(Account acct) {
        JsonObjectBuilder json = new JsonObjectBuilder()
            .put("id", acct.getUUID())
            .put("name", acct.getAccountName())
            .put("type", acct.getAccountType().name());

        if (acct.getSubAccountCount() > 0) {
            JsonArrayBuilder children = new JsonArrayBuilder();
            for (Account sub : acct.getSubAccounts()) {
                // Skip categories in the account hierarchy resource
                if (sub.getAccountType() != Account.AccountType.INCOME && 
                    sub.getAccountType() != Account.AccountType.EXPENSE) {
                    children.addObject(formatAccount(sub));
                }
            }
            if (children.size() > 0) {
                json.putArray("children", children);
            }
        }
        return json;
    }
}
