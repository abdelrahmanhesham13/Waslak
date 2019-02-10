package com.waslak.waslak.models;

public class OfferModel {
    String id;
    String deliveryId;
    String userId;
    String price;
    String description;
    String status;
    String longitude;
    String latitude;
    String address;
    String updated;
    String created;
    String requestId;
    String name;
    String duration;
    String rating;
    String image;
    String deliveryLongitude;
    String deliveryLatitude;
    String deliveryAddress;
    String userLongitude;
    String userLatitude;
    String userAddress;
    RequestOfferModel request;
    UserModel customer;
    UserModel delivery;

    public OfferModel(String id, String deliveryId, String userId, String price, String description, String status, String longitude, String latitude, String address, String updated, String created, String requestId, String name, String duration, String rating, String image, String deliveryLongitude, String deliveryLatitude, String deliveryAddress, String userLongitude, String userLatitude, String userAddress, RequestOfferModel request, UserModel customer, UserModel delivery) {
        this.id = id;
        this.deliveryId = deliveryId;
        this.userId = userId;
        this.price = price;
        this.description = description;
        this.status = status;
        this.longitude = longitude;
        this.latitude = latitude;
        this.address = address;
        this.updated = updated;
        this.created = created;
        this.requestId = requestId;
        this.name = name;
        this.duration = duration;
        this.rating = rating;
        this.image = image;
        this.deliveryLongitude = deliveryLongitude;
        this.deliveryLatitude = deliveryLatitude;
        this.deliveryAddress = deliveryAddress;
        this.userLongitude = userLongitude;
        this.userLatitude = userLatitude;
        this.userAddress = userAddress;
        this.request = request;
        this.customer = customer;
        this.delivery = delivery;
    }

    public String getId() {
        return id;
    }

    public String getDeliveryId() {
        return deliveryId;
    }

    public String getUserId() {
        return userId;
    }

    public String getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getAddress() {
        return address;
    }

    public String getUpdated() {
        return updated;
    }

    public String getCreated() {
        return created;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getName() {
        return name;
    }

    public String getDuration() {
        return duration;
    }

    public String getRating() {
        return rating;
    }

    public String getImage() {
        return image;
    }

    public String getDeliveryLongitude() {
        return deliveryLongitude;
    }

    public String getDeliveryLatitude() {
        return deliveryLatitude;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public String getUserLongitude() {
        return userLongitude;
    }

    public String getUserLatitude() {
        return userLatitude;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public RequestOfferModel getRequest() {
        return request;
    }

    public UserModel getCustomer() {
        return customer;
    }

    public UserModel getDelivery() {
        return delivery;
    }
}
