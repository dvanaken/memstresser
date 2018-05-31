package memstresser;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class Microbenchmark {
    
    private static final Logger LOG = Logger.getLogger(Microbenchmark.class);
    
    /* https://www.slideshare.net/AlexeyBashtanov/postgresql-and-ram-usage */
    private static final String MEM_BASE_QUERY = "with cte_1gb as (select " +
            "repeat('a', 1024*1024*1024 - 100) as a1gb) select count(*) from ";
    
    private final DatabaseConfiguration config;
   
    public Microbenchmark(DatabaseConfiguration config) {
        this.config = config;
    }
    
    public MicrobenchmarkResult run(int minMemoryGB, int maxMemoryGB) throws SQLException {
        MicrobenchmarkResult result = new MicrobenchmarkResult();
        result.setMinMemoryGB(minMemoryGB);
        result.setMaxMemoryGB(maxMemoryGB);

    	Connection conn = makeConnection();
        Statement s = conn.createStatement();
        int i;
        for (i = minMemoryGB; i < maxMemoryGB + 1; ++i) {
            LOG.info("Stressing " + i + "GB memory...");
            
            // Build query
            StringBuilder sb = new StringBuilder(MEM_BASE_QUERY);
            int j;
            for (j = 0; j < i - 1; ++j) {
                sb.append("cte_1gb a" + j + ", ");
            }
            sb.append("cte_1gb a" + j);
            
            // Execute query (see if it causes an OOM error)
            long startNanos = 0L;
            try {
            	startNanos = System.nanoTime();
                s.execute(sb.toString());
                result.addRuntimeMillis(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos));
            } catch (SQLException ex) {
                if (ex.getErrorCode() == 0 && ex.getSQLState() != null && ex.getSQLState().equals("53200")) {
                    LOG.info("Final memory size: " + i + "GB");
                    result.addRuntimeMillis(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos));
                    result.setMemoryGB(i);
                    result.setOutOfMemory(true);
                    break;
                }
                else {
                    LOG.error(String.format("%s thrown (ErrorCode='%d', SQLState='%s'): '%s'",
                            ex.getClass().getSimpleName(), ex.getErrorCode(), ex.getSQLState(), ex.getMessage()));
                    throw ex;
                }
            }
        }
        s.close();
        conn.close();
        if (result.getMemoryGB() < 0) {
        	LOG.warn("Never ran out of memory! (try increasing max-memory)");
        	result.setOutOfMemory(false);
        	result.setMemoryGB(i - 1);
        }
        return result;
    }

    public final Connection makeConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(
                config.getDatabaseUrl(),
                config.getUsername(),
                config.getPassword());
        return (conn);
    }
    
}
