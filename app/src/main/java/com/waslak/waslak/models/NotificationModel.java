package com.waslak.waslak.models;

import java.io.Serializable;

public class NotificationModel implements Serializable {

    private String text;
    private String id;
    private String type;
    private String userId;
    private String created;
    private String secondary_id;
    private String request_id;
    private String delivery_id;
    private String status;
    private String titleDelivery;

    public NotificationModel(String text, String id, String type, String userId, String created, String secondary_id, String request_id, String delivery_id, String status, String titleDelivery) {
        this.text = text;
        this.id = id;
        this.type = type;
        this.userId = userId;
        this.created = created;
        this.secondary_id = secondary_id;
        this.request_id = request_id;
        this.delivery_id = delivery_id;
        this.status = status;
        this.titleDelivery = titleDelivery;
    }

    public String getTitleDelivery() {
        return titleDelivery;
    }

    public String getStatus() {
        return status;
    }

    public String getDelivery_id() {
        return delivery_id;
    }

    public String getRequest_id() {
        return request_id;
    }

    public String getText() {
        return text;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getUserId() {
        return userId;
    }

    public String getCreated() {
        return created;
    }

    public String getSecondary_id() {
        return secondary_id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public void setSecondary_id(String secondary_id) {
        this.secondary_id = secondary_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }

    public void setDelivery_id(String delivery_id) {
        this.delivery_id = delivery_id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTitleDelivery(String titleDelivery) {
        this.titleDelivery = titleDelivery;
    }
}
