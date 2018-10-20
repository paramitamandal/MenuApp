package com.sip.menuapp.database;

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

import com.sip.menuapp.Item;
import com.sip.menuapp.ItemContract;
import com.sip.menuapp.ItemParser;
import com.sip.menuapp.MenuItemListActivity;
import com.sip.menuapp.ResourceConstant;
import com.sip.menuapp.service.SyncService;

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
    public static String serverURL;
    private final ContentResolver resolver;
    private Context context;


    public SyncAdapter(Context context, boolean autoInit) {
        this(context, autoInit, false);
    }

    public SyncAdapter(Context context, boolean autoInit, boolean parallelSync) {
        super(context, autoInit, parallelSync);
        this.resolver = context.getContentResolver();
        this.context = context;
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
            syncNewsFeed(syncResult);
            // Add any other things you may want to sync
            Intent intent = new Intent(SyncService.SYNC_FINISHED);
            this.context.sendBroadcast(intent);
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

    private void syncNewsFeed(SyncResult syncResult) throws IOException, JSONException, RemoteException, OperationApplicationException {
        final String endpoint = ResourceConstant.SERVER_URL + "api/getRecords";

        Log.i(TAG, "Fetching server entries...");
        Map<String, Item> networkEntries = new HashMap<>();

        String jsonFeed = download(endpoint);
        System.out.println("Records\n"+jsonFeed);
        String jsonString = jsonFeed.substring(15,jsonFeed.indexOf("]]")) + "]";

        JSONArray jsonItem = new JSONArray(jsonString);
        for (int i = 0; i < jsonItem.length(); i++) {
            Item item = ItemParser.parse(jsonItem.optJSONObject(i));
            networkEntries.put(item.getName(), item);
        }

        // Create list for batching ContentProvider transactions
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();

        Log.i(TAG, "Fetching local entries...");
        Cursor cursor = resolver.query(ItemContract.Items.CONTENT_URI, null, null, null, null, null);
        if (null == cursor) {
            Log.i(TAG, "No records found...");
        }
        else if (cursor.getCount() < 1) {
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
        }
        else {
            // Insert code here to do something with the results
            cursor.moveToFirst();

            int id;
            String name;
            String description;
            String videoPath;
            String price;
            String category;
            Item found;
            for (int i = 0; i < cursor.getCount(); i++) {
                syncResult.stats.numEntries++;

                // Create local item entry
                id = cursor.getInt(cursor.getColumnIndex(ItemContract.Items.ITEM_ID));
                name = cursor.getString(cursor.getColumnIndex(ItemContract.Items.ITEM_NAME));
                description = cursor.getString(cursor.getColumnIndex(ItemContract.Items.ITEM_DESCRIPTION));
                videoPath = cursor.getString(cursor.getColumnIndex(ItemContract.Items.ITEM_VIDEO_PATH));
                price = cursor.getString(cursor.getColumnIndex(ItemContract.Items.ITEM_PRICE));
                category = cursor.getString(cursor.getColumnIndex(ItemContract.Items.ITEM_CATEGORY));

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
                }
                else {
                    // Entry doesn't exist, remove it from the local database
                    Log.i(TAG, "Scheduling delete: " + name);
                    batch.add(ContentProviderOperation.newDelete(ItemContract.Items.CONTENT_URI)
                            .withSelection(ItemContract.Items.ITEM_ID + "='" + id + "'", null)
                            .build());
                    syncResult.stats.numDeletes++;
                }
                cursor.moveToNext();
            }
            cursor.close();
        }

        // Synchronize by performing batch update
        Log.i(TAG, "Merge solution ready, applying batch update...");
        resolver.applyBatch(ItemContract.CONTENT_AUTHORITY, batch);
        resolver.notifyChange(ItemContract.Items.CONTENT_URI, // URI where data was modified
                null, // No local observer
                false); // IMPORTANT: Do not sync to network
    }

    private String download(String url) throws IOException {
        HttpURLConnection client = null;
        InputStream inputStream = null;

        try {
            URL server = new URL(url);
            client = (HttpURLConnection)server.openConnection();
            client.connect();

            int status = client.getResponseCode();
            inputStream = (status == HttpURLConnection.HTTP_OK)
                    ? client.getInputStream() : client.getErrorStream();

            // Build the response or error as a string
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            for (String temp; ((temp = br.readLine()) != null);) {
                sb.append(temp);
            }

            return sb.toString();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (client != null) {
                client.disconnect();
            }
        }
    }

    public static void performSync() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(AccountGeneral.getAccount(),
                ItemContract.CONTENT_AUTHORITY, bundle);
    }
}
