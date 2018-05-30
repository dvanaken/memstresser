package memstresser;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

/**
 * https://www.slideshare.net/AlexeyBashtanov/postgresql-and-ram-usage
 */
public class Microbenchmark {
    
    private static final Logger LOG = Logger.getLogger(Microbenchmark.class);
    
    private static final String MEM_BASE_QUERY = "with cte_1gb as (select " +
            "repeat('a', 1024*1024*1024 - 100) as a1gb) select count(*) from ";
    
    private final DatabaseConfiguration config;
   
    public Microbenchmark(DatabaseConfiguration config) {
        this.config = config;
    }
    
    public int run(int minMemoryGB, int maxMemoryGB) throws SQLException {
        Connection conn = makeConnection();
        Statement s = conn.createStatement();
        int memoryGB = -1;
        for (int i = minMemoryGB; i < maxMemoryGB + 1; ++i) {
            LOG.info("Stressing " + i + "GB memory...");
            
            // Build query
            StringBuilder sb = new StringBuilder(MEM_BASE_QUERY);
            int j;
            for (j = 0; j < i - 1; ++j) {
                sb.append("cte_1gb a" + j + ", ");
            }
            sb.append("cte_1gb a" + j);
            
            // Execute query and see if it causes an OOM error
            try {
                s.execute(sb.toString());
            } catch (SQLException ex) {
                if (ex.getErrorCode() == 0 && ex.getSQLState() != null && ex.getSQLState().equals("53200")) {
                    // Stop when we finally run out of memory
                    LOG.info("Final memory size: " + i + "GB");
                    memoryGB = i;
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
        assert(memoryGB >= 0);
        return memoryGB;
    }

    public final Connection makeConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(
                config.getDBUrl(),
                config.getUsername(),
                config.getPassword());
        return (conn);
    }
    
}
