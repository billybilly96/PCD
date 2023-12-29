package common;

import java.util.Map;

public class SearchResult {

	private final String file;
	private final Map<String, Integer> words;

	public SearchResult(String filePath, Map<String, Integer> foundWords) {
		file = filePath;
		words = foundWords;
	}

	public String getFile() {
		return file;
	}

	public Map<String, Integer> getWords() {
		return words;
	}

}
