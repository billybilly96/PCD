package exercise1_1;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import common.SearchResult;
import common.SectionDocument;

public class WordsFinder extends Thread {

	private final Consumer<Map<String, Integer>> consumer;
	private final List<String> initialFiles;
	private final int frequentWords;
	private TaskManager taskManager;
	private AtomicBoolean computing;

	public WordsFinder(Consumer<Map<String, Integer>> operation, List<String> files, int words, int minChars, long taskChars) {
		consumer = operation;
		initialFiles = files;
		frequentWords = words;
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		taskManager = new TaskManager(executor, taskChars, minChars);
		computing = new AtomicBoolean(false);
	}

	@Override
	public void run() {
		for (String file : initialFiles) {
			taskManager.executeRead(file);
		}
		computing.set(true);
		while (computing.get()) {
			try {
				Future<SectionDocument> readFut = taskManager.getNextRead();
				if (readFut != null) {
					SectionDocument document = readFut.get();
					taskManager.executeSearch(document);
				} else {
					Future<SearchResult> searchFut = taskManager.getNextSearch();
					if (searchFut != null) {
						SearchResult searchResult = searchFut.get();
						taskManager.saveResult(searchResult);
						if (consumer != null) {
							consumer.accept(taskManager.getResult(frequentWords));
						}
					} else {
						computing.set(false);
						if (consumer != null) {
							consumer.accept(null);
						}
					}
				}
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

	public void addFile(String file) {
		taskManager.executeRead(file);
	}

	public void removeFile(String file) {
		taskManager.removeFile(file);
	}

	public void stopComputation() {
		computing.set(false);
	}

}