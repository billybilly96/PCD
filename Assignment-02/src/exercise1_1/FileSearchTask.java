package exercise1_1;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import common.CountUtility;
import common.SearchResult;

public class FileSearchTask implements Callable<SearchResult> {

	private final String inputFile;
	private final List<String> inputLines;
	private final int minChars;

	public FileSearchTask(String file, List<String> lines, int chars) {
		inputFile = file;
		inputLines = lines;
		minChars = chars;
	}

	@Override
	public SearchResult call() throws Exception {
		Map<String, Integer> foundWords = CountUtility.countWords(inputLines, minChars);
		return new SearchResult(inputFile, foundWords);
	}
}