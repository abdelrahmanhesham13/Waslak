package com.waslak.waslak.models;

public class ReviewModel {
    String name;
    String rating;
    String comment;
    String image;

    public ReviewModel(String name, String rating, String comment, String image) {
        this.name = name;
        this.rating = rating;
        this.comment = comment;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public String getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getImage() {
        return image;
    }
}
