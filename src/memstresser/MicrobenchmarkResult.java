package memstresser;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import memstresser.util.JSONSerializable;
import memstresser.util.JSONUtil;

public class MicrobenchmarkResult implements JSONSerializable {
	
	private final List<Long> runtimeMillis;
	private int memoryGB;
	private int minMemoryGB;
	private int maxMemoryGB;
	private boolean outOfMemory;

	public MicrobenchmarkResult() {
		runtimeMillis = new LinkedList<Long>();
		memoryGB = -1;
		minMemoryGB = -1;
		maxMemoryGB = -1;
		outOfMemory = false;
	}
	
	public void setMemoryGB(int memoryGB) {
		this.memoryGB = memoryGB;
	}
	
	public int getMemoryGB() {
		return memoryGB;
	}
	
	public void setMinMemoryGB(int minMemoryGB) {
		this.minMemoryGB = minMemoryGB;
	}
	
	public int getMinMemoryGB() {
		return minMemoryGB;
	}
	
	public void setOutOfMemory(boolean outOfMemory) {
		this.outOfMemory = outOfMemory;
	}
	
	public boolean getOutOfMemory() {
		return outOfMemory;
	}
	
	public void setMaxMemoryGB(int maxMemoryGB) {
		this.maxMemoryGB = maxMemoryGB;
	}
	
	public int getMaxMemoryGB() {
		return maxMemoryGB;
	}
	
	public void addRuntimeMillis(Long time) {
		runtimeMillis.add(time);
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
		stringer.key("memoryGB").value(getMemoryGB());
		stringer.key("minMemoryGB").value(getMinMemoryGB());
		stringer.key("maxMemoryGB").value(getMaxMemoryGB());
		stringer.key("outOfMemory").value(getOutOfMemory());
		stringer.key("runtimeMillis").array();
		for (long time : runtimeMillis) {
			stringer.value(time);
		}
		stringer.endArray();
	}

	@Override
	public void fromJSON(JSONObject jsonObject) throws JSONException {
		setMemoryGB(jsonObject.getInt("memoryGB"));
		setMinMemoryGB(jsonObject.getInt("minMemoryGB"));
		setMaxMemoryGB(jsonObject.getInt("maxMemoryGB"));
		setOutOfMemory(jsonObject.getBoolean("outOfMemory"));
		runtimeMillis.clear();
		JSONArray jsonArray = jsonObject.getJSONArray("runtimeMillis");
		for (int i = 0; i < jsonArray.length(); ++i) {
			addRuntimeMillis(jsonArray.getLong(i));
		}
	}

}
