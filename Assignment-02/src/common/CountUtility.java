package common;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CountUtility {

	public static Map<String, Integer> countWords(String fileContent, int minChars) {
		Map<String, Integer> countedWords = new HashMap<>();
		for (String word : wordsIn(fileContent)) {
			if (word.length() >= minChars) {
				countedWords.merge(word.toLowerCase(), 1, (val1, val2) -> val1 + val2);
			}
		}
		return countedWords;
	}

	public static Map<String, Integer> countWords(List<String> inputLines, int minChars) {
		Map<String, Integer> countedWords = new HashMap<>();
		for (String line : inputLines) {
			for (String word : wordsIn(line)) {
				if (word.length() >= minChars) {
					countedWords.merge(word.toLowerCase(), 1, (val1, val2) -> val1 + val2);
				}
			}
		}
		return countedWords;
	}

	private static String[] wordsIn(String content) {
		return content.trim().split("(\\s|\\p{Punct})+");
	}

	public static Map<String, Integer> getMostFrequentWords(Map<String, Map<String, Integer>> result, int frequentWords) {
		Map<String, Integer> selectedWords = new HashMap<>();
		for (Map<String, Integer> fileResult : result.values()) {
			fileResult.forEach((word, occurrences) -> {
				selectedWords.merge(word, occurrences, (val1, val2) -> val1 + val2);
			});
		}
		return selectedWords.entrySet().stream().sorted((w1, w2) -> (w2.getValue() - w1.getValue())).limit(frequentWords)
				.collect(Collectors.toMap(w -> w.getKey(), w -> w.getValue(), (w1, w2) -> w1, LinkedHashMap::new));
	}

}