package memstresser.util;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

public class JSONUtil {
	
	private static final Logger LOG = Logger.getLogger(JSONUtil.class);

    public static <T extends JSONSerializable> void save(T object, String output_path) throws IOException {
        if (LOG.isDebugEnabled()) LOG.debug("Writing out contents of " + object.getClass().getSimpleName() + " to '" + output_path + "'");
        File f = new File(output_path);
        try {
            FileUtil.makeDirIfNotExists(f.getParent());
            String json = object.toJSONString();
            FileUtil.writeStringToFile(f, format(json));
        } catch (Exception ex) {
            LOG.error("Failed to serialize the " + object.getClass().getSimpleName() + " file '" + f + "'", ex);
            throw new IOException(ex);
        }
    }

    public static <T extends JSONSerializable> void load(T object, String input_path) throws IOException {
        if (LOG.isDebugEnabled()) LOG.debug("Loading in serialized " + object.getClass().getSimpleName() + " from '" + input_path + "'");
        String contents = FileUtil.readFile(input_path);
        if (contents.isEmpty()) {
            throw new IOException("The " + object.getClass().getSimpleName() + " file '" + input_path + "' is empty");
        }
        try {
            object.fromJSON(new JSONObject(contents));
        } catch (Exception ex) {
            if (LOG.isDebugEnabled()) LOG.error("Failed to deserialize the " + object.getClass().getSimpleName() + " from file '" + input_path + "'", ex);
            throw new IOException(ex);
        }
        if (LOG.isDebugEnabled()) LOG.debug("The loading of the " + object.getClass().getSimpleName() + " is complete");
    }

    public static String format(String json) {
        try {
            return (JSONUtil.format(new JSONObject(json)));
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static <T extends JSONSerializable> String format(T object) {
        JSONStringer stringer = new JSONStringer();
        try {
            if (object instanceof JSONObject) return ((JSONObject)object).toString(2);
            stringer.object();
            object.toJSON(stringer);
            stringer.endObject();
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
        return (JSONUtil.format(stringer.toString()));
    }
    
    public static String format(JSONObject o) {
        try {
            return o.toString(1);
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static String toJSONString(JSONSerializable object) {
    	JSONStringer stringer = new JSONStringer();
    	stringer.object();
    	((JSONSerializable)object).toJSON(stringer);
    	stringer.endObject();
    	return stringer.toString();
    }

}
