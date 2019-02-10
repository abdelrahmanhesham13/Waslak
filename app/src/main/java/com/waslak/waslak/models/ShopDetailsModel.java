package com.waslak.waslak.models;

import java.util.ArrayList;

public class ShopDetailsModel extends ShopModel {

    String body;
    ArrayList<String> images;

    public ShopDetailsModel(String id, String city, String address, String lon, String lat, String description, String name, String approved, String created, String updated, String userId, String country, String region, String image, String views, String body, ArrayList<String> images,ArrayList<String> time) {
        super(id, city, address, lon, lat, description, name, approved, created, updated, userId, country, region, image, views,time);
        this.body = body;
        this.images = images;
    }

    public String getBody() {
        return body;
    }

    public ArrayList<String> getImages() {
        return images;
    }
}
