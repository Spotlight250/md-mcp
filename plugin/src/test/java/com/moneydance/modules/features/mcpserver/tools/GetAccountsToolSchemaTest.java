package com.moneydance.modules.features.mcpserver.tools;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GetAccountsToolSchemaTest {

    @Test
    void testSchemaDocumentation() {
        GetAccountsTool tool = new GetAccountsTool();
        String schema = tool.getInputSchema();
        
        assertTrue(tool.getDescription().contains("ticker"), "Description should mention ticker");
        assertTrue(tool.getDescription().contains("account number"), "Description should mention account number");
        assertTrue(tool.getDescription().contains("notes"), "Description should mention notes");
        
        assertTrue(schema.contains("ticker"), "Schema description should mention ticker");
        assertTrue(schema.contains("account_number"), "Schema description should mention account_number");
    }
}
