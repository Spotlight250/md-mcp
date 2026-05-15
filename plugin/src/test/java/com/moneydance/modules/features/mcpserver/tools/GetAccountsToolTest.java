package com.moneydance.modules.features.mcpserver.tools;

import com.moneydance.apps.md.controller.FeatureModuleContext;
import com.infinitekind.moneydance.model.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GetAccountsToolTest {

    @Test
    void testMetadataExtraction() {
        // Setup a pure proxy-based environment
        FeatureModuleContext mockCtx = createProxy(FeatureModuleContext.class, (proxy, method, args) -> {
            if (method.getName().equals("getCurrentAccountBook")) {
                return createAccountBookProxy();
            }
            return null;
        });

        // Tool execution
        GetAccountsTool tool = new GetAccountsTool();
        String result = tool.execute("{}", mockCtx);
        
        // Assertions: verify new fields are present in JSON
        assertTrue(result.contains("\"ticker\":\"TEST\""), "JSON should contain ticker. Result: " + result);
        assertTrue(result.contains("\"account_number\":\"123456\""), "JSON should contain account number. Result: " + result);
        assertTrue(result.contains("\"notes\":\"Sample note\""), "JSON should contain notes. Result: " + result);
    }

    private Object createAccountBookProxy() {
        return createProxy(AccountBook.class, (proxy, method, args) -> {
            if (method.getName().equals("getRootAccount")) {
                return createRootAccountProxy();
            }
            if (method.getName().equals("getCurrencies")) {
                return createCurrencyTableProxy();
            }
            return null;
        });
    }

    private Object createRootAccountProxy() {
        return createProxy(Account.class, (proxy, method, args) -> {
            if (method.getName().equals("getSubAccountCount")) return 1;
            if (method.getName().equals("getSubAccount")) return createTestAccountProxy();
            return null;
        });
    }

    private Object createTestAccountProxy() {
        return createProxy(Account.class, (proxy, method, args) -> {
            switch (method.getName()) {
                case "getUUID": return "test-uuid";
                case "getFullAccountName": return "Test Account";
                case "getAccountType": return Account.AccountType.BANK;
                case "getSubAccountCount": return 0;
                case "getBankAccountNumber": return "123456";
                case "getAccountDescription": return "Sample note";
                case "getCurrencyType": return createCurrencyTypeProxy();
                default: return null;
            }
        });
    }

    private Object createCurrencyTypeProxy() {
        return createProxy(CurrencyType.class, (proxy, method, args) -> {
            if (method.getName().equals("getTickerSymbol")) return "TEST";
            if (method.getName().equals("getIDString")) return "CUR-TEST";
            return null;
        });
    }

    private Object createCurrencyTableProxy() {
        return createProxy(CurrencyTable.class, (proxy, method, args) -> {
            if (method.getName().equals("getBaseType")) return createCurrencyTypeProxy();
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    private <T> T createProxy(Class<T> clazz, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[] { clazz }, handler);
    }
}
