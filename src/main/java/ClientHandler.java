import com.google.gson.Gson;
import models.Product;
import models.User;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.*;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientHandler extends Thread {

    static CustomMutex buyProductMutex = new CustomMutex("buy");
    Socket c;
    Repository  repository;
    int id;
    public ClientHandler(Socket client, Repository  repository,int id){
        this.c = client;
        this.repository = repository;
        this.id = id;
    }

    public void run() {
                try{

                DataInputStream isr=new DataInputStream(this.c.getInputStream());
                DataOutputStream dout=new DataOutputStream(this.c.getOutputStream());

                BufferedReader reader = new BufferedReader( new InputStreamReader( new DataInputStream(this.c.getInputStream())));

                String str = reader.readLine();
                Gson gson = new Gson();
                Map map = gson.fromJson(str, Map.class);
                String operation = (String) map.get("OPERATION");

                System.out.println("OPERATION is "+operation);
                HashMap<String, String> returnToHashMap = new HashMap<String, String>();


                if (operation.equals("LOGIN")) {
                    String py = (String)map.get("PAYLOAD");
                    HashMap mapHash = gson.fromJson(py, HashMap.class);
                    String email = (String) mapHash.get("EMAIL");
                    String password = (String) mapHash.get("PASSWORD");
                    UUID uuid = UUID.randomUUID();
                    String uuidString = uuid.toString();
                    User usr = repository.login(email,password,uuidString);
                    String jsonString = "NULL";
                    if(usr != null){
                        usr.uuid = uuidString;
                        returnToHashMap.put("user", gson.toJson(usr));
                        jsonString = gson.toJson(returnToHashMap);
                        System.out.println("jsonString is "+jsonString);
                    }
                    else {
                        System.out.println("USER is null ");
                    }
                    System.out.println("jsonString is in LOGIN "+jsonString);
                    PrintWriter pr = new PrintWriter(this.c.getOutputStream(),true);
                    pr.println(jsonString);
                    pr.flush();
                }
                else if (operation.equals("SIGN_UP")){
                    String py = (String)map.get("PAYLOAD");
                    HashMap mapHash = gson.fromJson(py, HashMap.class);
                    System.out.println("CREATE A SING_UP ACTION THREAD");
                    String email = (String) mapHash.get("EMAIL");
                    String username = (String) mapHash.get("USERNAME");
                    String password = (String) mapHash.get("PASSWORD");
                    String telno = (String) mapHash.get("TELNO");
                    String jsonString = "NULL";
                    UUID uuid = UUID.randomUUID();
                    String uuidString = uuid.toString();
                    User usr = repository.signUp(email,username,password,telno,uuidString,this.id);
                    if(usr != null){
                        returnToHashMap.put("user", gson.toJson(usr));
                        jsonString = gson.toJson(returnToHashMap);
                    }
                    else {
                        System.out.println("USER is null ");
                    }
                    System.out.println("jsonString is in SIGN_UP "+jsonString);
                    PrintWriter pr = new PrintWriter(this.c.getOutputStream(),true);
                    pr.println(jsonString);
                    pr.flush();
                }
                else if (operation.equals("SHOW")){
                    System.out.println("SHOWING THE NEW PRODUCTS");

                    String uuid = (String)map.get("UUID");

                    String product = repository.showNextProduct(uuid);
                    if(product != "NULL"){
                        System.out.println("product is "+product);
                    }
                    else {
                        System.out.println("SHOW is null ");
                    }
                    System.out.println("jsonString "+product);
                    PrintWriter pr = new PrintWriter(this.c.getOutputStream(),true);
                    pr.println(product);
                    pr.flush();
                }
                else if (operation.equals("BUY")){
                    String payload = (String)map.get("PAYLOAD");
                    System.out.println("PAYLOAD is "+payload);
                    JSONParser parser = new JSONParser();
                    JSONObject jsonPayload = (JSONObject) parser.parse(payload);
                    String userName = (String) jsonPayload.get("USERNAME");
                    int userId = Integer.valueOf((String)jsonPayload.get("USERID")) ;
                    String productName = (String) jsonPayload.get("PRODUCTNAME");
                    String buyer = repository.buyProduct(userName,userId,productName);
                    System.out.println("buyer is  "+buyer);
                    returnToHashMap.put("buyer", buyer);
                    String jsonString = gson.toJson(returnToHashMap);
                    System.out.println("jsonString is "+jsonString);
                    PrintWriter pr = new PrintWriter(this.c.getOutputStream(),true);
                    pr.println(jsonString);
                    pr.flush();
                }
                else if (operation.equals("LOG_OUT")){
                    System.out.println("LOGGING OUT");
                }

                }
                catch (IOException | ParseException e) {
                    e.printStackTrace();
                }

    }

}
