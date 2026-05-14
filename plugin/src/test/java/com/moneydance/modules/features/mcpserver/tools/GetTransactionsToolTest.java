package com.moneydance.modules.features.mcpserver.tools;

import org.junit.jupiter.api.Test;
import com.moneydance.modules.features.mcpserver.json.JsonParser;
import static org.junit.jupiter.api.Assertions.*;

public class GetTransactionsToolTest {

    @Test
    void testMetadata() {
        GetTransactionsTool tool = new GetTransactionsTool();
        assertEquals("get_transactions", tool.getName());
    }

    @Test
    void testParameterExtraction() {
        String params = "{\"start_date\":\"2024-01-01\", \"end_date\":\"2024-01-31\", \"account_id\":\"acct1\"}";
        assertEquals("2024-01-01", JsonParser.getString(params, "start_date"));
        assertEquals("2024-01-31", JsonParser.getString(params, "end_date"));
        assertEquals("acct1", JsonParser.getString(params, "account_id"));
    }
}
