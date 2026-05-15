package com.moneydance.modules.features.mcpserver.tools;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GetSecurityPerformanceToolSchemaTest {

    @Test
    void testSchemaDocumentation() {
        GetSecurityPerformanceTool tool = new GetSecurityPerformanceTool();
        
        String description = tool.getDescription();
        assertTrue(description.contains("basis"), "Description should mention cost basis");
        assertTrue(description.contains("gain"), "Description should mention gain/loss");
        assertTrue(description.contains("ROI"), "Description should mention ROI");
    }
}
