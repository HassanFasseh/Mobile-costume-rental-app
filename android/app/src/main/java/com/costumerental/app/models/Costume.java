package com.costumerental.app.models;

// Costume model
public class Costume {
    private int id;
    private String name;
    private String size;
    private double price;
    private String image;
    private boolean is_available;
    private String next_available_date;

    public Costume() {
    }

    public Costume(int id, String name, String size, double price, String image) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.price = price;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isIs_available() {
        return is_available;
    }

    public void setIs_available(boolean is_available) {
        this.is_available = is_available;
    }

    public String getNext_available_date() {
        return next_available_date;
    }

    public void setNext_available_date(String next_available_date) {
        this.next_available_date = next_available_date;
    }
}
