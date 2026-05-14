package com.moneydance.modules.features.mcpserver.tools;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GetCategoriesToolTest {

    @Test
    void testMetadata() {
        GetCategoriesTool tool = new GetCategoriesTool();
        assertEquals("get_categories", tool.getName());
        assertEquals("Lists all income and expense categories.", tool.getDescription());
    }
}
