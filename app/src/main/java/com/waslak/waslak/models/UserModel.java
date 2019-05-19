package com.waslak.waslak.models;

import java.io.Serializable;

public class UserModel implements Serializable {

    String name = "";
    String username = "";
    String token = "";
    String birthDate = "";
    String password = "";
    String mobile = "";
    String longitude = "";
    String latitude = "";
    String city = "";
    String country = "";
    String image = "";
    int activate = 0;
    int role = 0;
    String id = "";
    String gender;
    String rating;
    String orders = "0";
    String comment = "0";
    String delivery = "";
    String balance = "";
    String blocked = "0";
    String credit;
    String advanced = "0";

    public UserModel(String name, String image) {
        this.name = name;
        this.image = image;
    }

    public UserModel() {
    }

    public UserModel(String name, String username, String token, String birthDate, String password, String mobile, String longitude, String latitude, String city, String country, String image, int activate, int role, String id, String gender, String rating, String delivery,String blocked) {
        this.name = name;
        this.username = username;
        this.token = token;
        this.birthDate = birthDate;
        this.password = password;
        this.mobile = mobile;
        this.longitude = longitude;
        this.latitude = latitude;
        this.city = city;
        this.country = country;
        this.image = image;
        this.activate = activate;
        this.role = role;
        this.id = id;
        this.gender = gender;
        this.rating = rating;
        this.delivery = delivery;
        this.blocked = blocked;
    }

    public String getBlocked() {
        return blocked;
    }

    public void setBlocked(String blocked) {
        this.blocked = blocked;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getBalance() {
        return balance;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public String getDelivery() {
        return delivery;
    }

    public void setDelivery(String delivery) {
        this.delivery = delivery;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setActivate(int activate) {
        this.activate = activate;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrders(String orders) {
        this.orders = orders;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getRating() {
        return rating;
    }

    public String getOrders() {
        return orders;
    }

    public String getComment() {
        return comment;
    }

    public String getGender() {
        return gender;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getPassword() {
        return password;
    }

    public String getMobile() {
        return mobile;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getImage() {
        return image;
    }

    public int getActivate() {
        return activate;
    }

    public int getRole() {
        return role;
    }

    public String getId() {
        return id;
    }

    public String getAdvanced() {
        return advanced;
    }

    public void setAdvanced(String advanced) {
        this.advanced = advanced;
    }
}
