package com.moneydance.modules.features.mcpserver.tools;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GetAccountsToolSchemaTest {

    @Test
    void testSchemaDocumentation() {
        GetAccountsTool tool = new GetAccountsTool();
        String schema = tool.getInputSchema();
        
        // We want to ensure the schema mentions the new output fields in some way, 
        // or at least that we've updated the description.
        // For Phase 1, we'll update the description to mention 'ticker', 'account_number', etc.
        assertTrue(tool.getDescription().contains("ticker"), "Description should mention ticker");
        assertTrue(tool.getDescription().contains("account number"), "Description should mention account number");
    }
}
