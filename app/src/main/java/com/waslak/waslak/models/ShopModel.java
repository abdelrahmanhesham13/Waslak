package com.waslak.waslak.models;

import java.io.Serializable;
import java.util.ArrayList;

public class ShopModel implements Serializable {
    String id;
    String city;
    String address;
    String lon;
    String lat;
    String description;
    String name;
    String approved;
    String created;
    String updated;
    String userId;
    String country;
    String region;
    String image;
    String views;
    String distance;
    String delivery_count;
    ArrayList<String> time;
    double rating;


    public ShopModel() {}

    public ShopModel(String id, String city, String address, String lon, String lat, String description, String name, String approved, String created, String updated, String userId, String country, String region, String image, String views, ArrayList<String> time) {
        this.id = id;
        this.city = city;
        this.address = address;
        this.lon = lon;
        this.lat = lat;
        this.description = description;
        this.name = name;
        this.approved = approved;
        this.created = created;
        this.updated = updated;
        this.userId = userId;
        this.country = country;
        this.region = region;
        this.image = image;
        this.views = views;
        this.time = time;
    }


    public ShopModel(String id, String city, String address, String lon, String lat, String description, String name, String approved, String created, String updated, String userId, String country, String region, String image, String views, ArrayList<String> time,double rating) {
        this.id = id;
        this.city = city;
        this.address = address;
        this.lon = lon;
        this.lat = lat;
        this.description = description;
        this.name = name;
        this.approved = approved;
        this.created = created;
        this.updated = updated;
        this.userId = userId;
        this.country = country;
        this.region = region;
        this.image = image;
        this.views = views;
        this.time = time;
        this.rating = rating;
    }



    public void setDelivery_count(String delivery_count) {
        this.delivery_count = delivery_count;
    }

    public String getDelivery_count() {
        return delivery_count;
    }

    public ArrayList<String> getTime() {
        return time;
    }

    public String getId() {
        return id;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }

    public String getLon() {
        return lon;
    }

    public String getLat() {
        return lat;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getApproved() {
        return approved;
    }

    public String getCreated() {
        return created;
    }

    public String getUpdated() {
        return updated;
    }

    public String getUserId() {
        return userId;
    }

    public String getCountry() {
        return country;
    }

    public String getRegion() {
        return region;
    }

    public String getImage() {
        return image;
    }

    public String getViews() {
        return views;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
}
