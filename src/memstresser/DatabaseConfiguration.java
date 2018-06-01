package memstresser;

public class DatabaseConfiguration {

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

}
