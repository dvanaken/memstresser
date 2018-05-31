package memstresser;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import memstresser.util.JSONSerializable;
import memstresser.util.JSONUtil;

public class DatabaseConfiguration implements JSONSerializable {

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

	@Override
	public String toJSONString() {
		return JSONUtil.toJSONString(this);
	}

	@Override
	public void save(String outputPath) throws IOException {
		JSONUtil.save(this, outputPath);
	}

	@Override
	public void load(String inputPath) throws IOException {
		JSONUtil.load(this, inputPath);
	}

	@Override
	public void toJSON(JSONStringer stringer) throws JSONException {
		stringer.key("databaseUrl").value(getDatabaseUrl());
		stringer.key("username").value(getUsername());
		stringer.key("password").value(getPassword());
	}

	@Override
	public void fromJSON(JSONObject jsonObject) throws JSONException {
		setDatabaseUrl(jsonObject.getString("databaseUrl"));
		setUsername(jsonObject.getString("username"));
		setPassword(jsonObject.getString("password"));
	}
}
