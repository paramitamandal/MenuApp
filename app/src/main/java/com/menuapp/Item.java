package com.menuapp;

import android.support.v7.widget.RecyclerView;

public class Item {

    private int id;
    private String name;
    private String description;
    private String videoPath;
    private String price;
    private String category;

    public Item(String name, String description, String price){
        this.id = Integer.parseInt(null);
        this.name = name;
        this.description = description;
        this.videoPath = null;
        this.price = price;
        this.category = null;
    }

    public Item(int id, String name, String description, String videoPath, String price, String category){
       this.id = id;
       this.name = name;
       this.description = description;
       this.videoPath = videoPath;
       this.price = price;
       this.category = category;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public String getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

//    public void setPrice(String price) {
//        this.price = price;
//    }
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//    public void setVideoPath(String videoPath) {
//        this.videoPath = videoPath;
//    }
}
