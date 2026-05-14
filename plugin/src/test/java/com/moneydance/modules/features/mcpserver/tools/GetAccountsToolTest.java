package com.moneydance.modules.features.mcpserver.tools;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GetAccountsToolTest {

    @Test
    void testGetName() {
        GetAccountsTool tool = new GetAccountsTool();
        assertTrue(tool.getName().equals("get_accounts"));
    }

    @Test
    void testGetDescription() {
        GetAccountsTool tool = new GetAccountsTool();
        assertTrue(tool.getDescription().contains("Lists all accounts"));
    }
    
    // Additional tests for JSON formatting will be added once I define 
    // the internal data-to-json logic.
}
