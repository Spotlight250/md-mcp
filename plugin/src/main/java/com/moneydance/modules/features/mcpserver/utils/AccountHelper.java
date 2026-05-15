package com.moneydance.modules.features.mcpserver.utils;

import com.infinitekind.moneydance.model.Account;
import java.util.HashSet;
import java.util.Set;

/**
 * Shared logic for account hierarchy and classification.
 */
public class AccountHelper {

    /**
     * Recursively finds all descendants of the given account, including the account itself.
     * Returns a set of UUID strings.
     */
    public static Set<String> getAccountAndDescendants(Account parent) {
        Set<String> uuids = new HashSet<>();
        if (parent == null) return uuids;
        
        uuids.add(parent.getUUID());
        collectDescendants(parent, uuids);
        return uuids;
    }

    private static void collectDescendants(Account parent, Set<String> results) {
        for (int i = 0; i < parent.getSubAccountCount(); i++) {
            Account child = parent.getSubAccount(i);
            results.add(child.getUUID());
            collectDescendants(child, results);
        }
    }

    public static boolean isAsset(Account acct) {
        Account.AccountType type = acct.getAccountType();
        return type == Account.AccountType.BANK || 
               type == Account.AccountType.INVESTMENT || 
               type == Account.AccountType.ASSET ||
               type == Account.AccountType.SECURITY;
    }

    public static boolean isLiability(Account acct) {
        Account.AccountType type = acct.getAccountType();
        return type == Account.AccountType.CREDIT_CARD || 
               type == Account.AccountType.LOAN || 
               type == Account.AccountType.LIABILITY;
    }

    /**
     * Returns true if the account should be included in a net worth calculation.
     * Mirrors the logic used by Moneydance's built-in NetWorthCalculator:
     * - Excludes inactive accounts
     * - Excludes accounts where "Include in Net Worth" is unchecked
     * - Only includes asset or liability account types
     */
    public static boolean shouldIncludeInNetWorth(Account acct) {
        if (acct.getAccountIsInactive()) return false;
        return isAsset(acct) || isLiability(acct);
    }
}
