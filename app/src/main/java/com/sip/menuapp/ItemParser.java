package com.sip.menuapp;

import org.json.JSONObject;

public class ItemParser {

    public static Item parse(JSONObject jsonItem) {
        int id = jsonItem.optInt("Id");
        String name = jsonItem.optString("Name");
//        String description = jsonItem.optString("description");
//        String price = jsonItem.optString("price");
//        String videoPath = jsonItem.optString("videoPath");
        String category = jsonItem.optString("GroupCode");
        return new Item(id, name, null, null, null, category);
//        return new Item(id, name, description, videoPath, price, category);
    }
}
