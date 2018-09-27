package com.menuapp;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "SYNC_ADAPTER";
    private Context context;
    /**
     * This gives us access to our local data source.
     */
    private final ContentResolver resolver;


    public SyncAdapter(Context c, boolean autoInit) {
        this(c, autoInit, false);
    }

    public SyncAdapter(Context c, boolean autoInit, boolean parallelSync) {
        super(c, autoInit, parallelSync);
        this.resolver = c.getContentResolver();
        this.context = c;
    }

    /**
     * This method is run by the Android framework, on a new Thread, to perform a sync.
     * @param account Current account
     * @param extras Bundle extras
     * @param authority Content authority
     * @param provider {@link ContentProviderClient}
     * @param syncResult Object to write stats to
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.w(TAG, "Starting synchronization...");

        try {
            // Synchronize our news feed
            syncNewsFeed(syncResult);

            // Add any other things you may want to sync
            Intent i = new Intent(SyncService.SYNC_FINISHED);
            this.context.sendBroadcast(i);

        } catch (IOException ex) {
            Log.e(TAG, "Error synchronizing!", ex);
            syncResult.stats.numIoExceptions++;
        } catch (JSONException ex) {
            Log.e(TAG, "Error synchronizing!", ex);
            syncResult.stats.numParseExceptions++;
        } catch (RemoteException |OperationApplicationException ex) {
            Log.e(TAG, "Error synchronizing!", ex);
            syncResult.stats.numAuthExceptions++;
        }

        Log.w(TAG, "Finished synchronization!");
    }

    /**
     * Performs synchronization of our pretend news feed source.
     * @param syncResult Write our stats to this
     */
    private void syncNewsFeed(SyncResult syncResult) throws IOException, JSONException, RemoteException, OperationApplicationException {
        final String rssFeedEndpoint = "http://192.168.59.3:8080/api/getRecords";
//        final String rssFeedEndpoint = "http://192.168.1.12:8080/api/getRecords";

        // We need to collect all the network items in a hash table
        Log.i(TAG, "Fetching server entries...");
        Map<String, Item> networkEntries = new HashMap<>();

        // Parse the pretend json news feed
        String jsonFeed = download(rssFeedEndpoint);
        System.out.println("Records\n"+jsonFeed);
        String jsonString = jsonFeed.substring(15,jsonFeed.indexOf("]]")) + "]";

        JSONArray jsonItem = new JSONArray(jsonString);
        for (int i = 0; i < jsonItem.length(); i++) {
            Item item = ItemParser.parse(jsonItem.optJSONObject(i));
            networkEntries.put(item.getName(), item);
        }

        // Create list for batching ContentProvider transactions
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();

        // Compare the hash table of network entries to all the local entries
        Log.i(TAG, "Fetching local entries...");
        Cursor c = resolver.query(ItemContract.Items.CONTENT_URI, null, null, null, null, null);
//        assert c != null;

        if (null == c) {
            /*
             * Insert code here to handle the error. Be sure not to use the cursor! You may want to
             * call android.util.Log.e() to log this error.
             *
             */
// If the Cursor is empty, the provider found no matches
        } else if (c.getCount() < 1) {

            // Add all the new entries
            for (Item item : networkEntries.values()) {
                Log.i(TAG, "Scheduling insert: " + item.getName());
                batch.add(ContentProviderOperation.newInsert(ItemContract.Items.CONTENT_URI)
                        .withValue(ItemContract.Items.ITEM_ID, item.getId())
                        .withValue(ItemContract.Items.ITEM_NAME, item.getName())
                        .withValue(ItemContract.Items.ITEM_DESCRIPTION, item.getDescription())
                        .withValue(ItemContract.Items.ITEM_VIDEO_PATH, item.getVideoPath())
                        .withValue(ItemContract.Items.ITEM_PRICE, item.getPrice())
                        .withValue(ItemContract.Items.ITEM_CATEGORY, item.getCategory())
                        .build());
                syncResult.stats.numInserts++;
            }
        } else {
            // Insert code here to do something with the results
            c.moveToFirst();

            int id;
            String name;
            String description;
            String videoPath;
            String price;
            String category;
            Item found;
            for (int i = 0; i < c.getCount(); i++) {
                syncResult.stats.numEntries++;

                // Create local item entry
                id = c.getInt(c.getColumnIndex(ItemContract.Items.ITEM_ID));
                name = c.getString(c.getColumnIndex(ItemContract.Items.ITEM_NAME));
                description = c.getString(c.getColumnIndex(ItemContract.Items.ITEM_DESCRIPTION));
                videoPath = c.getString(c.getColumnIndex(ItemContract.Items.ITEM_VIDEO_PATH));
                price = c.getString(c.getColumnIndex(ItemContract.Items.ITEM_PRICE));
                category = c.getString(c.getColumnIndex(ItemContract.Items.ITEM_CATEGORY));


                // Try to retrieve the local entry from network entries
                found = networkEntries.get(id);
                if (found != null) {
                    // The entry exists, remove from hash table to prevent re-inserting it
                    networkEntries.remove(id);

                    // Check to see if it needs to be updated
                    if (!name.equals(found.getName())
                            || !description.equals(found.getDescription())
                            || !price.equals(found.getPrice())
                            || !category.equals(found.getCategory())) {
                        // Batch an update for the existing record
                        Log.i(TAG, "Scheduling update: " + name);
                        batch.add(ContentProviderOperation.newUpdate(ItemContract.Items.CONTENT_URI)
                                .withSelection(ItemContract.Items.ITEM_ID + "='" + id + "'", null)
                                .withValue(ItemContract.Items.ITEM_NAME, found.getName())
                                .withValue(ItemContract.Items.ITEM_DESCRIPTION, found.getDescription())
                                .withValue(ItemContract.Items.ITEM_VIDEO_PATH, found.getVideoPath())
                                .withValue(ItemContract.Items.ITEM_PRICE, found.getPrice())
                                .withValue(ItemContract.Items.ITEM_CATEGORY, found.getCategory())
                                .build());
                        syncResult.stats.numUpdates++;
                    }
                } else {
                    // Entry doesn't exist, remove it from the local database
                    Log.i(TAG, "Scheduling delete: " + name);
                    batch.add(ContentProviderOperation.newDelete(ItemContract.Items.CONTENT_URI)
                            .withSelection(ItemContract.Items.ITEM_ID + "='" + id + "'", null)
                            .build());
                    syncResult.stats.numDeletes++;
                }
                c.moveToNext();
            }
            c.close();
        }


//        if(c != null) {
//            c.moveToFirst();
//
//            int id;
//            String name;
//            String description;
//            String videoPath;
//            String price;
//            String category;
//            Item found;
//            for (int i = 0; i < c.getCount(); i++) {
//                syncResult.stats.numEntries++;
//
//                // Create local article entry
//                id = c.getInt(c.getColumnIndex(ItemContract.Items.ITEM_ID));
//                name = c.getString(c.getColumnIndex(ItemContract.Items.ITEM_NAME));
//                description = c.getString(c.getColumnIndex(ItemContract.Items.ITEM_DESCRIPTION));
//                videoPath = c.getString(c.getColumnIndex(ItemContract.Items.ITEM_VIDEO_PATH));
//                price = c.getString(c.getColumnIndex(ItemContract.Items.ITEM_PRICE));
//                category = c.getString(c.getColumnIndex(ItemContract.Items.ITEM_CATEGORY));
//
//
//                // Try to retrieve the local entry from network entries
//                found = networkEntries.get(id);
//                if (found != null) {
//                    // The entry exists, remove from hash table to prevent re-inserting it
//                    networkEntries.remove(id);
//
//                    // Check to see if it needs to be updated
//                    if (!name.equals(found.getName())
//                            || !description.equals(found.getDescription())
//                            || !price.equals(found.getPrice())
//                            || !category.equals(found.getCategory())) {
//                        // Batch an update for the existing record
//                        Log.i(TAG, "Scheduling update: " + name);
//                        batch.add(ContentProviderOperation.newUpdate(ItemContract.Items.CONTENT_URI)
//                                .withSelection(ItemContract.Items.ITEM_ID + "='" + id + "'", null)
//                                .withValue(ItemContract.Items.ITEM_NAME, found.getName())
//                                .withValue(ItemContract.Items.ITEM_DESCRIPTION, found.getDescription())
//                                .withValue(ItemContract.Items.ITEM_VIDEO_PATH, found.getVideoPath())
//                                .withValue(ItemContract.Items.ITEM_PRICE, found.getPrice())
//                                .withValue(ItemContract.Items.ITEM_CATEGORY, found.getCategory())
//                                .build());
//                        syncResult.stats.numUpdates++;
//                    }
//                } else {
//                    // Entry doesn't exist, remove it from the local database
//                    Log.i(TAG, "Scheduling delete: " + name);
//                    batch.add(ContentProviderOperation.newDelete(ItemContract.Items.CONTENT_URI)
//                            .withSelection(ItemContract.Items.ITEM_ID + "='" + id + "'", null)
//                            .build());
//                    syncResult.stats.numDeletes++;
//                }
//                c.moveToNext();
//            }
//            c.close();
//        }
//
//        // Add all the new entries
//        for (Item item : networkEntries.values()) {
//            Log.i(TAG, "Scheduling insert: " + item.getName());
//            batch.add(ContentProviderOperation.newInsert(ItemContract.Items.CONTENT_URI)
//                    .withValue(ItemContract.Items.ITEM_ID, item.getId())
//                    .withValue(ItemContract.Items.ITEM_NAME, item.getName())
//                    .withValue(ItemContract.Items.ITEM_DESCRIPTION, item.getDescription())
//                    .withValue(ItemContract.Items.ITEM_VIDEO_PATH, item.getVideoPath())
//                    .withValue(ItemContract.Items.ITEM_PRICE, item.getPrice())
//                    .withValue(ItemContract.Items.ITEM_CATEGORY, item.getCategory())
//                    .build());
//            syncResult.stats.numInserts++;
//        }

        // Synchronize by performing batch update
        Log.i(TAG, "Merge solution ready, applying batch update...");
        resolver.applyBatch(ItemContract.CONTENT_AUTHORITY, batch);
        resolver.notifyChange(ItemContract.Items.CONTENT_URI, // URI where data was modified
                null, // No local observer
                false); // IMPORTANT: Do not sync to network
    }

    /**
     * A blocking method to stream the server's content and build it into a string.
     * @param url API call
     * @return String response
     */
    private String download(String url) throws IOException {
        // Ensure we ALWAYS close these!
        HttpURLConnection client = null;
        InputStream is = null;

        try {
            // Connect to the server using GET protocol
            URL server = new URL(url);
            client = (HttpURLConnection)server.openConnection();
            client.connect();

            // Check for valid response code from the server
            int status = client.getResponseCode();
            is = (status == HttpURLConnection.HTTP_OK)
                    ? client.getInputStream() : client.getErrorStream();

            // Build the response or error as a string
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String temp; ((temp = br.readLine()) != null);) {
                sb.append(temp);
            }

            return sb.toString();
        } finally {
            if (is != null) { is.close(); }
            if (client != null) { client.disconnect(); }
        }
    }

    /**
     * Manual force Android to perform a sync with our SyncAdapter.
     */
    public static void performSync() {
        Bundle b = new Bundle();
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(AccountGeneral.getAccount(),
                ItemContract.CONTENT_AUTHORITY, b);
    }
}
