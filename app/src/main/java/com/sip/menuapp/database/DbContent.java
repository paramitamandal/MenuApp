package com.sip.menuapp.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sip.menuapp.Item;
import com.sip.menuapp.ItemContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DbContent {

    private static final List<Item> ITEMS = new ArrayList<Item>();
    public static final Map<String, List<Item>> ITEM_CATEGORY_MAP = new HashMap<String, List<Item>>();

    public static void loadData(Context context){
//        List<String> categorySet = new ArrayList<String>();
//        categorySet.add("Pizza");
//        categorySet.add("Sandwich");
//        categorySet.add("Burger");
//
//        List<Item> itemList = new ArrayList<Item>();
//        itemList.add(new Item(1, "Chicken",  "description :: item made with Chicken", null, "100", null));
//        itemList.add(new Item(2, "Veg",  "description :: item made with Veg", null, "50", null));
//        itemList.add(new Item(3, "Fish",  "description :: item made with Fish", null, "150", null));
//        itemList.add(new Item(1, "Chicken1",  "description :: item made with Chicken", null, "100", null));
//        itemList.add(new Item(2, "Veg1",  "description :: item made with Veg", null, "50", null));
//        itemList.add(new Item(3, "Fish1",  "description :: item made with Fish", null, "150", null));
//        itemList.add(new Item(1, "Chicken2",  "description :: item made with Chicken", null, "100", null));
//        itemList.add(new Item(2, "Veg2",  "description :: item made with Veg", null, "50", null));
//        itemList.add(new Item(3, "Fish2",  "description :: item made with Fish", null, "150", null));
//        for (int i=0; i<categorySet.size(); i++){
//            ITEM_CATEGORY_MAP.put(categorySet.get(i), itemList);
//        }

        List<String> categorySet = fetchItemCategoryFromDatabase(context);
        for (int i=0; i<categorySet.size(); i++){
            String category = categorySet.get(i);
            ITEM_CATEGORY_MAP.put(category, fetchItemsPerCategoryFromDatabase(context, category));
        }

    }


    private static List<String> fetchItemCategoryFromDatabase(Context context) {
        Set<String> categorySet = new HashSet<String>();
        String query = "SELECT "+ ItemContract.Items.ITEM_CATEGORY + " FROM " + ItemContract.Items.NAME;

        SQLiteDatabase database = DatabaseClient.getInstance(context).getReadableDatabase();
        Cursor data = database.rawQuery(query, null);

        while(data.moveToNext()){
            categorySet.add(data.getString(0));
        }
        return new ArrayList<String>(categorySet);
    }

    private static List<Item> fetchItemsPerCategoryFromDatabase(Context context, String category) {
        List<Item> arrayList = new ArrayList<Item>();
        String query = "SELECT * FROM " + ItemContract.Items.NAME + " WHERE " + ItemContract.Items.NAME + "." + ItemContract.Items.ITEM_CATEGORY + " = '" + category+"'";

        SQLiteDatabase database = DatabaseClient.getInstance(context).getReadableDatabase();
        Cursor cursor = database.rawQuery(query, null);

        cursor.moveToFirst();
        int id;
        String name;
        String description;
        String videoPath;
        String price;

        while(cursor.moveToNext()){
            id = cursor.getInt(cursor.getColumnIndex(ItemContract.Items.ITEM_ID));
            name = cursor.getString(cursor.getColumnIndex(ItemContract.Items.ITEM_NAME));
            description = cursor.getString(cursor.getColumnIndex(ItemContract.Items.ITEM_DESCRIPTION));
            videoPath = cursor.getString(cursor.getColumnIndex(ItemContract.Items.ITEM_VIDEO_PATH));
            price = cursor.getString(cursor.getColumnIndex(ItemContract.Items.ITEM_PRICE));
            category = cursor.getString(cursor.getColumnIndex(ItemContract.Items.ITEM_CATEGORY));
            arrayList.add(new Item(id,name,description,videoPath,price,category));
        }
        return arrayList;
    }

}
