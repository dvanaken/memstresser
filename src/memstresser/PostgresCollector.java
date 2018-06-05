package memstresser;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

public class PostgresCollector {
    
    private static final Logger LOG = Logger.getLogger(PostgresCollector.class);

    private static final String[] PG_STAT_VIEWS = {
            "pg_stat_archiver",
            "pg_stat_bgwriter",
            "pg_stat_database",
            "pg_stat_database_conflicts",
            "pg_stat_user_tables",
            "pg_statio_user_tables",
            "pg_stat_user_indexes",
            "pg_statio_user_indexes"
    };

    private final DatabaseManager dbManager;
    
    private final Map<String, List<List<String>>> results = new TreeMap<String, List<List<String>>>();

    public PostgresCollector(DatabaseManager dbManager) throws SQLException {
        this.dbManager = dbManager;
    }
    
    public Map<String, List<List<String>>> collectMetrics() throws SQLException {
        LOG.info("Collecting Postgres metrics...");
        Connection conn = null;
        Statement s = null;
        ResultSet out = null;
        try {
            conn = dbManager.makeConnection();
            s = conn.createStatement();
            for (String viewName : PG_STAT_VIEWS) {
                out = s.executeQuery("SELECT * FROM " + viewName);
                results.put(viewName, collectMetrics(out));
            }
        } finally {
            if (out != null) {
                out.close();
            }
            if (s != null) {
                s.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
        return results;
    }

    private static List<List<String>> collectMetrics(ResultSet out) throws SQLException {
        List<List<String>> result = new LinkedList<List<String>>();

        // Get table column names
        ResultSetMetaData metadata = out.getMetaData();
        int numColumns = metadata.getColumnCount();
        List<String> row = new LinkedList<String>();
        for (int i = 0; i < numColumns; ++i) {
            row.add(metadata.getColumnName(i + 1).toLowerCase());
        }
        result.add(row);

        while (out.next()) {
            row = new LinkedList<String>();
            for (int i = 0; i < numColumns; ++i) {
                row.add(out.getString(i + 1));
            }
            result.add(row);
        }
        return result;
    }

    public void clearStats() throws SQLException {
        LOG.info("Resetting Postgres metrics...");
        Connection conn = null;
        Statement s = null;
        try {
            conn = dbManager.makeConnection();
            s = conn.createStatement();
            s.execute("SELECT pg_stat_reset_shared('archiver')");
            s.execute("SELECT pg_stat_reset_shared('bgwriter')");
            s.execute("SELECT pg_stat_reset()");
        } finally {
            if (s != null) {
                s.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

}
