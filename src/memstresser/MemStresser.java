package memstresser;

import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.collections15.map.ListOrderedMap;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import memstresser.util.FileUtil;
import memstresser.util.StringUtil;

public class MemStresser {

    private static final Logger LOG = Logger.getLogger(MemStresser.class);

    public static void main(String [] args) throws Exception {
        PropertyConfigurator.configure("log4j.properties");
        
        CommandLineParser parser = new PosixParser();
        Options options = new Options();
        options.addOption(
                "c",
                "config",
                true,
                "[required] Database configuration file");
        options.addOption(
                "d",
                "directory",
                true,
                "Base directory for the results, default is '" +
                Constants.DEFAULT_RESULT_DIRECTORY + "'");
        options.addOption(
                "f",
                "filename",
                true,
                "Filename for the results, default is '" +
                Constants.DEFAULT_RESULT_FILENAME + "'");
        options.addOption(
                null,
                "min-memory",
                true,
                "The minimum amount of memory to stress in GB, " +
                "default is " + Constants.DEFAULT_MIN_MEM_GB + "GB");
        options.addOption(
                null,
                "max-memory",
                true,
                "The maximum amount of memory used to stress in GB, " +
                "default is " + Constants.DEFAULT_MAX_MEM_GB + "GB");
        
        // Parse the command line arguments
        CommandLine argsLine = parser.parse(options, args);
        if (argsLine.hasOption("h")) {
            printUsage(options);
            return;
        } else if (argsLine.hasOption("c") == false) {
            LOG.error("Missing configuration file");
            printUsage(options);
            return;
        }
        String configFile = argsLine.getOptionValue("c");
        DatabaseConfiguration config = new DatabaseConfiguration();
        config.load(configFile);
        
        String resultDirectory = getOptionValue(argsLine, "d", Constants.DEFAULT_RESULT_DIRECTORY);
        String resultFilename = getOptionValue(argsLine, "f", Constants.DEFAULT_RESULT_FILENAME);
        int minMemoryGB = getOptionValue(argsLine, "min-memory", Constants.DEFAULT_MIN_MEM_GB);
        int maxMemoryGB = getOptionValue(argsLine, "max-memory", Constants.DEFAULT_MAX_MEM_GB);
        if (minMemoryGB < 1) {
            throw new RuntimeException("Minimum memory must be greater than 0 " +
                    "(value = " + minMemoryGB + ")");
        }
        if (maxMemoryGB < minMemoryGB) {
            throw new RuntimeException("Maximum memory must be greater than minimum memory " +
                    "(" + maxMemoryGB + " < " + minMemoryGB + ")");
        }
        
        // Pretty-print options
        Map<String, Object> initDebug = new ListOrderedMap<String, Object>();
        initDebug.put("Microbenchmark", "MEMORY STRESSER");
        initDebug.put("Configuration", configFile);
        initDebug.put("Database Type", "Postgres");
        initDebug.put("Database URL", config.getDatabaseUrl());
        initDebug.put("Database Username", config.getUsername());
        initDebug.put("Database Password", config.getPassword());
        initDebug.put("Minimum Memory", minMemoryGB + "GB");
        initDebug.put("Maximum Memory", maxMemoryGB + "GB");
        LOG.info(Constants.SINGLE_LINE + "\n\n" + StringUtil.formatMaps(initDebug));
        LOG.info(Constants.SINGLE_LINE);
        
        // Run the microbenchmark
        Microbenchmark bench = new Microbenchmark(config);
        LOG.debug(String.format("Running microbenchmark: [min-memory=%dGB, max-memory=%dGB]",
                minMemoryGB, maxMemoryGB));
        MicrobenchmarkResult result = bench.run(minMemoryGB, maxMemoryGB);
        
        // Save the results
        FileUtil.makeDirIfNotExists(resultDirectory);
        String resultPath = FileUtil.getNextFilename(FileUtil.joinPath(
                resultDirectory, resultFilename));
        LOG.info("Saving results to " + resultPath);
        result.save(resultPath);
    }

    private static void printUsage(Options options) {
        HelpFormatter hlpfrmt = new HelpFormatter();
        hlpfrmt.printHelp("memstresser", options);
    }
    
    private static String getOptionValue(CommandLine argsLine, String opt, String defaultValue) {
        if (argsLine.hasOption(opt)) {
            return argsLine.getOptionValue(opt);
        }
        return defaultValue;
    }
    
    private static int getOptionValue(CommandLine argsLine, String opt, int defaultValue) {
        return Integer.parseInt(getOptionValue(argsLine, opt, Integer.toString(defaultValue)));
    }
}
