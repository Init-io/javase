package com.init.test;
import com.github.initio.javase.Server;
import com.github.initio.javase.Authenticate;
import com.github.initio.javase.Database;
import com.github.initio.javase.Storage;

public class JavaseTest {
    private static Storage storage;
    private static String data;
    private static Server server;
    private static Authenticate authenticate;
    private static Database database;
    private static String apiKey;
    private static String authDomain;
    private static String databaseUrl;
    private static String storageBucket;
    public static void main(String[] args) {

        apiKey = "your-api-key";
        authDomain = "your-auth-domain";
        databaseUrl = "your-database-url";
        storageBucket = "your-storage-bucket";

        server = new Server();
        server.initialize(apiKey,authDomain,databaseUrl,storageBucket);
        System.out.println(server.error());
        database = new Database(server);

        //database.remove("demodb/demo2", "");
        //System.out.println("Remove Error: " + database.error()); // Should show exact HTTP error

        //database.put("Users/7783674442/wrongpath/", "name", "kia", "");
        //System.out.println("Put Error: " + database.error()); // Should show exact HTTP error

        //String data = database.get("Users/7783674442/wrongpah/name", "");
        //System.out.println("Get Error: " + database.error()); // Should show exact HTTP error

        authenticate = new Authenticate(server);
        authenticate.signIn("test333@gmail.com", "password");
        System.out.println(authenticate.error());
        String token = authenticate.getIdToken();
        storage = new Storage(server,authenticate);
        storage.uploadFile("C:\\Users\\SIAM\\OneDrive\\Pictures\\sim","/");
        //authenticate.removeUser(token);
        //System.out.println(authenticate.error());
        //System.out.println(" Your id token is " + token + " Account is valid " + authenticate.isVerified(token));
    }
}