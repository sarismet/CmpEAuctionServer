package models;

import com.google.gson.Gson;

public class Product {
    public String name;
    public String time;
    public String link;
    public Double price;
    public String soldTo;

    public Product(String name,String time,String link, Double price, String soldTo) {
        this.name = name;
        this.time = time;
        this.link = link;
        this.price = price;
        this.soldTo = soldTo;
    }

    @Override
    public String toString() {
        Gson json = new Gson();
        json.toJson(this);
        return json.toJson(this);
    }
}
