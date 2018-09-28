package com.sip.menuapp;

import android.net.Uri;

public final class ItemContract {

    // ContentProvider information
    public static final String CONTENT_AUTHORITY = "com.sip.menuapp.sync";
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    static final String PATH_ITEMS = "items";

    // Database information
    public static final String DB_NAME = "items_db";
    public static final int DB_VERSION = 1;


    /**
     * This represents our SQLite table for our items.
     */
    public static abstract class Items {
        public static final String NAME = "items";
        public static final String ITEM_ID = "itemId";
        public static final String ITEM_NAME = "itemName";
        public static final String ITEM_DESCRIPTION = "itemDescription";
        public static final String ITEM_VIDEO_PATH = "itemVideoPath";
        public static final String ITEM_PRICE = "itemPrice";
        public static final String ITEM_CATEGORY = "itemCategory";

        // ContentProvider information for articles
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ITEMS).build();
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_URI + "/" + PATH_ITEMS;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_URI + "/" + PATH_ITEMS;
    }

}
