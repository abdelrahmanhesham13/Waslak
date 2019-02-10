package com.waslak.waslak.models;

public class StoreModel {

    int image;
    String name;
    String distance;

    public StoreModel(int image, String name, String distance) {
        this.image = image;
        this.name = name;
        this.distance = distance;
    }


    public int getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public String getDistance() {
        return distance;
    }
}
