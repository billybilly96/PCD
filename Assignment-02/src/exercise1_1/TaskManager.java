package exercise1_1;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import common.CountUtility;
import common.SearchResult;
import common.SectionDocument;

public class TaskManager {

	private final ExecutorService executor;
	private final long taskChars;
	private final int minChars;
	private Map<String, Future<SectionDocument>> readTasks;
	private Map<String, List<Future<SearchResult>>> searchTasks;
	private Map<String, Map<String, Integer>> computationResult;

	public TaskManager(ExecutorService exec, long tChars, int mChars) {
		executor = exec;
		taskChars = tChars;
		minChars = mChars;
		readTasks = new HashMap<>();
		searchTasks = new HashMap<>();
		computationResult = new HashMap<>();
	}

	public synchronized void executeRead(String file) {
		if (!readTasks.containsKey(file) && !searchTasks.containsKey(file) && !computationResult.containsKey(file)) {
			Future<SectionDocument> readFut = executor.submit(new FileReadTask(file, taskChars));
			readTasks.put(file, readFut);
		}
	}

	public synchronized void executeSearch(SectionDocument document) {
		String file = document.getFile();
		readTasks.remove(file);
		List<Future<SearchResult>> searchFutList = new LinkedList<>();
		for (List<String> lines : document.getLines()) {
			Future<SearchResult> searchFut = executor.submit(new FileSearchTask(file, lines, minChars));
			searchFutList.add(searchFut);
		}
		searchTasks.put(file, searchFutList);
	}

	public synchronized void saveResult(SearchResult searchResult) {
		String file = searchResult.getFile();
		if (searchTasks.get(file).size() == 0) {
			searchTasks.remove(file);
		}
		if (!computationResult.containsKey(file)) {
			computationResult.put(file, searchResult.getWords());
		} else {
			searchResult.getWords().forEach((word, occurrences) -> {
				computationResult.get(file).merge(word, occurrences, (val1, val2) -> val1 + val2);
			});
		}
	}

	public synchronized void removeFile(String file) {
		if (readTasks.containsKey(file)) {
			readTasks.get(file).cancel(true);
		}
		if (searchTasks.containsKey(file)) {
			searchTasks.get(file).forEach(searchFut -> searchFut.cancel(true));
		}
		readTasks.remove(file);
		searchTasks.remove(file);
		computationResult.remove(file);
	}

	public synchronized Future<SectionDocument> getNextRead() {
		try {
			return readTasks.values().iterator().next();
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	public synchronized Future<SearchResult> getNextSearch() {
		try {
			String file = searchTasks.keySet().iterator().next();
			return searchTasks.get(file).remove(0);
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	public synchronized Map<String, Integer> getResult(int frequentWords) {
		return CountUtility.getMostFrequentWords(computationResult, frequentWords);
	}

}