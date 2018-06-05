package memstresser;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private String databaseUrl;
    private String username;
    private String password;

    public String getDatabaseUrl() {
        return this.databaseUrl;
    }

    public void setDatabaseUrl(String databaseUrl) {
    	this.databaseUrl = databaseUrl;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
    	this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
    	this.password = password;
    }

    public final Connection makeConnection() throws SQLException {
        return DriverManager.getConnection(databaseUrl, username, password);
    }

}
