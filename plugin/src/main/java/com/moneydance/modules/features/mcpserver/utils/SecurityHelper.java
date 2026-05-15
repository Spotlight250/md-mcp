package com.moneydance.modules.features.mcpserver.utils;

import com.infinitekind.moneydance.model.AccountBook;
import com.infinitekind.moneydance.model.CurrencyType;
import com.infinitekind.moneydance.model.CurrencyTable;

/**
 * Utility methods for security-related operations.
 */
public class SecurityHelper {

    /**
     * Find a security in the given AccountBook by its ticker symbol or UUID.
     * 
     * @param book The AccountBook to search in
     * @param ticker The ticker symbol to search for (case-insensitive)
     * @param id The UUID of the security (if provided, this takes precedence)
     * @return The found CurrencyType, or null if not found
     */
    public static CurrencyType findSecurity(AccountBook book, String ticker, String id) {
        CurrencyTable table = book.getCurrencies();
        if (id != null) {
            return table.getCurrencyByIDString(id);
        }
        if (ticker != null) {
            for (CurrencyType curr : table.getAllCurrencies()) {
                if (ticker.equalsIgnoreCase(curr.getTickerSymbol())) {
                    return curr;
                }
            }
        }
        return null;
    }
}
