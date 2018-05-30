package memstresser.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;

public abstract class FileUtil {
    
    private static final Logger LOG = Logger.getLogger(FileUtil.class);
    
    private static final Pattern EXT_SPLIT = Pattern.compile("\\.");
    
    public static String joinPath(String... args) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (String a : args) {
            if (a != null && a.length() > 0) {
                if (!first) {
                    result.append("/");
                }
                result.append(a);
                first = false;
            }
        }
        return result.toString();
    }
    
    public static String getNextFilename(String basename) {
        
        if (!exists(basename))
            return basename;
        
        File f = new File(basename);
        if (f != null && f.isFile()) {
            String parts[] = EXT_SPLIT.split(basename);
            
            // Check how many files already exist
            int counter = 1;
            String nextName = parts[0] + "." + counter + "." + parts[1];
            while(exists(nextName)) {
                ++counter;
                nextName = parts[0] + "." + counter + "." + parts[1];
            }
            return nextName;
        }
        
        throw new RuntimeException("Failed to get next filename!");
    }
    
    public static boolean exists(String path) {
        return (new File(path).exists());
    }
    
    public static void makeDirIfNotExists(String... paths) {
        for (String p : paths) {
            if (p == null)
                continue;
            File f = new File(p);
            if (f.exists() == false) {
                f.mkdirs();
            }
        } // FOR
    }
    
    public static String readFile(String path) {
        StringBuilder buffer = new StringBuilder();
        try {
            BufferedReader in = FileUtil.getReader(path);
            while (in.ready()) {
                buffer.append(in.readLine()).append("\n");
            } // WHILE
            in.close();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read file contents from '" + path + "'", ex);
        }
        return (buffer.toString());
    }
    
    public static BufferedReader getReader(String path) throws IOException {
        return (FileUtil.getReader(new File(path)));
    }
    
    public static BufferedReader getReader(File file) throws IOException {
        if (!file.exists()) {
            throw new IOException("The file '" + file + "' does not exist");
        }

        BufferedReader in = null;
        if (file.getPath().endsWith(".gz")) {
            FileInputStream fin = new FileInputStream(file);
            GZIPInputStream gzis = new GZIPInputStream(fin);
            in = new BufferedReader(new InputStreamReader(gzis));
            LOG.debug("Reading in the zipped contents of '" + file.getName() + "'");
        } else {
            in = new BufferedReader(new FileReader(file));
            LOG.debug("Reading in the contents of '" + file.getName() + "'");
        }
        return (in);
    }
    
    public static String getWorkingDirectory() {
        return System.getProperty("user.dir");
    }
    
    public static File writeStringToFile(String file_path, String content) throws IOException {
        return (FileUtil.writeStringToFile(new File(file_path), content));
    }

    public static File writeStringToFile(File file, String content) throws IOException {
        FileWriter writer = new FileWriter(file);
        writer.write(content);
        writer.flush();
        writer.close();
        return (file);
    }

}
