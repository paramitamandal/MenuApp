package com.menuapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class SyncService extends Service {
    /**
     * Lock use to synchronize instantiation of SyncAdapter.
     */
    private static final Object SyncAdapterLock = new Object();
    public static final String SYNC_FINISHED = "Sync Finished";
    private static SyncAdapter syncAdapter;


    @Override
    public void onCreate() {
        // SyncAdapter is not Thread-safe
        synchronized (SyncAdapterLock) {
            if (syncAdapter == null) {
                // Instantiate our SyncAdapter
                syncAdapter = new SyncAdapter(this, false);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Return our SyncAdapter's IBinder
        return syncAdapter.getSyncAdapterBinder();
    }
}
