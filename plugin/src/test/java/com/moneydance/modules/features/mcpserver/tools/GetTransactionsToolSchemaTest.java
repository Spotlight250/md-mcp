package com.moneydance.modules.features.mcpserver.tools;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GetTransactionsToolSchemaTest {

    @Test
    void testSchemaDocumentation() {
        GetTransactionsTool tool = new GetTransactionsTool();
        
        // Ensure the description mentions the new high-fidelity fields
        String description = tool.getDescription();
        assertTrue(description.contains("splits"), "Description should mention splits");
        assertTrue(description.contains("status"), "Description should mention status");
        assertTrue(description.contains("tags"), "Description should mention tags");
    }
}
