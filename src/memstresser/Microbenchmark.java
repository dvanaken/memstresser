package memstresser;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import memstresser.util.StringUtil;

public class Microbenchmark {

    private static final Logger LOG = Logger.getLogger(Microbenchmark.class);

    /* https://www.slideshare.net/AlexeyBashtanov/postgresql-and-ram-usage */
    private static final String MEM_BASE_QUERY = "with cte_1gb as (select " +
            "repeat('a', 1024*1024*1024 - 100) as a1gb) select count(*) from ";

	private final DatabaseConfiguration config;
	private final Connection conn;
	private final MicrobenchmarkResult result;

	public Microbenchmark(DatabaseConfiguration config) throws SQLException {
		this.config = config;
		this.conn = makeConnection();
		this.result = new MicrobenchmarkResult();
	}

	public MicrobenchmarkResult getResult() {
		return result;
	}

	public void run(int maxMemLimitGB) throws SQLException {
		long startNanos = System.nanoTime();
		result.clear();
		result.setMaxMemoryLimitGB(maxMemLimitGB);
		int minMemGB = 1;

        //   Check there's at least minMemGB memory available
        if (!allocateMemory(minMemGB)) {
            result.setTotalExecutionTime(elapsedMillis(startNanos, System.nanoTime()));
            result.setFinalMemoryGB(minMemGB);
            result.setMemoryExhausted(true);
            LOG.warn("Less than " + minMemGB + "GB memory available!");
            return;
        }

		int maxMemGB = 16;
		if (maxMemGB > maxMemLimitGB) {
			maxMemGB = maxMemLimitGB;
		}

		// Incrementally search for an appropriate max memory
		while (allocateMemory(maxMemGB)) {
			if (maxMemGB == maxMemLimitGB) {
				// Give up if we're already at the max limit
				LOG.warn("We've hit the max memory limit and " +
						"never ran out of memory! (Try " +
						"increasing the max memory limit)");
				return;
			}
			// Max becomes the new min
			minMemGB = maxMemGB;
			// Double the max
			maxMemGB *= 2;
			if (maxMemGB > maxMemLimitGB) {
				maxMemGB = maxMemLimitGB;
			}
		}

		// We ran OOM with current maxMemoryGB setting
		// Build array with memory knowledge (hasMem=1,
		// !hasMem=-1, unknown=0)
		int[] hasMemArray = new int[maxMemGB + 1];
		Arrays.fill(hasMemArray, 0, minMemGB + 1, 1);
		Arrays.fill(hasMemArray, minMemGB + 1, maxMemGB, 0);
		hasMemArray[maxMemGB] = -1;
		LOG.info("Start array: " + Arrays.toString(hasMemArray) + "\n");

		// Do binary search on unknown region
		int memoryGB = binarySearch(hasMemArray, minMemGB + 1, maxMemGB);
		LOG.info("Final array: " + Arrays.toString(hasMemArray) + "\n");

		result.setTotalExecutionTime(elapsedMillis(startNanos, System.nanoTime()));
		result.setFinalMemoryGB(memoryGB);
		result.setMemoryExhausted(true);
		LOG.info(String.format("Final: [ memoryGB=%d, allocations=%d, runtime=%ds ]",
		        result.getFinalMemoryGB(), result.getNumAllocations(),
		        result.getTotalExecutionTime() / 1000));
	}

	private void debug(int[] hasMemArray, int minMemGB, int maxMemGB) {
		String sep = StringUtil.repeat("-", 40);
		LOG.info(String.format("%s\n\nminMemGB = %d, maxMemGB = %d\nArr=%s\n\n%s",
				sep, minMemGB, maxMemGB, Arrays.toString(hasMemArray), sep));
	}

	public int binarySearch(int[] hasMemArray, int minMemGB, int maxMemGB) throws SQLException {
		debug(hasMemArray, minMemGB, maxMemGB);
		if (maxMemGB >= minMemGB) {
			int midMemGB = minMemGB + (maxMemGB - minMemGB) / 2;
			int midHasMem = hasMemArray[midMemGB];
			if (midHasMem == 0) {
				midHasMem = allocateMemory(midMemGB) ? 1 : -1;
				if (midHasMem == 1) {
					Arrays.fill(hasMemArray, minMemGB, midMemGB + 1, 1);
					if (hasMemArray[midMemGB + 1] == -1) {
						return midMemGB;
					} else {
						// Try using more memory
						return binarySearch(hasMemArray, midMemGB + 1, maxMemGB);
					}
				} else {
					Arrays.fill(hasMemArray, midMemGB, maxMemGB + 1, -1);
					if (hasMemArray[midMemGB - 1] == 1) {
						return midMemGB - 1;
					} else {
						// Try using less memory
						return binarySearch(hasMemArray, minMemGB, midMemGB - 1);
					}
				}
			} else {
				// We've already examined this memory value
				throw new RuntimeException(String.format("We've already examined this "
						+ "memory value [minGB=%d, maxGB=%d, midGB=%d, midHasMem=%d, "
						+ "--midHasMem=%d, ++midHasMem=%d]", minMemGB, maxMemGB, midMemGB,
						midHasMem, hasMemArray[midMemGB - 1], hasMemArray[midMemGB + 1]));
			}
		}
		// This should not happen
		throw new RuntimeException("maxGB < minGB (" + maxMemGB + " < " + minMemGB + ")");
	}

	public boolean allocateMemory(int memoryGB) throws SQLException {
		LOG.info("Stressing " + memoryGB + "GB memory...");

        // Build query
        StringBuilder sb = new StringBuilder(MEM_BASE_QUERY);
        int j;
        for (j = 0; j < memoryGB - 1; ++j) {
            sb.append("cte_1gb a" + j + ", ");
        }
        sb.append("cte_1gb a" + j);

        // Execute query (see if it causes an OOM error)
        Statement s = conn.createStatement();
        boolean allocationSucceeded;
        long endNanos;
        long startNanos = System.nanoTime();
        try {
            s.execute(sb.toString());
            endNanos = System.nanoTime();
            allocationSucceeded = true;
            LOG.info("Succeeded in allocating " + memoryGB + "GB memory");
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 0 && ex.getSQLState() != null && ex.getSQLState().equals("53200")) {
            	endNanos = System.nanoTime();
            	allocationSucceeded = false;
            	LOG.info("Failed to allocate " + memoryGB + "GB memory");
            }
            else {
                LOG.error(String.format("%s thrown (ErrorCode='%d', SQLState='%s'): '%s'",
                        ex.getClass().getSimpleName(), ex.getErrorCode(), ex.getSQLState(), ex.getMessage()));
                throw ex;
            }
        } finally {
        	if (s != null) {
        		s.close();
        	}
        }
        result.addExecutionTime(elapsedMillis(startNanos, endNanos));
        result.addAllocationOutcome(allocationSucceeded);
        result.addMemoryGB(memoryGB);
        return allocationSucceeded;
	}

	public boolean allocateMemory2(int memoryGB) throws SQLException {
		int mem = 15;
		boolean success;
		if (memoryGB <= mem) {
			success = true;
		} else {
			success = false;
		}
		result.addAllocationOutcome(success);
		result.addExecutionTime(100L);
		result.addMemoryGB(memoryGB);
		LOG.info(String.format("Allocate %dGB: succeeded=%b", memoryGB, success));
		return success;
	}

	private long elapsedMillis(long startNanos, long endNanos) {
		return TimeUnit.NANOSECONDS.toMillis(endNanos - startNanos);
	}

    public final Connection makeConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(
                config.getDatabaseUrl(),
                config.getUsername(),
                config.getPassword());
        return (conn);
    }

    public void tearDown() throws SQLException {
    	conn.close();
    }

}
