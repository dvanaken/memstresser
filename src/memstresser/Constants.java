package memstresser;

import memstresser.util.StringUtil;

public class Constants {

    // Default base directory for the result files
    public static final String DEFAULT_RESULT_DIRECTORY = "results";

    // Default base directory for the result files
    public static final String DEFAULT_RESULT_FILENAME = "memstresser.json";

    // Default maximum amount of memory to stress in GP
    public static final int DEFAULT_MAX_MEM_LIMIT_GB = 256;

    // For pretty-printing benchmark output
    public static final String SINGLE_LINE = StringUtil.repeat("=", 70);
}
