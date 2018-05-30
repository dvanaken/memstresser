package memstresser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONObject;

public class DatabaseConfiguration {

    private final String dbUrl;
    private final String username;
    private final String password;
    
    private DatabaseConfiguration(String dbUrl,String username, String password) {
        this.dbUrl = dbUrl;
        this.username = username;
        this.password = password;
    }
    
    public String getDBUrl() {
        return this.dbUrl;
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public String getPassword() {
        return this.password;
    }

    public static DatabaseConfiguration loadConfiguration(String configFile) throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(configFile));
        StringBuilder sb = new StringBuilder();
        String line = reader.readLine();
        while (line != null) {
            sb.append(line);
            line = reader.readLine();
        }
        reader.close();
        JSONObject config = new JSONObject(sb.toString());
        return new DatabaseConfiguration(config.getString("database_url"),
                config.getString("username"), config.getString("password"));
    }
}
