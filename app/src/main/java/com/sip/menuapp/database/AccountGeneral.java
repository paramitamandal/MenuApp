package com.sip.menuapp.database;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import com.sip.menuapp.ItemContract;

public final class AccountGeneral {
    private static final String ACCOUNT_TYPE = "com.sip.menuapp.syncaccount";

    /**
     * This is the name that appears in the Android 'Accounts' settings.
     */
    private static final String ACCOUNT_NAME = "Example Sync";


    /**
     * Gets the standard sync account for our app.
     * @return {@link Account}
     */
    public static Account getAccount() {
        return new Account(ACCOUNT_NAME, ACCOUNT_TYPE);
    }

    /**
     * Creates the standard sync account for our app.
     * @param c {@link Context}
     */
    public static void createSyncAccount(Context c) {
        // Flag to determine if this is a new account or not
        boolean created = false;

        // Get an account and the account manager
        Account account = getAccount();
        AccountManager manager = (AccountManager)c.getSystemService(Context.ACCOUNT_SERVICE);

        // Attempt to explicitly create the account with no password or extra data
        if (manager.addAccountExplicitly(account, null, null)) {
            final String AUTHORITY = ItemContract.CONTENT_AUTHORITY;
            final long SYNC_FREQUENCY = 60 * 60 * 24; // 1 * 24 hour or 1 Day

            // Inform the system that this account supports sync
            ContentResolver.setIsSyncable(account, AUTHORITY, 1);

            // Inform the system that this account is eligible for auto sync when the network is up
            ContentResolver.setSyncAutomatically(account, AUTHORITY, true);

            // Recommend a schedule for automatic synchronization. The system may modify this based
            // on other scheduled syncs and network utilization.
            ContentResolver.addPeriodicSync(account, AUTHORITY, new Bundle(), SYNC_FREQUENCY);

            created = true;
        }

        // Force a sync if the account was just created
        if (created) {
            SyncAdapter.performSync();
        }
    }
}
