package com.example.stepper;

public class Contacts {

    String name,image,staus,uid;

    public Contacts() {

    }

    public Contacts(String name, String image, String staus, String uid) {
        this.name = name;
        this.image = image;
        this.staus = staus;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getStaus() {
        return staus;
    }

    public String getUid() {
        return uid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setStaus(String staus) {
        this.staus = staus;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
