import com.google.gson.Gson;
import models.Product;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;

import models.User;

public class Repository {
    int fileSize = 0;
    static CustomMutex userDataBaseMutex = new CustomMutex("userDataBase");
    static CustomMutex productDataBaseMutex = new CustomMutex("productDataBaseMutex");
    public  Repository() {
        System.out.println("Repository is initialized");
        File directory=new File("database/users");
        fileSize=1;
    }

     private String readFile(String filePath ){
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8))
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

    public User signUp(String email, String username, String password,String telNo,String uuid){
        ArrayList<String> belongings = new ArrayList<String>();
        int count = ++Main.USER_COUNT;
        String fileName = "database/users/users"+ Integer.toString(count / 10) + ".json";
        User usr = new User(uuid,username,Main.USER_COUNT,email,password,telNo,5000,belongings);
        userDataBaseMutex.requestCS();
        if(write(fileName,usr)){
            userDataBaseMutex.releaseCS();
            return usr;
        }
        userDataBaseMutex.releaseCS();
        return null;
    }

    public User login(String email, String password,String uuid) {
        for (int i = 0; i < fileSize; i++) {
            String fileName = "database/users/users"+ Integer.toString(i) + ".json";
            userDataBaseMutex.requestReading(uuid);
            JSONObject json = read(fileName);
            userDataBaseMutex.releaseReading(uuid);
            JSONArray jsonArray = (JSONArray) json.get("users");
            int size = jsonArray.size();
            for (int a = 0; a < size; a++) {
                Gson json2 = new Gson();
                String s = (String) jsonArray.get(a);
                Map map = json2.fromJson(s, Map.class);
                if( map.get("email").equals(email) && map.get("password").equals(password)){
                    return json2.fromJson(s, User.class);
                }
                }
        }
        return null;
    }

    public String showNextProduct(String time,String uuid) {
        String fileName = "database/products.json";
        productDataBaseMutex.requestReading(uuid);
        JSONObject json = read(fileName);
        productDataBaseMutex.releaseReading(uuid);
        JSONArray products = (JSONArray) json.get("products");
        String productToReturn = "NULL";
        int size = products.size();
        JSONArray productsToRemove = new JSONArray();

        int deleteSize = 0;
        for(int i=0;i<size;i++){
            String s = (String)products.get(i);
            System.out.println("S is ->"+s);
            Gson gson = new Gson();
            Product product = gson.fromJson(s, Product.class);

            String time1 = product.time;

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalTime t1 = LocalTime.parse(time1, fmt);
            LocalTime t2 = LocalTime.parse(time, fmt);
            long miliseconds = ChronoUnit.MILLIS.between(t1, t2);
            System.out.println("miliseconds -> "+miliseconds);
            if (miliseconds>300000){
                productsToRemove.add(product);
                deleteSize++;
            }else{
                JSONObject productJson = new JSONObject();
                productJson.put("TIME",(300000-miliseconds));
                productJson.put("products",product);

                productToReturn=  gson.toJson(productJson);
                break;
            }
        }
        if (deleteSize > 0) {
            while(deleteSize>0){
                products.remove(0);
                deleteSize--;
            }
            System.out.println("products after delete ->"+products);
            json.put("products",products);
            try{
                productDataBaseMutex.requestCS();
                File f = new File(fileName);
                Writer fileWriter = new FileWriter(f, false); //overwrites file
                fileWriter.write(json.toJSONString());
                fileWriter.close();
            }
            catch (IOException e){
                System.out.println("IOException in showNextProduct -> "+e);
                productDataBaseMutex.releaseCS();
            }
            productDataBaseMutex.releaseCS();
        }
        return productToReturn;
    }

    public String buyProduct(String userName,int userId, String productName){
        String buyerUserName = "";
        try{
            productDataBaseMutex.requestCS();
            String fileName = "database/products.json";
            JSONObject json = read(fileName);
            JSONArray products = (JSONArray) json.get("products");
            int size = products.size();
            for(int i = 0;i<size;i++){
                String s = (String)products.get(i);
                Gson gson = new Gson();
                Product product = gson.fromJson(s, Product.class);
                if ( product.name == productName ){
                    if (product.soldTo == "NULL") {
                        Boolean isAllowed = false;
                        userDataBaseMutex.requestCS();
                        int fileIndex = userId/10;
                        String fileNameOfUsers = "database/users/users"+ Integer.toString(fileIndex) + ".json";
                        JSONObject jsonUsers = read(fileNameOfUsers);
                        JSONArray users = (JSONArray) jsonUsers.get("users");
                        int userSize = users.size();
                        for(int u = 0; u < userSize; u++){
                            String userInfo = (String)users.get(u);
                            Gson gsonUsers = new Gson();
                            User user = gsonUsers.fromJson(userInfo, User.class);
                            if (user.username == userName){
                                if (user.balance > product.price) {
                                    user.balance = user.balance - product.price;
                                    isAllowed = true;
                                }else{
                                    isAllowed = false;
                                }
                                break;
                            }
                        }
                        jsonUsers.put("users",users);
                        Writer fileWriter = new FileWriter(fileNameOfUsers, false); //overwrites file
                        fileWriter.write(jsonUsers.toJSONString());
                        fileWriter.close();
                        userDataBaseMutex.releaseCS();
                        if (isAllowed) {
                            product.soldTo = userName;
                            buyerUserName = userName;
                        }else{
                            buyerUserName = "Your balance is not enough to buy this product.";
                        }
                    }
                    else {
                        buyerUserName = product.soldTo;
                    }
                    break;
                }
            }
            json.put("products",products);
            File f = new File(fileName);
            Writer fileWriter = new FileWriter(f, false); //overwrites file
            fileWriter.write(json.toJSONString());
            fileWriter.close();
        }
        catch (IOException e){
            System.out.println("IOException in showNextProduct -> "+e);
            productDataBaseMutex.releaseCS();
        }
        productDataBaseMutex.releaseCS();
        return buyerUserName;
    }

    private JSONObject read(String fileName) {
        JSONObject json = null;
        try {
            FileReader reader = new FileReader(fileName);
            JSONParser parser = new JSONParser();
            json = (JSONObject) parser.parse(reader);
        }catch (IOException | ParseException e){
            System.out.println("Exception while reading is occurred");
        }
        return json;
    }

    private Boolean write(String fine_name, User usr) {

        try {
            File f = new File(fine_name);
            if (f.exists()){
                System.out.println("File exists");
                userDataBaseMutex.requestCS();
                JSONObject json = read(fine_name);
                userDataBaseMutex.releaseCS();
                JSONArray solutions = (JSONArray) json.get("users");
                solutions.add(usr.toString());
                json.put("users",solutions);
                Writer fileWriter = new FileWriter(f, false); //overwrites file
                fileWriter.write(json.toJSONString());
                fileWriter.close();
                return true;
            }
            else{
                f.createNewFile();
                Writer fileWriter = new FileWriter(f, false); //overwrites file
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                jsonArray.add(usr.toString());
                jsonObject.put("users", jsonArray);
                fileWriter.write(jsonObject.toJSONString());
                fileWriter.close();
                return true;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

}
