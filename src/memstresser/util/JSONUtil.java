package memstresser.util;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JSONUtil {

	public static <T> void save(T object, String outputPath, boolean formatJson) throws IOException {
		Gson gson;
		if (formatJson) {
			gson = new GsonBuilder().setPrettyPrinting().create();
		} else {
			gson = new Gson();
		}
		String json = gson.toJson(object);
		FileUtil.writeStringToFile(outputPath, json);
	}

	public static <T> void save(T object, String outputPath) throws IOException {
		save(object, outputPath, false);
	}

	public static <T> T load(Class<T> clazz, String inputPath) throws IOException {
		String json = FileUtil.readFile(inputPath);
		Gson gson = new Gson();
		return gson.fromJson(json, clazz);
	}

}
