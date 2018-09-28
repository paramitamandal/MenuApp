package com.sip.menuapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.sip.menuapp.ItemContract;

public class DatabaseClient extends SQLiteOpenHelper {
    private static volatile DatabaseClient instance;
    private final SQLiteDatabase db;


    private DatabaseClient(Context c) {
        super(c, ItemContract.DB_NAME, null, ItemContract.DB_VERSION);
        this.db = getWritableDatabase();
    }

    /**
     * We use a Singleton to prevent leaking the SQLiteDatabase or Context.
     * @return {@link DatabaseClient}
     */
    public static DatabaseClient getInstance(Context c) {
        if (instance == null) {
            synchronized (DatabaseClient.class) {
                if (instance == null) {
                    instance = new DatabaseClient(c);
                }
            }
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create any SQLite tables here
        createItemsTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Update any SQLite tables here
        db.execSQL("DROP TABLE IF EXISTS [" + ItemContract.Items.NAME + "];");
        onCreate(db);
    }

    /**
     * Provide access to our database.
     */
    public SQLiteDatabase getDb() {
        return db;
    }

    /**
     * Creates our 'articles' SQLite database table.
     * @param db {@link SQLiteDatabase}
     */
    private void createItemsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE [" + ItemContract.Items.NAME + "] ([" +
                ItemContract.Items.ITEM_ID + "] INT UNIQUE PRIMARY KEY,[" +
                ItemContract.Items.ITEM_CATEGORY + "] TEXT NOT NULL,[" +
                ItemContract.Items.ITEM_NAME + "] TEXT NOT NULL,[" +
                ItemContract.Items.ITEM_DESCRIPTION + "] TEXT,[" +
                ItemContract.Items.ITEM_VIDEO_PATH + "] TEXT,[" +
                ItemContract.Items.ITEM_PRICE + "] TEXT);");

    }
}
