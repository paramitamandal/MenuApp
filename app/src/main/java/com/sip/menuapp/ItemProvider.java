package com.sip.menuapp;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.sip.menuapp.database.DatabaseClient;

public class ItemProvider extends ContentProvider {
    private static final int ITEM = 1;
    private static final int ITEM_ID = 2;
    private static final int ITEM_CATEGORY = 3;

    private static final UriMatcher uriMatcher;
    private SQLiteDatabase db;

    static {
        // Add all our query types to our UriMatcher
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEMS, ITEM);
        uriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEMS + "/#", ITEM_ID);
    }

    @Override
    public boolean onCreate() {
        this.db = DatabaseClient.getInstance(getContext()).getDb();
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        // Find the MIME type of the results... multiple results or a single result
        switch (uriMatcher.match(uri)) {
            case ITEM:
                return ItemContract.Items.CONTENT_TYPE;
            case ITEM_ID:
                return ItemContract.Items.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Invalid URI!");
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;
        switch (uriMatcher.match(uri)) {
            // Query for multiple article results
            case ITEM:
                cursor = db.query(ItemContract.Items.NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            // Query for single article result
            case ITEM_ID:
                long _id = ContentUris.parseId(uri);
                cursor = db.query(ItemContract.Items.ITEM_ID,
                        projection,
                        ItemContract.Items.ITEM_ID + "=?",
                        new String[] { String.valueOf(_id) },
                        null,
                        null,
                        sortOrder);
                break;

            // Query for single article result
//            case ITEM_CATEGORY:
////                long _id = ContentUris.parseId(uri);
//                cursor = db.query(ItemContract.Items.NAME,
//                        ItemContract.Items.ITEM_CATEGORY,
//                        ItemContract.Items.ITEM_ID + "=?",
//                        new String[] { String.valueOf(_id) },
//                        null,
//                        null,
//                        sortOrder);
//                break;
            default: throw new IllegalArgumentException("Invalid URI!");
        }

        // Tell the cursor to register a content observer to observe changes to the
        // URI or its descendants.
        assert getContext() != null;
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Uri returnUri;
        long _id;

        switch (uriMatcher.match(uri)) {
            case ITEM:
                _id = db.insert(ItemContract.Items.NAME, null, values);
                returnUri = ContentUris.withAppendedId(ItemContract.Items.CONTENT_URI, _id);
                break;
            default: throw new IllegalArgumentException("Invalid URI!");
        }

        // Notify any observers to update the UI
        assert getContext() != null;
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int rows;
        switch (uriMatcher.match(uri)) {
            case ITEM:
                rows = db.update(ItemContract.Items.NAME, values, selection, selectionArgs);
                break;
            default: throw new IllegalArgumentException("Invalid URI!");
        }

        // Notify any observers to update the UI
        if (rows != 0) {
            assert getContext() != null;
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rows;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int rows = 0;
        switch (uriMatcher.match(uri)) {
            case ITEM:
                rows = db.delete(ItemContract.Items.NAME, selection, selectionArgs);
                break;
            default: throw new IllegalArgumentException("Invalid URI!");
        }

        // Notify any observers to update the UI
        if (rows != 0) {
            assert getContext() != null;
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rows;
    }
}
