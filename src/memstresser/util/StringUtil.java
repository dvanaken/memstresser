package memstresser.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public abstract class StringUtil {
    
    private static final Logger LOG = Logger.getLogger(StringUtil.class);

    private static final Pattern LINE_SPLIT = Pattern.compile("\n");
    
    private static final String SET_PLAIN_TEXT = "\033[0;0m";
    private static final String SET_BOLD_TEXT = "\033[0;1m";
    
    private static String CACHE_REPEAT_STR = null;
    private static Integer CACHE_REPEAT_SIZE = null;
    private static String CACHE_REPEAT_RESULT = null;
    
    /**
     * Return key/value maps into a nicely formatted table
     * Delimiter ":", No UpperCase Keys, No Boxing
     * @param maps
     * @return
     */
    public static String formatMaps(Map<?, ?>...maps) {
        return (formatMaps(":", false, false, false, false, true, true, maps));
    }
    
    /**
     * Return key/value maps into a nicely formatted table using the given delimiter
     * No Uppercase Keys, No Boxing
     * @param delimiter
     * @param maps
     * @return
     */
    public static String formatMaps(String delimiter, Map<?, ?>...maps) {
        return (formatMaps(delimiter, false, false, false, false, true, true, maps));
    }

    /**
     * Return key/value maps into a nicely formatted table
     * The maps are displayed in order from first to last, and there will be a spacer
     * created between each map. The format for each record is:
     * 
     * <KEY><DELIMITER><SPACING><VALUE>
     * 
     * If the delimiter is an equal sign, then the format is:
     * 
     *  <KEY><SPACING><DELIMITER><VALUE>
     * 
     * @param delimiter
     * @param upper Upper-case all keys
     * @param box Box results
     * @param border_top TODO
     * @param border_bottom TODO
     * @param recursive TODO
     * @param first_element_title TODO
     * @param maps
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String formatMaps(String delimiter, boolean upper, boolean box, boolean border_top, boolean border_bottom, boolean recursive, boolean first_element_title, Map<?, ?>...maps) {
        boolean need_divider = (maps.length > 1 || border_bottom || border_top);
        
        // Figure out the largest key size so we can get spacing right
        int max_key_size = 0;
        int max_title_size = 0;
        final Map<Object, String[]> map_keys[] = (Map<Object, String[]>[])new Map[maps.length];
        final boolean map_titles[] = new boolean[maps.length];
        for (int i = 0; i < maps.length; i++) {
            Map<?, ?> m = maps[i];
            if (m == null) continue;
            Map<Object, String[]> keys = new HashMap<Object, String[]>();
            boolean first = true;
            for (Object k : m.keySet()) {
                String k_str[] = LINE_SPLIT.split(k != null ? k.toString() : "");
                keys.put(k, k_str);
                
                // If the first element has a null value, then we can let it be the title for this map
                // It's length doesn't affect the other keys, but will affect the total size of the map
                if (first && first_element_title && m.get(k) == null) {
                    for (String line : k_str) {
                        max_title_size = Math.max(max_title_size, line.length());    
                    } // FOR
                    map_titles[i] = true;
                } else {
                    for (String line : k_str) {
                        max_key_size = Math.max(max_key_size, line.length());
                    } // FOR
                    if (first) map_titles[i] = false;
                }
                first = false;
            } // FOR
            map_keys[i] = keys;
        } // FOR
        
        boolean equalsDelimiter = delimiter.equals("=");
        final String f = "%-" + (max_key_size + delimiter.length() + 1) + "s" +
                         (equalsDelimiter ? "= " : "") +
                         "%s\n";
        
        // Now make StringBuilder blocks for each map
        // We do it in this way so that we can get the max length of the values
        int max_value_size = 0;
        StringBuilder blocks[] = new StringBuilder[maps.length];
        for (int map_i = 0; map_i < maps.length; map_i++) {
            blocks[map_i] = new StringBuilder();
            Map<?, ?> m = maps[map_i];
            if (m == null) continue;
            Map<Object, String[]> keys = map_keys[map_i];
            
            boolean first = true;
            for (Entry<?, ?> e : m.entrySet()) {
                String key[] = keys.get(e.getKey());
                
                if (first && map_titles[map_i]) {
                    blocks[map_i].append(StringUtil.join("\n", key));
                    if (CollectionUtil.last(key).endsWith("\n") == false) blocks[map_i].append("\n");
                    
                } else {
                    Object v_obj = e.getValue();
                    String v = null;
                    if (recursive && v_obj instanceof Map<?, ?>) {
                        v = formatMaps(delimiter, upper, box, border_top, border_bottom, recursive, first_element_title, (Map<?,?>)v_obj).trim();
                    } else if (key.length == 1 && key[0].trim().isEmpty() && v_obj == null) {
                        blocks[map_i].append("\n");
                        continue;
                    } else if (v_obj == null) {
                        v = "null";
                    } else {
                        v = v_obj.toString();
                    }
    
                    
                    // If the key or value is multiple lines, format them nicely!
                    String value[] = LINE_SPLIT.split(v);
                    int total_lines = Math.max(key.length, value.length); 
                    for (int line_i = 0; line_i < total_lines; line_i++) {
                        String k_line = (line_i < key.length ? key[line_i] : ""); 
                        if (upper) k_line = k_line.toUpperCase();
                        
                        String v_line = (line_i < value.length ? value[line_i] : "");
                        
                        if (line_i == (key.length-1) && (first == false || (first && v_line.isEmpty() == false))) {
                            if (equalsDelimiter == false && k_line.trim().isEmpty() == false) k_line += ":";
                        }
                    
                        blocks[map_i].append(String.format(f, k_line, v_line));
                        if (need_divider) max_value_size = Math.max(max_value_size, v_line.length());
                    } // FOR
                    if (v.endsWith("\n")) blocks[map_i].append("\n");
                }
                first = false;
            }
        } // FOR
        
        // Put it all together!
        LOG.debug("max_title_size=" + max_title_size + ", max_key_size=" + max_key_size + ", max_value_size=" + max_value_size + ", delimiter=" + delimiter.length());
        int total_width = Math.max(max_title_size, (max_key_size + max_value_size + delimiter.length())) + 1;
        String dividing_line = (need_divider ? repeat("-", total_width) : "");
        StringBuilder sb = null;
        if (maps.length == 1) {
            sb = blocks[0];
        } else {
            sb = new StringBuilder();
            for (int i = 0; i < maps.length; i++) {
                if (blocks[i].length() == 0) continue;
                if (i != 0 && maps[i].size() > 0) sb.append(dividing_line).append("\n");
                sb.append(blocks[i]);
            } // FOR
        }
        return (box ? StringUtil.box(sb.toString()) :
                      (border_top ? dividing_line+"\n" : "") + sb.toString() + (border_bottom ? dividing_line : ""));
    }

    /**
     * Returns the given string repeated the given # of times
     * @param str
     * @param size
     * @return
     */
    public static String repeat(String str, int size) {
        // We cache the last call in case they are making repeated calls for the same thing
        if (CACHE_REPEAT_STR != null &&
            CACHE_REPEAT_STR.equals(str) &&
            CACHE_REPEAT_SIZE.equals(size)) {
            return (CACHE_REPEAT_RESULT);
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) sb.append(str);
        CACHE_REPEAT_RESULT = sb.toString();
        CACHE_REPEAT_STR = str;
        CACHE_REPEAT_SIZE = size;
        return (CACHE_REPEAT_RESULT);
    }
    
    /**
     * Make a box around some text. If str has multiple lines, then the box will be the length
     * of the longest string.
     * @param str
     * @return
     */
    public static String box(String str) {
        return (StringUtil.box(str, "*", null));
    }

    /**
     * Make a box around some text using the given marker character.
     * @param str
     * @param mark
     * @return
     */
    public static String box(String str, String mark) {
        return (StringUtil.box(str, mark, null));
    }
    
    /**
     * Create a box around some text
     * @param str
     * @param mark
     * @param max_len
     * @return
     */
    public static String box(String str, String mark, Integer max_len) {
        String lines[] = LINE_SPLIT.split(str);
        if (lines.length == 0) return "";
        
        if (max_len == null) {
            for (String line : lines) {
                if (max_len == null || line.length() > max_len) max_len = line.length();
            } // FOR
        }
        
        final String top_line = StringUtil.repeat(mark, max_len + 4); // padding
        final String f = "%s %-" + max_len + "s %s\n";
        
        StringBuilder sb = new StringBuilder();
        sb.append(top_line).append("\n");
        for (String line : lines) {
            sb.append(String.format(f, mark, line, mark));
        } // FOR
        sb.append(top_line);
        
        return (sb.toString());
    }

    /**
     * Python join()
     * @param <T>
     * @param delimiter
     * @param items
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> String join(String delimiter, T...items) {
        return (join(delimiter, Arrays.asList(items)));
    }
    
    public static <T> String join(String delimiter, final Iterator<T> items) {
        return (join("", delimiter, CollectionUtil.iterable(items)));
    }
    
    /**
     * Wrap the given string with the control characters
     * to make the text appear bold in the console
     */
    public static String bold(String str) {
        return (SET_BOLD_TEXT + str + SET_PLAIN_TEXT);
    }
    
    /**
     * Python join()
     * @param delimiter
     * @param items
     * @return
     */
    public static String join(String delimiter, Iterable<?> items) {
        return (join("", delimiter, items));
    }
    
    /**
     * Python join() with optional prefix
     * @param prefix
     * @param delimiter
     * @param items
     * @return
     */
    public static String join(String prefix, String delimiter, Iterable<?> items) {
        if (items == null) return ("");
        if (prefix == null) prefix = "";
        
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Object x : items) {
            if (prefix.isEmpty() == false) sb.append(prefix);
            sb.append(x != null ? x.toString() : x).append(delimiter);
            i++;
        }
        if (i == 0) return "";
        sb.delete(sb.length() - delimiter.length(), sb.length());
     
        return sb.toString();
    }

}
