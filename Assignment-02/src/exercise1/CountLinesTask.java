package exercise1;

import java.util.List;
import java.util.Map;
import java.util.concurrent.RecursiveTask;

import common.CountUtility;

public class CountLinesTask extends RecursiveTask<Map<String, Integer>> {

	private static final long serialVersionUID = 1L;
	private final List<String> lines;
	private final int minChars;

	public CountLinesTask(List<String> inputLines, int mChars) {
		lines = inputLines;
		minChars = mChars;
	}

	@Override
	protected Map<String, Integer> compute() {
		return CountUtility.countWords(lines, minChars);
	}

}