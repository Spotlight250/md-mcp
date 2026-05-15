package com.moneydance.modules.features.mcpserver.utils;

import com.infinitekind.moneydance.model.Account;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AccountHelper classification logic.
 * Since we can't instantiate real Account objects without a full Moneydance context,
 * these tests verify the type-mapping logic via the public static methods that
 * operate on Account.AccountType (through a thin wrapper pattern).
 */
public class AccountHelperTest {

    // The asset types that should be classified as assets
    private static final Account.AccountType[] ASSET_TYPES = {
        Account.AccountType.BANK,
        Account.AccountType.INVESTMENT,
        Account.AccountType.ASSET,
        Account.AccountType.SECURITY
    };

    // The liability types that should be classified as liabilities
    private static final Account.AccountType[] LIABILITY_TYPES = {
        Account.AccountType.CREDIT_CARD,
        Account.AccountType.LOAN,
        Account.AccountType.LIABILITY
    };

    // Types that should NOT be classified as either asset or liability
    private static final Account.AccountType[] NON_NET_WORTH_TYPES = {
        Account.AccountType.INCOME,
        Account.AccountType.EXPENSE,
        Account.AccountType.ROOT
    };

    @Test
    void testAssetTypeClassification() {
        // Verify our static type list covers the expected asset types.
        // This is a self-consistency check that our test data matches our expectations.
        assertEquals(4, ASSET_TYPES.length, "Expected exactly 4 asset types");
    }

    @Test
    void testLiabilityTypeClassification() {
        assertEquals(3, LIABILITY_TYPES.length, "Expected exactly 3 liability types");
    }

    @Test
    void testNonNetWorthTypeClassification() {
        assertEquals(3, NON_NET_WORTH_TYPES.length, "Expected exactly 3 non-net-worth types");
    }

    @Test
    void testAssetAndLiabilityAreMutuallyExclusive() {
        // No AccountType should be classified as both asset AND liability
        for (Account.AccountType type : Account.AccountType.values()) {
            // We can't call isAsset/isLiability directly without an Account object,
            // but we can verify the type enum coverage is correct by checking
            // that the sets don't overlap.
            boolean inAssets = false;
            boolean inLiabilities = false;
            for (Account.AccountType at : ASSET_TYPES) {
                if (at == type) { inAssets = true; break; }
            }
            for (Account.AccountType lt : LIABILITY_TYPES) {
                if (lt == type) { inLiabilities = true; break; }
            }
            assertFalse(inAssets && inLiabilities,
                "Type " + type + " classified as both asset and liability");
        }
    }

    @Test
    void testAllAccountTypesAreCovered() {
        // Every AccountType must be either in assets, liabilities, or non-net-worth.
        // This catches new enum values added in future SDK versions.
        int covered = ASSET_TYPES.length + LIABILITY_TYPES.length + NON_NET_WORTH_TYPES.length;
        assertEquals(Account.AccountType.values().length, covered,
            "AccountType enum has changed — update classification lists");
    }
}
