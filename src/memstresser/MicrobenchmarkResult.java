package memstresser;

import java.util.LinkedList;
import java.util.List;

public class MicrobenchmarkResult {// implements JSONSerializable {
	
	private final List<Long> executionTimesMillis = new LinkedList<Long>();
	private final List<Integer> memoryGBs = new LinkedList<Integer>();
	private final List<Boolean> allocationOutcomes = new LinkedList<Boolean>();
	private long totalExecutionTimeMillis;
	private int finalMemoryGB;
	private int maxMemoryLimitGB;
	private boolean memoryExhausted;
	
	public MicrobenchmarkResult() {
		clear();
	}
	
	public void clear() {
		totalExecutionTimeMillis = -1L;
		maxMemoryLimitGB = -1;
		finalMemoryGB = -1;
		memoryExhausted = false;
	}
	
	public void addExecutionTime(long timeMillis) {
		executionTimesMillis.add(timeMillis);
	}
	
	public List<Long> getExecutionTimes() {
		return new LinkedList<Long>(executionTimesMillis);
	}
	
	public void addMemoryGB(int memoryGB) {
		memoryGBs.add(memoryGB);
	}
	
	public List<Integer> getMemoryGBs() {
		return new LinkedList<Integer>(memoryGBs);
	}
	
	public void addAllocationOutcome(boolean success) {
		allocationOutcomes.add(success);
	}
	
	public List<Boolean> getAllocationOutcomes() {
		return new LinkedList<Boolean>(allocationOutcomes);
	}
	
	public void setTotalExecutionTime(long timeMillis) {
		totalExecutionTimeMillis = timeMillis;
	}
	
	public long getTotalExecutionTime() {
		return totalExecutionTimeMillis;
	}
	
	public void setMaxMemoryLimitGB(int maxMemoryLimitGB) {
		this.maxMemoryLimitGB = maxMemoryLimitGB;
	}
	
	public int getMaxMemoryLimitGB() {
		return maxMemoryLimitGB;
	}
	
	public void setFinalMemoryGB(int finalMemoryGB) {
		this.finalMemoryGB = finalMemoryGB;
	}
	
	public int getFinalMemoryGB() {
		return finalMemoryGB;
	}
	
	public void setMemoryExhausted(boolean memoryExhausted) {
		this.memoryExhausted = memoryExhausted;
	}
	
	public boolean getMemoryExhausted() {
		return memoryExhausted;
	}
	
	public int getNumAllocations() {
		return allocationOutcomes.size();
	}

}
