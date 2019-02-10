package com.waslak.waslak.models;

public class OrderModel {

    String name;
    String state;
    String expireDate;
    String delivery;
    String deliveryState;
    int image;
    String price;

    public OrderModel(String name, String state, String expireDate, String delivery, String deliveryState, int image, String price) {
        this.name = name;
        this.state = state;
        this.expireDate = expireDate;
        this.delivery = delivery;
        this.deliveryState = deliveryState;
        this.image = image;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public String getState() {
        return state;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public String getDelivery() {
        return delivery;
    }

    public String getDeliveryState() {
        return deliveryState;
    }

    public int getImage() {
        return image;
    }

    public String getPrice() {
        return price;
    }
}
