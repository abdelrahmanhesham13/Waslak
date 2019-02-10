package com.waslak.waslak.models;

public class PendingOrderModel {

    String name;
    String type;
    String description;
    String distance;
    String deliveryDate;

    public PendingOrderModel(String name, String type, String description, String distance, String deliveryDate) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.distance = distance;
        this.deliveryDate = deliveryDate;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getDistance() {
        return distance;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }
}
