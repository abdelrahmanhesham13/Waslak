package com.waslak.waslak.models;

public class RequestOfferModel {
    String idRequest;
    String cityIdRequest;
    String addressRequest;
    String longitudeRequest;
    String latitudeRequest;
    String descriptionRequest;
    String nameRequest;
    String statusRequest;
    String createdRequest;
    String updatedRequest;
    String userIdRequest;
    String countryRequest;
    String priceRequest;
    String imageRequest;
    String viewsRequest;
    String deliveryIdRequest;
    String durationRequest;
    String shopIdRequest;
    String detailRequest;
    String longitudeUpdateRequest;
    String latitudeUpdateRequest;
    String type;


    public RequestOfferModel(String idRequest, String cityIdRequest, String addressRequest, String longitudeRequest, String latitudeRequest, String descriptionRequest, String nameRequest, String statusRequest, String createdRequest, String updatedRequest, String userIdRequest, String countryRequest, String priceRequest, String imageRequest, String viewsRequest, String deliveryIdRequest, String durationRequest, String shopIdRequest, String detailRequest, String longitudeUpdateRequest, String latitudeUpdateRequest) {
        this.idRequest = idRequest;
        this.cityIdRequest = cityIdRequest;
        this.addressRequest = addressRequest;
        this.longitudeRequest = longitudeRequest;
        this.latitudeRequest = latitudeRequest;
        this.descriptionRequest = descriptionRequest;
        this.nameRequest = nameRequest;
        this.statusRequest = statusRequest;
        this.createdRequest = createdRequest;
        this.updatedRequest = updatedRequest;
        this.userIdRequest = userIdRequest;
        this.countryRequest = countryRequest;
        this.priceRequest = priceRequest;
        this.imageRequest = imageRequest;
        this.viewsRequest = viewsRequest;
        this.deliveryIdRequest = deliveryIdRequest;
        this.durationRequest = durationRequest;
        this.shopIdRequest = shopIdRequest;
        this.detailRequest = detailRequest;
        this.longitudeUpdateRequest = longitudeUpdateRequest;
        this.latitudeUpdateRequest = latitudeUpdateRequest;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIdRequest() {
        return idRequest;
    }

    public String getCityIdRequest() {
        return cityIdRequest;
    }

    public String getAddressRequest() {
        return addressRequest;
    }

    public String getLongitudeRequest() {
        return longitudeRequest;
    }

    public String getLatitudeRequest() {
        return latitudeRequest;
    }

    public String getDescriptionRequest() {
        return descriptionRequest;
    }

    public String getNameRequest() {
        return nameRequest;
    }

    public String getStatusRequest() {
        return statusRequest;
    }

    public String getCreatedRequest() {
        return createdRequest;
    }

    public String getUpdatedRequest() {
        return updatedRequest;
    }

    public String getUserIdRequest() {
        return userIdRequest;
    }

    public String getCountryRequest() {
        return countryRequest;
    }

    public String getPriceRequest() {
        return priceRequest;
    }

    public String getImageRequest() {
        return imageRequest;
    }

    public String getViewsRequest() {
        return viewsRequest;
    }

    public String getDeliveryIdRequest() {
        return deliveryIdRequest;
    }

    public String getDurationRequest() {
        return durationRequest;
    }

    public String getShopIdRequest() {
        return shopIdRequest;
    }

    public String getDetailRequest() {
        return detailRequest;
    }

    public String getLongitudeUpdateRequest() {
        return longitudeUpdateRequest;
    }

    public String getLatitudeUpdateRequest() {
        return latitudeUpdateRequest;
    }
}
