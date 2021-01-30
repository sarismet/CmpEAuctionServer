package models;

import org.json.simple.JSONArray;
import com.google.gson.Gson;

import java.util.ArrayList;

public class User {

    public String username;
    public int id;
    public String email;
    public String password;
    public String telNo;
    public Double balance;
    public ArrayList<String> belongings;
    public String uuid;
    public User(String uuid,String username,int id, String email, String password, String telNo, Double balance, ArrayList<String> belongings) {
        this.uuid = uuid;
        this.username = username;
        this.id = id;
        this.email = email;
        this.password = password;
        this.telNo = telNo;
        this.balance = balance;
        this.belongings = belongings;
    }

    public void setBelongings(JSONArray belongings) {
        this.belongings = belongings;
    }

    @Override
    public String toString() {
        Gson json = new Gson();
        json.toJson(this);
        return json.toJson(this);
    }
}
