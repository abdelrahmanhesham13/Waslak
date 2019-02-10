package com.waslak.waslak.models;

import java.io.Serializable;

public class ChatModel implements Serializable {

    String chatId;
    String lastMessage;
    String seen;
    String name;
    String toId;
    String email;
    String messageSenderId;
    String image;


    public ChatModel() {
    }

    public ChatModel(String chatId, String lastMessage, String seen, String name, String toId, String email, String messageSenderId, String image) {
        this.chatId = chatId;
        this.lastMessage = lastMessage;
        this.seen = seen;
        this.name = name;
        this.toId = toId;
        this.email = email;
        this.messageSenderId = messageSenderId;
        this.image = image;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMessageSenderId(String messageSenderId) {
        this.messageSenderId = messageSenderId;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getChatId() {
        return chatId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getSeen() {
        return seen;
    }

    public String getName() {
        return name;
    }

    public String getToId() {
        return toId;
    }

    public String getEmail() {
        return email;
    }

    public String getMessageSenderId() {
        return messageSenderId;
    }

    public String getImage() {
        return image;
    }
}
