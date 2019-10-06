package com.waslak.waslak.models;

import java.io.Serializable;

public class RequestModel implements Serializable {

    String id;
    String city;
    String address;
    String longitude;
    String latitude;
    String description;
    String name;
    String status;
    String created;
    String updated;
    String user_id;
    String country;
    String price;
    String image;
    String views;
    String deliveryId;
    String duration;
    String shopId;
    String detail;
    String longitudeUpdate;
    String latitudeUpdate;
    String shopName;
    UserModel delivery;
    String note;
    ShopModel shop;
    UserModel user;
    boolean deleteStatus;
    String promo;
    String userRequestLon;
    String userRequestLat;
    String userAddress;
    String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public RequestModel() {
    }

    public String getUserRequestLon() {
        return userRequestLon;
    }

    public void setUserRequestLon(String userRequestLon) {
        this.userRequestLon = userRequestLon;
    }

    public String getUserRequestLat() {
        return userRequestLat;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public void setUserRequestLat(String userRequestLat) {
        this.userRequestLat = userRequestLat;
    }

    public RequestModel(String id, String city, String address, String longitude, String latitude, String description, String name, String status, String created, String updated, String user_id, String country, String price, String image, String views, String deliveryId, String duration, String shopId, String detail, String longitudeUpdate, String latitudeUpdate, String shopName, UserModel delivery, String note, ShopModel shop, UserModel user, boolean deleteStatus, String promo) {
        this.id = id;
        this.city = city;
        this.address = address;
        this.longitude = longitude;
        this.latitude = latitude;
        this.description = description;
        this.name = name;
        this.status = status;
        this.created = created;
        this.updated = updated;
        this.user_id = user_id;
        this.country = country;
        this.price = price;
        this.image = image;
        this.views = views;
        this.deliveryId = deliveryId;
        this.duration = duration;
        this.shopId = shopId;
        this.detail = detail;
        this.longitudeUpdate = longitudeUpdate;
        this.latitudeUpdate = latitudeUpdate;
        this.shopName = shopName;
        this.delivery = delivery;
        this.note = note;
        this.shop = shop;
        this.user = user;
        this.deleteStatus = deleteStatus;
        this.promo = promo;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setViews(String views) {
        this.views = views;
    }

    public void setDeliveryId(String deliveryId) {
        this.deliveryId = deliveryId;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public void setLongitudeUpdate(String longitudeUpdate) {
        this.longitudeUpdate = longitudeUpdate;
    }

    public void setLatitudeUpdate(String latitudeUpdate) {
        this.latitudeUpdate = latitudeUpdate;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public void setDelivery(UserModel delivery) {
        this.delivery = delivery;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setShop(ShopModel shop) {
        this.shop = shop;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    public void setDeleteStatus(boolean deleteStatus) {
        this.deleteStatus = deleteStatus;
    }

    public boolean isDeleteStatus() {
        return deleteStatus;
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

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getCreated() {
        return created;
    }

    public String getUpdated() {
        return updated;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getCountry() {
        return country;
    }

    public String getPrice() {
        return price;
    }

    public String getImage() {
        return image;
    }

    public String getViews() {
        return views;
    }

    public String getDeliveryId() {
        return deliveryId;
    }

    public String getDuration() {
        return duration;
    }

    public String getShopId() {
        return shopId;
    }

    public String getDetail() {
        return detail;
    }

    public String getLongitudeUpdate() {
        return longitudeUpdate;
    }

    public String getLatitudeUpdate() {
        return latitudeUpdate;
    }

    public String getShopName() {
        return shopName;
    }

    public UserModel getDelivery() {
        return delivery;
    }

    public String getNote() {
        return note;
    }

    public ShopModel getShop() {
        return shop;
    }

    public UserModel getUser() {
        return user;
    }

    public String getPromo() {
        return promo;
    }

    public void setPromo(String promo) {
        this.promo = promo;
    }
}
